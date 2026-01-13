package com.example.smarttimeline.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.smarttimeline.data.repository.PostRepository;
import com.example.smarttimeline.util.Constants;

public class SettingsViewModel extends AndroidViewModel {

    private final PostRepository repository;
    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<Boolean> dataCleared;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        repository = new PostRepository(application);
        sharedPreferences = application.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        dataCleared = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getDataCleared() {
        return dataCleared;
    }

    public void clearAllData() {
        repository.deleteAll();
        dataCleared.setValue(true);
    }

    public void resetDataClearedState() {
        dataCleared.setValue(false);
    }

    public boolean areNotificationsEnabled() {
        return sharedPreferences.getBoolean(Constants.PREFS_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        sharedPreferences.edit()
                .putBoolean(Constants.PREFS_NOTIFICATIONS_ENABLED, enabled)
                .apply();
    }

    public String getThemePreference() {
        return sharedPreferences.getString(Constants.PREFS_THEME, "system");
    }

    public void setThemePreference(String theme) {
        sharedPreferences.edit()
                .putString(Constants.PREFS_THEME, theme)
                .apply();
    }
}