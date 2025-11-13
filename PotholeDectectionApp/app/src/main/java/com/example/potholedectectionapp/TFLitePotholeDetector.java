package com.example.potholedectectionapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TFLitePotholeDetector {
    private static final String TAG = "TFLitePotholeDetector";

    private final Interpreter tflite;
    private static final int INPUT_SIZE = 640;
    private static final float CONFIDENCE_THRESHOLD = 0.6f; // Increased from 0.5
    private static final float IOU_THRESHOLD = 0.4f; // For NMS
    private static final int NUM_DETECTIONS = 8400;

    public static class DetectionResult {
        public final String label;
        public final float confidence;
        public final RectF location;

        public DetectionResult(String label, float confidence, RectF location) {
            this.label = label;
            this.confidence = confidence;
            this.location = location;
        }
    }

    public TFLitePotholeDetector(Context context, String modelPath) throws Exception {
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, modelPath);
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        tflite = new Interpreter(tfliteModel, options);
        Log.d(TAG, "Model loaded successfully");
    }

    public List<DetectionResult> detect(Bitmap bitmap) {
        if (bitmap == null) return new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // Resize bitmap
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

        // Prepare input tensor
        float[][][][] input = new float[1][INPUT_SIZE][INPUT_SIZE][3];
        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) {
                int pixel = resized.getPixel(x, y);
                input[0][y][x][0] = ((pixel >> 16) & 0xFF) / 255.0f;
                input[0][y][x][1] = ((pixel >> 8) & 0xFF) / 255.0f;
                input[0][y][x][2] = (pixel & 0xFF) / 255.0f;
            }
        }

        // Recycle resized bitmap to save memory
        if (resized != bitmap) {
            resized.recycle();
        }

        // Run inference
        float[][][] output = new float[1][5][NUM_DETECTIONS];
        try {
            tflite.run(input, output);
        } catch (Exception e) {
            Log.e(TAG, "Inference error: " + e.getMessage());
            return new ArrayList<>();
        }

        List<DetectionResult> detections = processDetections(output);

        // Apply Non-Maximum Suppression to remove overlapping boxes
        List<DetectionResult> finalResults = applyNMS(detections);

        long endTime = System.currentTimeMillis();
        Log.d(TAG, "Detection took " + (endTime - startTime) + "ms, found " + finalResults.size() + " potholes");

        return finalResults;
    }

    private List<DetectionResult> processDetections(float[][][] output) {
        List<DetectionResult> results = new ArrayList<>();

        for (int i = 0; i < NUM_DETECTIONS; i++) {
            float x = output[0][0][i];
            float y = output[0][1][i];
            float w = output[0][2][i];
            float h = output[0][3][i];
            float conf = output[0][4][i];

            if (conf > CONFIDENCE_THRESHOLD) {
                // Convert from center format to corner format
                float left = x - w / 2f;
                float top = y - h / 2f;
                float right = x + w / 2f;
                float bottom = y + h / 2f;

                // Clamp to [0,1]
                left = Math.max(0, Math.min(left, 1));
                top = Math.max(0, Math.min(top, 1));
                right = Math.max(0, Math.min(right, 1));
                bottom = Math.max(0, Math.min(bottom, 1));

                RectF rect = new RectF(left, top, right, bottom);
                results.add(new DetectionResult("Pothole", conf, rect));
            }
        }

        return results;
    }

    /**
     * Non-Maximum Suppression (NMS)
     * Removes overlapping bounding boxes, keeping only the best ones
     */
    private List<DetectionResult> applyNMS(List<DetectionResult> detections) {
        if (detections.isEmpty()) return detections;

        // Sort by confidence (highest first)
        Collections.sort(detections, new Comparator<DetectionResult>() {
            @Override
            public int compare(DetectionResult o1, DetectionResult o2) {
                return Float.compare(o2.confidence, o1.confidence);
            }
        });

        List<DetectionResult> result = new ArrayList<>();
        boolean[] suppressed = new boolean[detections.size()];

        for (int i = 0; i < detections.size(); i++) {
            if (suppressed[i]) continue;

            DetectionResult current = detections.get(i);
            result.add(current);

            // Suppress overlapping boxes
            for (int j = i + 1; j < detections.size(); j++) {
                if (suppressed[j]) continue;

                DetectionResult candidate = detections.get(j);
                float iou = calculateIOU(current.location, candidate.location);

                if (iou > IOU_THRESHOLD) {
                    suppressed[j] = true;
                }
            }
        }

        return result;
    }

    /**
     * Calculate Intersection over Union (IoU) between two bounding boxes
     */
    private float calculateIOU(RectF box1, RectF box2) {
        float intersectionLeft = Math.max(box1.left, box2.left);
        float intersectionTop = Math.max(box1.top, box2.top);
        float intersectionRight = Math.min(box1.right, box2.right);
        float intersectionBottom = Math.min(box1.bottom, box2.bottom);

        float intersectionWidth = Math.max(0, intersectionRight - intersectionLeft);
        float intersectionHeight = Math.max(0, intersectionBottom - intersectionTop);
        float intersectionArea = intersectionWidth * intersectionHeight;

        float box1Area = (box1.right - box1.left) * (box1.bottom - box1.top);
        float box2Area = (box2.right - box2.left) * (box2.bottom - box2.top);
        float unionArea = box1Area + box2Area - intersectionArea;

        return unionArea > 0 ? intersectionArea / unionArea : 0;
    }

    public void close() {
        tflite.close();
    }
}