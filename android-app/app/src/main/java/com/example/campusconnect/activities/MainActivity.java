package com.example.campusconnect.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

//aryan
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.widget.TextView;
//aryan

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusconnect.R;
import com.example.campusconnect.CampusMapView;
import com.example.campusconnect.network.MapService;
import com.example.campusconnect.model.Poi;
import com.example.campusconnect.model.UserPresence;
import com.example.campusconnect.model.Event;
import com.example.campusconnect.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// aryan
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
// aryan

public class MainActivity extends AppCompatActivity {
    private static final boolean USE_TEST_DATA = false;
    FusedLocationProviderClient fusedLocationProviderClient;   // aryan
    LocationRequest locationRequest;     // aryan
    LocationCallback locationCallback;   // aryan
    private CampusMapView campusMapView;
    private MapService mapService;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_INTERVAL_MS = 10_000;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {

            fetchVisibleUsers();

            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        campusMapView = findViewById(R.id.campusMapView);

        FloatingActionButton fabMap = findViewById(R.id.fabMap);
        FloatingActionButton fabChat = findViewById(R.id.fabChat);

        fabMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        fabChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        //aryan
        mapService = RetrofitClient.getMapService();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //  create location request
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //  create callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();

                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    sendLocationToBackend(lat, lng);
                }
            }
        };
        //aryan

        campusMapView.clearAllMarkers();
        campusMapView.updatePOIs(new ArrayList<>());

        getLocation();
        fetchPOIs();
        fetchEvents();
    }

    private void getLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1
            );
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }
    private void sendLocationToBackend(double lat, double lng) {

        SharedPreferences prefs =
                getSharedPreferences("CampusApp", MODE_PRIVATE);

        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Log.e("MAP", "User not logged in");
            return;
        }

        // 🔹 STEP 1: show ME locally (instant)
        UserPresence me = new UserPresence();
        me.setUserId(userId);
        me.setUsername("Me"); // temporary
        me.setLatitude(lat);
        me.setLongitude(lng);

        List<UserPresence> tempList = new ArrayList<>();
        tempList.add(me);


        // 🔹 STEP 2: send to backend
        mapService.updatePresence(me).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                // 🔹 STEP 3: fetch real users (replace UI)
                fetchVisibleUsers();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("API", "Update failed");
            }
        });
    }

    //aryan
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEvents();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }


    private void fetchVisibleUsers() {
        mapService.getVisibleUsers().enqueue(new Callback<List<UserPresence>>() {
            @Override
            public void onResponse(Call<List<UserPresence>> call, Response<List<UserPresence>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    Log.d("API", "Total users: " + response.body().size());

                    for (UserPresence u : response.body()) {
                        Log.d("API", "User: " + u.getUserId() + " -> " + u.getUsername());
                    }

                    campusMapView.updateUsers(response.body());

                } else {
                    Log.e("MainActivity", "Users response unsuccessful");
                }
            }

            @Override
            public void onFailure(Call<List<UserPresence>> call, Throwable t) {
                Log.e("MainActivity", "Failed to fetch users: " + t.getMessage());
            }
        });
    }

    private void fetchPOIs() {
        mapService.getPOIs().enqueue(new Callback<List<Poi>>() {
            @Override
            public void onResponse(Call<List<Poi>> call, Response<List<Poi>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    campusMapView.updatePOIs(response.body());
                } else {
                    Log.e("MainActivity", "POIs response unsuccessful");
                }
            }

            @Override
            public void onFailure(Call<List<Poi>> call, Throwable t) {
                Log.e("MainActivity", "Failed to fetch POIs: " + t.getMessage());
            }
        });
    }

    private void fetchEvents() {
        mapService.getEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    campusMapView.updateEvents(response.body());
                } else {
                    Log.e("EVENT", "Events response unsuccessful");
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Log.e("EVENT", "Failed: " + t.getMessage());
            }
        });
    }
}