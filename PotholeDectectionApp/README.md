# 🚧 Real-Time Pothole Detection Android App

An Android application that uses **YOLOv8 TensorFlow Lite** model to detect potholes in real-time through the device camera, helping improve road safety and infrastructure monitoring.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![TensorFlow](https://img.shields.io/badge/TensorFlow-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

---

## 📋 Table of Contents
- [Features](#-features)
- [Demo](#-demo)
- [Architecture](#-architecture)
- [Requirements](#-requirements)
- [Installation](#-installation)
- [Model Setup](#-model-setup)
- [Usage](#-usage)
- [Technical Details](#-technical-details)
- [Project Structure](#-project-structure)
- [Known Limitations](#-known-limitations)
- [Future Enhancements](#-future-enhancements)
- [Contributing](#-contributing)
- [License](#-license)
- [Acknowledgments](#-acknowledgments)

---

## ✨ Features

- ✅ **Real-time Detection**: Detects potholes in live camera feed
- ✅ **Visual Feedback**: Draws bounding boxes around detected potholes
- ✅ **Confidence Scores**: Displays detection confidence percentage
- ✅ **Optimized Performance**: Uses NMS (Non-Maximum Suppression) to eliminate duplicate detections
- ✅ **Smooth UI**: Background processing ensures lag-free camera preview
- ✅ **Lightweight**: Runs entirely on-device using TensorFlow Lite

---

## 🎥 Demo

> **Note**: Add screenshots or video demos here

### Screenshots
| Camera View | Detection in Action | Multiple Potholes |
|------------|---------------------|-------------------|
| ![Screenshot 1](screenshots/screen1.jpg) | ![Screenshot 2](screenshots/screen2.jpg) | ![Screenshot 3](screenshots/screen3.jpg) |

### Video Demo
[Link to demo video]

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                  MainActivity                        │
│  - Camera permission handling                       │
│  - CameraX lifecycle management                     │
│  - UI updates                                       │
└─────────────────┬───────────────────────────────────┘
                  │
                  ├──────────────────────────────────┐
                  │                                  │
        ┌─────────▼──────────┐          ┌───────────▼──────────┐
        │  PreviewView       │          │  TFLitePotholeDetector│
        │  - Camera preview  │          │  - YOLOv8 inference   │
        │  - Frame capture   │          │  - NMS processing     │
        └─────────┬──────────┘          │  - Confidence filter  │
                  │                      └───────────┬──────────┘
                  │                                  │
        ┌─────────▼──────────┐                      │
        │  OverlayView       │◄─────────────────────┘
        │  - Bounding boxes  │
        │  - Label display   │
        └────────────────────┘
```

### Data Flow
1. **Camera Capture**: CameraX captures frames from device camera
2. **Frame Extraction**: `PreviewView.getBitmap()` extracts current frame
3. **Preprocessing**: Frame resized to 640×640 and normalized
4. **Inference**: YOLOv8 TFLite model processes the frame
5. **Post-processing**: NMS removes overlapping detections
6. **Visualization**: Bounding boxes drawn on `OverlayView`

---

## 📱 Requirements

### Hardware
- Android device with camera
- Minimum 2GB RAM recommended
- Android 7.0 (API 24) or higher

### Software
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Gradle 7.0+

### Dependencies
```gradle
dependencies {
    // CameraX
    implementation 'androidx.camera:camera-core:1.3.0'
    implementation 'androidx.camera:camera-camera2:1.3.0'
    implementation 'androidx.camera:camera-lifecycle:1.3.0'
    implementation 'androidx.camera:camera-view:1.3.0'
    
    // TensorFlow Lite
    implementation 'org.tensorflow:tensorflow-lite:2.13.0'
    implementation 'org.tensorflow:tensorflow-lite-support:0.4.4'
    
    // AndroidX
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
}
```

---

## 🚀 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/pothole-detection-app.git
cd pothole-detection-app
```

### 2. Open in Android Studio
- Open Android Studio
- Select "Open an Existing Project"
- Navigate to the cloned directory

### 3. Setup Model File
Download the YOLOv8 TFLite model and place it in the assets folder:
```bash
app/src/main/assets/model.tflite
```

**Model Download**: added the model.tflite file in model files dir.

### 4. Build and Run
- Connect your Android device or start an emulator
- Click "Run" in Android Studio
- Grant camera permissions when prompted

---

## 🤖 Model Setup

### Model Specifications
- **Architecture**: YOLOv8 (You Only Look Once v8)
- **Input Size**: 640×640×3 (RGB)
- **Output Format**: `[1][5][8400]`
  - 5 channels: `[x, y, width, height, confidence]`
  - 8400 possible detection boxes
- **Framework**: TensorFlow Lite
- **Quantization**: Float32 (can be optimized to Float16 or INT8)

### Training Your Own Model
If you want to train a custom model:

1. **Collect Dataset**: Gather pothole images with annotations
2. **Use YOLOv8**: Follow [Ultralytics YOLOv8](https://github.com/ultralytics/ultralytics) documentation
3. **Export to TFLite**:
   ```python
   from ultralytics import YOLO
   
   model = YOLO('yolov8n.pt')
   model.train(data='pothole.yaml', epochs=100)
   model.export(format='tflite')
   ```
4. **Place in Assets**: Copy `model.tflite` to `app/src/main/assets/`

---

## 💻 Usage

### Basic Usage
1. Launch the app
2. Point camera at road surface
3. Potholes will be automatically detected and highlighted in real-time
4. Red bounding boxes indicate detected potholes
5. Confidence percentage shown above each detection

### Adjusting Detection Sensitivity

In `TFLitePotholeDetector.java`, modify:
```java
private static final float CONFIDENCE_THRESHOLD = 0.6f; // Increase for fewer false positives
private static final float IOU_THRESHOLD = 0.4f; // Adjust NMS sensitivity
```

### Processing Performance

To improve performance on slower devices, adjust frame skip in `MainActivity.java`:
```java
private static final int FRAME_SKIP_COUNT = 2; // Process every 3rd frame
```

---

## 🔧 Technical Details

### Key Components

#### 1. **MainActivity.java**
- Handles camera permissions and lifecycle
- Manages CameraX setup and frame capture
- Coordinates between camera preview and detection
- Updates UI with detection results

#### 2. **TFLitePotholeDetector.java**
- Loads and manages TensorFlow Lite model
- Preprocesses camera frames (resize, normalize)
- Runs inference on processed frames
- Applies Non-Maximum Suppression (NMS)
- Returns filtered detection results

**Key Methods:**
```java
public List<DetectionResult> detect(Bitmap bitmap)
private List<DetectionResult> applyNMS(List<DetectionResult> detections)
private float calculateIOU(RectF box1, RectF box2)
```

#### 3. **OverlayView.java**
- Custom View for drawing bounding boxes
- Scales normalized coordinates to screen pixels
- Renders detection labels and confidence scores

### Performance Optimizations

1. **Frame Skipping**: Processes every Nth frame to reduce computational load
2. **Non-Maximum Suppression**: Eliminates duplicate/overlapping detections
3. **Background Processing**: Detection runs on separate thread
4. **Bitmap Recycling**: Proper memory management to prevent leaks
5. **Confidence Filtering**: Only shows high-confidence detections (>60%)

### Threading Model
```
Main Thread:
├─ Camera preview rendering
├─ Bitmap extraction from preview
└─ UI updates (OverlayView, TextView)

Background Thread (detectionExecutor):
├─ TensorFlow Lite inference
├─ NMS processing
└─ Result preparation
```

---

## 📁 Project Structure

```
app/src/main/
├── java/com/example/potholedectectionapp/
│   ├── MainActivity.java              # Main activity, camera setup
│   ├── TFLitePotholeDetector.java    # ML model handler with NMS
│   └── OverlayView.java              # Custom view for bounding boxes
├── res/
│   ├── layout/
│   │   └── activity_main.xml         # UI layout
│   ├── values/
│   │   ├── strings.xml
│   │   ├── colors.xml
│   │   └── themes.xml
│   └── drawable/                      # App icons/images
├── assets/
│   └── model.tflite                  # YOLOv8 TFLite model (not in repo)
└── AndroidManifest.xml               # App configuration
```

---

## ⚠️ Known Limitations

- **Lighting Conditions**: Performance may vary in low-light environments
- **Angle Dependency**: Best results when camera is pointed directly at road
- **Processing Speed**: ~50-150ms per frame depending on device
- **Model Accuracy**: Some potholes may be missed; detection depends on training data quality
- **Battery Usage**: Continuous camera and ML inference consume significant battery

---

## 🔮 Future Enhancements

- [ ] **GPS Integration**: Log pothole locations with coordinates
- [ ] **Cloud Sync**: Upload detections to central database
- [ ] **Severity Classification**: Categorize potholes (minor, moderate, severe)
- [ ] **Historical Data**: Track previously detected potholes
- [ ] **Offline Maps**: Mark potholes on offline maps
- [ ] **Multi-class Detection**: Detect cracks, speed bumps, other road hazards
- [ ] **Model Optimization**: Quantization for faster inference
- [ ] **Night Mode**: Enhanced detection for low-light conditions
- [ ] **Audio Alerts**: Voice warnings when pothole detected
- [ ] **Dashboard**: Analytics showing detection statistics

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow standard Java conventions
- Add comments for complex logic
- Update README if adding new features

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **YOLOv8**: [Ultralytics](https://github.com/ultralytics/ultralytics) for the object detection model
- **TensorFlow Lite**: Google's ML framework for mobile deployment
- **CameraX**: Android Jetpack library for camera operations
- **Dataset**: [Mention dataset source if applicable]

---

## 📧 Contact

**Your Name** - your.email@example.com

**Project Link**: [https://github.com/yourusername/pothole-detection-app](https://github.com/yourusername/pothole-detection-app)

---

## 📊 Performance Metrics

| Metric | Value |
|--------|-------|
| Average Inference Time | 50-150ms |
| Model Size | ~6MB |
| Minimum Android Version | API 24 (Android 7.0) |
| Camera Resolution | 640×640 (processed) |
| Detection Confidence | >60% |
| FPS (approx) | 10-15 FPS |

---

## 🐛 Troubleshooting

### App crashes on launch
- Check camera permissions in device settings
- Ensure `model.tflite` exists in `assets/` folder

### Poor detection accuracy
- Increase `CONFIDENCE_THRESHOLD` to reduce false positives
- Ensure good lighting conditions
- Clean camera lens

### Lag/Performance issues
- Increase `FRAME_SKIP_COUNT` value
- Close background apps
- Test on device with better specs

---

**⭐ If you find this project helpful, please consider giving it a star!**
