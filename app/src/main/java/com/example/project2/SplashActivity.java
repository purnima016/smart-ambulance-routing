package com.example.project2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

// ✅ ADD FIREBASE IMPORT
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private Handler sirenHandler;
    private boolean isSirenActive = false;
    private FirebaseAuth mAuth; // ✅ ADD FIREBASE AUTH

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // ✅ INITIALIZE FIREBASE AUTH
        mAuth = FirebaseAuth.getInstance();

        startProfessionalAnimation();
    }

    private void startProfessionalAnimation() {
        TextView appName = findViewById(R.id.appName);
        LinearLayout whiteCircle = findViewById(R.id.whiteCircle);
        TextView ambulanceIcon = findViewById(R.id.ambulanceIcon);
        ProgressBar loadingProgress = findViewById(R.id.loadingProgress);
        TextView loadingText = findViewById(R.id.loadingText);

        // Load animations
        Animation ambulanceDrop = AnimationUtils.loadAnimation(this, R.anim.ambulance_drop);
        Animation textZoom = AnimationUtils.loadAnimation(this, R.anim.text_zoom);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        // ✅ REMOVED UNUSED sirenFlash variable

        // FASTER SEQUENCE: Text → White Circle (within 1 second) → Ambulance → Loading
        new Handler().postDelayed(() -> {
            // 1. First show "Smart Ambulance Routing" text
            appName.setAlpha(1f);
            appName.startAnimation(textZoom);
        }, 500); // Text appears at 0.5 seconds

        new Handler().postDelayed(() -> {
            // 2. Then show white circle (FASTER - within 1 second of text)
            whiteCircle.setAlpha(1f);
            whiteCircle.startAnimation(fadeIn);
        }, 1000); // Circle appears at 1.0 seconds (only 0.5s after text)

        new Handler().postDelayed(() -> {
            // 3. Then ambulance drops into the circle
            ambulanceIcon.setAlpha(1f);
            ambulanceIcon.startAnimation(ambulanceDrop);

            // Start siren flash AFTER ambulance finishes dropping
            new Handler().postDelayed(() -> {
                startSirenFlash(ambulanceIcon);
            }, 1200); // Start siren after drop animation completes
        }, 1500); // Ambulance drops at 1.5 seconds

        new Handler().postDelayed(() -> {
            // 4. Then loading appears
            loadingText.setAlpha(1f);
            loadingProgress.setAlpha(1f);
            loadingText.startAnimation(fadeIn);
            loadingProgress.startAnimation(bounce);
        }, 3000); // Loading appears at 3.0 seconds

        // AUTO-NAVIGATE AFTER 5 SECONDS - CHECK LOGIN STATUS
        new Handler().postDelayed(() -> {
            checkLoginStatus(); // ✅ CHECK IF USER IS ALREADY LOGGED IN
        }, 5000); // 5 seconds total
    }

    private void startSirenFlash(final TextView ambulanceIcon) {
        isSirenActive = true;
        sirenHandler = new Handler();

        sirenHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isSirenActive && ambulanceIcon != null) {
                    // Load and start siren flash animation
                    Animation flash = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.alpha_siren_flash);
                    ambulanceIcon.startAnimation(flash);
                    // Repeat every 1.5 seconds
                    sirenHandler.postDelayed(this, 1500);
                }
            }
        });
    }

    private void stopSirenFlash() {
        isSirenActive = false;
        if (sirenHandler != null) {
            sirenHandler.removeCallbacksAndMessages(null);
        }
    }

    // ✅ CHECK LOGIN STATUS AND NAVIGATE ACCORDINGLY
    private void checkLoginStatus() {
        stopSirenFlash();

        if (mAuth.getCurrentUser() != null) {
            // User is ALREADY LOGGED IN → Go directly to Main App
            Intent intent = new Intent(SplashActivity.this, MainActivity.class); // ✅ FIXED: Go to MainActivity
            startActivity(intent);
            finish();
        } else {
            // User is NOT logged in → Show Login Screen
            Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // ✅ REMOVED navigateToAuthActivity() - REPLACED BY checkLoginStatus()

    @Override
    protected void onPause() {
        super.onPause();
        stopSirenFlash();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSirenFlash();
        // ✅ REMOVED THE METHOD INSIDE onDestroy() - CAUSING ERRORS
    }
}