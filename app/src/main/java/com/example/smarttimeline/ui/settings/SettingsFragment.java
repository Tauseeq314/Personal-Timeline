package com.example.smarttimeline.ui.settings;

import android.app.AlertDialog;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttimeline.R;
import com.example.smarttimeline.ai.AIRepository;
import com.example.smarttimeline.ai.AIUtils;
import com.example.smarttimeline.util.Constants;
import com.example.smarttimeline.viewmodel.SettingsViewModel;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private AIRepository aiRepository;

    private TextView textViewApiKeyStatus;
    private Button buttonConfigureApiKey;
    private Button buttonRemoveApiKey;
    private Switch switchNotifications;
    private Button buttonClearAllData;
    private Button buttonExportData;
    private TextView textViewVersion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeViews(view);
        setupViewModel();
        setupListeners();
        updateUI();

        return view;
    }

    private void initializeViews(View view) {
        textViewApiKeyStatus = view.findViewById(R.id.textViewApiKeyStatus);
        buttonConfigureApiKey = view.findViewById(R.id.buttonConfigureApiKey);
        buttonRemoveApiKey = view.findViewById(R.id.buttonRemoveApiKey);
        switchNotifications = view.findViewById(R.id.switchNotifications);
        buttonClearAllData = view.findViewById(R.id.buttonClearAllData);
        buttonExportData = view.findViewById(R.id.buttonExportData);
        textViewVersion = view.findViewById(R.id.textViewVersion);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        Application application = requireActivity().getApplication();
        aiRepository = new AIRepository(application);

        viewModel.getDataCleared().observe(getViewLifecycleOwner(), cleared -> {
            if (cleared != null && cleared) {
                Toast.makeText(getContext(), "All data cleared successfully", Toast.LENGTH_SHORT).show();
                viewModel.resetDataClearedState();
            }
        });
    }

    private void setupListeners() {
        buttonConfigureApiKey.setOnClickListener(v -> showApiKeyDialog());

        buttonRemoveApiKey.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Remove API Key")
                    .setMessage("Are you sure you want to remove the AI API key? AI summary features will be disabled.")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        aiRepository.clearApiKey();
                        updateUI();
                        Toast.makeText(getContext(), "API key removed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setNotificationsEnabled(isChecked);
            Toast.makeText(getContext(),
                    isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        buttonClearAllData.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Clear All Data")
                    .setMessage("This will permanently delete all your posts and data. This action cannot be undone.")
                    .setPositiveButton("Clear All", (dialog, which) -> {
                        viewModel.clearAllData();
                    })
                    .setNegativeButton("Cancel", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });

        buttonExportData.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Export feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void showApiKeyDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_api_key, null);
        EditText editTextApiKey = dialogView.findViewById(R.id.editTextApiKey);
        TextView textViewHint = dialogView.findViewById(R.id.textViewApiKeyHint);

        String currentKey = aiRepository.getApiKey();
        if (currentKey != null && !currentKey.isEmpty()) {
            editTextApiKey.setText(maskApiKey(currentKey));
        }

        textViewHint.setText("Enter your API key (starts with 'gsk-', 'xai-', or 'sk-')");

        new AlertDialog.Builder(requireContext())
                .setTitle("Configure AI API Key")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String apiKey = editTextApiKey.getText().toString().trim();

                    if (apiKey.isEmpty()) {
                        Toast.makeText(getContext(), "API key cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!AIUtils.isValidApiKey(apiKey)) {
                        Toast.makeText(getContext(), "Invalid API key format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    aiRepository.saveApiKey(apiKey);
                    updateUI();
                    Toast.makeText(getContext(), "API key saved successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUI() {
        boolean hasApiKey = aiRepository.isApiKeyConfigured();

        if (hasApiKey) {
            textViewApiKeyStatus.setText("AI API Key: Configured ✓");
            textViewApiKeyStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            buttonRemoveApiKey.setVisibility(View.VISIBLE);
            buttonConfigureApiKey.setText("Update API Key");
        } else {
            textViewApiKeyStatus.setText("AI API Key: Not Configured");
            textViewApiKeyStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            buttonRemoveApiKey.setVisibility(View.GONE);
            buttonConfigureApiKey.setText("Configure API Key");
        }

        boolean notificationsEnabled = viewModel.areNotificationsEnabled();
        switchNotifications.setChecked(notificationsEnabled);

        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            textViewVersion.setText("Version " + versionName);
        } catch (Exception e) {
            textViewVersion.setText("Version 1.0.0");
        }
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "••••••••";
        }
        return apiKey.substring(0, 7) + "••••••••" + apiKey.substring(apiKey.length() - 4);
    }
}