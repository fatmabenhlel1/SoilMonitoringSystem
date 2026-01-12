import os, json, boto3
from urllib.parse import urlparse

def parse_s3_uri(uri: str):
    p = urlparse(uri)
    if p.scheme != "s3":
        raise ValueError(f"Not an s3 uri: {uri}")
    bucket = p.netloc
    key = p.path.lstrip("/")
    return bucket, key

def s3_read_json(s3, bucket, key, default):
    try:
        obj = s3.get_object(Bucket=bucket, Key=key)
        return json.loads(obj["Body"].read())
    except Exception:
        return default

def s3_write_json(s3, bucket, key, data):
    s3.put_object(Bucket=bucket, Key=key, Body=json.dumps(data).encode("utf-8"))

def main():
    best_uri = os.environ["BEST_METRICS_S3"]  # full s3://.../best_metrics.json
    best_bucket, best_key = parse_s3_uri(best_uri)

    eval_path = "/opt/ml/processing/input/eval/evaluation.json"
    out_dir = "/opt/ml/processing/output"
    os.makedirs(out_dir, exist_ok=True)

    with open(eval_path, "r") as f:
        evaluation = json.load(f)

    new_acc = float(evaluation["classification_metrics"]["accuracy"]["value"])

    s3 = boto3.client("s3")
    best = s3_read_json(s3, best_bucket, best_key, default={"best_accuracy": 0.0})
    best_acc = float(best.get("best_accuracy", 0.0))

    deploy = new_acc > best_acc

    result = {
        "deploy": bool(deploy),
        "new_accuracy": new_acc,
        "best_accuracy": best_acc,
        "best_metrics_s3": best_uri,
    }

    with open(os.path.join(out_dir, "deploy.json"), "w") as f:
        json.dump(result, f)

    if deploy:
        s3_write_json(s3, best_bucket, best_key, {"best_accuracy": new_acc})
        print("✅ Improved! Updated best_metrics.json")

    print("Decision:", result)

if __name__ == "__main__":
    main()
