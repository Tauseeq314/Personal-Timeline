package com.example.smarttimeline.ai;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.smarttimeline.data.database.AppDatabase;
import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.util.DateUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AISummaryWorker extends Worker {

    private static final String TAG = "AISummaryWorker";

    public AISummaryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting weekly summary generation");

            Context context = getApplicationContext();
            AppDatabase database = AppDatabase.getInstance(context);
            AIRepository aiRepository = new AIRepository(context);

            if (!aiRepository.isApiKeyConfigured()) {
                Log.w(TAG, "API key not configured, skipping summary generation");
                return Result.success();
            }

            long endDate = System.currentTimeMillis();
            long startDate = DateUtils.getWeeksAgo(1);

            // Execute database query synchronously using Executor
            ExecutorService executor = Executors.newSingleThreadExecutor();
            List<Post> posts = executor.submit(() ->
                    getPostsDirectly(database, startDate, endDate)
            ).get();

            executor.shutdown();

            if (posts == null || posts.isEmpty()) {
                Log.d(TAG, "No posts found for weekly summary");
                return Result.success();
            }

            Log.d(TAG, "Found " + posts.size() + " posts for weekly summary");

            // Generate summary synchronously using callback
            final Object lock = new Object();
            final boolean[] completed = {false};
            final boolean[] success = {false};

            aiRepository.generateAISummarySync(posts, "Weekly", new AIRepository.SummarySyncCallback() {
                @Override
                public void onSuccess() {
                    synchronized (lock) {
                        Log.d(TAG, "Weekly summary generated successfully");
                        success[0] = true;
                        completed[0] = true;
                        lock.notify();
                    }
                }

                @Override
                public void onError(String error) {
                    synchronized (lock) {
                        Log.e(TAG, "Failed to generate weekly summary: " + error);
                        completed[0] = true;
                        lock.notify();
                    }
                }
            });

            // Wait for summary generation with timeout
            synchronized (lock) {
                if (!completed[0]) {
                    lock.wait(90000); // 90 second timeout
                }
            }

            if (success[0]) {
                Log.d(TAG, "Weekly summary work completed successfully");
                return Result.success();
            } else {
                Log.w(TAG, "Weekly summary generation failed or timed out");
                return Result.retry();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in AISummaryWorker", e);
            return Result.failure();
        }
    }

    private List<Post> getPostsDirectly(AppDatabase database, long startDate, long endDate) {
        // Direct synchronous query - needs a synchronous DAO method
        return database.postDao().getPostsByDateRangeSync(startDate, endDate);
    }
}