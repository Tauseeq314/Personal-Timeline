package com.example.smarttimeline;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.smarttimeline.notification.NotificationScheduler;
import com.example.smarttimeline.ui.timeline.TimelineFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Initialize daily reminder notification
        NotificationScheduler.scheduleDefaultDailyReminder(this);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_timeline) {
                selectedFragment = new TimelineFragment();
            } else if (itemId == R.id.nav_analytics) {
                selectedFragment = new com.example.smarttimeline.ui.analytics.AnalyticsFragment();
            } else if (itemId == R.id.nav_summary) {
                selectedFragment = new com.example.smarttimeline.ui.summary.SummaryFragment();
            }
            else if (itemId == R.id.nav_settings) {
                selectedFragment = new com.example.smarttimeline.ui.settings.SettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TimelineFragment())
                    .commit();
        }

    }
}