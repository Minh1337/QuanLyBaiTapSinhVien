package com.example.quanlybaitapsinhvien.model;

public class ChecklistItem {
    private long id;
    private long progressId;
    private String content;
    private boolean isCompleted;

    public ChecklistItem() {}

    public ChecklistItem(long id, long progressId, String content, boolean isCompleted) {
        this.id = id;
        this.progressId = progressId;
        this.content = content;
        this.isCompleted = isCompleted;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getProgressId() { return progressId; }
    public void setProgressId(long progressId) { this.progressId = progressId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
