package com.example.smarttimeline.ui.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttimeline.R;
import com.example.smarttimeline.viewmodel.AnalyticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private AnalyticsViewModel viewModel;

    private PieChart pieChartMood;
    private BarChart barChartPostsPerDay;
    private BarChart barChartTags;
    private TextView textViewTotalPosts;
    private TextView textViewNoData;
    private View emptyStateView;
    private ImageView emptyStateIcon;
    private TextView emptyStateTitle;
    private TextView emptyStateMessage;
    private MaterialButton emptyStateButton;
    private TextView textViewTotalPostsCount;
    private TextView textViewTopMood;
    private TextView textViewTopMoodEmoji;
    private TextView textViewActivityPeriod;
    private TextView textViewTotalTags;
    private TextView textViewInsights;
    private MaterialCardView cardMoodChart;
    private MaterialCardView cardActivityChart;
    private MaterialCardView cardTagsChart;
    private MaterialCardView cardInsights;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        initializeViews(view);
        setupViewModel();

        return view;
    }

    private void initializeViews(View view) {
        pieChartMood = view.findViewById(R.id.pieChartMood);
        barChartPostsPerDay = view.findViewById(R.id.barChartPostsPerDay);
        barChartTags = view.findViewById(R.id.barChartTags);

        textViewTotalPostsCount = view.findViewById(R.id.textViewTotalPostsCount);
        textViewTopMood = view.findViewById(R.id.textViewTopMood);
        textViewTopMoodEmoji = view.findViewById(R.id.textViewTopMoodEmoji);
        textViewActivityPeriod = view.findViewById(R.id.textViewActivityPeriod);
        textViewTotalTags = view.findViewById(R.id.textViewTotalTags);
        textViewInsights = view.findViewById(R.id.textViewInsights);

        cardMoodChart = view.findViewById(R.id.cardMoodChart);
        cardActivityChart = view.findViewById(R.id.cardActivityChart);
        cardTagsChart = view.findViewById(R.id.cardTagsChart);
        cardInsights = view.findViewById(R.id.cardInsights);

        emptyStateView = view.findViewById(R.id.emptyStateView);
        emptyStateIcon = emptyStateView.findViewById(R.id.emptyStateIcon);
        emptyStateTitle = emptyStateView.findViewById(R.id.emptyStateTitle);
        emptyStateMessage = emptyStateView.findViewById(R.id.emptyStateMessage);
        emptyStateButton = emptyStateView.findViewById(R.id.emptyStateButton);

        setupCharts();
        setupEmptyState();
    }

    private void setupEmptyState() {
        emptyStateIcon.setImageResource(R.drawable.ic_empty_analytics);
        emptyStateTitle.setText("No analytics yet");
        emptyStateMessage.setText("Create posts to see insights about your timeline, moods, and activities");
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
    }

    private void setupCharts() {
        pieChartMood.getDescription().setEnabled(false);
        pieChartMood.setDrawHoleEnabled(true);
        pieChartMood.setHoleColor(Color.WHITE);
        pieChartMood.setTransparentCircleRadius(61f);

        barChartPostsPerDay.getDescription().setEnabled(false);
        barChartPostsPerDay.setDrawGridBackground(false);
        barChartPostsPerDay.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartPostsPerDay.getAxisRight().setEnabled(false);

        barChartTags.getDescription().setEnabled(false);
        barChartTags.setDrawGridBackground(false);
        barChartTags.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartTags.getAxisRight().setEnabled(false);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);

        viewModel.getPostCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                textViewTotalPostsCount.setText(String.valueOf(count));

                if (count == 0) {
                    emptyStateView.setVisibility(View.VISIBLE);
                    cardMoodChart.setVisibility(View.GONE);
                    cardActivityChart.setVisibility(View.GONE);
                    cardTagsChart.setVisibility(View.GONE);
                    cardInsights.setVisibility(View.GONE);
                } else {
                    emptyStateView.setVisibility(View.GONE);
                    cardMoodChart.setVisibility(View.VISIBLE);
                    cardActivityChart.setVisibility(View.VISIBLE);
                    cardTagsChart.setVisibility(View.VISIBLE);
                    generateInsights(count);
                }
            }
        });

        viewModel.getMoodDistribution().observe(getViewLifecycleOwner(), this::updateMoodChart);

        viewModel.getPostsPerDay().observe(getViewLifecycleOwner(), this::updatePostsPerDayChart);

        viewModel.getTagsDistribution().observe(getViewLifecycleOwner(), this::updateTagsChart);
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

    private void updateMoodChart(Map<String, Integer> moodData) {
        if (moodData == null || moodData.isEmpty()) {
            pieChartMood.clear();
            textViewTopMood.setText("N/A");
            textViewTopMoodEmoji.setText("😊");
            return;
        }

        // Find top mood
        String topMood = "Happy";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : moodData.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topMood = entry.getKey();
            }
        }

        textViewTopMood.setText(topMood);
        textViewTopMoodEmoji.setText(getMoodEmoji(topMood));

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : moodData.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        pieChartMood.setData(data);
        pieChartMood.setDrawEntryLabels(true);
        pieChartMood.setEntryLabelColor(Color.BLACK);
        pieChartMood.setEntryLabelTextSize(12f);
        pieChartMood.animateY(1000);
        pieChartMood.invalidate();
    }

    private void updatePostsPerDayChart(Map<String, Integer> dailyData) {
        if (dailyData == null || dailyData.isEmpty()) {
            barChartPostsPerDay.clear();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> entry : dailyData.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Posts Per Day");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        barChartPostsPerDay.setData(data);
        barChartPostsPerDay.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartPostsPerDay.getXAxis().setGranularity(1f);
        barChartPostsPerDay.invalidate();
    }

    private void updateTagsChart(Map<String, Integer> tagsData) {
        if (tagsData == null || tagsData.isEmpty()) {
            barChartTags.clear();
            textViewTotalTags.setText("0 tags");
            return;
        }
        textViewTotalTags.setText(tagsData.size() + " tags");

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> entry : tagsData.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Tag Usage");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[2]);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        barChartTags.setData(data);
        barChartTags.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartTags.getXAxis().setGranularity(1f);
        barChartTags.invalidate();
    }

    private void generateInsights(int postCount) {
        cardInsights.setVisibility(View.VISIBLE);

        String insight;
        if (postCount < 5) {
            insight = "🌱 Great start! You have " + postCount + " posts. Keep building your timeline!";
        } else if (postCount < 20) {
            insight = "📈 You're building momentum with " + postCount + " posts. Your timeline is taking shape!";
        } else if (postCount < 50) {
            insight = "⭐ Impressive! " + postCount + " posts captured. You're creating a rich personal history!";
        } else {
            insight = "🏆 Amazing dedication! " + postCount + " posts and counting. Your timeline is a treasure!";
        }

        textViewInsights.setText(insight);
    }
}