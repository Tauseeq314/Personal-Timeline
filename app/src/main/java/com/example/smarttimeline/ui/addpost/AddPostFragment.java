package com.example.smarttimeline.ui.addpost;

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
import com.example.smarttimeline.viewmodel.AddPostViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AddPostFragment extends Fragment {

    private AddPostViewModel viewModel;

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

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);

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
        viewModel = new ViewModelProvider(this).get(AddPostViewModel.class);

        viewModel.getPostInserted().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Post added successfully!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
                viewModel.resetInsertionState();
            } else if (success != null && !success) {
                Toast.makeText(getContext(), "Failed to add post", Toast.LENGTH_SHORT).show();
            }
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

        Post post = new Post();
        post.setText(content);
        post.setLocation(location.isEmpty() ? null : location);
        post.setTimestamp(System.currentTimeMillis());

        int moodPosition = spinnerMood.getSelectedItemPosition();
        if (moodPosition > 0) {
            post.setMood(spinnerMood.getSelectedItem().toString());
        }

        if (selectedImageUri != null) {
            post.setImageUri(selectedImageUri.toString());
        }

        if (!tags.isEmpty()) {
            post.setTags(new ArrayList<>(tags));
        }

        viewModel.insertPost(post);
    }
}