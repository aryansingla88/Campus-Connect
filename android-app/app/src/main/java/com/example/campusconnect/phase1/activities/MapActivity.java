package com.example.campusconnect.phase1.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusconnect.R;
import com.example.campusconnect.phase1.CampusMapView;
import com.example.campusconnect.phase1.model.Event;
import com.example.campusconnect.network.MapService;
import com.example.campusconnect.network.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity {

    CampusMapView mapView;
    boolean isPlacingEvent = false;

    List<Event> eventList = new ArrayList<>();

    float selectedX;
    float selectedY;

    MapService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);

        FloatingActionButton btn = findViewById(R.id.btnAddEvent);

        api = RetrofitClient.getMapService();

        // 👉 Step 1: Enable placement mode
        btn.setOnClickListener(v -> {
            isPlacingEvent = true;
            Toast.makeText(this, "Tap on map to place event", Toast.LENGTH_SHORT).show();
        });

        // 👉 Step 2: Handle map click
        mapView.setOnMapClickListener((x, y) -> {

            if (isPlacingEvent) {

                selectedX = x;
                selectedY = y;

                showEventDialog();

                isPlacingEvent = false;
            }
        });
    }

    void sendEventToBackend(Event e) {

        api.postEvent(e).enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(MapActivity.this, "Event sent!", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.e("API_ERROR", "Code: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                android.util.Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    // 👉 Step 3: Show dialog
    void showEventDialog() {

        View v = getLayoutInflater().inflate(R.layout.dialog_event, null);

        EditText title = v.findViewById(R.id.titleInput);
        EditText desc = v.findViewById(R.id.descInput);
        EditText dateInput = v.findViewById(R.id.dateInput);
        EditText timeInput = v.findViewById(R.id.timeInput);
        Calendar calendar = Calendar.getInstance();

        dateInput.setFocusable(false);

        dateInput.setOnClickListener(view -> {

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view1, selectedYear, selectedMonth, selectedDay) -> {

                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        dateInput.setText(date);

                    }, year, month, day);

            datePicker.show();
        });

        timeInput.setFocusable(false);

        timeInput.setOnClickListener(view -> {

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(this,
                    (view12, selectedHour, selectedMinute) -> {

                        @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                        timeInput.setText(time);

                    }, hour, minute, true);

            timePicker.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Create Event")
                .setView(v)
                .setPositiveButton("Post", (dialog, which) -> {

                    String t = title.getText().toString();
                    String d = desc.getText().toString();
                    String date = dateInput.getText().toString();
                    String time = timeInput.getText().toString();

                    createEvent(t, d, date, time, selectedX, selectedY);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // 👉 Step 4: Store event + update map
    void createEvent(String title, String desc, String date, String time, float x, float y) {

        String eventTime = convertToMySQLFormat(date, time);

        double[] latLng = mapView.getCoordinateConverter().toLatLng(x, y);

        double lat = latLng[0];
        double lng = latLng[1];

        Event e = new Event(
                title,
                desc,
                eventTime,
                lat,
                lng,
                1
        );

        eventList.add(e);

        mapView.addTempEvent(e, selectedX, selectedY);

        sendEventToBackend(e);
    }

    String convertToMySQLFormat(String date, String time) {

        // input: 5/3/2026 → output: 2026-03-05
        String[] parts = date.split("/");

        String day = parts[0];
        String month = parts[1];
        String year = parts[2];

        if (day.length() == 1) day = "0" + day;
        if (month.length() == 1) month = "0" + month;

        return year + "-" + month + "-" + day + " " + time + ":00";
    }
}