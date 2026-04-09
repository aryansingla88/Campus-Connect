package com.example.campusconnect.map;
import com.example.campusconnect.R;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
// yatharth
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.content.Context;
// yatharth

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerRenderer {

    private final Paint markerPaint;
    private final Paint textPaint;
    private final Paint labelBgPaint;
    private final Paint selectedRingPaint;

    Bitmap eventBitmap;    //yatharth
    private final Map<String, Float> textWidthCache = new HashMap<>();

    public MarkerRenderer(Context context) {
        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        textPaint.setFakeBoldText(true);

        labelBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelBgPaint.setColor(Color.argb(170, 20, 20, 20));
        labelBgPaint.setStyle(Paint.Style.FILL);

        selectedRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedRingPaint.setStyle(Paint.Style.STROKE);
        selectedRingPaint.setStrokeWidth(4f);
        selectedRingPaint.setColor(Color.rgb(255, 214, 102));

        // yatharth
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_location_on);

        if (drawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            eventBitmap = bitmap;
        }
        // yatharth

    }

    public void drawMarkers(Canvas canvas, List<MapMarker> markers, MapMarker selectedMarker) {
        for (MapMarker marker : markers) {
            float x = marker.getX();
            float y = marker.getY();

            markerPaint.setColor(getColor(marker.getType()));

            if (marker == selectedMarker) {
                canvas.drawCircle(x, y, 14f, selectedRingPaint);
            }

            // yatharth
            if (marker.getType() == MarkerType.EVENT && eventBitmap != null) {

                canvas.drawBitmap(
                        eventBitmap,
                        x - eventBitmap.getWidth() / 2f,
                        y - eventBitmap.getHeight(),
                        null
                );
                // yatharth

            } else {
                // default circle
                canvas.drawCircle(x, y, 8.4f, markerPaint);
            }

            if (marker == selectedMarker) {
                drawLabel(canvas, marker.getLabel(), x, y);
            }
        }
    }

    private void drawLabel(Canvas canvas, String text, float x, float y) {
        if (text == null || text.trim().isEmpty()) return;

        float textWidth = getTextWidth(text);
        float paddingX = 12f;
        float paddingY = 8f;

        float left = x + 16f;
        float bottom = y - 14f;
        float top = bottom - 36f;
        float right = left + textWidth + paddingX * 2;

        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, 10f, 10f, labelBgPaint);

        float textX = left + paddingX;
        float textY = bottom - paddingY;
        canvas.drawText(text, textX, textY, textPaint);
    }

    private float getTextWidth(String text) {
        Float cached = textWidthCache.get(text);
        if (cached != null) {
            return cached;
        }

        float width = textPaint.measureText(text);
        textWidthCache.put(text, width);
        return width;
    }

    private int getColor(MarkerType type) {
        switch (type) {
            case USER:
                return Color.rgb(102, 204, 255);   // smooth light blue

            case POI:
                return Color.rgb(255, 140, 120);   // smooth coral red

            case EVENT:
                return Color.RED;

            default:
                return Color.rgb(140, 220, 140);   // soft green
        }
    }
}