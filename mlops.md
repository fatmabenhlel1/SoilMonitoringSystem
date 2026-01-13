## Dataset

### Public Dataset Sources

This project is based on two public datasets:

🔗 **Fertilizer Prediction Dataset**  
https://www.kaggle.com/datasets/mohitsingh1804/fertilizer-prediction-dataset  
Used to predict the most suitable fertilizer based on soil, climate, and crop features.

🔗 **Crop Recommendation Dataset**  
https://www.kaggle.com/datasets/atharvaingle/crop-recommendation-dataset  
Used to recommend the most suitable crop based on soil nutrients and climate conditions.

> Note: These public datasets are used as the **base datasets**. New records are continuously appended from **MongoDB** using an incremental (checkpoint-based) strategy.

---

## Work Environment

This project is developed and deployed using cloud-native MLOps tools:

- **AWS Academy Account** – Educational AWS environment  
- **Amazon SageMaker** – Training jobs, pipelines, and deployment  
- **SageMaker Notebook Instance / JupyterLab** – Development & testing  
- **Amazon S3** – Dataset storage and model artifacts  
- **Amazon EventBridge** – Weekly pipeline scheduling  

All experiments, pipeline executions, and deployments run directly on AWS infrastructure.

---

## Dataset Exploration

### 1) Fertilizer Dataset

Main features:
- Temparature  
- Humidity  
- Moisture  
- Soil Type  
- Crop Type  
- Nitrogen  
- Potassium  
- Phosphorous  
- Fertilizer Name (target)

 

### 2) Crop Recommendation Dataset

Main features:
- Nitrogen  
- Phosphorous  
- Potassium  
- Temparature  
- Humidity  
- pH  
- Rainfall  
- Crop (target)

  

---

## System Architecture

The system is implemented as an automated MLOps workflow.

### Global Flow 

1. Data ingestion from MongoDB and S3 
2. Data merging and checkpointing 
3. Model training 
4. Model evaluation 
5. Metric comparison with best model 
6. Conditional deployment 
7. Weekly automated retraining
---

## Pipeline Architecture

### Step 1 – Export & Merge 
- Fetch new data from MongoDB 
- Merge with base dataset 
- Save in S3 
- Update checkpoint 
### Step 2 – Training 
- Train fertilizer (crop) prediction model 
- Save model artifact in S3
 ### Step 3 – Evaluation 
 - Evaluate model on test data 
 - Generate metrics file 
 ### Step 4 – Compare Metrics 
 - Compare with best stored metrics 
 - Decide whether to deploy 
 ### Step 5 – Deployment 
 - Update endpoint only if model is better 
 ### Step 6 – Scheduling 
 - EventBridge triggers pipeline weekly

---

## Folder Structure

Same structure is used for both **fertilizer** and **crop** pipelines.

```text

  fertilizer(crop)/
    pipeline/
      build_pipeline.py
    code/
      export_and_merge.py
      train.py
      evaluate.py
      compare_metrics.py
      inference.py
    schedules/
      weekly_rule.py        
  
