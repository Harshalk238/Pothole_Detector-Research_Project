from ultralytics import YOLO

# Load a pretrained YOLOv8n model
model = YOLO("yolov8n.pt")

# Train the model with your dataset
results = model.train(
    data="F:\Research_Project\pothole-detector\Pothole_Dataset/data.yaml",
    epochs=50,
    imgsz=640,
    batch=8
)