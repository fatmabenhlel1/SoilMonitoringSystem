import os
import argparse
import pandas as pd
import numpy as np
from sklearn.tree import DecisionTreeClassifier
from sklearn.preprocessing import MinMaxScaler, LabelEncoder
from sklearn.pipeline import Pipeline
from sklearn.compose import ColumnTransformer
import joblib

# Définition des colonnes
FEATURE_COLS = [
    "nitrogen",
    "phosphorus",
    "potassium",
    "temperature",
    "humidity",
    "soilPH",
    "rainfall"
]
TARGET_COL = "recommendation"

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--train", type=str, default=os.environ.get("SM_CHANNEL_TRAIN", "/opt/ml/input/data/train"))
    parser.add_argument("--model-dir", type=str, default=os.environ.get("SM_MODEL_DIR", "/opt/ml/model"))
    parser.add_argument("--max-depth", type=int, default=10)
    parser.add_argument("--min-samples-split", type=int, default=4)
    parser.add_argument("--min-samples-leaf", type=int, default=2)
    args = parser.parse_args()
    
    # Localiser le fichier CSV
    train_path = args.train
    if os.path.isdir(train_path):
        for name in ["latest.csv", "train.csv", "data.csv"]:
            p = os.path.join(train_path, name)
            if os.path.exists(p):
                train_path = p
                break
    
    print(f"📂 Chargement des données depuis: {train_path}")
    df = pd.read_csv(train_path)
    
    # Supprimer les lignes sans label
    df = df.dropna(subset=[TARGET_COL]).copy()
    print(f"📊 Données d'entraînement: {len(df)} lignes")
    
    # Séparer features et target
    X = df[FEATURE_COLS].copy()
    y = df[TARGET_COL].astype(str).copy()
    
    print(f"🌾 Classes de cultures: {sorted(y.unique())}")
    print(f"📊 Distribution des classes:")
    print(y.value_counts())
    
    # Pipeline de preprocessing + modèle
    # Toutes les features sont numériques, donc juste scaler
    preprocessor = ColumnTransformer(
        transformers=[
            ("scaler", MinMaxScaler(), FEATURE_COLS)
        ]
    )
    
    # Modèle DecisionTree avec hyperparamètres optimisés
    model = DecisionTreeClassifier(
        criterion='gini',
        max_depth=args.max_depth,
        min_samples_split=args.min_samples_split,
        min_samples_leaf=args.min_samples_leaf,
        random_state=42
    )
    
    # Pipeline complet
    pipeline = Pipeline(steps=[
        ("preprocessor", preprocessor),
        ("classifier", model)
    ])
    
    # Encoder pour les labels
    label_encoder = LabelEncoder()
    y_encoded = label_encoder.fit_transform(y)
    
    print("🚀 Entraînement du modèle...")
    pipeline.fit(X, y_encoded)
    print("✅ Entraînement terminé")
    
    # Sauvegarder le modèle et les encoders
    os.makedirs(args.model_dir, exist_ok=True)
    
    joblib.dump(pipeline, os.path.join(args.model_dir, "model.joblib"))
    joblib.dump(label_encoder, os.path.join(args.model_dir, "label_encoder.joblib"))
    
    # Sauvegarder les noms de features
    with open(os.path.join(args.model_dir, "features.txt"), "w") as f:
        f.write("\n".join(FEATURE_COLS))
    
    # Sauvegarder les classes
    with open(os.path.join(args.model_dir, "classes.txt"), "w") as f:
        f.write("\n".join(label_encoder.classes_))
    
    print(f"✅ Modèle sauvegardé dans: {args.model_dir}")
    print(f"   - model.joblib")
    print(f"   - label_encoder.joblib")
    print(f"   - features.txt")
    print(f"   - classes.txt")

if __name__ == "__main__":
    main()