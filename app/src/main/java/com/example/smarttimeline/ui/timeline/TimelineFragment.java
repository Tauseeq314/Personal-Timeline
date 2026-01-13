package com.example.smarttimeline.ui.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttimeline.R;
import com.example.smarttimeline.viewmodel.TimelineViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TimelineFragment extends Fragment {

    private TimelineViewModel viewModel;
    private RecyclerView recyclerView;
    private TimelineAdapter adapter;
    private TextView emptyTextView;
    private FloatingActionButton fabAddPost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewTimeline);
        emptyTextView = view.findViewById(R.id.textViewEmpty);
        fabAddPost = view.findViewById(R.id.fabAddPost);

        setupRecyclerView();
        setupViewModel();
        setupFab();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new TimelineAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(post -> {
            // Handle post click - navigate to detail view if needed
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TimelineViewModel.class);

        viewModel.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
            adapter.submitList(posts);

            if (posts == null || posts.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            }
        });
    }

    private void setupFab() {
        fabAddPost.setOnClickListener(v -> {
            // Navigate to AddPostFragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new com.example.smarttimeline.ui.addpost.AddPostFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
}