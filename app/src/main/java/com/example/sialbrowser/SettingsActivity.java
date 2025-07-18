package com.example.sialbrowser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox javaScriptCheckBox;
    private CheckBox darkModeCheckBox;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "BrowserPrefs";
    private static final String JAVASCRIPT_KEY = "javaScriptEnabled";
    private static final String DARK_MODE_KEY = "darkModeEnabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize UI elements
        TextView title = findViewById(R.id.settingsTitle);
        javaScriptCheckBox = findViewById(R.id.javaScriptCheckBox);
        darkModeCheckBox = findViewById(R.id.darkModeCheckBox);

        // Load saved preferences
        boolean isJavaScriptEnabled = sharedPreferences.getBoolean(JAVASCRIPT_KEY, true);
        boolean isDarkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_KEY, false);

        javaScriptCheckBox.setChecked(isJavaScriptEnabled);
        darkModeCheckBox.setChecked(isDarkModeEnabled);

        // Apply dark mode based on saved preference
        applyTheme(isDarkModeEnabled);

        // JavaScript toggle listener
        javaScriptCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(JAVASCRIPT_KEY, isChecked);
            editor.apply();
        });

        // Dark Mode toggle listener
        darkModeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DARK_MODE_KEY, isChecked);
            editor.apply();
            applyTheme(isChecked);
            recreate(); // Recreate activity to apply theme change immediately
        });
    }

    private void applyTheme(boolean isDarkModeEnabled) {
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}