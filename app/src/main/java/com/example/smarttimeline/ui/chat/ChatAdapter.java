package com.example.smarttimeline.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttimeline.R;
import com.example.smarttimeline.data.model.ChatMessage;
import com.example.smarttimeline.util.DateUtils;

public class ChatAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;
    private static final int VIEW_TYPE_SYSTEM = 3;
    private static final int VIEW_TYPE_TYPING = 4;

    public ChatAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChatMessage>() {
        @Override
        public boolean areItemsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
            return oldItem.getMessage().equals(newItem.getMessage()) &&
                    oldItem.getType() == newItem.getType() &&
                    oldItem.isTyping() == newItem.isTyping();
        }
    };

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = getItem(position);
        if (message.isTyping()) {
            return VIEW_TYPE_TYPING;
        }
        switch (message.getType()) {
            case USER:
                return VIEW_TYPE_USER;
            case AI:
                return VIEW_TYPE_AI;
            case SYSTEM:
                return VIEW_TYPE_SYSTEM;
            default:
                return VIEW_TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_USER:
                View userView = inflater.inflate(R.layout.item_chat_user, parent, false);
                return new UserMessageViewHolder(userView);
            case VIEW_TYPE_AI:
                View aiView = inflater.inflate(R.layout.item_chat_ai, parent, false);
                return new AIMessageViewHolder(aiView);
            case VIEW_TYPE_SYSTEM:
                View systemView = inflater.inflate(R.layout.item_chat_system, parent, false);
                return new SystemMessageViewHolder(systemView);
            case VIEW_TYPE_TYPING:
                View typingView = inflater.inflate(R.layout.item_chat_typing, parent, false);
                return new TypingViewHolder(typingView);
            default:
                View defaultView = inflater.inflate(R.layout.item_chat_ai, parent, false);
                return new AIMessageViewHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = getItem(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AIMessageViewHolder) {
            ((AIMessageViewHolder) holder).bind(message);
        } else if (holder instanceof SystemMessageViewHolder) {
            ((SystemMessageViewHolder) holder).bind(message);
        }
        // TypingViewHolder doesn't need binding
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewMessage;
        private final TextView textViewTime;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }

        public void bind(ChatMessage message) {
            textViewMessage.setText(message.getMessage());
            textViewTime.setText(DateUtils.formatTime(message.getTimestamp()));
        }
    }

    static class AIMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewMessage;
        private final TextView textViewTime;

        public AIMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }

        public void bind(ChatMessage message) {
            textViewMessage.setText(message.getMessage());
            textViewTime.setText(DateUtils.formatTime(message.getTimestamp()));
        }
    }

    static class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewMessage;

        public SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
        }

        public void bind(ChatMessage message) {
            textViewMessage.setText(message.getMessage());
        }
    }

    static class TypingViewHolder extends RecyclerView.ViewHolder {
        public TypingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}