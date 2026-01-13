package com.example.smarttimeline.ai;

import com.example.smarttimeline.data.entity.Post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AIUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final int MAX_PROMPT_LENGTH = 4000;

    public static String buildDetailedSummaryPrompt(List<Post> posts, String period) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an AI assistant helping to summarize personal journal entries. ");
        prompt.append("Analyze the following ").append(posts.size()).append(" posts from a ");
        prompt.append(period.toLowerCase()).append(" period.\n\n");

        prompt.append("Provide a comprehensive summary that includes:\n");
        prompt.append("1. Main themes and recurring topics\n");
        prompt.append("2. Emotional journey and mood patterns\n");
        prompt.append("3. Notable events or milestones\n");
        prompt.append("4. Personal growth or changes observed\n");
        prompt.append("5. Areas of focus or concern\n\n");

        prompt.append("Posts (chronological order):\n\n");
        prompt.append(formatPostsForPrompt(posts, 40));

        prompt.append("\n\nProvide a thoughtful summary in 4-6 sentences that captures the essence of this period.");

        return truncatePrompt(prompt.toString());
    }

    public static String buildInsightPrompt(List<Post> posts, String period) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Analyze these journal entries and provide 3-5 key insights about the person's ");
        prompt.append(period.toLowerCase()).append(" experience.\n\n");

        prompt.append("Focus on:\n");
        prompt.append("- Patterns in behavior or thoughts\n");
        prompt.append("- Emotional well-being\n");
        prompt.append("- Progress toward goals or challenges\n");
        prompt.append("- Social interactions and relationships\n\n");

        prompt.append("Posts:\n");
        prompt.append(formatPostsForPrompt(posts, 30));

        prompt.append("\n\nProvide insights in a supportive, constructive tone.");

        return truncatePrompt(prompt.toString());
    }

    public static String buildMoodAnalysisPrompt(List<Post> posts, String period) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Analyze the emotional patterns in these journal entries from a ");
        prompt.append(period.toLowerCase()).append(" period.\n\n");

        prompt.append("Provide:\n");
        prompt.append("1. Overall emotional trend (improving, declining, stable)\n");
        prompt.append("2. Dominant emotions and their frequency\n");
        prompt.append("3. Possible triggers for mood changes\n");
        prompt.append("4. Recommendations for emotional well-being\n\n");

        prompt.append("Posts with mood indicators:\n");
        prompt.append(formatPostsWithMood(posts, 35));

        prompt.append("\n\nProvide analysis in 3-4 sentences.");

        return truncatePrompt(prompt.toString());
    }

    public static String buildQuickSummaryPrompt(List<Post> posts, String period) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Summarize these ").append(posts.size()).append(" journal entries from ");
        prompt.append(period.toLowerCase()).append(" in 2-3 sentences. ");
        prompt.append("Focus on main themes and overall mood.\n\n");

        prompt.append(formatPostsForPrompt(posts, 25));

        return truncatePrompt(prompt.toString());
    }

    public static String buildThemeExtractionPrompt(List<Post> posts) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Identify the top 5 recurring themes or topics in these journal entries. ");
        prompt.append("List them as comma-separated keywords.\n\n");

        prompt.append(formatPostsForPrompt(posts, 30));

        prompt.append("\n\nRespond with only the theme keywords, separated by commas.");

        return truncatePrompt(prompt.toString());
    }

    private static String formatPostsForPrompt(List<Post> posts, int maxPosts) {
        StringBuilder formatted = new StringBuilder();
        int count = Math.min(posts.size(), maxPosts);

        for (int i = 0; i < count; i++) {
            Post post = posts.get(i);
            formatted.append(i + 1).append(". ");

            if (post.getTimestamp() > 0) {
                formatted.append("[").append(dateFormat.format(new Date(post.getTimestamp()))).append("] ");
            }

            formatted.append(post.getText());

            if (post.getLocation() != null && !post.getLocation().isEmpty()) {
                formatted.append(" (Location: ").append(post.getLocation()).append(")");
            }

            formatted.append("\n");
        }

        if (posts.size() > maxPosts) {
            formatted.append("\n... and ").append(posts.size() - maxPosts).append(" more posts");
        }

        return formatted.toString();
    }

    private static String formatPostsWithMood(List<Post> posts, int maxPosts) {
        StringBuilder formatted = new StringBuilder();
        int count = Math.min(posts.size(), maxPosts);

        for (int i = 0; i < count; i++) {
            Post post = posts.get(i);

            String mood = post.getMood();
            if (mood != null && !mood.isEmpty()) {
                formatted.append("- [").append(mood).append("] ");
                formatted.append(post.getText());
                formatted.append("\n");
            }
        }

        return formatted.toString();
    }

    public static String extractKeywordsFromPosts(List<Post> posts) {
        Map<String, Integer> tagFrequency = new HashMap<>();

        for (Post post : posts) {
            if (post.getTags() != null) {
                for (String tag : post.getTags()) {
                    tagFrequency.put(tag, tagFrequency.getOrDefault(tag, 0) + 1);
                }
            }
        }

        List<Map.Entry<String, Integer>> sortedTags = new ArrayList<>(tagFrequency.entrySet());
        sortedTags.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        StringBuilder keywords = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedTags) {
            if (count >= 10) break;
            if (keywords.length() > 0) {
                keywords.append(", ");
            }
            keywords.append(entry.getKey());
            count++;
        }

        return keywords.toString();
    }

    public static String formatSummaryResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isEmpty()) {
            return "No summary available.";
        }

        String formatted = rawResponse.trim();

        formatted = formatted.replaceAll("(?m)^\\s*[*-]\\s*", "• ");

        formatted = formatted.replaceAll("\\n{3,}", "\n\n");

        if (!formatted.endsWith(".") && !formatted.endsWith("!") && !formatted.endsWith("?")) {
            formatted += ".";
        }

        return formatted;
    }

    public static String truncatePrompt(String prompt) {
        if (prompt.length() <= MAX_PROMPT_LENGTH) {
            return prompt;
        }

        return prompt.substring(0, MAX_PROMPT_LENGTH - 50) + "\n\n[Content truncated for length]";
    }

    public static Map<String, Integer> analyzeMoodDistribution(List<Post> posts) {
        Map<String, Integer> distribution = new HashMap<>();

        for (Post post : posts) {
            String mood = post.getMood();
            if (mood != null && !mood.isEmpty()) {
                distribution.put(mood, distribution.getOrDefault(mood, 0) + 1);
            }
        }

        return distribution;
    }

    public static String getMoodSummary(Map<String, Integer> moodDistribution) {
        if (moodDistribution.isEmpty()) {
            return "No mood data available";
        }

        String dominantMood = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : moodDistribution.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantMood = entry.getKey();
            }
        }

        return String.format("Most frequent mood: %s (%d occurrences)", dominantMood, maxCount);
    }

    public static int calculatePostFrequency(List<Post> posts, long periodInMillis) {
        if (posts.isEmpty() || periodInMillis <= 0) {
            return 0;
        }

        long days = periodInMillis / (24 * 60 * 60 * 1000);
        if (days == 0) days = 1;

        return (int) (posts.size() / (double) days);
    }

    public static String generatePeriodLabel(long startTimestamp, long endTimestamp) {
        String startDate = dateFormat.format(new Date(startTimestamp));
        String endDate = dateFormat.format(new Date(endTimestamp));
        return startDate + " - " + endDate;
    }

    public static boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        String trimmed = apiKey.trim();
        return trimmed.startsWith("gsk") ||
                trimmed.startsWith("xai-") ||
                trimmed.startsWith("sk-") ||
                trimmed.startsWith("AIza");
    }

    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }

        return input.trim()
                .replaceAll("[<>]", "")
                .replaceAll("\\s+", " ");
    }
}