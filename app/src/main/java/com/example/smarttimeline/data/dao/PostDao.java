package com.example.smarttimeline.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smarttimeline.data.entity.Post;

import java.util.List;

@Dao
public interface PostDao {

    @Insert
    void insert(Post post);

    @Update
    void update(Post post);

    @Delete
    void delete(Post post);

    @Query("DELETE FROM posts")
    void deleteAll();

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    LiveData<List<Post>> getAllPosts();

    @Query("SELECT * FROM posts WHERE id = :postId")
    LiveData<Post> getPostById(int postId);

    @Query("SELECT * FROM posts WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    LiveData<List<Post>> getPostsByDateRange(long startDate, long endDate);

    @Query("SELECT * FROM posts WHERE mood = :mood ORDER BY timestamp DESC")
    LiveData<List<Post>> getPostsByMood(String mood);

    @Query("SELECT * FROM posts WHERE timestamp >= :startDate ORDER BY timestamp DESC")
    LiveData<List<Post>> getPostsFromDate(long startDate);

    @Query("SELECT DISTINCT mood FROM posts WHERE mood IS NOT NULL")
    LiveData<List<String>> getAllMoods();

    @Query("SELECT COUNT(*) FROM posts")
    LiveData<Integer> getPostCount();

    @Query("SELECT * FROM posts WHERE timestamp BETWEEN :startDate AND :endDate AND mood = :mood ORDER BY timestamp DESC")
    LiveData<List<Post>> getPostsByDateRangeAndMood(long startDate, long endDate, String mood);

    // Add this method to your Dao for synchronous fetching

    @Query("SELECT * FROM posts WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    List<Post> getPostsByDateRangeSync(long startDate, long endDate);

}