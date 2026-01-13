import json
import io
import pandas as pd
import joblib
import numpy as np

FEATURE_COLS = [
    "nitrogen",
    "phosphorus",
    "potassium",
    "temperature",
    "humidity",
    "soilPH",
    "rainfall"
]

def model_fn(model_dir):
    """Charger le modèle au démarrage de l'endpoint"""
    pipeline = joblib.load(f"{model_dir}/model.joblib")
    label_encoder = joblib.load(f"{model_dir}/label_encoder.joblib")
    
    return {
        "pipeline": pipeline,
        "label_encoder": label_encoder
    }

def _to_df(obj):
    """Convertir JSON en DataFrame"""
    if isinstance(obj, dict):
        return pd.DataFrame([obj])
    if isinstance(obj, list):
        return pd.DataFrame(obj)
    raise ValueError("Input must be JSON object or list of objects.")

def input_fn(request_body, request_content_type):
    """Parser les données d'entrée"""
    if request_content_type == "application/json":
        data = json.loads(request_body)
        df = _to_df(data)
    elif request_content_type in ("text/csv", "application/csv"):
        df = pd.read_csv(io.StringIO(request_body), header=None)
        if df.shape[1] != len(FEATURE_COLS):
            raise ValueError(f"CSV must have {len(FEATURE_COLS)} columns.")
        df.columns = FEATURE_COLS
    else:
        raise ValueError(f"Unsupported content type: {request_content_type}")
    
    # Vérifier que toutes les colonnes sont présentes
    for col in FEATURE_COLS:
        if col not in df.columns:
            df[col] = 0  # Valeur par défaut si manquante
    
    return df[FEATURE_COLS]

def predict_fn(input_data, model_dict):
    """Faire la prédiction"""
    pipeline = model_dict["pipeline"]
    label_encoder = model_dict["label_encoder"]
    
    # Prédiction
    pred_encoded = pipeline.predict(input_data)
    pred_labels = label_encoder.inverse_transform(pred_encoded)
    
    # Probabilités
    pred_proba = pipeline.predict_proba(input_data)
    
    results = []
    for i in range(len(input_data)):
        # Confiance (probabilité max)
        confidence = float(np.max(pred_proba[i]))
        
        # Top 3 recommandations
        top_3_indices = np.argsort(pred_proba[i])[-3:][::-1]
        top_3_crops = label_encoder.inverse_transform(top_3_indices)
        top_3_probs = pred_proba[i][top_3_indices]
        
        result = {
            "prediction": pred_labels[i],
            "confidence": confidence,
            "top_3_recommendations": [
                {"crop": crop, "probability": float(prob)}
                for crop, prob in zip(top_3_crops, top_3_probs)
            ]
        }
        results.append(result)
    
    # Si une seule prédiction, retourner l'objet directement
    if len(results) == 1:
        return results[0]
    
    return {"predictions": results}

def output_fn(prediction, accept):
    """Formatter la sortie"""
    if accept == "application/json":
        return json.dumps(prediction), accept
    raise ValueError(f"Unsupported accept type: {accept}")