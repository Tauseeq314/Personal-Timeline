package com.example.smarttimeline.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.smarttimeline.data.dao.PostDao;
import com.example.smarttimeline.data.database.AppDatabase;
import com.example.smarttimeline.data.entity.Post;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostRepository {

    private final PostDao postDao;
    private final LiveData<List<Post>> allPosts;
    private final ExecutorService executorService;

    public PostRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        postDao = database.postDao();
        allPosts = postDao.getAllPosts();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Post post) {
        executorService.execute(() -> postDao.insert(post));
    }

    public void update(Post post) {
        executorService.execute(() -> postDao.update(post));
    }

    public void delete(Post post) {
        executorService.execute(() -> postDao.delete(post));
    }

    public void deleteAll() {
        executorService.execute(() -> postDao.deleteAll());
    }

    public LiveData<List<Post>> getAllPosts() {
        return allPosts;
    }

    public LiveData<Post> getPostById(int postId) {
        return postDao.getPostById(postId);
    }

    public LiveData<List<Post>> getPostsByDateRange(long startDate, long endDate) {
        return postDao.getPostsByDateRange(startDate, endDate);
    }

    public LiveData<List<Post>> getPostsByMood(String mood) {
        return postDao.getPostsByMood(mood);
    }

    public LiveData<List<Post>> getPostsFromDate(long startDate) {
        return postDao.getPostsFromDate(startDate);
    }

    public LiveData<List<String>> getAllMoods() {
        return postDao.getAllMoods();
    }

    public LiveData<Integer> getPostCount() {
        return postDao.getPostCount();
    }

    public LiveData<List<Post>> getPostsByDateRangeAndMood(long startDate, long endDate, String mood) {
        return postDao.getPostsByDateRangeAndMood(startDate, endDate, mood);
    }
}