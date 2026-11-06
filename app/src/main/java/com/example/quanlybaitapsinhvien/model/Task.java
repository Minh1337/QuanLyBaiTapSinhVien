package com.example.quanlybaitapsinhvien.model;

public class Task {
    public static final String SOURCE_PERSONAL = "PERSONAL";
    public static final String SOURCE_TEACHER = "TEACHER";

    private long id;
    private String title;
    private String description;
    private String dueDate; // e.g. "2025-05-20 23:59"
    private String sourceType; // PERSONAL or TEACHER
    private Long classId; // nullable
    private Long personalSubjectId; // nullable
    private long createdByUserId;

    public Task() {}

    public Task(long id, String title, String description, String dueDate, String sourceType, Long classId, Long personalSubjectId, long createdByUserId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.sourceType = sourceType;
        this.classId = classId;
        this.personalSubjectId = personalSubjectId;
        this.createdByUserId = createdByUserId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public Long getPersonalSubjectId() { return personalSubjectId; }
    public void setPersonalSubjectId(Long personalSubjectId) { this.personalSubjectId = personalSubjectId; }

    public long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(long createdByUserId) { this.createdByUserId = createdByUserId; }

    public boolean isPersonal() {
        return SOURCE_PERSONAL.equalsIgnoreCase(sourceType);
    }
}
