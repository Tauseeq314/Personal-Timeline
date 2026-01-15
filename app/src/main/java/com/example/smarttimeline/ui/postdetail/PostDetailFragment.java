package com.example.smarttimeline.ui.postdetail;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttimeline.R;
import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.util.Constants;
import com.example.smarttimeline.util.DateUtils;
import com.example.smarttimeline.viewmodel.PostDetailViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class PostDetailFragment extends Fragment {

    private PostDetailViewModel viewModel;

    private TextView textViewContent;
    private TextView textViewTimestamp;
    private TextView textViewMood;
    private TextView textViewLocation;
    private ImageView imageViewPost;
    private ChipGroup chipGroupTags;
    private MaterialButton buttonEdit;
    private MaterialButton buttonDelete;

    private int postId = -1;

    public static PostDetailFragment newInstance(int postId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.EXTRA_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getInt(Constants.EXTRA_POST_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        initializeViews(view);
        setupViewModel();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        textViewContent = view.findViewById(R.id.textViewContent);
        textViewTimestamp = view.findViewById(R.id.textViewTimestamp);
        textViewMood = view.findViewById(R.id.textViewMood);
        textViewLocation = view.findViewById(R.id.textViewLocation);
        imageViewPost = view.findViewById(R.id.imageViewPost);
        chipGroupTags = view.findViewById(R.id.chipGroupTags);
        buttonEdit = view.findViewById(R.id.buttonEdit);
        buttonDelete = view.findViewById(R.id.buttonDelete);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        if (postId != -1) {
            viewModel.loadPost(postId);
        }

        viewModel.getPost().observe(getViewLifecycleOwner(), this::displayPost);

        viewModel.getDeleteStatus().observe(getViewLifecycleOwner(), deleted -> {
            if (deleted != null && deleted) {
                Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void setupListeners() {
        buttonEdit.setOnClickListener(v -> editPost());
        buttonDelete.setOnClickListener(v -> confirmDelete());
    }

    private void displayPost(Post post) {
        if (post == null) {
            Toast.makeText(getContext(), "Post not found", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return;
        }

        textViewContent.setText(post.getText());
        textViewTimestamp.setText(DateUtils.formatDateTime(post.getTimestamp()));

        if (post.getMood() != null && !post.getMood().isEmpty()) {
            textViewMood.setVisibility(View.VISIBLE);
            textViewMood.setText("Mood: " + post.getMood());
        } else {
            textViewMood.setVisibility(View.GONE);
        }

        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            textViewLocation.setVisibility(View.VISIBLE);
            textViewLocation.setText("Location: " + post.getLocation());
        } else {
            textViewLocation.setVisibility(View.GONE);
        }

        if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
            imageViewPost.setVisibility(View.VISIBLE);
            imageViewPost.setImageURI(Uri.parse(post.getImageUri()));
        } else {
            imageViewPost.setVisibility(View.GONE);
        }

        chipGroupTags.removeAllViews();
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            chipGroupTags.setVisibility(View.VISIBLE);
            for (String tag : post.getTags()) {
                Chip chip = new Chip(getContext());
                chip.setText(tag);
                chip.setClickable(false);
                chipGroupTags.addView(chip);
            }
        } else {
            chipGroupTags.setVisibility(View.GONE);
        }
    }

    private void editPost() {
        Post post = viewModel.getPost().getValue();
        if (post != null && getActivity() != null) {
            com.example.smarttimeline.ui.editpost.EditPostFragment editFragment =
                    com.example.smarttimeline.ui.editpost.EditPostFragment.newInstance(post.getId());

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, editFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePost())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePost() {
        Post post = viewModel.getPost().getValue();
        if (post != null) {
            viewModel.deletePost(post);
        }
    }
}