package com.example.smarttimeline.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.data.repository.PostRepository;

import java.util.List;

public class TimelineViewModel extends AndroidViewModel {

    private final PostRepository repository;
    private final LiveData<List<Post>> allPosts;
    private final LiveData<Integer> postCount;

    public TimelineViewModel(@NonNull Application application) {
        super(application);
        repository = new PostRepository(application);
        allPosts = repository.getAllPosts();
        postCount = repository.getPostCount();
    }

    public LiveData<List<Post>> getAllPosts() {
        return allPosts;
    }

    public LiveData<Integer> getPostCount() {
        return postCount;
    }

    public LiveData<Post> getPostById(int postId) {
        return repository.getPostById(postId);
    }

    public LiveData<List<Post>> getPostsByDateRange(long startDate, long endDate) {
        return repository.getPostsByDateRange(startDate, endDate);
    }

    public LiveData<List<Post>> getPostsByMood(String mood) {
        return repository.getPostsByMood(mood);
    }

    public LiveData<List<Post>> getPostsFromDate(long startDate) {
        return repository.getPostsFromDate(startDate);
    }

    public LiveData<List<String>> getAllMoods() {
        return repository.getAllMoods();
    }

    public LiveData<List<Post>> getPostsByDateRangeAndMood(long startDate, long endDate, String mood) {
        return repository.getPostsByDateRangeAndMood(startDate, endDate, mood);
    }

    public void insert(Post post) {
        repository.insert(post);
    }

    public void update(Post post) {
        repository.update(post);
    }

    public void delete(Post post) {
        repository.delete(post);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}