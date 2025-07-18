package com.example.sialbrowser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    private Button btnBack;
    private Button btnLogin;
    private String previousUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        btnLogin = findViewById(R.id.btnLogin);

        // Check if views are initialized
        if (btnBack == null || btnLogin == null) {
            Toast.makeText(this, "Error: One or more views not found. Check activity_about.xml IDs.", Toast.LENGTH_LONG).show();
            return;
        }

        // Get previous URL from Intent
        previousUrl = getIntent().getStringExtra("previousUrl");
        if (previousUrl == null) {
            previousUrl = "https://www.google.com"; // Fallback URL
        }

        // Back button to return to previous page
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, BrowserActivity.class);
            intent.putExtra("loadUrl", previousUrl);
            startActivity(intent);
            finish();
        });

        // Login button to navigate to LoginActivity
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}