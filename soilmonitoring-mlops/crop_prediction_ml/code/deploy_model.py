import os
import json
import boto3
import time

def main():
    # Variables d'environnement
    decision_path = "/opt/ml/processing/input/decision/deploy.json"
    endpoint_name = os.environ["ENDPOINT_NAME"]
    model_data_s3 = os.environ["MODEL_DATA_S3"]
    image_uri = os.environ["SKLEARN_INFERENCE_IMAGE"]
    instance_type = os.environ.get("INSTANCE_TYPE", "ml.m5.large")
    role = os.environ["ROLE"]
    
    # Lire la décision
    with open(decision_path, "r") as f:
        decision = json.load(f)
    
    deploy = decision.get("deploy", False)
    
    if not deploy:
        print("⏸️ Pas de déploiement (modèle non amélioré)")
        return
    
    print(f"🚀 Déploiement du modèle sur l'endpoint: {endpoint_name}")
    print(f"   Model Data: {model_data_s3}")
    print(f"   Instance:   {instance_type}")
    
    sm = boto3.client("sagemaker")
    
    # Nom unique pour le modèle
    timestamp = int(time.time())
    model_name = f"crop-model-{timestamp}"
    config_name = f"crop-config-{timestamp}"
    
    # 1. Créer le modèle SageMaker
    print(f"📦 Création du modèle: {model_name}")
    sm.create_model(
        ModelName=model_name,
        PrimaryContainer={
            "Image": image_uri,
            "ModelDataUrl": model_data_s3,
            "Environment": {
                "SAGEMAKER_PROGRAM": "inference.py",
                "SAGEMAKER_SUBMIT_DIRECTORY": model_data_s3
            }
        },
        ExecutionRoleArn=role
    )
    
    # 2. Créer la configuration de l'endpoint
    print(f"⚙️ Création de la configuration: {config_name}")
    sm.create_endpoint_config(
        EndpointConfigName=config_name,
        ProductionVariants=[{
            "VariantName": "AllTraffic",
            "ModelName": model_name,
            "InitialInstanceCount": 1,
            "InstanceType": instance_type,
            "InitialVariantWeight": 1.0
        }]
    )
    
    # 3. Créer ou mettre à jour l'endpoint
    try:
        # Vérifier si l'endpoint existe
        sm.describe_endpoint(EndpointName=endpoint_name)
        print(f"🔄 Mise à jour de l'endpoint existant: {endpoint_name}")
        
        sm.update_endpoint(
            EndpointName=endpoint_name,
            EndpointConfigName=config_name
        )
        
        action = "updated"
        
    except sm.exceptions.ClientError as e:
        if "Could not find endpoint" in str(e):
            print(f"✨ Création du nouvel endpoint: {endpoint_name}")
            
            sm.create_endpoint(
                EndpointName=endpoint_name,
                EndpointConfigName=config_name
            )
            
            action = "created"
        else:
            raise
    
    # 4. Attendre que l'endpoint soit en service
    print(f"⏳ Attente de la disponibilité de l'endpoint (cela peut prendre 5-10 minutes)...")
    
    waiter = sm.get_waiter('endpoint_in_service')
    try:
        waiter.wait(
            EndpointName=endpoint_name,
            WaiterConfig={
                'Delay': 30,  # Vérifier toutes les 30 secondes
                'MaxAttempts': 40  # Max 20 minutes
            }
        )
        print(f"✅ Endpoint {action} avec succès: {endpoint_name}")
        
    except Exception as e:
        print(f"❌ Erreur lors du déploiement: {e}")
        raise
    
    # 5. Afficher les détails de l'endpoint
    response = sm.describe_endpoint(EndpointName=endpoint_name)
    print(f"\n📍 Détails de l'endpoint:")
    print(f"   Nom:       {response['EndpointName']}")
    print(f"   Statut:    {response['EndpointStatus']}")
    print(f"   ARN:       {response['EndpointArn']}")
    print(f"   Créé le:   {response['CreationTime']}")
    print(f"   Modifié:   {response['LastModifiedTime']}")
    
    print(f"\n✅ Déploiement terminé!")

if __name__ == "__main__":
    main()