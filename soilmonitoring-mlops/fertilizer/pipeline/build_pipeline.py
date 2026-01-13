import sagemaker
from sagemaker.workflow.pipeline import Pipeline
from sagemaker.workflow.parameters import ParameterString
from sagemaker.workflow.steps import ProcessingStep, TrainingStep
from sagemaker.workflow.condition_step import ConditionStep
from sagemaker.workflow.conditions import ConditionEquals
from sagemaker.workflow.functions import JsonGet
from sagemaker.workflow.properties import PropertyFile
from sagemaker.processing import ProcessingInput, ProcessingOutput, ScriptProcessor
from sagemaker.sklearn.estimator import SKLearn
from sagemaker.image_uris import retrieve
from sagemaker.workflow.pipeline_context import PipelineSession

def build_pipeline(role, region, bucket):
    pipe_sess = PipelineSession()

    # ---- parameters ----
    BaseS3 = ParameterString("BaseS3", default_value=f"s3://{bucket}/fertilizer-data/base.csv")
    LatestS3 = ParameterString("LatestS3", default_value=f"s3://{bucket}/fertilizer-data/latest.csv")

    ModelS3 = ParameterString("ModelS3", default_value=f"s3://{bucket}/fertilizer-artifacts/")
    BestMetricsS3 = ParameterString(
        "BestMetricsS3",
        default_value=f"s3://{bucket}/fertilizer-evaluation/best_metrics.json"
    )
    EndpointName = ParameterString("EndpointName", default_value="soil-fertilizer-endpoint-v1")

    # images
    sklearn_train_image = retrieve(
        framework="sklearn",
        region=region,
        version="1.2-1",
        py_version="py3",
        instance_type="ml.m5.large",
        image_scope="training",
    )
    sklearn_infer_image = retrieve(
        framework="sklearn",
        region=region,
        version="1.2-1",
        py_version="py3",
        instance_type="ml.m5.large",
        image_scope="inference",
    )

    # ============ STEP 1: ExportAndMerge (needs pymongo) ============
    # We run your bash script (which pip installs pymongo) inside processing
    processor_export = ScriptProcessor(
        image_uri=sklearn_train_image,
        command=["bash"],
        role=role,
        instance_type="ml.m5.large",
        instance_count=1,
        sagemaker_session=pipe_sess,
        env={
            "MONGO_SECRET_NAME": "soilmonitoring/mongodb",
            "BUCKET": bucket,
            "CHECKPOINT_KEY": "fertilizer-data/checkpoint.json",
        },
    )

    export_args = processor_export.run(
        code="code/run_processing.sh",  # bash
        inputs=[
            ProcessingInput(source=BaseS3, destination="/opt/ml/processing/input/base"),
        ],
        outputs=[
            ProcessingOutput(source="/opt/ml/processing/output", destination=f"s3://{bucket}/fertilizer-data/"),
        ],
    )

    step_export = ProcessingStep(name="ExportAndMerge", step_args=export_args)

    # ============ STEP 2: TrainModel ============
    estimator = SKLearn(
        entry_point="train.py",
        source_dir="code",
        role=role,
        instance_type="ml.m5.large",
        instance_count=1,
        framework_version="1.2-1",
        sagemaker_session=pipe_sess,
        output_path=ModelS3,
    )

    step_train = TrainingStep(
        name="TrainModel",
        estimator=estimator,
        inputs={"train": LatestS3},  # s3://.../fertilizer-data/latest.csv
        depends_on=[step_export.name],
    )

    # ============ STEP 3: Evaluate ============
    eval_processor = SKLearnProcessor(
    framework_version="1.2-1",
    role=role,
    instance_type="ml.m5.large",
    instance_count=1,
    sagemaker_session=pipe_sess,
)

evaluation_report = PropertyFile(
    name="EvaluationReport",
    output_name="evaluation",
    path="evaluation.json",
)

step_eval_args = eval_processor.run(
    code="evaluate.py",
    source_dir="code",
    inputs=[
        # model.tar.gz from the training step:
        ProcessingInput(
            source=step_train.properties.ModelArtifacts.S3ModelArtifacts,
            destination="/opt/ml/processing/input/model",
        ),
        # latest.csv for evaluation:
        ProcessingInput(
            source=f"s3://{bucket}/fertilizer-data/latest.csv",
            destination="/opt/ml/processing/input/test",
        ),
    ],
    outputs=[
        ProcessingOutput(
            output_name="evaluation",
            source="/opt/ml/processing/evaluation",
            destination=f"s3://{bucket}/fertilizer-evaluation/",
        )
    ],
)

step_eval = ProcessingStep(
    name="Evaluate",
    step_args=step_eval_args,
    property_files=[evaluation_report],
)


    # ============ STEP 4: CompareMetrics ============
    processor_cmp = ScriptProcessor(
        image_uri=sklearn_train_image,
        command=["python3"],
        role=role,
        instance_type="ml.m5.large",
        instance_count=1,
        sagemaker_session=pipe_sess,
        env={"BEST_METRICS_S3": BestMetricsS3},
    )

    deploy_report = PropertyFile(name="DeployDecision", output_name="decision", path="deploy.json")

    cmp_args = processor_cmp.run(
        code="code/compare_metrics.py",
        inputs=[
            ProcessingInput(
                source=step_eval.properties.ProcessingOutputConfig.Outputs["evaluation"].S3Output.S3Uri,
                destination="/opt/ml/processing/input/eval",
            )
        ],
        outputs=[
            ProcessingOutput(source="/opt/ml/processing/output", output_name="decision"),
        ],
    )

    step_cmp = ProcessingStep(
        name="CompareMetrics",
        step_args=cmp_args,
        property_files=[deploy_report],
        depends_on=[step_eval.name],
    )

    # ============ STEP 5: Deploy (only if deploy==1) ============
    processor_deploy = ScriptProcessor(
        image_uri=sklearn_train_image,
        command=["python3"],
        role=role,
        instance_type="ml.m5.large",
        instance_count=1,
        sagemaker_session=pipe_sess,
        env={
            "ENDPOINT_NAME": EndpointName,
            "MODEL_DATA_S3": step_train.properties.ModelArtifacts.S3ModelArtifacts,
            "SAGEMAKER_ROLE_ARN": role,
            "SKLEARN_INFERENCE_IMAGE": sklearn_infer_image,
            "INSTANCE_TYPE": "ml.m5.large",
            "AWS_REGION": region,
        },
    )

    deploy_args = processor_deploy.run(
        code="code/deploy.py",
        inputs=[
            ProcessingInput(
                source=step_cmp.properties.ProcessingOutputConfig.Outputs["decision"].S3Output.S3Uri,
                destination="/opt/ml/processing/input/decision",
            )
        ],
        outputs=[],
    )

    step_deploy = ProcessingStep(
        name="DeployModel",
        step_args=deploy_args,
        depends_on=[step_cmp.name],
    )

    cond = ConditionStep(
        name="DeployIfBetter",
        conditions=[
            ConditionEquals(
                left=JsonGet(
                    step_name=step_cmp.name,
                    property_file=deploy_report,
                    json_path="deploy",
                ),
                right=1,
            )
        ],
        if_steps=[step_deploy],
        else_steps=[],
        depends_on=[step_cmp.name],
    )

    return Pipeline(
        name="FertilizerWeeklyPipeline",
        parameters=[BaseS3, LatestS3, ModelS3, BestMetricsS3, EndpointName],
        steps=[step_export, step_train, step_eval, step_cmp, cond],
        sagemaker_session=pipe_sess,
    )
