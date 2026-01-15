package com.example.smarttimeline.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.smarttimeline.util.Constants;

public class DailyReminderWorker extends Worker {

    private static final String TAG = "DailyReminderWorker";

    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();

            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
            boolean notificationsEnabled = prefs.getBoolean(Constants.PREFS_NOTIFICATIONS_ENABLED, true);

            if (!notificationsEnabled) {
                Log.d(TAG, "Notifications disabled by user");
                return Result.success();
            }

            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.sendDailyReminderNotification();

            Log.d(TAG, "Daily reminder notification sent successfully");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error sending daily reminder", e);
            return Result.failure();
        }
    }
}