package com.example.smarttimeline.ui.timeline;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttimeline.R;
import com.example.smarttimeline.data.entity.Post;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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
        private final Chip chipMood;
        private final TextView textViewLocation;
        private final ImageView imageViewPost;
        private final View moodColorStrip;
        private final MaterialCardView imageCardView;
        private final ChipGroup chipGroupTags;
        private final LinearLayout metaInfoContainer;
        private final LinearLayout locationContainer;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            chipMood = itemView.findViewById(R.id.chipMood);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            imageViewPost = itemView.findViewById(R.id.imageViewPost);
            moodColorStrip = itemView.findViewById(R.id.moodColorStrip);
            imageCardView = itemView.findViewById(R.id.imageCardView);
            chipGroupTags = itemView.findViewById(R.id.chipGroupTags);
            metaInfoContainer = itemView.findViewById(R.id.metaInfoContainer);
            locationContainer = itemView.findViewById(R.id.locationContainer);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        public void bind(Post post) {
            // Set content
            textViewContent.setText(post.getText());
            textViewTimestamp.setText(dateFormat.format(new Date(post.getTimestamp())));

            // Set mood color strip and chip
            if (post.getMood() != null && !post.getMood().isEmpty()) {
                chipMood.setVisibility(View.VISIBLE);
                chipMood.setText(getMoodEmoji(post.getMood()) + " " + post.getMood());

                int moodColor = getMoodColor(post.getMood());
                moodColorStrip.setBackgroundColor(moodColor);
                chipMood.setChipBackgroundColorResource(getMoodColorResource(post.getMood()));
            } else {
                chipMood.setVisibility(View.GONE);
                moodColorStrip.setBackgroundColor(itemView.getContext().getColor(R.color.mood_neutral_accent));
            }

            // Set image
            if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
                imageCardView.setVisibility(View.VISIBLE);
                imageViewPost.setImageURI(Uri.parse(post.getImageUri()));
            } else {
                imageCardView.setVisibility(View.GONE);
            }

            // Set tags
            chipGroupTags.removeAllViews();
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                chipGroupTags.setVisibility(View.VISIBLE);
                for (String tag : post.getTags()) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText("#" + tag);
                    chip.setTextSize(11);
                    chip.setChipBackgroundColorResource(R.color.primary_light);
                    chip.setTextColor(itemView.getContext().getColor(R.color.primary_dark));
                    chip.setClickable(false);
                    chip.setChipMinHeight(24);
                    chipGroupTags.addView(chip);
                }
            } else {
                chipGroupTags.setVisibility(View.GONE);
            }

            // Set location
            if (post.getLocation() != null && !post.getLocation().isEmpty()) {
                locationContainer.setVisibility(View.VISIBLE);
                textViewLocation.setText(post.getLocation());
            } else {
                locationContainer.setVisibility(View.GONE);
            }

            // Show/hide meta info container
            boolean hasMetaInfo = (post.getTags() != null && !post.getTags().isEmpty()) ||
                    (post.getLocation() != null && !post.getLocation().isEmpty());
            metaInfoContainer.setVisibility(hasMetaInfo ? View.VISIBLE : View.GONE);
        }

        private String getMoodEmoji(String mood) {
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

        private int getMoodColor(String mood) {
            int colorResId;
            switch (mood.toLowerCase()) {
                case "happy": colorResId = R.color.mood_happy_accent; break;
                case "sad": colorResId = R.color.mood_sad_accent; break;
                case "excited": colorResId = R.color.mood_excited_accent; break;
                case "calm": colorResId = R.color.mood_calm_accent; break;
                case "anxious": colorResId = R.color.mood_anxious_accent; break;
                case "grateful": colorResId = R.color.mood_grateful_accent; break;
                case "frustrated": colorResId = R.color.mood_frustrated_accent; break;
                case "motivated": colorResId = R.color.mood_motivated_accent; break;
                default: colorResId = R.color.mood_neutral_accent; break;
            }
            return itemView.getContext().getColor(colorResId);
        }

        private int getMoodColorResource(String mood) {
            switch (mood.toLowerCase()) {
                case "happy": return R.color.mood_happy;
                case "sad": return R.color.mood_sad;
                case "excited": return R.color.mood_excited;
                case "calm": return R.color.mood_calm;
                case "anxious": return R.color.mood_anxious;
                case "grateful": return R.color.mood_grateful;
                case "frustrated": return R.color.mood_frustrated;
                case "motivated": return R.color.mood_motivated;
                default: return R.color.mood_neutral;
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