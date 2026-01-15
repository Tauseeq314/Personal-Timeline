package com.example.smarttimeline.data.repository;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.util.ExportImportManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportImportRepository {

    private final PostRepository postRepository;
    private final ExportImportManager exportImportManager;
    private final ExecutorService executorService;
    private final MutableLiveData<String> operationStatus;
    private final MutableLiveData<Boolean> operationInProgress;

    public ExportImportRepository(Application application) {
        this.postRepository = new PostRepository(application);
        this.exportImportManager = new ExportImportManager(application);
        this.executorService = Executors.newSingleThreadExecutor();
        this.operationStatus = new MutableLiveData<>();
        this.operationInProgress = new MutableLiveData<>(false);
    }

    public LiveData<String> getOperationStatus() {
        return operationStatus;
    }

    public LiveData<Boolean> getOperationInProgress() {
        return operationInProgress;
    }

    public void exportData(Uri destinationUri) {
        operationInProgress.postValue(true);
        operationStatus.postValue("Preparing export...");

        LiveData<List<Post>> allPostsLiveData = postRepository.getAllPosts();

        allPostsLiveData.observeForever(new androidx.lifecycle.Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                allPostsLiveData.removeObserver(this);

                if (posts == null || posts.isEmpty()) {
                    operationStatus.postValue("No posts to export");
                    operationInProgress.postValue(false);
                    return;
                }

                executorService.execute(() -> {
                    exportImportManager.exportToJson(destinationUri, posts,
                            new ExportImportManager.ExportCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    operationStatus.postValue(message);
                                    operationInProgress.postValue(false);
                                }

                                @Override
                                public void onError(String error) {
                                    operationStatus.postValue("Export failed: " + error);
                                    operationInProgress.postValue(false);
                                }
                            });
                });
            }
        });
    }

    public void importData(Uri sourceUri, boolean replaceExisting) {
        operationInProgress.postValue(true);
        operationStatus.postValue("Reading import file...");

        executorService.execute(() -> {
            exportImportManager.importFromJson(sourceUri,
                    new ExportImportManager.ImportCallback() {
                        @Override
                        public void onSuccess(List<Post> posts, String message) {
                            if (replaceExisting) {
                                postRepository.deleteAll();
                                operationStatus.postValue("Clearing existing data...");
                            }

                            for (Post post : posts) {
                                postRepository.insert(post);
                            }

                            operationStatus.postValue(message);
                            operationInProgress.postValue(false);
                        }

                        @Override
                        public void onError(String error) {
                            operationStatus.postValue("Import failed: " + error);
                            operationInProgress.postValue(false);
                        }
                    });
        });
    }

    public String generateExportFileName() {
        return exportImportManager.generateExportFileName();
    }
}