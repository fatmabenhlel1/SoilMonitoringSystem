import os
import json
import glob
import tarfile
import pandas as pd
import joblib
import numpy as np
from sklearn.metrics import (
    accuracy_score,
    f1_score,
    precision_score,
    recall_score,
    classification_report,
    confusion_matrix
)

FEATURE_COLS = [
    "nitrogen",
    "phosphorus",
    "potassium",
    "temperature",
    "humidity",
    "soilPH",
    "rainfall"
]
LABEL_COL = "recommendation"

def find_csv(folder: str) -> str:
    """Trouver le fichier CSV dans un dossier"""
    # Préférer validation.csv si présent
    p = os.path.join(folder, "validation.csv")
    if os.path.exists(p):
        return p
    
    # Sinon chercher latest.csv
    p = os.path.join(folder, "latest.csv")
    if os.path.exists(p):
        return p
    
    # Sinon n'importe quel CSV
    cands = glob.glob(os.path.join(folder, "*.csv"))
    if not cands:
        raise FileNotFoundError(f"No CSV found in {folder}. Contents: {os.listdir(folder)}")
    return cands[0]

def load_model_from_tar(model_input_dir: str) -> tuple:
    """Extraire et charger le modèle depuis model.tar.gz"""
    tar_candidates = glob.glob(os.path.join(model_input_dir, "*.tar.gz"))
    if not tar_candidates:
        raise FileNotFoundError(f"No model.tar.gz found in {model_input_dir}")
    
    tar_path = tar_candidates[0]
    extracted_dir = os.path.join(model_input_dir, "extracted")
    os.makedirs(extracted_dir, exist_ok=True)
    
    with tarfile.open(tar_path, "r:gz") as tf:
        tf.extractall(extracted_dir)
    
    # Charger les fichiers
    model_path = os.path.join(extracted_dir, "model.joblib")
    encoder_path = os.path.join(extracted_dir, "label_encoder.joblib")
    
    if not os.path.exists(model_path):
        raise FileNotFoundError(f"model.joblib not found after extraction")
    
    pipeline = joblib.load(model_path)
    label_encoder = joblib.load(encoder_path)
    
    return pipeline, label_encoder

def main():
    model_dir = os.environ.get("SM_CHANNEL_MODEL", "/opt/ml/processing/input/model")
    test_dir = os.environ.get("SM_CHANNEL_TEST", "/opt/ml/processing/input/test")
    out_dir = "/opt/ml/processing/evaluation"
    os.makedirs(out_dir, exist_ok=True)
    
    print("🔍 Chargement du modèle...")
    pipeline, label_encoder = load_model_from_tar(model_dir)
    
    print("📂 Chargement des données de test...")
    test_csv = find_csv(test_dir)
    df = pd.read_csv(test_csv)
    
    # Nettoyer les noms de colonnes
    df.columns = [c.strip() for c in df.columns]
    
    # Vérifier les colonnes
    missing = [c for c in FEATURE_COLS + [LABEL_COL] if c not in df.columns]
    if missing:
        raise KeyError(f"Missing columns: {missing}. Available: {list(df.columns)}")
    
    X = df[FEATURE_COLS]
    y_true = df[LABEL_COL].astype(str)
    
    print(f"📊 Évaluation sur {len(df)} échantillons...")
    
    # Prédictions
    y_pred_encoded = pipeline.predict(X)
    y_pred = label_encoder.inverse_transform(y_pred_encoded)
    
    # Métriques
    accuracy = float(accuracy_score(y_true, y_pred))
    f1_macro = float(f1_score(y_true, y_pred, average="macro", zero_division=0))
    f1_weighted = float(f1_score(y_true, y_pred, average="weighted", zero_division=0))
    precision = float(precision_score(y_true, y_pred, average="weighted", zero_division=0))
    recall = float(recall_score(y_true, y_pred, average="weighted", zero_division=0))
    
    print(f"\n📊 Résultats de l'évaluation:")
    print(f"   Accuracy:    {accuracy:.4f}")
    print(f"   F1 Macro:    {f1_macro:.4f}")
    print(f"   F1 Weighted: {f1_weighted:.4f}")
    print(f"   Precision:   {precision:.4f}")
    print(f"   Recall:      {recall:.4f}")
    
    # Rapport de classification
    print("\n📋 Classification Report:")
    print(classification_report(y_true, y_pred, zero_division=0))
    
    # Sauvegarder les métriques
    metrics = {
        "classification_metrics": {
            "accuracy": {"value": accuracy},
            "f1_macro": {"value": f1_macro},
            "f1_weighted": {"value": f1_weighted},
            "precision": {"value": precision},
            "recall": {"value": recall}
        }
    }
    
    with open(os.path.join(out_dir, "evaluation.json"), "w") as f:
        json.dump(metrics, f, indent=2)
    
    print(f"\n✅ Métriques sauvegardées dans: {out_dir}/evaluation.json")

if __name__ == "__main__":
    main()