package com.example.smarttimeline.util;

public class Constants {

    // Database
    public static final String DATABASE_NAME = "smarttimeline_database";
    public static final int DATABASE_VERSION = 1;

    // SharedPreferences
    public static final String PREFS_NAME = "smarttimeline_prefs";
    public static final String PREFS_AI_API_KEY = "ai_api_key";
    public static final String PREFS_THEME = "theme_preference";
    public static final String PREFS_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String PREFS_FIRST_LAUNCH = "first_launch";

    // Period Types
    public static final String PERIOD_DAILY = "Daily";
    public static final String PERIOD_WEEKLY = "Weekly";
    public static final String PERIOD_MONTHLY = "Monthly";
    public static final String PERIOD_YEARLY = "Yearly";
    public static final String PERIOD_CUSTOM = "Custom";

    // Mood Options
    public static final String MOOD_HAPPY = "Happy";
    public static final String MOOD_SAD = "Sad";
    public static final String MOOD_EXCITED = "Excited";
    public static final String MOOD_CALM = "Calm";
    public static final String MOOD_ANXIOUS = "Anxious";
    public static final String MOOD_GRATEFUL = "Grateful";
    public static final String MOOD_FRUSTRATED = "Frustrated";
    public static final String MOOD_MOTIVATED = "Motivated";
    public static final String MOOD_NEUTRAL = "Neutral";

    public static final String[] MOOD_OPTIONS = {
            MOOD_HAPPY, MOOD_SAD, MOOD_EXCITED, MOOD_CALM,
            MOOD_ANXIOUS, MOOD_GRATEFUL, MOOD_FRUSTRATED,
            MOOD_MOTIVATED, MOOD_NEUTRAL
    };

    // Intent Extras
    public static final String EXTRA_POST_ID = "extra_post_id";
    public static final String EXTRA_PERIOD_TYPE = "extra_period_type";
    public static final String EXTRA_START_DATE = "extra_start_date";
    public static final String EXTRA_END_DATE = "extra_end_date";
    public static final String EXTRA_MOOD_FILTER = "extra_mood_filter";

    // Request Codes
    public static final int REQUEST_IMAGE_PICK = 1001;
    public static final int REQUEST_LOCATION_PERMISSION = 1002;
    public static final int REQUEST_STORAGE_PERMISSION = 1003;
    public static final int REQUEST_CAMERA_PERMISSION = 1004;

    // Limits
    public static final int MAX_POST_LENGTH = 5000;
    public static final int MAX_TAG_LENGTH = 50;
    public static final int MAX_TAGS_PER_POST = 10;
    public static final int MAX_LOCATION_LENGTH = 200;
    public static final int MAX_IMAGE_SIZE_MB = 10;

    // Time Constants (milliseconds)
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
    public static final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;
    public static final long MILLIS_PER_MONTH = 30 * MILLIS_PER_DAY;
    public static final long MILLIS_PER_YEAR = 365 * MILLIS_PER_DAY;

    // API
    public static final int API_TIMEOUT_SECONDS = 30;
    public static final int API_MAX_RETRIES = 3;
    public static final String API_CONTENT_TYPE = "application/json";

    // Notification
    public static final String NOTIFICATION_CHANNEL_ID = "smarttimeline_channel";
    public static final String NOTIFICATION_CHANNEL_NAME = "SmartTimeline Notifications";
    public static final int NOTIFICATION_ID_DAILY_REMINDER = 2001;

    // Analytics Chart Colors
    public static final int[] CHART_COLORS = {
            0xFF2196F3, // Blue
            0xFF4CAF50, // Green
            0xFFF44336, // Red
            0xFFFF9800, // Orange
            0xFF9C27B0, // Purple
            0xFF00BCD4, // Cyan
            0xFFFFEB3B, // Yellow
            0xFFE91E63, // Pink
            0xFF607D8B  // Blue Grey
    };

    // Error Messages
    public static final String ERROR_EMPTY_CONTENT = "Post content cannot be empty";
    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_API_KEY_MISSING = "API key not configured. Please set it in Settings.";
    public static final String ERROR_PERMISSION_DENIED = "Permission denied";
    public static final String ERROR_IMAGE_TOO_LARGE = "Image size exceeds maximum limit";
    public static final String ERROR_GENERIC = "An error occurred. Please try again.";

    // Success Messages
    public static final String SUCCESS_POST_CREATED = "Post created successfully";
    public static final String SUCCESS_POST_UPDATED = "Post updated successfully";
    public static final String SUCCESS_POST_DELETED = "Post deleted successfully";
    public static final String SUCCESS_SUMMARY_GENERATED = "Summary generated successfully";
    public static final String SUCCESS_SETTINGS_SAVED = "Settings saved successfully";

    // Date Formats
    public static final String FORMAT_DATE = "MMM dd, yyyy";
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_DATETIME = "MMM dd, yyyy HH:mm";
    public static final String FORMAT_FULL_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    // Fragment Tags
    public static final String FRAGMENT_TIMELINE = "fragment_timeline";
    public static final String FRAGMENT_ADD_POST = "fragment_add_post";
    public static final String FRAGMENT_ANALYTICS = "fragment_analytics";
    public static final String FRAGMENT_SUMMARY = "fragment_summary";
    public static final String FRAGMENT_SETTINGS = "fragment_settings";

    private Constants() {
        // Prevent instantiation
    }
}