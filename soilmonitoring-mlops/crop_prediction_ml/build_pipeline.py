import boto3
import sagemaker
from sagemaker.workflow.pipeline import Pipeline
from sagemaker.workflow.pipeline_context import PipelineSession
from sagemaker.workflow.parameters import ParameterString, ParameterInteger
from sagemaker.workflow.steps import ProcessingStep, TrainingStep
from sagemaker.workflow.conditions import ConditionGreaterThanOrEqualTo
from sagemaker.workflow.condition_step import ConditionStep
from sagemaker.workflow.properties import PropertyFile
from sagemaker.workflow.functions import JsonGet
from sagemaker.processing import ProcessingInput, ProcessingOutput
from sagemaker.sklearn.processing import SKLearnProcessor
from sagemaker.sklearn.estimator import SKLearn
from sagemaker.inputs import TrainingInput
from datetime import datetime

# Configuration
REGION = sagemaker.Session().boto_region_name
ROLE = sagemaker.get_execution_role()
BUCKET = sagemaker.Session().default_bucket()
PREFIX = "crop-mlops-pipeline"

print(f"🌍 Région: {REGION}")
print(f"👤 Rôle: {ROLE}")
print(f"🪣 Bucket: {BUCKET}")

# ⚠️ IMPORTANT : Utiliser PipelineSession pour les pipelines
pipeline_session = PipelineSession()

# =======================
# PARAMÈTRES DU PIPELINE
# =======================

base_s3 = ParameterString(
    name="BaseS3",
    default_value=f"s3://{BUCKET}/crop-data/base.csv"
)

latest_s3 = ParameterString(
    name="LatestS3",
    default_value=f"s3://{BUCKET}/crop-data/"
)

train_s3 = ParameterString(
    name="TrainS3",
    default_value=f"s3://{BUCKET}/crop-data/train/"
)

validation_s3 = ParameterString(
    name="ValidationS3",
    default_value=f"s3://{BUCKET}/crop-data/validation/"
)

model_s3 = ParameterString(
    name="ModelS3",
    default_value=f"s3://{BUCKET}/crop-artifacts/"
)

best_metrics_s3 = ParameterString(
    name="BestMetricsS3",
    default_value=f"s3://{BUCKET}/crop-evaluation/best_metrics.json"
)

endpoint_name = ParameterString(
    name="EndpointName",
    default_value="soil-crop-recommendation-endpoint-v1"
)

instance_type = ParameterString(
    name="InstanceType",
    default_value="ml.m5.large"
)

# Hyperparamètres du modèle
max_depth = ParameterInteger(name="MaxDepth", default_value=10)
min_samples_split = ParameterInteger(name="MinSamplesSplit", default_value=4)
min_samples_leaf = ParameterInteger(name="MinSamplesLeaf", default_value=2)

# =======================
# ÉTAPE 1: EXPORT ET MERGE
# =======================

print("\n📦 Configuration du Step 1: ExportAndMerge")

export_processor = SKLearnProcessor(
    framework_version="1.2-1",
    role=ROLE,
    instance_type="ml.t3.medium",
    instance_count=1,
    sagemaker_session=pipeline_session,  # ✅ Utiliser PipelineSession
    env={
        "MONGO_SECRET_NAME": "soilmonitoring/mongodb",
        "BUCKET": BUCKET,
        "CHECKPOINT_KEY": "crop-data/checkpoint.json"
    }
)

# ✅ Utiliser .run() DANS le contexte du pipeline
export_step_args = export_processor.run(
    code="code/export_data.py",
    inputs=[
        ProcessingInput(
            source=base_s3,  # ✅ Maintenant ça fonctionne avec PipelineSession
            destination="/opt/ml/processing/input/base"
        )
    ],
    outputs=[
        ProcessingOutput(
            source="/opt/ml/processing/output",
            destination=latest_s3
        )
    ]
)

step_export = ProcessingStep(
    name="ExportAndMerge",
    step_args=export_step_args
)

# =======================
# ÉTAPE 2: DATA SPLIT
# =======================

print("📦 Configuration du Step 2: DataSplit")

split_processor = SKLearnProcessor(
    framework_version="1.2-1",
    role=ROLE,
    instance_type="ml.t3.medium",
    instance_count=1,
    sagemaker_session=pipeline_session
)

split_step_args = split_processor.run(
    code="code/split_data.py",
    inputs=[
        ProcessingInput(
            source=step_export.properties.ProcessingOutputConfig.Outputs[0].S3Output.S3Uri,
            destination="/opt/ml/processing/input"
        )
    ],
    outputs=[
        ProcessingOutput(
            source="/opt/ml/processing/output/train",
            destination=train_s3,
            output_name="train"
        ),
        ProcessingOutput(
            source="/opt/ml/processing/output/validation",
            destination=validation_s3,
            output_name="validation"
        )
    ]
)

step_split = ProcessingStep(
    name="DataSplit",
    step_args=split_step_args
)

# =======================
# ÉTAPE 3: TRAINING
# =======================

print("📦 Configuration du Step 3: TrainModel")

sklearn_estimator = SKLearn(
    entry_point="train_model.py",
    source_dir="code",
    role=ROLE,
    instance_type="ml.m5.large",
    instance_count=1,
    framework_version="1.2-1",
    py_version="py3",
    hyperparameters={
        "max-depth": max_depth,
        "min-samples-split": min_samples_split,
        "min-samples-leaf": min_samples_leaf
    },
    output_path=model_s3,
    sagemaker_session=pipeline_session  # ✅ Ajouter PipelineSession
)

