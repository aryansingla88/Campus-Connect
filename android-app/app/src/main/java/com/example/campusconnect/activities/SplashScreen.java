package com.example.campusconnect.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.campusconnect.R;

public class SplashScreen extends AppCompatActivity {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        LottieAnimationView lottie = findViewById(R.id.lottieAnimation);
        TextView appName = findViewById(R.id.appName);

        lottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                appName.setTranslationY(40);
                appName.setAlpha(0);

                appName.animate()
                        .translationY(0)
                        .alpha(1)
                        .setDuration(600)
                        .start();

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences prefs =
                                getSharedPreferences("CampusApp", MODE_PRIVATE);

                        int userId = prefs.getInt("user_id", -1);

                        Intent intent;

                        if (userId != -1) {

                            // User already logged in

                            intent = new Intent(
                                    SplashScreen.this,
                                    MainActivity.class
                            );

                        } else {

                            // User not logged in

                            intent = new Intent(
                                    SplashScreen.this,
                                    LoginActivity.class
                            );
                        }

                        startActivity(intent);
                        finish();
                    }
                };

                handler.postDelayed(runnable, 1200);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}