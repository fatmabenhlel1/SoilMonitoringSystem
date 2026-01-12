import os
import argparse
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder
from sklearn.ensemble import RandomForestClassifier
import joblib

FEATURE_COLS = [
    "Temparature",
    "Humidity ",
    "Moisture",
    "Soil Type",
    "Crop Type",
    "Nitrogen",
    "Potassium",
    "Phosphorous",
]
TARGET_COL = "Fertilizer Name"

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--train", type=str, default=os.environ.get("SM_CHANNEL_TRAIN", "/opt/ml/input/data/train"))
    parser.add_argument("--model-dir", type=str, default=os.environ.get("SM_MODEL_DIR", "/opt/ml/model"))
    args = parser.parse_args()

    train_path = args.train
    if os.path.isdir(train_path):
        for name in ["latest.csv", "train.csv", "data.csv"]:
            p = os.path.join(train_path, name)
            if os.path.exists(p):
                train_path = p
                break

    df = pd.read_csv(train_path)
    df = df.dropna(subset=[TARGET_COL]).copy()

    X = df[FEATURE_COLS].copy()
    y = df[TARGET_COL].astype(str).copy()

    cat_cols = ["Soil Type", "Crop Type"]
    num_cols = [c for c in FEATURE_COLS if c not in cat_cols]

    pre = ColumnTransformer(
        transformers=[
            ("cat", OneHotEncoder(handle_unknown="ignore"), cat_cols),
            ("num", "passthrough", num_cols),
        ]
    )

    clf = Pipeline(
        steps=[
            ("pre", pre),
            ("model", RandomForestClassifier(n_estimators=250, random_state=42)),
        ]
    )

    clf.fit(X, y)

    os.makedirs(args.model_dir, exist_ok=True)
    joblib.dump(clf, os.path.join(args.model_dir, "model.joblib"))

    with open(os.path.join(args.model_dir, "features.txt"), "w") as f:
        f.write("\n".join(FEATURE_COLS))

    print("✅ saved model to", args.model_dir)

if __name__ == "__main__":
    main()
