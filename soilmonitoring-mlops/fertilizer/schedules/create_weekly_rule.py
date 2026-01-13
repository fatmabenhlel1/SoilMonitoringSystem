import boto3
import sagemaker
from datetime import datetime

region = sagemaker.Session().boto_region_name
events = boto3.client("events", region_name=region)
sm = boto3.client("sagemaker", region_name=region)

pipeline_name = "FertilizerWeeklyPipeline"

# every Monday at 09:00 UTC (change if you want)
rule_name = "WeeklyFertilizerPipelineRule"
cron = "cron(0 9 ? * MON *)"

events.put_rule(
    Name=rule_name,
    ScheduleExpression=cron,
    State="ENABLED"
)

# EventBridge target to start pipeline execution
target_arn = f"arn:aws:sagemaker:{region}:{boto3.client('sts').get_caller_identity()['Account']}:pipeline/{pipeline_name}"

events.put_targets(
    Rule=rule_name,
    Targets=[{
        "Id": "StartSageMakerPipeline",
        "Arn": target_arn,
        "RoleArn": role,  # uses your notebook execution role
        "SageMakerPipelineParameters": {
            "PipelineParameterList": []
        }
    }]
)

print("✅ Weekly schedule created:", rule_name, cron)
