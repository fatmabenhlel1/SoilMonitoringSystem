
import pandas as pd
from sklearn.model_selection import train_test_split
import os

df = pd.read_csv("/opt/ml/processing/input/latest.csv")
print(f"📊 Total samples: {len(df)}")

train_df, val_df = train_test_split(
    df,
    test_size=0.2,
    random_state=42,
    stratify=df["recommendation"]
)

print(f"📊 Train samples: {len(train_df)}")
print(f"📊 Validation samples: {len(val_df)}")

os.makedirs("/opt/ml/processing/output/train", exist_ok=True)
os.makedirs("/opt/ml/processing/output/validation", exist_ok=True)

train_df.to_csv("/opt/ml/processing/output/train/train.csv", index=False)
val_df.to_csv("/opt/ml/processing/output/validation/validation.csv", index=False)

print("✅ Split completed")
