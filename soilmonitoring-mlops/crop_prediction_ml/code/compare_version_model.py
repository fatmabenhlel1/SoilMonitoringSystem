import os
import json
import boto3
from urllib.parse import urlparse

def parse_s3_uri(uri: str):
    """Parser une URI S3"""
    p = urlparse(uri)
    if p.scheme != "s3":
        raise ValueError(f"Not an s3 uri: {uri}")
    bucket = p.netloc
    key = p.path.lstrip("/")
    return bucket, key

def s3_read_json(s3, bucket, key, default):
    """Lire un fichier JSON depuis S3"""
    try:
        obj = s3.get_object(Bucket=bucket, Key=key)
        return json.loads(obj["Body"].read())
    except s3.exceptions.NoSuchKey:
        print(f"⚠️ Fichier {key} n'existe pas encore, utilisation des valeurs par défaut")
        return default
    except Exception as e:
        print(f"❌ Erreur lecture S3: {e}")
        return default

def s3_write_json(s3, bucket, key, data):
    """Écrire un fichier JSON sur S3"""
    s3.put_object(
        Bucket=bucket,
        Key=key,
        Body=json.dumps(data, indent=2).encode("utf-8")
    )

def main():
    # Variables d'environnement
    best_uri = os.environ["BEST_METRICS_S3"]  # s3://bucket/crop-evaluation/best_metrics.json
    eval_path = "/opt/ml/processing/input/eval/evaluation.json"
    out_dir = "/opt/ml/processing/output"
    os.makedirs(out_dir, exist_ok=True)
    
    print("📊 Comparaison des métriques...")
    
    # Lire les nouvelles métriques
    with open(eval_path, "r") as f:
        evaluation = json.load(f)
    
    new_acc = float(evaluation["classification_metrics"]["accuracy"]["value"])
    new_f1 = float(evaluation["classification_metrics"]["f1_weighted"]["value"])
    
    print(f"   Nouvelle accuracy: {new_acc:.4f}")
    print(f"   Nouveau F1:        {new_f1:.4f}")
    
    # Lire les meilleures métriques historiques
    best_bucket, best_key = parse_s3_uri(best_uri)
    s3 = boto3.client("s3")
    
    best = s3_read_json(s3, best_bucket, best_key, default={
        "best_accuracy": 0.0,
        "best_f1": 0.0,
        "timestamp": None
    })
    
    best_acc = float(best.get("best_accuracy", 0.0))
    best_f1 = float(best.get("best_f1", 0.0))
    
    print(f"   Meilleure accuracy historique: {best_acc:.4f}")
    print(f"   Meilleur F1 historique:        {best_f1:.4f}")
    
    # Décision de déploiement
    # Critère: amélioration de l'accuracy OU du F1
    deploy = (new_acc > best_acc) or (new_f1 > best_f1)
    
    if deploy:
        print("✅ DÉCISION: DÉPLOYER (amélioration détectée)")
    else:
        print("⏸️ DÉCISION: NE PAS DÉPLOYER (pas d'amélioration)")
    
    # Résultat
    result = {
        "deploy": bool(deploy),
        "new_accuracy": new_acc,
        "new_f1": new_f1,
        "best_accuracy": best_acc,
        "best_f1": best_f1,
        "best_metrics_s3": best_uri,
    }
    
    # Sauvegarder la décision
    with open(os.path.join(out_dir, "deploy.json"), "w") as f:
        json.dump(result, f, indent=2)
    
    print(f"✅ Décision sauvegardée dans: {out_dir}/deploy.json")
    
    # Si déploiement, mettre à jour le meilleur score
    if deploy:
        from datetime import datetime
        updated_best = {
            "best_accuracy": new_acc,
            "best_f1": new_f1,
            "timestamp": datetime.now().isoformat()
        }
        s3_write_json(s3, best_bucket, best_key, updated_best)
        print(f"✅ Fichier best_metrics.json mis à jour sur S3")
    
    print("\n📋 Résumé de la décision:")
    print(json.dumps(result, indent=2))

if __name__ == "__main__":
    main()