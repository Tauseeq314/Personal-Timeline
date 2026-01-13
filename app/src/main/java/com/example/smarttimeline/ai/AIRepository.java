package com.example.smarttimeline.ai;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.data.model.AISummary;

import java.util.List;

public class AIRepository {

    private static final String PREFS_NAME = "ai_prefs";
    private static final String KEY_API_KEY = "api_key";

    private final AIService aiService;
    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<String> summaryStatus;
    private final MutableLiveData<AISummary> generatedSummary;

    public AIRepository(Context context) {
        this.aiService = new AIService();
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.summaryStatus = new MutableLiveData<>();
        this.generatedSummary = new MutableLiveData<>();

        loadApiKey();
    }

    private void loadApiKey() {
        String apiKey = sharedPreferences.getString(KEY_API_KEY, null);
        if (apiKey != null) {
            aiService.setApiKey(apiKey);
        }
    }

    public void saveApiKey(String apiKey) {
        sharedPreferences.edit()
                .putString(KEY_API_KEY, apiKey)
                .apply();
        aiService.setApiKey(apiKey);
    }

    public String getApiKey() {
        return sharedPreferences.getString(KEY_API_KEY, null);
    }

    public boolean isApiKeyConfigured() {
        String apiKey = getApiKey();
        return apiKey != null && !apiKey.isEmpty();
    }

    public void clearApiKey() {
        sharedPreferences.edit()
                .remove(KEY_API_KEY)
                .apply();
        aiService.setApiKey(null);
    }

    public LiveData<String> getSummaryStatus() {
        return summaryStatus;
    }

    public LiveData<AISummary> getGeneratedSummary() {
        return generatedSummary;
    }

    public void generateAISummary(List<Post> posts, String period) {
        if (!isApiKeyConfigured()) {
            summaryStatus.postValue("API key not configured. Please set it in Settings.");
            return;
        }

        if (posts == null || posts.isEmpty()) {
            summaryStatus.postValue("No posts available to summarize");
            return;
        }

        summaryStatus.postValue("Generating summary...");

        aiService.generateSummary(posts, period, new AIService.SummaryCallback() {
            @Override
            public void onSuccess(String summaryText) {
                AISummary summary = new AISummary(summaryText, period);
                summary.setPostCount(posts.size());

                String dominantMood = calculateDominantMood(posts);
                summary.setDominantMood(dominantMood);

                String keyThemes = extractKeyThemes(posts);
                summary.setKeyThemes(keyThemes);

                generatedSummary.postValue(summary);
                summaryStatus.postValue("Summary generated successfully");
            }

            @Override
            public void onError(String error) {
                summaryStatus.postValue("Error: " + error);
            }
        });
    }

    private String calculateDominantMood(List<Post> posts) {
        int[] moodCounts = new int[10];
        String[] moods = {"Happy", "Sad", "Excited", "Calm", "Anxious", "Grateful", "Frustrated", "Motivated", "Neutral", "Other"};

        for (Post post : posts) {
            String mood = post.getMood();
            if (mood != null) {
                for (int i = 0; i < moods.length; i++) {
                    if (mood.equalsIgnoreCase(moods[i])) {
                        moodCounts[i]++;
                        break;
                    }
                }
            }
        }

        int maxIndex = 0;
        for (int i = 1; i < moodCounts.length; i++) {
            if (moodCounts[i] > moodCounts[maxIndex]) {
                maxIndex = i;
            }
        }

        return moodCounts[maxIndex] > 0 ? moods[maxIndex] : "Neutral";
    }

    private String extractKeyThemes(List<Post> posts) {
        StringBuilder themes = new StringBuilder();
        int themeCount = 0;

        for (Post post : posts) {
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                for (String tag : post.getTags()) {
                    if (themeCount < 5) {
                        if (themes.length() > 0) {
                            themes.append(", ");
                        }
                        themes.append(tag);
                        themeCount++;
                    } else {
                        break;
                    }
                }
            }
            if (themeCount >= 5) {
                break;
            }
        }

        return themes.length() > 0 ? themes.toString() : "Daily activities";
    }

    public void shutdown() {
        aiService.shutdown();
    }

    public interface SummarySyncCallback {
        void onSuccess();
        void onError(String error);
    }

    public void generateAISummarySync(List<Post> posts, String period, SummarySyncCallback callback) {
        if (!isApiKeyConfigured()) {
            callback.onError("API key not configured. Please set it in Settings.");
            return;
        }

        if (posts == null || posts.isEmpty()) {
            callback.onError("No posts available to summarize");
            return;
        }

        aiService.generateSummary(posts, period, new AIService.SummaryCallback() {
            @Override
            public void onSuccess(String summaryText) {
                AISummary summary = new AISummary(summaryText, period);
                summary.setPostCount(posts.size());

                String dominantMood = calculateDominantMood(posts);
                summary.setDominantMood(dominantMood);

                String keyThemes = extractKeyThemes(posts);
                summary.setKeyThemes(keyThemes);

                generatedSummary.postValue(summary);
                summaryStatus.postValue("Summary generated successfully");
                callback.onSuccess();
            }

            @Override
            public void onError(String error) {
                summaryStatus.postValue("Error: " + error);
                callback.onError(error);
            }
        });
    }
}