package com.example.campus_connect.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.campus_connect.R;
import com.example.campus_connect.api.ApiService;
import com.example.campus_connect.model.LoginRequest;
import com.example.campus_connect.model.LoginResponse;
import com.example.campus_connect.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button button;
    private TextView warning;
    private TextView registerText;
    private TextView clickableusername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.UserName);
        password = findViewById(R.id.Password);
        registerText = findViewById(R.id.registerText);
        button = findViewById(R.id.button);
        warning = findViewById(R.id.warning);
        clickableusername=findViewById(R.id.username);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Name = username.getText().toString().trim();
                String Pass = password.getText().toString().trim();

                // -------------------------------
                // 1️⃣ Username empty check
                // -------------------------------

                if(Name.isEmpty()){
                    warning.setText("Username cannot be empty");
                    return;
                }

                // -------------------------------
                // 2️⃣ Password empty check
                // -------------------------------

                if(Pass.isEmpty()){
                    warning.setText("Password cannot be empty");
                    return;
                }

                // ==================================================
                // ✅ If both fields valid → CALL BACKEND
                // ==================================================

                LoginRequest request = new LoginRequest(Name, Pass);

                ApiService apiService = RetrofitClient
                        .getInstance()
                        .create(ApiService.class);

                Call<LoginResponse> call = apiService.loginUser(request);

                call.enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(Call<LoginResponse> call,
                                           Response<LoginResponse> response) {

                        if(response.body() != null){

                            if(response.body().isSuccess()){
                                /*
                                SharedPreferences in Android is a lightweight key–value storage used to
                                 save small amounts of persistent data on the device.
                                 a simple local storage file for app settings or small data that
                                 should remain even after the app is closed.

                                 Why the name SharedPreferences?
                                 Because the data stored there is:
                                 Shared → accessible across different parts of the app
                                 Preferences → typically used to store user/app settings
                                 */
                                SharedPreferences prefs = getSharedPreferences("CampusApp", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();

                                editor.putInt("user_id", response.body().getUser_id());
                                editor.apply();

                                // Login successful → go to next activity
                                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                //WHENEVER MAP ACTIVITY GETS READY WE ARE GOING TO REPLACE ChatActivity.class with MapActivity.class
                                // thus we will go to map activity.
                                startActivity(intent);
                                finish();

                            } else {

                                // Show backend message
                                warning.setText(response.body().getMessage());
                            }

                        } else {
                            warning.setText("Server error.");
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {

                        warning.setText("Network error: " + t.getMessage());
                    }
                });

            }
        });

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, User_Registration.class);
                startActivity(intent);

            }
        });
        clickableusername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);

            }
        });
    }
}