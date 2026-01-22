package com.example.smarttimeline.ui.summary;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttimeline.R;
import com.example.smarttimeline.data.model.AISummary;
import com.example.smarttimeline.viewmodel.SummaryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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
    private TextView textViewLoadingStatus;
    private ProgressBar progressBar;
    private MaterialButton buttonGenerateSummary;
    private ChipGroup chipGroupPeriod;
    private Chip chipWeekly;
    private Chip chipMonthly;
    private Chip chipYearly;
    private ImageView iconShare;

    private MaterialCardView cardSummaryResult;
    private MaterialCardView cardLoading;
    private LinearLayout layoutDominantMood;
    private LinearLayout layoutKeyThemes;

    private View emptyStateView;
    private ImageView emptyStateIcon;
    private TextView emptyStateTitle;
    private TextView emptyStateMessage;
    private MaterialButton emptyStateButton;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    private String selectedPeriod = "Weekly";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        initializeViews(view);
        setupViewModel();
        setupListeners();
        setupEmptyState();

        return view;
    }

    private void initializeViews(View view) {
        textViewSummaryText = view.findViewById(R.id.textViewSummaryText);
        textViewPeriod = view.findViewById(R.id.textViewPeriod);
        textViewPostCount = view.findViewById(R.id.textViewPostCount);
        textViewDominantMood = view.findViewById(R.id.textViewDominantMood);
        textViewKeyThemes = view.findViewById(R.id.textViewKeyThemes);
        textViewGeneratedTime = view.findViewById(R.id.textViewGeneratedTime);
        textViewLoadingStatus = view.findViewById(R.id.textViewLoadingStatus);
        progressBar = view.findViewById(R.id.progressBar);
        buttonGenerateSummary = view.findViewById(R.id.buttonGenerateSummary);

        chipGroupPeriod = view.findViewById(R.id.chipGroupPeriod);
        chipWeekly = view.findViewById(R.id.chipWeekly);
        chipMonthly = view.findViewById(R.id.chipMonthly);
        chipYearly = view.findViewById(R.id.chipYearly);
        iconShare = view.findViewById(R.id.iconShare);

        cardSummaryResult = view.findViewById(R.id.cardSummaryResult);
        cardLoading = view.findViewById(R.id.cardLoading);
        layoutDominantMood = view.findViewById(R.id.layoutDominantMood);
        layoutKeyThemes = view.findViewById(R.id.layoutKeyThemes);

        emptyStateView = view.findViewById(R.id.emptyStateView);
        emptyStateIcon = emptyStateView.findViewById(R.id.emptyStateIcon);
        emptyStateTitle = emptyStateView.findViewById(R.id.emptyStateTitle);
        emptyStateMessage = emptyStateView.findViewById(R.id.emptyStateMessage);
        emptyStateButton = emptyStateView.findViewById(R.id.emptyStateButton);

        // Set Weekly as default
        chipWeekly.setChecked(true);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SummaryViewModel.class);

        viewModel.getLatestSummary().observe(getViewLifecycleOwner(), this::displaySummary);

        viewModel.getSummaryStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                if (status.startsWith("Error:") || status.contains("not configured") || status.contains("No posts")) {
                    cardLoading.setVisibility(View.GONE);
                    emptyStateView.setVisibility(View.VISIBLE);
                    cardSummaryResult.setVisibility(View.GONE);
                    buttonGenerateSummary.setEnabled(true);

                    if (status.contains("not configured")) {
                        emptyStateIcon.setImageResource(R.drawable.ic_empty_summary);
                        emptyStateTitle.setText("API Key Required");
                        emptyStateMessage.setText("Configure your Groq API key in Settings to generate AI summaries");
                        emptyStateButton.setVisibility(View.VISIBLE);
                        emptyStateButton.setText("Go to Settings");
                        emptyStateButton.setOnClickListener(v -> {
                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, new com.example.smarttimeline.ui.settings.SettingsFragment())
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    } else if (status.contains("No posts")) {
                        emptyStateIcon.setImageResource(R.drawable.ic_empty_summary);
                        emptyStateTitle.setText("No posts found");
                        emptyStateMessage.setText("Create some posts first to generate meaningful summaries");
                        emptyStateButton.setVisibility(View.VISIBLE);
                        emptyStateButton.setText("Create Post");
                        emptyStateButton.setOnClickListener(v -> {
                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, new com.example.smarttimeline.ui.addpost.AddPostFragment())
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    } else {
                        emptyStateIcon.setImageResource(R.drawable.ic_empty_summary);
                        emptyStateTitle.setText("Generation Failed");
                        emptyStateMessage.setText(status);
                        emptyStateButton.setVisibility(View.GONE);
                    }
                } else if (status.equals("Generating summary...")) {
                    cardLoading.setVisibility(View.VISIBLE);
                    emptyStateView.setVisibility(View.GONE);
                    cardSummaryResult.setVisibility(View.GONE);
                    textViewLoadingStatus.setText("Analyzing your " + selectedPeriod.toLowerCase() + " timeline...");
                } else if (status.equals("Summary generated successfully")) {
                    cardLoading.setVisibility(View.GONE);
                    emptyStateView.setVisibility(View.GONE);
                    cardSummaryResult.setVisibility(View.VISIBLE);
                    buttonGenerateSummary.setEnabled(true);
                }
            }
        });
    }

    private void setupListeners() {
        chipGroupPeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipWeekly)) {
                selectedPeriod = "Weekly";
            } else if (checkedIds.contains(R.id.chipMonthly)) {
                selectedPeriod = "Monthly";
            } else if (checkedIds.contains(R.id.chipYearly)) {
                selectedPeriod = "Yearly";
            }
        });

        buttonGenerateSummary.setOnClickListener(v -> generateSummary());

        iconShare.setOnClickListener(v -> shareSummary());
    }

    private void setupEmptyState() {
        emptyStateIcon.setImageResource(R.drawable.ic_empty_summary);
        emptyStateTitle.setText("No summary yet");
        emptyStateMessage.setText("Select a time period and generate an AI-powered summary of your timeline");
        emptyStateButton.setVisibility(View.GONE);

        // Show empty state by default
        emptyStateView.setVisibility(View.VISIBLE);
        cardSummaryResult.setVisibility(View.GONE);
    }

    private void generateSummary() {
        cardLoading.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);
        cardSummaryResult.setVisibility(View.GONE);
        textViewLoadingStatus.setText("Analyzing your " + selectedPeriod.toLowerCase() + " timeline...");
        buttonGenerateSummary.setEnabled(false);
        switch (selectedPeriod) {
            case "Weekly":
                viewModel.generateWeeklySummary();
                break;
            case "Monthly":
                viewModel.generateMonthlySummary();
                break;
            case "Yearly":
                viewModel.generateYearlySummary();
                break;
        }
    }

    private void displaySummary(AISummary summary) {
        if (summary == null) {
            return;
        }

        cardSummaryResult.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);

        textViewSummaryText.setText(summary.getSummaryText());
        textViewPeriod.setText(summary.getPeriod() + " Summary");
        textViewPostCount.setText(String.valueOf(summary.getPostCount()));
        textViewGeneratedTime.setText("Generated: " + dateFormat.format(new Date(summary.getGeneratedTimestamp())));

        if (summary.getDominantMood() != null && !summary.getDominantMood().isEmpty()) {
            layoutDominantMood.setVisibility(View.VISIBLE);
            textViewDominantMood.setText(getMoodEmoji(summary.getDominantMood()) + " " + summary.getDominantMood());
        } else {
            layoutDominantMood.setVisibility(View.GONE);
        }

        if (summary.getKeyThemes() != null && !summary.getKeyThemes().isEmpty()) {
            layoutKeyThemes.setVisibility(View.VISIBLE);
            textViewKeyThemes.setText(summary.getKeyThemes());
        } else {
            layoutKeyThemes.setVisibility(View.GONE);
        }
    }

    private String getMoodEmoji(String mood) {
        if (mood == null) return "😊";
        switch (mood.toLowerCase()) {
            case "happy": return "😊";
            case "sad": return "😢";
            case "excited": return "🤩";
            case "calm": return "😌";
            case "anxious": return "😰";
            case "grateful": return "🙏";
            case "frustrated": return "😤";
            case "motivated": return "💪";
            case "neutral": return "😐";
            default: return "😊";
        }
    }

    private void shareSummary() {
        AISummary summary = viewModel.getLatestSummary().getValue();
        if (summary == null) return;

        String shareText = "📊 " + summary.getPeriod() + " Summary\n\n" +
                summary.getSummaryText() + "\n\n" +
                "Posts: " + summary.getPostCount() + "\n" +
                (summary.getDominantMood() != null ? "Mood: " + summary.getDominantMood() + "\n" : "") +
                (summary.getKeyThemes() != null ? "Themes: " + summary.getKeyThemes() + "\n" : "") +
                "\n📱 Generated by SmartTimeline";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Summary"));
    }
}