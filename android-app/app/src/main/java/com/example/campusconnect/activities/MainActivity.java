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
import android.widget.Button;
import android.content.Intent;

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
// aryan

public class MainActivity extends AppCompatActivity {
    private static final boolean USE_TEST_DATA = false;
    FusedLocationProviderClient fusedLocationProviderClient;   // aryan
    private CampusMapView campusMapView;
    private MapService mapService;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_INTERVAL_MS = 10_000;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {

            getLocation();
            fetchVisibleUsers();

            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        campusMapView = findViewById(R.id.campusMapView);

        Button btnMap = findViewById(R.id.btnMap);
        Button btnChat = findViewById(R.id.btnChat);

        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        mapService = RetrofitClient.getMapService();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        campusMapView.clearAllMarkers();
        campusMapView.updatePOIs(new ArrayList<>());

        getLocation();
        fetchPOIs();
        fetchEvents();
    }

    //aryan
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

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {


                    if (location == null) {
                        return;
                    }


                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    // 🔹 create user
                    UserPresence me = new UserPresence();
                    me.setUserId(1);
                    me.setUsername("ME");
                    me.setLatitude(lat);
                    me.setLongitude(lng);

                    // 🔹 send to backend
                    mapService.updatePresence(me).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                        }
                    });

                    // 🔹 show locally
                    List<UserPresence> users = new ArrayList<>();
                    users.add(me);

                    campusMapView.updateUsers(users);
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

    private void loadTestData() {
        List<UserPresence> users = new ArrayList<>();
        users.add(new UserPresence(2,"Arpit", 29.9448, 76.8164));
        users.add(new UserPresence(3,"Aryan", 29.9446, 76.8168));
        users.add(new UserPresence(4,"Yatharth", 29.9451, 76.8162));

        List<Poi> pois = new ArrayList<>();
        pois.add(new Poi("Library", 29.9450, 76.8160));
        pois.add(new Poi("Cafeteria", 29.9444, 76.8166));
        pois.add(new Poi("Admin Block", 29.9453, 76.8169));
        pois.add(new Poi("Hostel", 29.9442, 76.8158));

        campusMapView.updateUsers(users);
        campusMapView.updatePOIs(pois);
    }

    private void fetchVisibleUsers() {
        mapService.getVisibleUsers().enqueue(new Callback<List<UserPresence>>() {
            @Override
            public void onResponse(Call<List<UserPresence>> call, Response<List<UserPresence>> response) {
                if (response.isSuccessful() && response.body() != null) {
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