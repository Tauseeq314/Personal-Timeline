package com.example.smarttimeline.ui.summary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttimeline.R;
import com.example.smarttimeline.data.model.AISummary;
import com.example.smarttimeline.viewmodel.SummaryViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SummaryFragment extends Fragment {

    private SummaryViewModel viewModel;

    private TextView textViewSummaryText;
    private TextView textViewPeriod;
    private TextView textViewPostCount;
    private TextView textViewDominantMood;
    private TextView textViewKeyThemes;
    private TextView textViewGeneratedTime;
    private TextView textViewNoSummary;
    private Button buttonWeeklySummary;
    private Button buttonMonthlySummary;
    private Button buttonYearlySummary;
    private ProgressBar progressBar;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        initializeViews(view);
        setupViewModel();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        textViewSummaryText = view.findViewById(R.id.textViewSummaryText);
        textViewPeriod = view.findViewById(R.id.textViewPeriod);
        textViewPostCount = view.findViewById(R.id.textViewPostCount);
        textViewDominantMood = view.findViewById(R.id.textViewDominantMood);
        textViewKeyThemes = view.findViewById(R.id.textViewKeyThemes);
        textViewGeneratedTime = view.findViewById(R.id.textViewGeneratedTime);
        textViewNoSummary = view.findViewById(R.id.textViewNoSummary);
        buttonWeeklySummary = view.findViewById(R.id.buttonWeeklySummary);
        buttonMonthlySummary = view.findViewById(R.id.buttonMonthlySummary);
        buttonYearlySummary = view.findViewById(R.id.buttonYearlySummary);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SummaryViewModel.class);

        viewModel.getLatestSummary().observe(getViewLifecycleOwner(), this::displaySummary);

        viewModel.getSummaryStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                if (status.startsWith("Error:") || status.contains("not configured") || status.contains("No posts")) {
                    progressBar.setVisibility(View.GONE);
                    textViewNoSummary.setVisibility(View.VISIBLE);
                    textViewNoSummary.setText(status);
                    hideSummaryViews();
                } else if (status.equals("Generating summary...")) {
                    progressBar.setVisibility(View.VISIBLE);
                    textViewNoSummary.setVisibility(View.GONE);
                } else if (status.equals("Summary generated successfully")) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getCurrentPeriodPosts().observe(getViewLifecycleOwner(), posts -> {
            progressBar.setVisibility(View.GONE);
        });
    }

    private void setupListeners() {
        buttonWeeklySummary.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            viewModel.generateWeeklySummary();
        });

        buttonMonthlySummary.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            viewModel.generateMonthlySummary();
        });

        buttonYearlySummary.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            viewModel.generateYearlySummary();
        });
    }

    private void displaySummary(AISummary summary) {
        if (summary == null) {
            textViewNoSummary.setVisibility(View.VISIBLE);
            hideSummaryViews();
            return;
        }

        textViewNoSummary.setVisibility(View.GONE);
        showSummaryViews();

        textViewSummaryText.setText(summary.getSummaryText());
        textViewPeriod.setText("Period: " + summary.getPeriod());
        textViewPostCount.setText("Posts: " + summary.getPostCount());

        if (summary.getDominantMood() != null && !summary.getDominantMood().isEmpty()) {
            textViewDominantMood.setVisibility(View.VISIBLE);
            textViewDominantMood.setText("Mood: " + summary.getDominantMood());
        } else {
            textViewDominantMood.setVisibility(View.GONE);
        }

        if (summary.getKeyThemes() != null && !summary.getKeyThemes().isEmpty()) {
            textViewKeyThemes.setVisibility(View.VISIBLE);
            textViewKeyThemes.setText("Themes: " + summary.getKeyThemes());
        } else {
            textViewKeyThemes.setVisibility(View.GONE);
        }

        textViewGeneratedTime.setText("Generated: " + dateFormat.format(new Date(summary.getGeneratedTimestamp())));
    }

    private void showSummaryViews() {
        textViewSummaryText.setVisibility(View.VISIBLE);
        textViewPeriod.setVisibility(View.VISIBLE);
        textViewPostCount.setVisibility(View.VISIBLE);
        textViewGeneratedTime.setVisibility(View.VISIBLE);
    }

    private void hideSummaryViews() {
        textViewSummaryText.setVisibility(View.GONE);
        textViewPeriod.setVisibility(View.GONE);
        textViewPostCount.setVisibility(View.GONE);
        textViewDominantMood.setVisibility(View.GONE);
        textViewKeyThemes.setVisibility(View.GONE);
        textViewGeneratedTime.setVisibility(View.GONE);
    }
}