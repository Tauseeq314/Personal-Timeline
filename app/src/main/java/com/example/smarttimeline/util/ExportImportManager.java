package com.example.smarttimeline.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.smarttimeline.data.entity.Post;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportImportManager {

    private static final String TAG = "ExportImportManager";
    private static final String EXPORT_FILE_PREFIX = "smarttimeline_backup_";
    private static final String EXPORT_FILE_EXTENSION = ".json";

    private final Context context;
    private final Gson gson;

    public ExportImportManager(Context context) {
        this.context = context;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public interface ExportCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface ImportCallback {
        void onSuccess(List<Post> posts, String message);
        void onError(String error);
    }

    public void exportToJson(Uri destinationUri, List<Post> posts, ExportCallback callback) {
        if (posts == null || posts.isEmpty()) {
            callback.onError("No posts to export");
            return;
        }

        try {
            ExportData exportData = new ExportData();
            exportData.exportDate = System.currentTimeMillis();
            exportData.version = "1.0";
            exportData.postCount = posts.size();
            exportData.posts = posts;

            String jsonData = gson.toJson(exportData);

            OutputStream outputStream = context.getContentResolver().openOutputStream(destinationUri);
            if (outputStream == null) {
                callback.onError("Failed to open output stream");
                return;
            }

            outputStream.write(jsonData.getBytes());
            outputStream.flush();
            outputStream.close();

            callback.onSuccess("Successfully exported " + posts.size() + " posts");
            Log.d(TAG, "Export successful: " + posts.size() + " posts");

        } catch (IOException e) {
            Log.e(TAG, "Export failed", e);
            callback.onError("Export failed: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during export", e);
            callback.onError("Unexpected error: " + e.getMessage());
        }
    }

    public void importFromJson(Uri sourceUri, ImportCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) {
                callback.onError("Failed to open input stream");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            reader.close();
            inputStream.close();

            String jsonData = jsonBuilder.toString();

            if (jsonData.trim().isEmpty()) {
                callback.onError("File is empty");
                return;
            }

            ExportData exportData = gson.fromJson(jsonData, ExportData.class);

            if (exportData == null || exportData.posts == null || exportData.posts.isEmpty()) {
                callback.onError("No valid posts found in file");
                return;
            }

            List<Post> posts = exportData.posts;

            for (Post post : posts) {
                post.setId(0);
            }

            callback.onSuccess(posts, "Successfully imported " + posts.size() + " posts");
            Log.d(TAG, "Import successful: " + posts.size() + " posts");

        } catch (IOException e) {
            Log.e(TAG, "Import failed", e);
            callback.onError("Import failed: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during import", e);
            callback.onError("Invalid file format: " + e.getMessage());
        }
    }

    public String generateExportFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        return EXPORT_FILE_PREFIX + timestamp + EXPORT_FILE_EXTENSION;
    }

    public boolean isValidExportFile(String fileName) {
        return fileName != null && fileName.endsWith(EXPORT_FILE_EXTENSION);
    }

    public static class ExportData {
        public String version;
        public long exportDate;
        public int postCount;
        public List<Post> posts;
    }
}