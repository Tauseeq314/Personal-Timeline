package com.example.smarttimeline.data.model;

public class AISummary {

    private String summaryText;
    private String period;
    private long generatedTimestamp;
    private int postCount;
    private String dominantMood;
    private String keyThemes;

    public AISummary() {
        this.generatedTimestamp = System.currentTimeMillis();
    }

    public AISummary(String summaryText, String period) {
        this.summaryText = summaryText;
        this.period = period;
        this.generatedTimestamp = System.currentTimeMillis();
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getGeneratedTimestamp() {
        return generatedTimestamp;
    }

    public void setGeneratedTimestamp(long generatedTimestamp) {
        this.generatedTimestamp = generatedTimestamp;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public String getDominantMood() {
        return dominantMood;
    }

    public void setDominantMood(String dominantMood) {
        this.dominantMood = dominantMood;
    }

    public String getKeyThemes() {
        return keyThemes;
    }

    public void setKeyThemes(String keyThemes) {
        this.keyThemes = keyThemes;
    }
}