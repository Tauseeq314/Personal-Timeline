package com.example.smarttimeline.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.smarttimeline.data.repository.ExportImportRepository;

public class ExportImportViewModel extends AndroidViewModel {

    private final ExportImportRepository repository;

    public ExportImportViewModel(@NonNull Application application) {
        super(application);
        repository = new ExportImportRepository(application);
    }

    public LiveData<String> getOperationStatus() {
        return repository.getOperationStatus();
    }

    public LiveData<Boolean> getOperationInProgress() {
        return repository.getOperationInProgress();
    }

    public void exportData(Uri destinationUri) {
        repository.exportData(destinationUri);
    }

    public void importData(Uri sourceUri, boolean replaceExisting) {
        repository.importData(sourceUri, replaceExisting);
    }

    public String generateExportFileName() {
        return repository.generateExportFileName();
    }
}