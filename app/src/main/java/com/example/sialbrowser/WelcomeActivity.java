package com.example.sialbrowser;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class WelcomeActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Set status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));

        // Initialize views
        ImageView logo = findViewById(R.id.logo);
        TextView welcomeText = findViewById(R.id.welcomeText);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation progressAnim = AnimationUtils.loadAnimation(this, R.anim.progress_anim);

        // Start animations
        logo.startAnimation(fadeIn);
        welcomeText.startAnimation(bounce);

        progressBar.startAnimation(progressAnim);

        // Proceed to login after delay
        new Handler().postDelayed(() -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DELAY);
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash screen
    }
}