step_train = TrainingStep(
    name="TrainModel",
    estimator=sklearn_estimator,
    inputs={
        "train": TrainingInput(
            s3_data=step_split.properties.ProcessingOutputConfig.Outputs["train"].S3Output.S3Uri,
            content_type="text/csv"
        )
    }
)

# =======================
# ÉTAPE 4: EVALUATION
# =======================

print("📦 Configuration du Step 4: EvaluateModel")

eval_processor = SKLearnProcessor(
    framework_version="1.2-1",
    role=ROLE,
    instance_type="ml.t3.medium",
    instance_count=1,
    sagemaker_session=pipeline_session
)

evaluation_report = PropertyFile(
    name="EvaluationReport",
    output_name="evaluation",
    path="evaluation.json"
)

eval_step_args = eval_processor.run(
    code="code/evaluate_model.py",
    inputs=[
        ProcessingInput(
            source=step_train.properties.ModelArtifacts.S3ModelArtifacts,
            destination="/opt/ml/processing/input/model"
        ),
        ProcessingInput(
            source=step_split.properties.ProcessingOutputConfig.Outputs["validation"].S3Output.S3Uri,
            destination="/opt/ml/processing/input/test"
        )
    ],
    outputs=[
        ProcessingOutput(
            source="/opt/ml/processing/evaluation",
            destination=f"s3://{BUCKET}/crop-evaluation/",
            output_name="evaluation"
        )
    ]
)

step_eval = ProcessingStep(
    name="EvaluateModel",
    step_args=eval_step_args,
    property_files=[evaluation_report]
)

# =======================
# ÉTAPE 5: COMPARE METRICS
# =======================

print("📦 Configuration du Step 5: CompareMetrics")

compare_processor = SKLearnProcessor(
    framework_version="1.2-1",
    role=ROLE,
    instance_type="ml.t3.medium",
    instance_count=1,
    sagemaker_session=pipeline_session,
    env={"BEST_METRICS_S3": best_metrics_s3.default_value}  # ✅ Utiliser .default_value
)

deploy_decision = PropertyFile(
    name="DeployDecision",
    output_name="decision",
    path="deploy.json"
)

compare_step_args = compare_processor.run(
    code="code/compare_version_model.py",
    inputs=[
        ProcessingInput(
            source=step_eval.properties.ProcessingOutputConfig.Outputs["evaluation"].S3Output.S3Uri,
            destination="/opt/ml/processing/input/eval"
        )
    ],
    outputs=[
        ProcessingOutput(
            source="/opt/ml/processing/output",
            destination=f"s3://{BUCKET}/crop-decisions/",
            output_name="decision"
        )
    ]
)

step_compare = ProcessingStep(
    name="CompareMetrics",
    step_args=compare_step_args,
    property_files=[deploy_decision]
)

# =======================
# ÉTAPE 6: DEPLOY
# =======================

print("📦 Configuration du Step 6: DeployModel")

deploy_processor = SKLearnProcessor(
    framework_version="1.2-1",
    role=ROLE,
    instance_type="ml.t3.medium",
    instance_count=1,
    sagemaker_session=pipeline_session,
    env={
        "ENDPOINT_NAME": endpoint_name.default_value,  # ✅ .default_value
        "SKLEARN_INFERENCE_IMAGE": f"141502667606.dkr.ecr.{REGION}.amazonaws.com/sagemaker-scikit-learn:1.2-1-cpu-py3",
        "INSTANCE_TYPE": instance_type.default_value,  # ✅ .default_value
        "ROLE": ROLE
    }
)

deploy_step_args = deploy_processor.run(
    code="code/deploy_model.py",
    inputs=[
        ProcessingInput(
            source=step_compare.properties.ProcessingOutputConfig.Outputs["decision"].S3Output.S3Uri,
            destination="/opt/ml/processing/input/decision"
        ),
        ProcessingInput(
            source=step_train.properties.ModelArtifacts.S3ModelArtifacts,
            destination="/opt/ml/processing/input/model"
        )
    ]
)

step_deploy = ProcessingStep(
    name="DeployModel",
    step_args=deploy_step_args
)

# Condition
cond_deploy = ConditionGreaterThanOrEqualTo(
    left=JsonGet(
        step_name=step_compare.name,
        property_file=deploy_decision,
        json_path="deploy"
    ),
    right=1
)

step_cond = ConditionStep(
    name="CheckDeployCondition",
    conditions=[cond_deploy],
    if_steps=[step_deploy],
    else_steps=[]
)

# =======================
# CRÉATION DU PIPELINE
# =======================

print("\n🔨 Création du pipeline...")

pipeline = Pipeline(
    name="CropRecommendationPipeline",
    parameters=[
        base_s3,
        latest_s3,
        train_s3,
        validation_s3,
        model_s3,
        best_metrics_s3,
        endpoint_name,
        instance_type,
        max_depth,
        min_samples_split,
        min_samples_leaf
    ],
    steps=[
        step_export,
        step_split,
        step_train,
        step_eval,
        step_compare,
        step_cond
    ],
    sagemaker_session=pipeline_session  # ✅ Ajouter PipelineSession
)

# Upsert
response = pipeline.upsert(role_arn=ROLE)

print(f"✅ Pipeline créé/mis à jour: {response['PipelineArn']}")
print(f"\n🔗 Console SageMaker:")
print(f"https://console.aws.amazon.com/sagemaker/home?region={REGION}#/pipelines/{pipeline.name}")

# Démarrage optionnel
print("\n🚀 Voulez-vous démarrer l'exécution maintenant ? (décommentez la ligne suivante)")
execution = pipeline.start()
print(f"✅ Exécution démarrée: {execution.arn}")