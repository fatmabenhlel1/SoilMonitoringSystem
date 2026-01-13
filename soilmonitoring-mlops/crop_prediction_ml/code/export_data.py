import os
import json
import boto3
import pandas as pd
from datetime import datetime
from pymongo import MongoClient

def get_mongo_uri():
    """Récupérer les credentials MongoDB depuis Secrets Manager"""
    secret_name = os.environ.get("MONGO_SECRET_NAME", "soilmonitoring/mongodb")
    region = os.environ.get("AWS_REGION", "us-east-1")
    
    client = boto3.client("secretsmanager", region_name=region)
    try:
        response = client.get_secret_value(SecretId=secret_name)
        secret = json.loads(response["SecretString"])
        return secret["MONGODB_URI"], secret["DB_NAME"]
    except Exception as e:
        print(f"❌ Erreur Secrets Manager: {e}")
        raise

def get_checkpoint(s3_client, bucket, key):
    """Lire le dernier checkpoint depuis S3"""
    try:
        obj = s3_client.get_object(Bucket=bucket, Key=key)
        data = json.loads(obj["Body"].read())
        return datetime.fromisoformat(data["last_timestamp"])
    except Exception:
        # Premier run - utiliser une date très ancienne
        return datetime(2020, 1, 1)

def save_checkpoint(s3_client, bucket, key, timestamp):
    """Sauvegarder le nouveau checkpoint"""
    data = {"last_timestamp": timestamp.isoformat()}
    s3_client.put_object(
        Bucket=bucket,
        Key=key,
        Body=json.dumps(data).encode("utf-8")
    )

def fetch_new_data(mongo_uri, db_name, since):
    """Extraire les nouvelles prédictions de cultures depuis MongoDB"""
    client = MongoClient(mongo_uri)
    db = client[db_name]
    collection = db["Prediction"]
    
    query = {
        "predictionType": "crop",
        "createdAt": {"$gt": since}
    }
    
    cursor = collection.find(query).sort("createdAt", 1)
    
    records = []
    for doc in cursor:
        # Mapping des champs MongoDB → DataFrame
        record = {
            "nitrogen": doc.get("nitrogen", 0),
            "phosphorus": doc.get("phosphorus", 0),
            "potassium": doc.get("potassium", 0),
            "temperature": doc.get("temperature", 0),
            "humidity": doc.get("humidity", 0),
            "soilPH": doc.get("soilPH", 0),
            "rainfall": doc.get("rainfall", 0),
            "soilType": doc.get("soilType", "Unknown"),
            "recommendation": doc.get("recommendation", "rice"),  # label
            "createdAt": doc.get("createdAt")
        }
        records.append(record)
    
    client.close()
    
    if not records:
        return None, None
    
    df = pd.DataFrame(records)
    latest_ts = df["createdAt"].max()
    df = df.drop("createdAt", axis=1)
    
    return df, latest_ts

def main():
    # Variables d'environnement
    bucket = os.environ.get("BUCKET")
    base_key = os.environ.get("BASE_KEY", "crop-data/base.csv")
    checkpoint_key = os.environ.get("CHECKPOINT_KEY", "crop-data/checkpoint.json")
    output_key = os.environ.get("OUTPUT_KEY", "crop-data/latest.csv")
    
    s3 = boto3.client("s3")
    
    # 1. Récupérer checkpoint
    last_ts = get_checkpoint(s3, bucket, checkpoint_key)
    print(f"📅 Dernière extraction: {last_ts}")
    
    # 2. Extraire nouvelles données MongoDB
    mongo_uri, db_name = get_mongo_uri()
    new_df, latest_ts = fetch_new_data(mongo_uri, db_name, last_ts)
    
    if new_df is None or len(new_df) == 0:
        print("ℹ️ Aucune nouvelle donnée. Copie de base.csv → latest.csv")
        # Copier base.csv tel quel
        s3.copy_object(
            Bucket=bucket,
            CopySource={"Bucket": bucket, "Key": base_key},
            Key=output_key
        )
        return
    
    print(f"✅ {len(new_df)} nouvelles prédictions extraites")
    
    # 3. Charger base.csv depuis S3
    obj = s3.get_object(Bucket=bucket, Key=base_key)
    base_df = pd.read_csv(obj["Body"])
    print(f"📊 Dataset de base: {len(base_df)} lignes")
    
    # 4. Fusionner
    merged_df = pd.concat([base_df, new_df], ignore_index=True)
    merged_df = merged_df.drop_duplicates()
    print(f"📊 Dataset fusionné: {len(merged_df)} lignes")
    
    # 5. Sauvegarder latest.csv sur S3
    csv_buffer = merged_df.to_csv(index=False)
    s3.put_object(Bucket=bucket, Key=output_key, Body=csv_buffer.encode("utf-8"))
    print(f"✅ latest.csv sauvegardé: s3://{bucket}/{output_key}")
    
    # 6. Mettre à jour checkpoint
    save_checkpoint(s3, bucket, checkpoint_key, latest_ts)
    print(f"✅ Checkpoint mis à jour: {latest_ts}")

if __name__ == "__main__":
    main()