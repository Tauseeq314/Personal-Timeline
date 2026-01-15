package com.example.smarttimeline.ui.timeline;

import com.example.smarttimeline.data.entity.Post;

import java.util.ArrayList;
import java.util.List;

public class SearchFilterHelper {

    public static List<Post> filterPosts(List<Post> posts, String searchQuery, String moodFilter) {
        List<Post> filteredPosts = new ArrayList<>();

        for (Post post : posts) {
            if (matchesFilters(post, searchQuery, moodFilter)) {
                filteredPosts.add(post);
            }
        }

        return filteredPosts;
    }

    private static boolean matchesFilters(Post post, String searchQuery, String moodFilter) {
        boolean matchesSearch = matchesSearchQuery(post, searchQuery);
        boolean matchesMood = matchesMoodFilter(post, moodFilter);

        return matchesSearch && matchesMood;
    }

    private static boolean matchesSearchQuery(Post post, String query) {
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

    private static boolean matchesMoodFilter(Post post, String moodFilter) {
        if (moodFilter == null || moodFilter.equals("All Moods")) {
            return true;
        }

        return post.getMood() != null && post.getMood().equals(moodFilter);
    }

    public static List<Post> searchByText(List<Post> posts, String query) {
        List<Post> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return posts;
        }

        String lowerQuery = query.toLowerCase().trim();

        for (Post post : posts) {
            if (matchesSearchQuery(post, lowerQuery)) {
                results.add(post);
            }
        }

        return results;
    }

    public static List<Post> filterByMood(List<Post> posts, String mood) {
        List<Post> results = new ArrayList<>();

        if (mood == null || mood.equals("All Moods")) {
            return posts;
        }

        for (Post post : posts) {
            if (post.getMood() != null && post.getMood().equals(mood)) {
                results.add(post);
            }
        }

        return results;
    }

    public static List<Post> filterByTag(List<Post> posts, String tag) {
        List<Post> results = new ArrayList<>();

        if (tag == null || tag.trim().isEmpty()) {
            return posts;
        }

        for (Post post : posts) {
            if (post.getTags() != null && post.getTags().contains(tag)) {
                results.add(post);
            }
        }

        return results;
    }

    public static List<Post> filterByDateRange(List<Post> posts, long startDate, long endDate) {
        List<Post> results = new ArrayList<>();

        for (Post post : posts) {
            long timestamp = post.getTimestamp();
            if (timestamp >= startDate && timestamp <= endDate) {
                results.add(post);
            }
        }

        return results;
    }

    public static int countPostsByMood(List<Post> posts, String mood) {
        int count = 0;

        for (Post post : posts) {
            if (post.getMood() != null && post.getMood().equals(mood)) {
                count++;
            }
        }

        return count;
    }
}