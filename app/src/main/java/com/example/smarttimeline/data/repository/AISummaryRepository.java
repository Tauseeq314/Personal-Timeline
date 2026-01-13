package com.example.smarttimeline.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.data.model.AISummary;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AISummaryRepository {

    private final PostRepository postRepository;
    private final ExecutorService executorService;
    private final MutableLiveData<AISummary> latestSummary;

    public AISummaryRepository(Application application) {
        this.postRepository = new PostRepository(application);
        this.executorService = Executors.newSingleThreadExecutor();
        this.latestSummary = new MutableLiveData<>();
    }

    public LiveData<AISummary> getLatestSummary() {
        return latestSummary;
    }

    public void generateSummary(List<Post> posts, String period) {
        executorService.execute(() -> {
            AISummary summary = createSummaryFromPosts(posts, period);
            latestSummary.postValue(summary);
        });
    }

    private AISummary createSummaryFromPosts(List<Post> posts, String period) {
        AISummary summary = new AISummary();
        summary.setPeriod(period);
        summary.setPostCount(posts.size());

        if (posts.isEmpty()) {
            summary.setSummaryText("No posts available for this period.");
            return summary;
        }

        String dominantMood = calculateDominantMood(posts);
        summary.setDominantMood(dominantMood);

        String themes = extractKeyThemes(posts);
        summary.setKeyThemes(themes);

        String summaryText = buildSummaryText(posts, dominantMood, themes);
        summary.setSummaryText(summaryText);

        return summary;
    }

    private String calculateDominantMood(List<Post> posts) {
        // Simplified mood calculation - can be enhanced with AI
        int moodCount = 0;
        String dominantMood = "neutral";

        for (Post post : posts) {
            if (post.getMood() != null && !post.getMood().isEmpty()) {
                dominantMood = post.getMood();
                break;
            }
        }

        return dominantMood;
    }

    private String extractKeyThemes(List<Post> posts) {
        // Simplified theme extraction - placeholder for AI integration
        StringBuilder themes = new StringBuilder();
        int count = 0;

        for (Post post : posts) {
            if (post.getTags() != null && !post.getTags().isEmpty() && count < 3) {
                for (String tag : post.getTags()) {
                    if (count < 3) {
                        if (themes.length() > 0) {
                            themes.append(", ");
                        }
                        themes.append(tag);
                        count++;
                    }
                }
            }
        }

        return themes.length() > 0 ? themes.toString() : "Daily activities";
    }

    private String buildSummaryText(List<Post> posts, String dominantMood, String themes) {
        return String.format("You created %d posts during this period. " +
                        "The overall mood was %s. Key themes include: %s.",
                posts.size(), dominantMood, themes);
    }
}