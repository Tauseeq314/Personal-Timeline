// Replace the entire class with this:
package com.example.smarttimeline.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.smarttimeline.ai.AIRepository;
import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.data.model.AISummary;
import com.example.smarttimeline.data.repository.PostRepository;

import java.util.List;

public class SummaryViewModel extends AndroidViewModel {

    private final PostRepository postRepository;
    private final AIRepository aiRepository;
    private final MediatorLiveData<List<Post>> currentPeriodPosts;

    public SummaryViewModel(@NonNull Application application) {
        super(application);
        postRepository = new PostRepository(application);
        aiRepository = new AIRepository(application);
        currentPeriodPosts = new MediatorLiveData<>();
    }

    public LiveData<AISummary> getLatestSummary() {
        return aiRepository.getGeneratedSummary();
    }

    public LiveData<String> getSummaryStatus() {
        return aiRepository.getSummaryStatus();
    }

    public LiveData<List<Post>> getCurrentPeriodPosts() {
        return currentPeriodPosts;
    }

    public void generateSummaryForDateRange(long startDate, long endDate, String period) {
        LiveData<List<Post>> postsLiveData = postRepository.getPostsByDateRange(startDate, endDate);

        currentPeriodPosts.addSource(postsLiveData, posts -> {
            currentPeriodPosts.setValue(posts);
            if (posts != null) {
                aiRepository.generateAISummary(posts, period);
            }
            currentPeriodPosts.removeSource(postsLiveData);
        });
    }

    public void generateWeeklySummary() {
        long endDate = System.currentTimeMillis();
        long startDate = endDate - (7L * 24 * 60 * 60 * 1000);
        generateSummaryForDateRange(startDate, endDate, "Weekly");
    }

    public void generateMonthlySummary() {
        long endDate = System.currentTimeMillis();
        long startDate = endDate - (30L * 24 * 60 * 60 * 1000);
        generateSummaryForDateRange(startDate, endDate, "Monthly");
    }

    public void generateYearlySummary() {
        long endDate = System.currentTimeMillis();
        long startDate = endDate - (365L * 24 * 60 * 60 * 1000);
        generateSummaryForDateRange(startDate, endDate, "Yearly");
    }

    public LiveData<List<Post>> getAllPosts() {
        return postRepository.getAllPosts();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        aiRepository.shutdown();
    }
}