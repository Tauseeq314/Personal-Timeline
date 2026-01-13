package com.example.smarttimeline.ui.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        textViewTotalPosts = view.findViewById(R.id.textViewTotalPosts);
        textViewNoData = view.findViewById(R.id.textViewNoData);

        setupCharts();
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
                textViewTotalPosts.setText("Total Posts: " + count);

                if (count == 0) {
                    textViewNoData.setVisibility(View.VISIBLE);
                    pieChartMood.setVisibility(View.GONE);
                    barChartPostsPerDay.setVisibility(View.GONE);
                    barChartTags.setVisibility(View.GONE);
                } else {
                    textViewNoData.setVisibility(View.GONE);
                    pieChartMood.setVisibility(View.VISIBLE);
                    barChartPostsPerDay.setVisibility(View.VISIBLE);
                    barChartTags.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getMoodDistribution().observe(getViewLifecycleOwner(), this::updateMoodChart);

        viewModel.getPostsPerDay().observe(getViewLifecycleOwner(), this::updatePostsPerDayChart);

        viewModel.getTagsDistribution().observe(getViewLifecycleOwner(), this::updateTagsChart);
    }

    private void updateMoodChart(Map<String, Integer> moodData) {
        if (moodData == null || moodData.isEmpty()) {
            pieChartMood.clear();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : moodData.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Mood Distribution");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChartMood.setData(data);
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
            return;
        }

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
}