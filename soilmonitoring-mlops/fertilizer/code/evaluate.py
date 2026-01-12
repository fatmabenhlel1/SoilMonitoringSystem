import os, json, glob, tarfile
import pandas as pd
import joblib
from sklearn.metrics import accuracy_score, f1_score

# Normalize column names because your dataset has "Humidity " with trailing space sometimes.
FEATURE_COLS = [
    "Temparature", "Humidity", "Moisture", "Soil Type", "Crop Type",
    "Nitrogen", "Potassium", "Phosphorous"
]
LABEL_COL = "Fertilizer Name"

def find_csv(folder: str) -> str:
    # Prefer latest.csv if present, else any csv
    p = os.path.join(folder, "latest.csv")
    if os.path.exists(p):
        return p
    cands = glob.glob(os.path.join(folder, "*.csv"))
    if not cands:
        raise FileNotFoundError(f"No CSV found in {folder}. Contents: {os.listdir(folder)}")
    return cands[0]

def load_model_from_tar(model_input_dir: str) -> str:
    # In pipelines, training output is a model.tar.gz
    # It will be downloaded into model_input_dir (not auto-extracted).
    tar_candidates = glob.glob(os.path.join(model_input_dir, "*.tar.gz")) + glob.glob(os.path.join(model_input_dir, "model.tar.gz"))
    tar_candidates = list(dict.fromkeys(tar_candidates))  # unique
    if not tar_candidates:
        raise FileNotFoundError(f"No model.tar.gz found in {model_input_dir}. Contents: {os.listdir(model_input_dir)}")

    tar_path = tar_candidates[0]
    extracted_dir = os.path.join(model_input_dir, "extracted")
    os.makedirs(extracted_dir, exist_ok=True)

    with tarfile.open(tar_path, "r:gz") as tf:
        tf.extractall(extracted_dir)

    # Look for model.joblib after extraction
    joblib_candidates = glob.glob(os.path.join(extracted_dir, "**", "model.joblib"), recursive=True)
    if not joblib_candidates:
        raise FileNotFoundError(f"model.joblib not found after extracting {tar_path}. Extracted contents: {os.listdir(extracted_dir)}")

    return joblib_candidates[0]

def main():
    model_dir = os.environ.get("SM_CHANNEL_MODEL", "/opt/ml/processing/input/model")
    test_dir  = os.environ.get("SM_CHANNEL_TEST",  "/opt/ml/processing/input/test")
    out_dir   = "/opt/ml/processing/evaluation"
    os.makedirs(out_dir, exist_ok=True)

    model_path = load_model_from_tar(model_dir)
    model = joblib.load(model_path)

    test_csv = find_csv(test_dir)
    df = pd.read_csv(test_csv)

    # Strip whitespace from column names so "Humidity " becomes "Humidity"
    df.columns = [c.strip() for c in df.columns]

    # Also strip feature names to match
    feats = [c.strip() for c in FEATURE_COLS]
    label = LABEL_COL.strip()

    missing = [c for c in feats + [label] if c not in df.columns]
    if missing:
        raise KeyError(f"Missing columns: {missing}. Available columns: {list(df.columns)}")

    X = df[feats]
    y = df[label].astype(str)

    y_pred = model.predict(X)

    acc = float(accuracy_score(y, y_pred))
    f1  = float(f1_score(y, y_pred, average="macro"))

    metrics = {
        "classification_metrics": {
            "accuracy": {"value": acc},
            "f1_macro": {"value": f1}
        }
    }

    with open(os.path.join(out_dir, "evaluation.json"), "w") as f:
        json.dump(metrics, f)

    print("✅ Saved metrics:", metrics)

if __name__ == "__main__":
    main()
