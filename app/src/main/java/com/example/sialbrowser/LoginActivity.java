package com.example.sialbrowser;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button guestBtn = findViewById(R.id.guestLoginButton);

        // Set click listeners
        btnLogin.setOnClickListener(v -> loginWithEmail());
        guestBtn.setOnClickListener(v -> proceedToBrowser());
    }

    private void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be ≥ 6 characters");
            return;
        }

        // Fake authentication
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
        proceedToBrowser();
    }

    private void proceedToBrowser() {
        startActivity(new Intent(this, BrowserActivity.class));
        finish();
    }
}