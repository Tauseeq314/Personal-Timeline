package com.example.smarttimeline.ui.editpost;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttimeline.R;
import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.util.Constants;
import com.example.smarttimeline.viewmodel.EditPostViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class EditPostFragment extends Fragment {

    private EditPostViewModel viewModel;

    private EditText editTextContent;
    private EditText editTextLocation;
    private EditText editTextTag;
    private Spinner spinnerMood;
    private ChipGroup chipGroupTags;
    private ImageView imageViewPreview;
    private Button buttonAddImage;
    private Button buttonAddTag;
    private Button buttonSave;
    private Button buttonCancel;

    private Uri selectedImageUri;
    private List<String> tags;
    private int postId = -1;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static EditPostFragment newInstance(int postId) {
        EditPostFragment fragment = new EditPostFragment();
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
        View view = inflater.inflate(R.layout.fragment_edit_post, container, false);

        initializeViews(view);
        setupImagePicker();
        setupViewModel();
        setupMoodSpinner();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        editTextContent = view.findViewById(R.id.editTextContent);
        editTextLocation = view.findViewById(R.id.editTextLocation);
        editTextTag = view.findViewById(R.id.editTextTag);
        spinnerMood = view.findViewById(R.id.spinnerMood);
        chipGroupTags = view.findViewById(R.id.chipGroupTags);
        imageViewPreview = view.findViewById(R.id.imageViewPreview);
        buttonAddImage = view.findViewById(R.id.buttonAddImage);
        buttonAddTag = view.findViewById(R.id.buttonAddTag);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        tags = new ArrayList<>();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            imageViewPreview.setVisibility(View.VISIBLE);
                            imageViewPreview.setImageURI(selectedImageUri);
                        }
                    }
                }
        );
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(EditPostViewModel.class);

        if (postId != -1) {
            viewModel.loadPost(postId);
        }

        viewModel.getPost().observe(getViewLifecycleOwner(), this::populateFields);

        // FIX THIS OBSERVER:
        viewModel.getUpdateStatus().observe(getViewLifecycleOwner(), success -> {
            // Only process non-null values
            if (success == null) {
                return;
            }

            if (success) {
                Toast.makeText(getContext(), "Post updated successfully!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            } else {
                Toast.makeText(getContext(), "Failed to update post", Toast.LENGTH_SHORT).show();
            }

            // Reset after handling
            viewModel.resetUpdateStatus();
        });
    }

    private void setupMoodSpinner() {
        String[] moods = {"Select Mood", "Happy", "Sad", "Excited", "Calm", "Anxious", "Grateful", "Frustrated", "Motivated"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                moods
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(adapter);
    }

    private void setupListeners() {
        buttonAddImage.setOnClickListener(v -> openImagePicker());

        buttonAddTag.setOnClickListener(v -> addTag());

        buttonSave.setOnClickListener(v -> savePost());

        buttonCancel.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void populateFields(Post post) {
        if (post == null) {
            Toast.makeText(getContext(), "Post not found", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return;
        }

        editTextContent.setText(post.getText());

        if (post.getLocation() != null) {
            editTextLocation.setText(post.getLocation());
        }

        if (post.getMood() != null && !post.getMood().isEmpty()) {
            String[] moods = {"Select Mood", "Happy", "Sad", "Excited", "Calm", "Anxious", "Grateful", "Frustrated", "Motivated"};
            for (int i = 0; i < moods.length; i++) {
                if (moods[i].equals(post.getMood())) {
                    spinnerMood.setSelection(i);
                    break;
                }
            }
        }

        if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
            selectedImageUri = Uri.parse(post.getImageUri());
            imageViewPreview.setVisibility(View.VISIBLE);
            imageViewPreview.setImageURI(selectedImageUri);
        }

        if (post.getTags() != null && !post.getTags().isEmpty()) {
            tags.clear();
            tags.addAll(post.getTags());

            for (String tag : post.getTags()) {
                Chip chip = new Chip(getContext());
                chip.setText(tag);
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(v -> {
                    chipGroupTags.removeView(chip);
                    tags.remove(tag);
                });
                chipGroupTags.addView(chip);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void addTag() {
        String tag = editTextTag.getText().toString().trim();

        if (tag.isEmpty()) {
            editTextTag.setError("Tag cannot be empty");
            return;
        }

        if (tags.contains(tag)) {
            Toast.makeText(getContext(), "Tag already added", Toast.LENGTH_SHORT).show();
            return;
        }

        tags.add(tag);

        Chip chip = new Chip(getContext());
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroupTags.removeView(chip);
            tags.remove(tag);
        });

        chipGroupTags.addView(chip);
        editTextTag.setText("");
    }

    private void savePost() {
        String content = editTextContent.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        if (content.isEmpty()) {
            editTextContent.setError("Content is required");
            editTextContent.requestFocus();
            return;
        }

        Post post = viewModel.getPost().getValue();
        if (post == null) {
            Toast.makeText(getContext(), "Error: Post not found", Toast.LENGTH_SHORT).show();
            return;
        }

        post.setText(content);
        post.setLocation(location.isEmpty() ? null : location);

        int moodPosition = spinnerMood.getSelectedItemPosition();
        if (moodPosition > 0) {
            post.setMood(spinnerMood.getSelectedItem().toString());
        } else {
            post.setMood(null);
        }

        if (selectedImageUri != null) {
            post.setImageUri(selectedImageUri.toString());
        } else {
            post.setImageUri(null);
        }

        if (!tags.isEmpty()) {
            post.setTags(new ArrayList<>(tags));
        } else {
            post.setTags(null);
        }

        viewModel.updatePost(post);
    }
}