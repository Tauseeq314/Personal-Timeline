package com.example.smarttimeline.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.data.repository.PostRepository;

public class PostDetailViewModel extends AndroidViewModel {

    private final PostRepository repository;
    private final MutableLiveData<Integer> postIdLiveData;
    private final LiveData<Post> post;
    private final MutableLiveData<Boolean> deleteStatus;

    public PostDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new PostRepository(application);
        postIdLiveData = new MutableLiveData<>();
        deleteStatus = new MutableLiveData<>();

        post = Transformations.switchMap(postIdLiveData, postId -> {
            if (postId != null && postId != -1) {
                return repository.getPostById(postId);
            }
            return new MutableLiveData<>(null);
        });
    }

    public void loadPost(int postId) {
        postIdLiveData.setValue(postId);
    }

    public LiveData<Post> getPost() {
        return post;
    }

    public LiveData<Boolean> getDeleteStatus() {
        return deleteStatus;
    }

    public void deletePost(Post post) {
        repository.delete(post);
        deleteStatus.setValue(true);
    }
}