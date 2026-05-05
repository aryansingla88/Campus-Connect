package com.example.campusconnect.phase1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusconnect.R;
import com.example.campusconnect.phase1.model.RegisterRequest;
import com.example.campusconnect.phase1.model.RegisterResponse;
import com.example.campusconnect.network.AuthService;
import com.example.campusconnect.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class User_Registration extends AppCompatActivity {

    private EditText emailInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmInput;
    private Button button;
    private TextView warning;

    private static final String EMAIL_SUFFIX = "@nitkkr.ac.in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        emailInput = findViewById(R.id.emailInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmInput = findViewById(R.id.confirmInput);
        button = findViewById(R.id.button);
        warning = findViewById(R.id.warning);

        button.setOnClickListener(v -> {

            String roll = emailInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String confirm = confirmInput.getText().toString();

            String email = roll + "@nitkkr.ac.in";


            // -------------------------------
            // 1️⃣ Roll number validation
            // -------------------------------

            if(!roll.matches("\\d{6,12}")){
                warning.setText("Email must be 6–12 digits.");
                return;
            }


            // -------------------------------
            // 2️⃣ Username validation
            // -------------------------------

            if(!username.matches("^[A-Za-z0-9_]{6,15}$")){
                warning.setText("Username must be 6–15 characters and contain only letters, numbers or underscore.");
                return;
            }


            // -------------------------------
            // 3️⃣ Password validation
            // -------------------------------

            String passwordRegex =
                    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+=-]).{8,15}$";

            if(!password.matches(passwordRegex)){
                warning.setText("Password must be 8–15 chars with uppercase, lowercase, number and special symbol.");
                return;
            }


            // -------------------------------
            // 4️⃣ Confirm password
            // -------------------------------

            if(!password.equals(confirm)){
                warning.setText("Passwords do not match.");
                return;
            }


            // ==================================================
            // ✅ If all validations pass → CALL BACKEND
            // ==================================================

            RegisterRequest request = new RegisterRequest(email, username, password);

            AuthService authService = RetrofitClient
                    .getInstance()
                    .create(AuthService.class);

            Call<RegisterResponse> call = authService.registerUser(request);


            call.enqueue(new Callback<RegisterResponse>() {

                @Override
                public void onResponse(Call<RegisterResponse> call,
                                       Response<RegisterResponse> response) {

                    if(response.body() != null){

                        warning.setText(response.body().getMessage());

                        if(response.body().isSuccess()){

                            // Navigate to login screen
                            Intent intent = new Intent(User_Registration.this, LoginActivity.class);
                            startActivity(intent);

                            // Close registration screen so user can't return with back button
                            finish();
                        }

                    } else {
                        warning.setText("Server error.");
                    }
                }

                @Override
                public void onFailure(Call<RegisterResponse> call, Throwable t) {

                    warning.setText("Network error: " + t.getMessage());

                }
            });

        });
    }
}