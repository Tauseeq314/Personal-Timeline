package com.example.smarttimeline.ui.timeline;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttimeline.R;
import com.example.smarttimeline.data.entity.Post;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimelineAdapter extends ListAdapter<Post, TimelineAdapter.PostViewHolder> {

    private OnItemClickListener listener;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public TimelineAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {
        @Override
        public boolean areItemsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.getText().equals(newItem.getText()) &&
                    oldItem.getTimestamp() == newItem.getTimestamp() &&
                    ((oldItem.getMood() == null && newItem.getMood() == null) ||
                            (oldItem.getMood() != null && oldItem.getMood().equals(newItem.getMood())));
        }
    };

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline_post, parent, false);
        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post currentPost = getItem(position);
        holder.bind(currentPost);
    }

    public Post getPostAt(int position) {
        return getItem(position);
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewContent;
        private final TextView textViewTimestamp;
        private final TextView textViewMood;
        private final TextView textViewLocation;
        private final ImageView imageViewPost;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            textViewMood = itemView.findViewById(R.id.textViewMood);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            imageViewPost = itemView.findViewById(R.id.imageViewPost);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        public void bind(Post post) {
            textViewContent.setText(post.getText());
            textViewTimestamp.setText(dateFormat.format(new Date(post.getTimestamp())));

            if (post.getMood() != null && !post.getMood().isEmpty()) {
                textViewMood.setVisibility(View.VISIBLE);
                textViewMood.setText(post.getMood());
            } else {
                textViewMood.setVisibility(View.GONE);
            }

            if (post.getLocation() != null && !post.getLocation().isEmpty()) {
                textViewLocation.setVisibility(View.VISIBLE);
                textViewLocation.setText(post.getLocation());
            } else {
                textViewLocation.setVisibility(View.GONE);
            }

            if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
                imageViewPost.setVisibility(View.VISIBLE);
                imageViewPost.setImageURI(Uri.parse(post.getImageUri()));
            } else {
                imageViewPost.setVisibility(View.GONE);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}