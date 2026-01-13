import boto3
import sagemaker
from datetime import datetime

# Configuration
REGION = sagemaker.Session().boto_region_name
ROLE = sagemaker.get_execution_role()
ACCOUNT_ID = boto3.client("sts").get_caller_identity()["Account"]

events = boto3.client("events", region_name=REGION)
sm = boto3.client("sagemaker", region_name=REGION)

# Nom du pipeline
PIPELINE_NAME = "CropRecommendationPipeline"

# Nom de la règle EventBridge
RULE_NAME = "WeeklyCropPipelineRule"

# Schedule: Tous les lundis à 09h00 UTC
CRON_EXPRESSION = "cron(0 9 ? * MON *)"

print(f"📅 Création de la règle EventBridge...")
print(f"   Nom:      {RULE_NAME}")
print(f"   Schedule: {CRON_EXPRESSION}")
print(f"   Pipeline: {PIPELINE_NAME}")

# 1. Créer la règle EventBridge
try:
    events.put_rule(
        Name=RULE_NAME,
        ScheduleExpression=CRON_EXPRESSION,
        State="ENABLED",
        Description=f"Exécute automatiquement le pipeline {PIPELINE_NAME} chaque lundi"
    )
    print(f"✅ Règle créée/mise à jour")
except Exception as e:
    print(f"❌ Erreur création règle: {e}")
    raise

# 2. Ajouter la cible (pipeline SageMaker)
target_arn = f"arn:aws:sagemaker:{REGION}:{ACCOUNT_ID}:pipeline/{PIPELINE_NAME}"

try:
    events.put_targets(
        Rule=RULE_NAME,
        Targets=[{
            "Id": "StartSageMakerPipeline",
            "Arn": target_arn,
            "RoleArn": ROLE,
            "SageMakerPipelineParameters": {
                "PipelineParameterList": []
            }
        }]
    )
    print(f"✅ Cible ajoutée au pipeline")
except Exception as e:
    print(f"❌ Erreur ajout cible: {e}")
    raise

print(f"\n✅ Automatisation configurée avec succès!")
print(f"\n📋 Détails:")
print(f"   - La règle '{RULE_NAME}' est active")
print(f"   - Le pipeline '{PIPELINE_NAME}' s'exécutera automatiquement chaque lundi à 09h00 UTC")
print(f"\n🔗 Voir dans la console EventBridge:")
print(f"https://console.aws.amazon.com/events/home?region={REGION}#/rules/{RULE_NAME}")
