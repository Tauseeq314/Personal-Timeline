package com.example.smarttimeline.ai;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AIChatService {

    private static final String TAG = "AIChatService";
    private static final String API_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final int TIMEOUT = 30000;
    private static final int MAX_CONTEXT_MESSAGES = 10;

    private final ExecutorService executorService;
    private String apiKey;
    private List<JSONObject> conversationHistory;

    public AIChatService() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.conversationHistory = new ArrayList<>();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public interface ChatCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void sendMessage(String userMessage, List<Post> userPosts, ChatCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API key not configured");
            return;
        }

        if (userMessage == null || userMessage.trim().isEmpty()) {
            callback.onError("Message cannot be empty");
            return;
        }

        executorService.execute(() -> {
            try {
                String response = callChatAPI(userMessage, userPosts);
                callback.onResponse(response);
            } catch (Exception e) {
                Log.e(TAG, "Error in chat", e);
                callback.onError(e.getMessage());
            }
        });
    }

    private String callChatAPI(String userMessage, List<Post> userPosts) throws IOException, JSONException {
        URL url = new URL(API_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);

            JSONObject requestBody = buildChatRequestBody(userMessage, userPosts);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = parseResponse(connection);
                updateConversationHistory(userMessage, response);
                return response;
            } else {
                String errorMessage = readErrorStream(connection);
                throw new IOException("API call failed with code " + responseCode + ": " + errorMessage);
            }

        } finally {
            connection.disconnect();
        }
    }

    private JSONObject buildChatRequestBody(String userMessage, List<Post> userPosts) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("max_tokens", 800);
        requestBody.put("temperature", 0.7);

        JSONArray messages = new JSONArray();

        // System message with context
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildSystemPrompt(userPosts));
        messages.put(systemMessage);

        // Add conversation history (limited)
        int historyStart = Math.max(0, conversationHistory.size() - MAX_CONTEXT_MESSAGES);
        for (int i = historyStart; i < conversationHistory.size(); i++) {
            messages.put(conversationHistory.get(i));
        }

        // Current user message
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.put(userMsg);

        requestBody.put("messages", messages);

        return requestBody;
    }

    private String buildSystemPrompt(List<Post> userPosts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful AI assistant for SmartTimeline, a personal journaling app. ");
        prompt.append("You help users reflect on their timeline, understand patterns, and gain insights. ");
        prompt.append("Be supportive, encouraging, and insightful. Keep responses concise and friendly.\n\n");

        if (userPosts != null && !userPosts.isEmpty()) {
            prompt.append("User's Timeline Context:\n");
            prompt.append("Total posts: ").append(userPosts.size()).append("\n");

            // Add recent posts summary
            int recentCount = Math.min(5, userPosts.size());
            prompt.append("Recent posts:\n");
            for (int i = 0; i < recentCount; i++) {
                Post post = userPosts.get(i);
                prompt.append("- ");
                if (post.getMood() != null) {
                    prompt.append("[").append(post.getMood()).append("] ");
                }
                String text = post.getText();
                if (text != null && text.length() > 100) {
                    text = text.substring(0, 97) + "...";
                }
                prompt.append(text).append("\n");
            }
        } else {
            prompt.append("The user has not created any posts yet.\n");
        }

        return prompt.toString();
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

    private void updateConversationHistory(String userMessage, String aiResponse) {
        try {
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            conversationHistory.add(userMsg);

            JSONObject aiMsg = new JSONObject();
            aiMsg.put("role", "assistant");
            aiMsg.put("content", aiResponse);
            conversationHistory.add(aiMsg);

            // Limit history size
            while (conversationHistory.size() > MAX_CONTEXT_MESSAGES * 2) {
                conversationHistory.remove(0);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error updating conversation history", e);
        }
    }

    public void clearHistory() {
        conversationHistory.clear();
    }

    public void shutdown() {
        executorService.shutdown();
    }
}