import os, json, time
import boto3

def main():
    region = os.environ.get("AWS_REGION") or os.environ.get("AWS_DEFAULT_REGION") or "us-east-1"
    endpoint_name = os.environ["ENDPOINT_NAME"]
    model_data_s3 = os.environ["MODEL_DATA_S3"]  # s3://.../model.tar.gz

    # compare step will mount deploy.json here
    deploy_path = "/opt/ml/processing/input/decision/deploy.json"
    with open(deploy_path, "r") as f:
        decision = json.load(f)

    deploy = decision.get("deploy")
    # allow bool or 0/1
    deploy_flag = bool(deploy) if isinstance(deploy, bool) else (int(deploy) == 1)

    print("Decision loaded:", decision)
    if not deploy_flag:
        print("✅ Not deploying (model is not better).")
        return

    sm = boto3.client("sagemaker", region_name=region)

    ts = int(time.time())
    model_name = f"{endpoint_name}-model-{ts}"
    cfg_name = f"{endpoint_name}-cfg-{ts}"

    role = os.environ["SAGEMAKER_ROLE_ARN"]

    # 1) CreateModel
    print("Creating model:", model_name)
    sm.create_model(
        ModelName=model_name,
        ExecutionRoleArn=role,
        PrimaryContainer={
            # Use the SKLearn serving image for inference
            "Image": os.environ["SKLEARN_INFERENCE_IMAGE"],
            "ModelDataUrl": model_data_s3,
            # If you use inference.py in model.tar.gz, SageMaker SKLearn container will load it via env.
            # If you didn't package inference.py, remove these envs.
            "Environment": {
                "SAGEMAKER_PROGRAM": "inference.py",
                "SAGEMAKER_SUBMIT_DIRECTORY": model_data_s3,
                "SAGEMAKER_CONTAINER_LOG_LEVEL": "20",
            },
        },
    )

    # 2) CreateEndpointConfig
    print("Creating endpoint config:", cfg_name)
    sm.create_endpoint_config(
        EndpointConfigName=cfg_name,
        ProductionVariants=[
            {
                "VariantName": "AllTraffic",
                "ModelName": model_name,
                "InitialInstanceCount": 1,
                "InstanceType": os.environ.get("INSTANCE_TYPE", "ml.m5.large"),
            }
        ],
    )

    # 3) UpdateEndpoint (in-place)
    print("Updating endpoint:", endpoint_name, "->", cfg_name)
    sm.update_endpoint(EndpointName=endpoint_name, EndpointConfigName=cfg_name)
    print("✅ Deploy triggered successfully.")

if __name__ == "__main__":
    main()
