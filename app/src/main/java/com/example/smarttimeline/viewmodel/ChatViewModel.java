package com.example.smarttimeline.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarttimeline.ai.AIChatService;
import com.example.smarttimeline.ai.AIRepository;
import com.example.smarttimeline.data.entity.Post;
import com.example.smarttimeline.data.model.ChatMessage;
import com.example.smarttimeline.data.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final AIChatService chatService;
    private final AIRepository aiRepository;
    private final PostRepository postRepository;

    private final MutableLiveData<List<ChatMessage>> chatMessages;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final LiveData<List<Post>> userPosts;

    private List<ChatMessage> messageList;

    public ChatViewModel(@NonNull Application application) {
        super(application);

        chatService = new AIChatService();
        aiRepository = new AIRepository(application);
        postRepository = new PostRepository(application);

        chatMessages = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();

        messageList = new ArrayList<>();
        userPosts = postRepository.getAllPosts();

        // Load API key
        String apiKey = aiRepository.getApiKey();
        if (apiKey != null) {
            chatService.setApiKey(apiKey);
        }

        // Add welcome message
        addSystemMessage("Hello! I'm your SmartTimeline AI assistant. I can help you reflect on your timeline, understand patterns, and provide insights. How can I help you today?");
    }

    public LiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<Post>> getUserPosts() {
        return userPosts;
    }

    public void sendMessage(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        // Check API key
        if (!aiRepository.isApiKeyConfigured()) {
            errorMessage.setValue("Please configure your API key in Settings");
            return;
        }

        // Add user message
        ChatMessage userMsg = ChatMessage.createUserMessage(userMessage.trim());
        messageList.add(userMsg);
        chatMessages.setValue(new ArrayList<>(messageList));

        // Add typing indicator
        ChatMessage typingMsg = ChatMessage.createTypingIndicator();
        messageList.add(typingMsg);
        chatMessages.setValue(new ArrayList<>(messageList));

        isLoading.setValue(true);

        // Get user posts for context
        List<Post> posts = userPosts.getValue();

        // Send to AI
        chatService.sendMessage(userMessage, posts, new AIChatService.ChatCallback() {
            @Override
            public void onResponse(String response) {
                // Remove typing indicator
                messageList.remove(messageList.size() - 1);

                // Add AI response
                ChatMessage aiMsg = ChatMessage.createAIMessage(response);
                messageList.add(aiMsg);
                chatMessages.postValue(new ArrayList<>(messageList));

                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                // Remove typing indicator
                messageList.remove(messageList.size() - 1);
                chatMessages.postValue(new ArrayList<>(messageList));

                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    private void addSystemMessage(String message) {
        ChatMessage systemMsg = new ChatMessage(message, ChatMessage.MessageType.SYSTEM);
        messageList.add(systemMsg);
        chatMessages.setValue(new ArrayList<>(messageList));
    }

    public void clearChat() {
        messageList.clear();
        chatService.clearHistory();
        addSystemMessage("Chat cleared. How can I help you?");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        chatService.shutdown();
    }
}