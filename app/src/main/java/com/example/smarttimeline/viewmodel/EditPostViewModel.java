package com.example.smarttimeline.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.data.repository.PostRepository;

public class EditPostViewModel extends AndroidViewModel {

    private final PostRepository repository;
    private final MutableLiveData<Integer> postIdLiveData;
    private final LiveData<Post> post;
    private final MutableLiveData<Boolean> updateStatus;
    private boolean hasResetStatus = false;

    public EditPostViewModel(@NonNull Application application) {
        super(application);
        repository = new PostRepository(application);
        postIdLiveData = new MutableLiveData<>();
        updateStatus = new MutableLiveData<>();
        hasResetStatus = false;

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

    public LiveData<Boolean> getUpdateStatus() {
        return updateStatus;
    }

    public void updatePost(Post post) {
        if (post == null) {
            updateStatus.setValue(false);
            return;
        }

        repository.update(post);
        // Use setValue instead of postValue for immediate UI thread update
        updateStatus.setValue(true);
    }

    public void resetUpdateStatus() {
        if (!hasResetStatus) {
            hasResetStatus = true;
            updateStatus.setValue(null);
        }
    }
}