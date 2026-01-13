package com.example.smarttimeline.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.data.repository.PostRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsViewModel extends AndroidViewModel {

    private final PostRepository repository;
    private final LiveData<List<Post>> allPosts;
    private final MediatorLiveData<Map<String, Integer>> moodDistribution;
    private final MediatorLiveData<Map<String, Integer>> postsPerDay;
    private final MediatorLiveData<Map<String, Integer>> tagsDistribution;

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        repository = new PostRepository(application);
        allPosts = repository.getAllPosts();

        moodDistribution = new MediatorLiveData<>();
        postsPerDay = new MediatorLiveData<>();
        tagsDistribution = new MediatorLiveData<>();

        setupMoodDistribution();
        setupPostsPerDay();
        setupTagsDistribution();
    }

    private void setupMoodDistribution() {
        moodDistribution.addSource(allPosts, posts -> {
            Map<String, Integer> distribution = new HashMap<>();

            if (posts != null) {
                for (Post post : posts) {
                    String mood = post.getMood();
                    if (mood != null && !mood.isEmpty()) {
                        distribution.put(mood, distribution.getOrDefault(mood, 0) + 1);
                    }
                }
            }

            moodDistribution.setValue(distribution);
        });
    }

    private void setupPostsPerDay() {
        postsPerDay.addSource(allPosts, posts -> {
            Map<String, Integer> dailyPosts = new HashMap<>();

            if (posts != null) {
                Calendar calendar = Calendar.getInstance();

                for (Post post : posts) {
                    calendar.setTimeInMillis(post.getTimestamp());
                    String dateKey = String.format("%04d-%02d-%02d",
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH));

                    dailyPosts.put(dateKey, dailyPosts.getOrDefault(dateKey, 0) + 1);
                }
            }

            postsPerDay.setValue(dailyPosts);
        });
    }

    private void setupTagsDistribution() {
        tagsDistribution.addSource(allPosts, posts -> {
            Map<String, Integer> tagCounts = new HashMap<>();

            if (posts != null) {
                for (Post post : posts) {
                    List<String> tags = post.getTags();
                    if (tags != null) {
                        for (String tag : tags) {
                            if (tag != null && !tag.isEmpty()) {
                                tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                            }
                        }
                    }
                }
            }

            tagsDistribution.setValue(tagCounts);
        });
    }

    public LiveData<List<Post>> getAllPosts() {
        return allPosts;
    }

    public LiveData<Map<String, Integer>> getMoodDistribution() {
        return moodDistribution;
    }

    public LiveData<Map<String, Integer>> getPostsPerDay() {
        return postsPerDay;
    }

    public LiveData<Map<String, Integer>> getTagsDistribution() {
        return tagsDistribution;
    }

    public LiveData<List<Post>> getPostsByDateRange(long startDate, long endDate) {
        return repository.getPostsByDateRange(startDate, endDate);
    }

    public LiveData<Integer> getPostCount() {
        return repository.getPostCount();
    }
}