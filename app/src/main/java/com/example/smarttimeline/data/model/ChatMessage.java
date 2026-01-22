package com.example.smarttimeline.data.model;

public class ChatMessage {

    public enum MessageType {
        USER,
        AI,
        SYSTEM
    }

    private String id;
    private String message;
    private MessageType type;
    private long timestamp;
    private boolean isTyping;

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
        this.id = String.valueOf(timestamp);
    }

    public ChatMessage(String message, MessageType type) {
        this();
        this.message = message;
        this.type = type;
        this.isTyping = false;
    }

    public static ChatMessage createUserMessage(String message) {
        return new ChatMessage(message, MessageType.USER);
    }

    public static ChatMessage createAIMessage(String message) {
        return new ChatMessage(message, MessageType.AI);
    }

    public static ChatMessage createTypingIndicator() {
        ChatMessage msg = new ChatMessage("", MessageType.AI);
        msg.setTyping(true);
        return msg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public boolean isUser() {
        return type == MessageType.USER;
    }

    public boolean isAI() {
        return type == MessageType.AI;
    }
}