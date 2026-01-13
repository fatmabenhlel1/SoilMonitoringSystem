# code/export_and_merge.py

import sys
import subprocess

def _pip_install(pkgs):
    subprocess.check_call([sys.executable, "-m", "pip", "install", "-q"] + pkgs)

# ✅ Make sure deps exist in the Processing container
try:
    import pymongo  # noqa
    import dns  # noqa  (dnspython)
except Exception:
    _pip_install(["pymongo[srv]==4.6.1", "dnspython==2.7.0"])

import os, json, glob
import boto3
import pandas as pd
from datetime import timezone
from pymongo import MongoClient
from botocore.exceptions import ClientError

DEFAULT_CHECKPOINT = "2025-01-01T00:00:00Z"

def load_secret(secret_name, region):
    sm = boto3.client("secretsmanager", region_name=region)
    resp = sm.get_secret_value(SecretId=secret_name)
    return json.loads(resp["SecretString"])

def read_checkpoint_s3(s3, bucket, key):
    try:
        obj = s3.get_object(Bucket=bucket, Key=key)
        data = json.loads(obj["Body"].read())
        return data.get("last_createdAt", DEFAULT_CHECKPOINT)
    except ClientError as e:
        if e.response["Error"]["Code"] in ("NoSuchKey", "404"):
            return DEFAULT_CHECKPOINT
        raise

def write_checkpoint_s3(s3, bucket, key, last_createdAt):
    body = json.dumps({"last_createdAt": last_createdAt}).encode("utf-8")
    s3.put_object(Bucket=bucket, Key=key, Body=body)

def to_iso_z(dt):
    # normalize createdAt to "...Z" without nanos
    if isinstance(dt, str):
        dt = dt.replace("Z", "")
        if "+" in dt:
            dt = dt.split("+")[0]
        if len(dt) > 26:  # trims nanoseconds if present
            dt = dt[:26]
        return dt + "Z"

    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    return dt.astimezone(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z")

def main():
    region = os.environ.get("AWS_REGION", "us-east-1")
    secret_name = os.environ["MONGO_SECRET_NAME"]
    bucket = os.environ["BUCKET"]
    checkpoint_key = os.environ.get("CHECKPOINT_KEY", "fertilizer-data/checkpoint.json")

    s3 = boto3.client("s3")
    last_ts = read_checkpoint_s3(s3, bucket, checkpoint_key)
    print("Checkpoint last_createdAt:", last_ts)

    # In ProcessingStep: base is mounted at /opt/ml/processing/input/base/
    base_dir = "/opt/ml/processing/input/base"
    candidates = glob.glob(os.path.join(base_dir, "*.csv"))
    if not candidates:
        raise FileNotFoundError(f"No .csv found in {base_dir}. Contents: {os.listdir(base_dir)}")
    base_path = candidates[0]

    base = pd.read_csv(base_path)
    print("Base loaded:", base.shape, "from", base_path)

    # ensure doc_id exists for merge logic
    if "doc_id" not in base.columns:
        base["doc_id"] = None

    secret = load_secret(secret_name, region)
    uri = secret["MONGODB_URI"]
    db_name = secret.get("DB_NAME", "soilmonitoring_db")
    collection_name = secret.get("COLLECTION", "Prediction")

    client = MongoClient(uri)
    col = client[db_name][collection_name]

    query = {"predictionType": "fertilizer", "createdAt": {"$gt": last_ts}}
    docs = list(col.find(query))
    print("New fertilizer docs fetched:", len(docs))

    out_dir = "/opt/ml/processing/output"
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, "latest.csv")

    if len(docs) == 0:
        base.drop(columns=["doc_id"], errors="ignore").to_csv(out_path, index=False)
        print("No new docs. Saved latest.csv =", base.shape)
        return

    df = pd.DataFrame(docs)
    df["doc_id"] = df["_id"].astype(str)

    mongo_out = pd.DataFrame({
        "Temparature": df["temperature"],
        "Humidity ": df["humidity"],
        "Moisture": df.get("soilMoisture"),
        "Soil Type": df.get("soilType").fillna("Unknown"),
        "Crop Type": df.get("cropType").fillna("Unknown"),
        "Nitrogen": df["nitrogen"],
        "Potassium": df["potassium"],
        "Phosphorous": df["phosphorus"],
        "Fertilizer Name": df["recommendation"],
        "doc_id": df["doc_id"],
    })

    mongo_out = mongo_out.dropna(subset=["Fertilizer Name","Temparature","Humidity ","Nitrogen","Potassium","Phosphorous"])
    if mongo_out["Moisture"].isna().any():
        mongo_out["Moisture"] = mongo_out["Moisture"].fillna(mongo_out["Moisture"].median())

    # ✅ keep all base rows + dedupe mongo rows only
    merged = pd.concat([base, mongo_out], ignore_index=True)
    mask = merged["doc_id"].notna()
    merged_mongo = merged[mask].drop_duplicates(subset=["doc_id"], keep="last")
    merged_base = merged[~mask]
    merged = pd.concat([merged_base, merged_mongo], ignore_index=True)

    merged.drop(columns=["doc_id"], errors="ignore").to_csv(out_path, index=False)
    print("✅ Saved merged latest.csv:", merged.shape, "->", out_path)

    max_created = max(df["createdAt"].tolist())
    new_checkpoint = to_iso_z(max_created)
    write_checkpoint_s3(s3, bucket, checkpoint_key, new_checkpoint)
    print("✅ Updated checkpoint:", new_checkpoint, f"s3://{bucket}/{checkpoint_key}")

if __name__ == "__main__":
    main()
