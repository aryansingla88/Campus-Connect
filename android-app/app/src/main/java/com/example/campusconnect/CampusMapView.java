package com.example.campusconnect;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.campusconnect.map.CoordinateConverter;
import com.example.campusconnect.map.MapMarker;
import com.example.campusconnect.map.MarkerRenderer;
import com.example.campusconnect.map.MarkerType;
import com.example.campusconnect.model.Poi;
import com.example.campusconnect.model.UserPresence;
import com.example.campusconnect.model.Event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CampusMapView extends View {

    private Bitmap campusMap;

    private final List<MapMarker> markers = new ArrayList<>();
    private final List<UserPresence> userPresences = new ArrayList<>();
    private final List<Poi> pois = new ArrayList<>();
    // Arpit (API events)
    private final List<Event> events = new ArrayList<>();

    // Yatharth (temporary events)
    private final List<Event> tempEvents = new ArrayList<>();

    private MarkerRenderer markerRenderer;
    private CoordinateConverter coordinateConverter;

    private float scaleFactor = 1f;
    private float minScale = 1f;
    private float maxScale = 5f;

    private float offsetX = 0f;
    private float offsetY = 0f;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private MapMarker selectedMarker = null;

    private static final float TAP_RADIUS = 35f;

    private Paint bgPaint;

    //yatharth
    public CoordinateConverter getCoordinateConverter() {
        return coordinateConverter;
    }
    //yatharth


    //yatharth
    public interface OnMapClickListener {
        void onMapClick(float x, float y);
    }

    private OnMapClickListener mapClickListener;

    public void setOnMapClickListener(OnMapClickListener listener) {
        this.mapClickListener = listener;
    }
    // yatharth


    public CampusMapView(Context context) {
        super(context);
        init(context);
    }

    public CampusMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CampusMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        campusMap = loadLargeBitmapFromAssets(context, "campus_map_large.png", 3000, 3000);

        markerRenderer = new MarkerRenderer(context);

        coordinateConverter = new CoordinateConverter(
                29.946639, 76.817920, 558.0475f, 742.3594f,   // P1
                29.947625, 76.816872, 799.96313f, 510.70312f, // P2
                29.946102, 76.815308, 325.72125f, 182.39062f  // P3
        );
        bgPaint = new Paint();
        bgPaint.setColor(Color.BLACK);

        setLayerType(LAYER_TYPE_HARDWARE, null);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());

        if (campusMap == null) {
            Log.e("CampusMapView", "Failed to load bitmap");
        }
    }

    public void updateUsers(List<UserPresence> users) {
        userPresences.clear();
        if (users != null) {
            userPresences.addAll(users);
        }
        rebuildMarkers();
    }

    public void updatePOIs(List<Poi> poiList) {
        pois.clear();
        if (poiList != null) {
            pois.addAll(poiList);
        }
        rebuildMarkers();
    }

    public void updateEvents(List<Event> eventList) {
        events.clear();
        if (eventList != null) {
            events.addAll(eventList);
        }
        rebuildMarkers();
    }

    // yatharth
    private final List<PointF> tempEventPixels = new ArrayList<>();

    public void addTempEvent(Event event, float x, float y) {
        tempEvents.add(event);                    // for backend
        tempEventPixels.add(new PointF(x, y));    // for UI
        rebuildMarkers();
    }

    public void clearTempEvents() {
        tempEvents.clear();
        rebuildMarkers();
    }

    public void clearAllMarkers() {
        userPresences.clear();
        pois.clear();
        markers.clear();
        selectedMarker = null;
        postInvalidateOnAnimation();
    }
    private void rebuildMarkers() {
        MapMarker oldSelected = selectedMarker;
        markers.clear();
        selectedMarker = null;

        if (campusMap == null || coordinateConverter == null) {
            return;
        }

        for (UserPresence user : userPresences) {
            MapMarker userMarker = new MapMarker(
                    MarkerType.USER,
                    user.getLatitude(),
                    user.getLongitude(),
                    user.getUsername()
            );

            PointF point = coordinateConverter.toPixel(
                    userMarker.getLatitude(),
                    userMarker.getLongitude()
            );

            float clampedX = Math.max(0, Math.min(point.x, campusMap.getWidth()));
            float clampedY = Math.max(0, Math.min(point.y, campusMap.getHeight()));

            userMarker.setPixelPosition(clampedX, clampedY);
            markers.add(userMarker);
        }

        for (Poi poi : pois) {
            MapMarker poiMarker = new MapMarker(
                    MarkerType.POI,
                    poi.getLatitude(),
                    poi.getLongitude(),
                    poi.getName()
            );

            PointF point = coordinateConverter.toPixel(
                    poiMarker.getLatitude(),
                    poiMarker.getLongitude()
            );

            float clampedX = Math.max(0, Math.min(point.x, campusMap.getWidth()));
            float clampedY = Math.max(0, Math.min(point.y, campusMap.getHeight()));

            poiMarker.setPixelPosition(clampedX, clampedY);
            markers.add(poiMarker);
        }

        // 🔵 Arpit events (API)
        for (Event event : events) {
            MapMarker eventMarker = new MapMarker(
                    MarkerType.EVENT,
                    event.latitude,
                    event.longitude,
                    "Event"
            );

            PointF point = coordinateConverter.toPixel(
                    event.latitude,
                    event.longitude
            );

            float clampedX = Math.max(0, Math.min(point.x, campusMap.getWidth()));
            float clampedY = Math.max(0, Math.min(point.y, campusMap.getHeight()));

            eventMarker.setPixelPosition(clampedX, clampedY);
            markers.add(eventMarker);
        }

// 🟡 Yatharth temp events
        for (PointF p : tempEventPixels) {

            MapMarker tempMarker = new MapMarker(
                    MarkerType.EVENT,
                    0, 0,
                    "Temp Event"
            );

            tempMarker.setPixelPosition(p.x, p.y);
            markers.add(tempMarker);
        }

        postInvalidateOnAnimation();
        if (oldSelected != null) {
            for (MapMarker m : markers) {
                if (m.getType() == oldSelected.getType() &&
                        m.getLabel().trim().equalsIgnoreCase(oldSelected.getLabel().trim())) {

                    selectedMarker = m;
                    break;
                }
            }
        }
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (campusMap == null || w == 0 || h == 0) return;

        float scaleX = (float) w / campusMap.getWidth();
        float scaleY = (float) h / campusMap.getHeight();

        float initialScale = Math.max(scaleX, scaleY);

        scaleFactor = initialScale;
        minScale = initialScale;
        maxScale = initialScale * 5f;

        float scaledWidth = campusMap.getWidth() * scaleFactor;
        float scaledHeight = campusMap.getHeight() * scaleFactor;

        offsetX = (w - scaledWidth) / 2f;
        offsetY = (h - scaledHeight) / 2f;

        clampOffsets();
    }


    private void clampOffsets() {
        if (campusMap == null) return;

        float scaledWidth = campusMap.getWidth() * scaleFactor;
        float scaledHeight = campusMap.getHeight() * scaleFactor;

        float minOffsetX = getWidth() - scaledWidth;
        float maxOffsetX = 0f;

        float minOffsetY = getHeight() - scaledHeight;
        float maxOffsetY = 0f;

        if (scaledWidth <= getWidth()) {
            offsetX = (getWidth() - scaledWidth) / 2f;
        } else {
            offsetX = Math.max(minOffsetX, Math.min(offsetX, maxOffsetX));
        }

        if (scaledHeight <= getHeight()) {
            offsetY = (getHeight() - scaledHeight) / 2f;
        } else {
            offsetY = Math.max(minOffsetY, Math.min(offsetY, maxOffsetY));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        if (campusMap == null) return;

        canvas.save();

        canvas.translate(offsetX, offsetY);
        canvas.scale(scaleFactor, scaleFactor);

        canvas.drawBitmap(campusMap, 0, 0, null);
        markerRenderer.drawMarkers(canvas, markers, selectedMarker);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean scaleHandled = scaleGestureDetector.onTouchEvent(event);
        boolean gestureHandled = gestureDetector.onTouchEvent(event);

        return scaleHandled || gestureHandled || super.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            handleTap(e.getX(), e.getY());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            offsetX -= distanceX;
            offsetY -= distanceY;

            clampOffsets();
            postInvalidateOnAnimation();
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float prevScale = scaleFactor;

            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));

            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            float scaleChange = scaleFactor / prevScale;

            offsetX = focusX - (focusX - offsetX) * scaleChange;
            offsetY = focusY - (focusY - offsetY) * scaleChange;

            clampOffsets();
            postInvalidateOnAnimation();
            return true;
        }
    }

    private void handleTap(float screenX, float screenY) {
        float mapX = (screenX - offsetX) / scaleFactor;
        float mapY = (screenY - offsetY) / scaleFactor;

        // yatharth
        if (mapClickListener != null) {
            mapClickListener.onMapClick(mapX, mapY);
        }
        // yatharth

        MapMarker tappedMarker = findTappedMarker(mapX, mapY);

        if (tappedMarker != null) {
            selectedMarker = tappedMarker;
        } else {
            selectedMarker = null;
        }

        postInvalidateOnAnimation();
    }

    private MapMarker findTappedMarker(float mapX, float mapY) {
        for (MapMarker marker : markers) {
            float dx = marker.getX() - mapX;
            float dy = marker.getY() - mapY;
            float distSq = dx * dx + dy * dy;

            if (distSq <= TAP_RADIUS * TAP_RADIUS) {
                return marker;
            }
        }
        return null;
    }

    private Bitmap loadLargeBitmapFromAssets(Context context, String fileName, int reqWidth, int reqHeight) {
        AssetManager assetManager = context.getAssets();

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            InputStream is = assetManager.open(fileName);
            BitmapFactory.decodeStream(is, null, options);
            is.close();

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            is = assetManager.open(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            is.close();

            return bitmap;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}