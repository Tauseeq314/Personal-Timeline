package com.example.smarttimeline.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.data.repository.PostRepository;

public class AddPostViewModel extends AndroidViewModel {

    private final PostRepository repository;
    private final MutableLiveData<Boolean> postInserted;

    public AddPostViewModel(@NonNull Application application) {
        super(application);
        repository = new PostRepository(application);
        postInserted = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getPostInserted() {
        return postInserted;
    }

    public void insertPost(Post post) {
        if (post == null) {
            postInserted.setValue(false);
            return;
        }

        if (post.getTimestamp() == 0) {
            post.setTimestamp(System.currentTimeMillis());
        }

        repository.insert(post);
        postInserted.setValue(true);
    }

    public void resetInsertionState() {
        postInserted.setValue(false);
    }
}