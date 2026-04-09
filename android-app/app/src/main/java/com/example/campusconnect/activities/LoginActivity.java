package com.example.campusconnect.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusconnect.R;
import com.example.campusconnect.model.LoginRequest;
import com.example.campusconnect.model.LoginResponse;
import com.example.campusconnect.network.AuthService;
import com.example.campusconnect.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button button;
    private TextView warning;
    private TextView registerText;
    private TextView clickableusername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

                AuthService authService = RetrofitClient
                        .getInstance()
                        .create(AuthService.class);

                Call<LoginResponse> call = authService.loginUser(request);

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
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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

                Intent intent = new Intent(LoginActivity.this, User_Registration.class);
                startActivity(intent);

            }
        });
        clickableusername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                startActivity(intent);

            }
        });
    }
}