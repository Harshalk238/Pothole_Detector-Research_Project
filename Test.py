from ultralytics import YOLO
import os

# 🔸 1️⃣ Load your trained model (path to your best weights)
model = YOLO("runs/detect/train3/weights/best.pt")

# 🔸 2️⃣ Set your input path (image, folder, or video)
# Example options:
# source = "D:/Demos/PathholeDetector/test_images"  # folder with images
# source = "D:/Demos/PathholeDetector/test_videos/pothole.mp4"  # video file
# source = 0  # webcam
source = "Pothole_Dataset/test/images"   # ✅ Update this path

# 🔸 3️⃣ Run inference
results = model.predict(
    source=source,       # path to file, folder, or webcam (0)
    conf=0.25,           # confidence threshold (0–1); lower = more detections
    save=True,           # save output images/videos with boxes
    show=True,           # display output live (optional)
    save_txt=True,       # save detection results in .txt files
    save_conf=True       # save confidence scores
)

# 🔸 4️⃣ Print results summary
print("\n✅ Detection completed.")
print(f"Results saved in: {os.path.abspath(model.predictor.save_dir)}")
