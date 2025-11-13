from ultralytics import YOLO

# Load your custom trained model
model = YOLO("runs/detect/train/weights/best.pt")

# Validate the model and get the results object
results = model.val()  # No need to specify data if it's already in the model

# Access the metrics
print("Precision:", results.box.p)  # Precision
print("Recall:", results.box.r)  # Recall
print("mAP50:", results.box.map50)  # mAP at an IoU threshold of 0.5
print("mAP50-95:", results.box.map)  # mAP over various IoU thresholds (0.5 to 0.95)

# You can also access the confusion matrix from the results object
confusion_matrix = results.confusion_matrix



# Precision measures how many of the model's positive predictions were actually correct. It answers: "Of all the potholes I predicted, how many were real?".

# Recall measures how many of the actual potholes in the dataset the model was able to find. It answers: "Of all the real potholes, how many did my model find?".

# F1-score is a single metric that balances both precision and recall. It's especially useful for datasets that are imbalanced.

# mAP is the most widely used metric for object detection. It averages the precision across different recall values and across all object classes, giving a holistic measure of the model's performance.