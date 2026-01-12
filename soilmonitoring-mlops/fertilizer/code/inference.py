import json, io
import pandas as pd
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

def model_fn(model_dir):
    return joblib.load(f"{model_dir}/model.joblib")

def _to_df(obj):
    if isinstance(obj, dict):
        return pd.DataFrame([obj])
    if isinstance(obj, list):
        return pd.DataFrame(obj)
    raise ValueError("Input must be JSON object or list of objects.")

def input_fn(request_body, request_content_type):
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

    for c in FEATURE_COLS:
        if c not in df.columns:
            df[c] = None
    return df[FEATURE_COLS]

def predict_fn(input_data, model):
    pred = model.predict(input_data)
    return {"prediction": pred.tolist()}

def output_fn(prediction, accept):
    return json.dumps(prediction), "application/json"
