package com.example.smarttimeline.notification;

import android.content.Context;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";
    private static final String WORK_NAME = "daily_reminder_work";

    public static void scheduleDailyReminder(Context context, int hour, int minute) {
        long initialDelay = calculateInitialDelay(hour, minute);

        PeriodicWorkRequest dailyWorkRequest = new PeriodicWorkRequest.Builder(
                DailyReminderWorker.class,
                24, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
        )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyWorkRequest
        );

        Log.d(TAG, "Daily reminder scheduled for " + hour + ":" + minute);
    }

    public static void scheduleDefaultDailyReminder(Context context) {
        scheduleDailyReminder(context, 20, 0); // 8:00 PM default
    }

    public static void cancelDailyReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
        Log.d(TAG, "Daily reminder cancelled");
    }

    private static long calculateInitialDelay(int hour, int minute) {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();

        dueDate.set(Calendar.HOUR_OF_DAY, hour);
        dueDate.set(Calendar.MINUTE, minute);
        dueDate.set(Calendar.SECOND, 0);

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
    }
}