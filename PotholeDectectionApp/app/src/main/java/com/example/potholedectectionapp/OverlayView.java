package com.example.potholedectectionapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private static final String TAG = "OverlayView";
    private final Paint boxPaint;
    private final Paint textPaint;
    private List<TFLitePotholeDetector.DetectionResult> results = new ArrayList<>();

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        boxPaint = new Paint();
        boxPaint.setColor(Color.RED);
        boxPaint.setStrokeWidth(10f); // Thicker for visibility
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(50f); // Bigger text
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setShadowLayer(5f, 0f, 0f, Color.BLACK); // Shadow for visibility

        // Make view background slightly visible for debugging
        // setBackgroundColor(Color.argb(30, 0, 255, 0)); // Uncomment to see if view is rendering
    }

    public void setResults(List<TFLitePotholeDetector.DetectionResult> detections) {
        this.results = detections;
        Log.d(TAG, "setResults called with " + (detections != null ? detections.size() : 0) + " detections");
        invalidate();
        postInvalidate(); // Force redraw on UI thread
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        Log.d(TAG, "onDraw - View size: " + viewWidth + "x" + viewHeight +
                ", Results: " + (results != null ? results.size() : "null"));

        if (results == null || results.isEmpty()) {
            return;
        }

        for (TFLitePotholeDetector.DetectionResult result : results) {
            // Calculate actual pixel coordinates
            float left = result.location.left * viewWidth;
            float top = result.location.top * viewHeight;
            float right = result.location.right * viewWidth;
            float bottom = result.location.bottom * viewHeight;

            RectF rect = new RectF(left, top, right, bottom);

            Log.d(TAG, "Drawing box: left=" + left + ", top=" + top +
                    ", right=" + right + ", bottom=" + bottom +
                    ", conf=" + result.confidence);

            // Draw the box
            canvas.drawRect(rect, boxPaint);

            // Draw confidence text above the box
            String label = result.label + " " + String.format("%.0f%%", result.confidence * 100);
            canvas.drawText(label, left + 10, top - 15, textPaint);
        }
    }
}