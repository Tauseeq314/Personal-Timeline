package com.example.smarttimeline.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttimeline.R;
import com.example.smarttimeline.data.model.ChatMessage;
import com.example.smarttimeline.viewmodel.ChatViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ChatFragment extends Fragment {

    private ChatViewModel viewModel;
    private RecyclerView recyclerViewChat;
    private ChatAdapter adapter;
    private EditText editTextMessage;
    private MaterialButton buttonSend;
    private ImageView buttonClearChat;
    private TextView textViewEmptyState;
    private MaterialCardView cardEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);
        buttonClearChat = view.findViewById(R.id.buttonClearChat);
        textViewEmptyState = view.findViewById(R.id.textViewEmptyState);
        cardEmptyState = view.findViewById(R.id.cardEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);

        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        viewModel.getChatMessages().observe(getViewLifecycleOwner(), this::updateChatMessages);

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            buttonSend.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        buttonSend.setOnClickListener(v -> sendMessage());

        buttonClearChat.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Clear Chat")
                    .setMessage("Are you sure you want to clear the chat history?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        viewModel.clearChat();
                        Toast.makeText(getContext(), "Chat cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Send on Enter key (optional)
        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            viewModel.sendMessage(message);
            editTextMessage.setText("");

            // Hide keyboard
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(editTextMessage.getWindowToken(), 0);
            }
        }
    }

    private void updateChatMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
            recyclerViewChat.setVisibility(View.GONE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
            recyclerViewChat.setVisibility(View.VISIBLE);
            adapter.submitList(messages);

            // Scroll to bottom
            recyclerViewChat.postDelayed(() -> {
                if (messages.size() > 0) {
                    recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
                }
            }, 100);
        }
    }
}