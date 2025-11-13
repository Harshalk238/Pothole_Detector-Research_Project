package com.example.potholedectectionapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int FRAME_SKIP_COUNT = 2; // Process every 3rd frame (0, 3, 6, 9...)

    private PreviewView previewView;
    private OverlayView overlayView;
    private TextView tvInfo;

    private TFLitePotholeDetector potholeDetector;
    private ExecutorService detectionExecutor;
    private Handler mainHandler;

    private volatile boolean isProcessing = false;
    private int frameCounter = 0;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
                if (cameraGranted) {
                    startCamera();
                } else {
                    tvInfo.setText("Camera permission required.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);
        tvInfo = findViewById(R.id.tvInfo);

        detectionExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        try {
            potholeDetector = new TFLitePotholeDetector(getApplicationContext(), "model.tflite");
            Log.d(TAG, "Model loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create detector", e);
            tvInfo.setText("Model load failed: " + e.getMessage());
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
                    frameCounter++;

                    // Process every 3rd frame for better performance
                    if (frameCounter % (FRAME_SKIP_COUNT + 1) != 0) {
                        imageProxy.close();
                        return;
                    }

                    // Skip if already processing
                    if (isProcessing) {
                        imageProxy.close();
                        return;
                    }

                    isProcessing = true;

                    Bitmap frameBitmap = previewView.getBitmap();

                    if (frameBitmap != null && potholeDetector != null) {
                        final Bitmap bitmapCopy = frameBitmap.copy(frameBitmap.getConfig(), false);

                        detectionExecutor.execute(() -> {
                            try {
                                long startTime = System.currentTimeMillis();

                                List<TFLitePotholeDetector.DetectionResult> detections =
                                        potholeDetector.detect(bitmapCopy);

                                long endTime = System.currentTimeMillis();
                                long processingTime = endTime - startTime;

                                mainHandler.post(() -> {
                                    overlayView.setResults(detections);

                                    if (!detections.isEmpty()) {
                                        tvInfo.setVisibility(View.VISIBLE);
                                        tvInfo.setText("🚧 " + detections.size() + " pothole" +
                                                (detections.size() > 1 ? "s" : "") +
                                                " detected (" + processingTime + "ms)");
                                    } else {
                                        tvInfo.setVisibility(View.VISIBLE);
                                        tvInfo.setText("✓ Road clear (" + processingTime + "ms)");
                                    }
                                });

                            } catch (Exception e) {
                                Log.e(TAG, "Detection error: " + e.getMessage(), e);
                            } finally {
                                bitmapCopy.recycle();
                                isProcessing = false;
                            }
                        });
                    } else {
                        isProcessing = false;
                    }

                    imageProxy.close();
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

                Log.d(TAG, "Camera started successfully");

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (potholeDetector != null) potholeDetector.close();
        if (detectionExecutor != null) detectionExecutor.shutdown();
    }
}