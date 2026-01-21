package com.example.smarttimeline.ui.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttimeline.R;
import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.ui.postdetail.PostDetailFragment;
import com.example.smarttimeline.util.Constants;
import com.example.smarttimeline.viewmodel.TimelineViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TimelineFragment extends Fragment {

    private TimelineViewModel viewModel;
    private RecyclerView recyclerView;
    private TimelineAdapter adapter;
    private TextView emptyTextView;
    private FloatingActionButton fabAddPost;
    private SearchView searchView;
    private AutoCompleteTextView moodFilterDropdown;

    private List<Post> allPosts = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentMoodFilter = "All Moods";
    private View emptyStateView;
    private ImageView emptyStateIcon;
    private TextView emptyStateTitle;
    private TextView emptyStateMessage;
    private MaterialButton emptyStateButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewTimeline);
        fabAddPost = view.findViewById(R.id.fabAddPost);
        searchView = view.findViewById(R.id.searchView);
        moodFilterDropdown = view.findViewById(R.id.moodFilterDropdown);

        emptyStateView = view.findViewById(R.id.emptyStateView);
        emptyStateIcon = emptyStateView.findViewById(R.id.emptyStateIcon);
        emptyStateTitle = emptyStateView.findViewById(R.id.emptyStateTitle);
        emptyStateMessage = emptyStateView.findViewById(R.id.emptyStateMessage);
        emptyStateButton = emptyStateView.findViewById(R.id.emptyStateButton);

        setupRecyclerView();
        setupSearchView();
        setupMoodFilter();
        setupViewModel();
        setupFab();
        setupEmptyState();

        return view;
    }
    private void setupEmptyState() {
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

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new TimelineAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(post -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, PostDetailFragment.newInstance(post.getId()))
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                filterPosts();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                filterPosts();
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            currentSearchQuery = "";
            filterPosts();
            return false;
        });
    }

    private void setupMoodFilter() {
        List<String> moodOptions = new ArrayList<>();
        moodOptions.add("All Moods");
        for (String mood : Constants.MOOD_OPTIONS) {
            moodOptions.add(mood);
        }

        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                moodOptions
        );

        moodFilterDropdown.setAdapter(moodAdapter);
        moodFilterDropdown.setText("All Moods", false);

        moodFilterDropdown.setOnItemClickListener((parent, view, position, id) -> {
            currentMoodFilter = moodOptions.get(position);
            filterPosts();
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TimelineViewModel.class);

        viewModel.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                allPosts = new ArrayList<>(posts);
                filterPosts();
            }
        });
    }

    private void filterPosts() {
        List<Post> filteredPosts = new ArrayList<>();

        for (Post post : allPosts) {
            boolean matchesSearch = matchesSearchQuery(post, currentSearchQuery);
            boolean matchesMood = matchesMoodFilter(post, currentMoodFilter);

            if (matchesSearch && matchesMood) {
                filteredPosts.add(post);
            }
        }

        adapter.submitList(filteredPosts);
        updateEmptyView(filteredPosts);
    }

    private boolean matchesSearchQuery(Post post, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String lowerQuery = query.toLowerCase().trim();

        // Search in text content
        if (post.getText() != null && post.getText().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // Search in location
        if (post.getLocation() != null && post.getLocation().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // Search in tags
        if (post.getTags() != null) {
            for (String tag : post.getTags()) {
                if (tag.toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }

        // Search in mood
        if (post.getMood() != null && post.getMood().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        return false;
    }

    private boolean matchesMoodFilter(Post post, String moodFilter) {
        if (moodFilter == null || moodFilter.equals("All Moods")) {
            return true;
        }

        return post.getMood() != null && post.getMood().equals(moodFilter);
    }

    private void updateEmptyView(List<Post> filteredPosts) {
        if (filteredPosts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);

            // Determine the reason for empty state
            boolean hasFilters = !currentSearchQuery.isEmpty() || !currentMoodFilter.equals("All Moods");

            if (hasFilters) {
                // No results for search/filter
                emptyStateIcon.setImageResource(R.drawable.ic_empty_search);
                emptyStateTitle.setText("No matching posts");
                emptyStateMessage.setText("Try adjusting your filters or search terms");
                emptyStateButton.setVisibility(View.GONE);
            } else if (allPosts.isEmpty()) {
                // Truly empty timeline
                emptyStateIcon.setImageResource(R.drawable.ic_empty_timeline);
                emptyStateTitle.setText("No posts yet");
                emptyStateMessage.setText("Start capturing your moments and build your personal timeline");
                emptyStateButton.setVisibility(View.VISIBLE);
                emptyStateButton.setText("Create First Post");
            } else {
                // Has posts but all filtered out (shouldn't normally happen)
                emptyStateIcon.setImageResource(R.drawable.ic_empty_search);
                emptyStateTitle.setText("No posts found");
                emptyStateMessage.setText("Try different search criteria");
                emptyStateButton.setVisibility(View.GONE);
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void setupFab() {
        fabAddPost.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new com.example.smarttimeline.ui.addpost.AddPostFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reset filters when returning to timeline
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
    }
}