# Pothole Detector - Research Project 🚧

This repository contains the code for a real-time pothole detection system developed as a first step of a research project. The system uses a custom-trained **YOLOv8** model and is designed for eventual deployment on an Android application.

## Getting Started 🚀

### Prerequisites

  - Python 3.x
  - A GPU (NVIDIA preferred) with CUDA support for faster training

### Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Harshalk238/Pothole_Detector-Research_Project.git
    cd Pothole_Detector-Research_Project
    ```
2.  **Create and activate a Python virtual environment:**
      - On macOS/Linux:
        ```bash
        python3 -m venv venv
        source venv/bin/activate
        ```
      - On Windows:
        ```bash
        python -m venv venv
        .\venv\Scripts\activate
        ```
3.  **Install the required packages:**
    ```bash
    pip install -r requirements.txt
    ```

## Usage 🛠️

### 1\. Training the Model

To train the YOLOv8 model on your custom dataset, use the `train.py` script. Ensure your dataset is in the correct YOLO format and your `data.yaml` file points to the right image directories.

```bash
python train.py
```

### 2\. Testing the Model

After training, you can test the model's performance on a video. The output video with detected potholes will be saved to the `runs/` directory.

```bash
yolo task=detect mode=predict model=runs/detect/train/weights/best.pt source="path/to/your/video.mp4" save=True
```

### 3\. Validation

To evaluate the model's performance and get metrics like Precision and mAP, run the validation script:

```bash
python validate.py
```

## Technologies Used ✨

  - **YOLOv8**: The deep learning model used for real-time object detection.
  - **Ultralytics**: The framework that provides the YOLOv8 model and a simplified training pipeline.
  - **OpenCV**: Used for video processing and visualization during testing.
  - **TensorFlow Lite**: The framework for converting the model to a mobile-friendly format for the Android app. **( working on it )**

-----
