package com.example.smarttimeline.ai;

import android.content.Context;
import android.util.Log;

import com.example.smarttimeline.data.entity.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AIService {

    private static final String TAG = "AIService";
    private static final String API_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final int TIMEOUT = 30000;

    private final ExecutorService executorService;
    private String apiKey;

    public AIService() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public interface SummaryCallback {
        void onSuccess(String summary);
        void onError(String error);
    }

    public void generateSummary(List<Post> posts, String period, SummaryCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API key not configured");
            return;
        }

        if (posts == null || posts.isEmpty()) {
            callback.onError("No posts to summarize");
            return;
        }

        executorService.execute(() -> {
            try {
                String prompt = buildPrompt(posts, period);
                String summary = callAPI(prompt);
                callback.onSuccess(summary);
            } catch (Exception e) {
                Log.e(TAG, "Error generating summary", e);
                callback.onError(e.getMessage());
            }
        });
    }

    private String buildPrompt(List<Post> posts, String period) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following posts from a ").append(period.toLowerCase())
                .append(" period and provide a concise, insightful summary. ");
        prompt.append("Include:\n");
        prompt.append("1. Overall themes and topics\n");
        prompt.append("2. Emotional patterns\n");
        prompt.append("3. Key highlights or notable events\n");
        prompt.append("4. Any patterns or trends\n\n");
        prompt.append("Posts:\n");

        for (int i = 0; i < posts.size() && i < 50; i++) {
            Post post = posts.get(i);
            prompt.append("- ");
            if (post.getMood() != null && !post.getMood().isEmpty()) {
                prompt.append("[").append(post.getMood()).append("] ");
            }
            prompt.append(post.getText());
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                prompt.append(" (Tags: ").append(String.join(", ", post.getTags())).append(")");
            }
            prompt.append("\n");
        }

        if (posts.size() > 50) {
            prompt.append("... and ").append(posts.size() - 50).append(" more posts\n");
        }

        prompt.append("\nProvide a summary in 3-5 sentences.");

        return prompt.toString();
    }

    private String callAPI(String prompt) throws IOException, JSONException {
        URL url = new URL(API_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);

            JSONObject requestBody = buildRequestBody(prompt);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return parseResponse(connection);
            } else {
                String errorMessage = readErrorStream(connection);
                throw new IOException("API call failed with code " + responseCode + ": " + errorMessage);
            }

        } finally {
            connection.disconnect();
        }
    }

    private JSONObject buildRequestBody(String prompt) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);

        requestBody.put("messages", messages);

        return requestBody;
    }

    private String parseResponse(HttpURLConnection connection) throws IOException, JSONException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray choices = jsonResponse.getJSONArray("choices");

        if (choices.length() > 0) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            return message.getString("content").trim();
        }

        throw new JSONException("No choices in API response");
    }

    private String readErrorStream(HttpURLConnection connection) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } catch (Exception e) {
            return "Unknown error";
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}