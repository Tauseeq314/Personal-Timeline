package com.example.smarttimeline;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.smarttimeline.notification.NotificationScheduler;
import com.example.smarttimeline.ui.timeline.TimelineFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private TextView toolbarTitle;
    private ImageView iconProfile;
    private FloatingActionButton fabQuickAdd;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        initializeViews();
        setupBottomNavigation();
        setupClickListeners();

        // Initialize daily reminder notification
        NotificationScheduler.scheduleDefaultDailyReminder(this);

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new TimelineFragment(), "Timeline");
        }
        showWelcomeBannerIfFirstLaunch();
    }

    private void initializeViews() {
        toolbarTitle = findViewById(R.id.toolbarTitle);
        iconProfile = findViewById(R.id.iconProfile);
        fabQuickAdd = findViewById(R.id.fabQuickAdd);
        bottomNav = findViewById(R.id.bottom_navigation);
    }

    private void showWelcomeBannerIfFirstLaunch() {
        android.content.SharedPreferences prefs = getSharedPreferences("smarttimeline_prefs", MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean("first_launch", true);

        if (isFirstLaunch) {
            // Show a welcome snackbar
            View rootView = findViewById(R.id.main);
            com.google.android.material.snackbar.Snackbar.make(
                            rootView,
                            "Welcome to SmartTimeline! Start capturing your moments 📝",
                            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    ).setAction("Get Started", v -> {
                        bottomNav.setSelectedItemId(R.id.nav_timeline);
                    }).setActionTextColor(getColor(R.color.accent))
                    .show();

            prefs.edit().putBoolean("first_launch", false).apply();
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";
            int itemId = item.getItemId();

            if (itemId == R.id.nav_timeline) {
                selectedFragment = new TimelineFragment();
                title = "Timeline";
                showFab(false);
            } else if (itemId == R.id.nav_analytics) {
                selectedFragment = new com.example.smarttimeline.ui.analytics.AnalyticsFragment();
                title = "Analytics";
                showFab(false);
            } else if (itemId == R.id.nav_summary) {
                selectedFragment = new com.example.smarttimeline.ui.summary.SummaryFragment();
                title = "AI Summary";
                showFab(false);
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new com.example.smarttimeline.ui.settings.SettingsFragment();
                title = "Settings";
                showFab(false);
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
            }

            return true;
        });
    }

    private void setupClickListeners() {
        // Profile icon opens settings
        iconProfile.setOnClickListener(v -> {
            bottomNav.setSelectedItemId(R.id.nav_settings);
        });

        // Quick add FAB (if needed in future)
        fabQuickAdd.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.example.smarttimeline.ui.addpost.AddPostFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadFragment(Fragment fragment, String title) {
        // Update toolbar title with animation
        toolbarTitle.animate()
                .alpha(0f)
                .setDuration(100)
                .withEndAction(() -> {
                    toolbarTitle.setText(title);
                    toolbarTitle.animate()
                            .alpha(1f)
                            .setDuration(100)
                            .start();
                })
                .start();

        // Load fragment with fade animation
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void showFab(boolean show) {
        if (show) {
            fabQuickAdd.show();
        } else {
            fabQuickAdd.hide();
        }
    }

    @Override
    public void onBackPressed() {
        // If not on timeline, go back to timeline
        if (bottomNav.getSelectedItemId() != R.id.nav_timeline) {
            bottomNav.setSelectedItemId(R.id.nav_timeline);
        } else {
            super.onBackPressed();
        }
    }
}