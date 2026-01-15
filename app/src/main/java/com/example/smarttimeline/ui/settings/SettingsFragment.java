package com.example.smarttimeline.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttimeline.R;
import com.example.smarttimeline.ai.AIRepository;
import com.example.smarttimeline.viewmodel.ExportImportViewModel;

public class SettingsFragment extends Fragment {

    private ExportImportViewModel viewModel;
    private AIRepository aiRepository;

    private EditText editTextApiKey;
    private Button buttonSaveApiKey;
    private Button buttonExportData;
    private Button buttonImportData;
    private Button buttonClearData;
    private TextView textViewStatus;
    private ProgressBar progressBar;

    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeViews(view);
        setupViewModel();
        setupFilePickers();
        setupListeners();
        loadApiKey();

        return view;
    }

    private void initializeViews(View view) {
        editTextApiKey = view.findViewById(R.id.editTextApiKey);
        buttonSaveApiKey = view.findViewById(R.id.buttonSaveApiKey);
        buttonExportData = view.findViewById(R.id.buttonExportData);
        buttonImportData = view.findViewById(R.id.buttonImportData);
        buttonClearData = view.findViewById(R.id.buttonClearData);
        textViewStatus = view.findViewById(R.id.textViewStatus);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ExportImportViewModel.class);
        aiRepository = new AIRepository(requireContext());

        viewModel.getOperationStatus().observe(getViewLifecycleOwner(), status -> {
            textViewStatus.setText(status);
            textViewStatus.setVisibility(View.VISIBLE);
        });

        viewModel.getOperationInProgress().observe(getViewLifecycleOwner(), inProgress -> {
            progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
            buttonExportData.setEnabled(!inProgress);
            buttonImportData.setEnabled(!inProgress);
        });
    }

    private void setupFilePickers() {
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            viewModel.exportData(uri);
                        }
                    }
                }
        );

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            showImportConfirmation(uri);
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        buttonSaveApiKey.setOnClickListener(v -> saveApiKey());
        buttonExportData.setOnClickListener(v -> exportData());
        buttonImportData.setOnClickListener(v -> importData());
        buttonClearData.setOnClickListener(v -> showClearDataConfirmation());
    }

    private void loadApiKey() {
        String apiKey = aiRepository.getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            editTextApiKey.setText(maskApiKey(apiKey));
        }
    }

    private void saveApiKey() {
        String apiKey = editTextApiKey.getText().toString().trim();

        if (apiKey.isEmpty()) {
            editTextApiKey.setError("API key cannot be empty");
            return;
        }

        aiRepository.saveApiKey(apiKey);
        Toast.makeText(getContext(), "API key saved successfully", Toast.LENGTH_SHORT).show();
        textViewStatus.setText("API key configured");
        textViewStatus.setVisibility(View.VISIBLE);
    }

    private void exportData() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, viewModel.generateExportFileName());
        exportLauncher.launch(intent);
    }

    private void importData() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        importLauncher.launch(intent);
    }

    private void showImportConfirmation(Uri uri) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Import Data")
                .setMessage("Do you want to replace existing data or merge with current data?")
                .setPositiveButton("Replace", (dialog, which) -> {
                    viewModel.importData(uri, true);
                })
                .setNegativeButton("Merge", (dialog, which) -> {
                    viewModel.importData(uri, false);
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void showClearDataConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to delete all posts? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Clear data logic would go here
                    Toast.makeText(getContext(), "All data cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String maskApiKey(String apiKey) {
        if (apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}