package com.example.quanlybaitapsinhvien.model;

public class Task {
    public static final String SOURCE_PERSONAL = "PERSONAL";
    public static final String SOURCE_TEACHER = "TEACHER";

    public static final String TYPE_NORMAL = "Tự luận";
    public static final String TYPE_QUIZ = "Trắc nghiệm";

    private long id;
    private String title;
    private String description;
    private String dueDate; // e.g. "2025-05-20 23:59"
    private String sourceType; // PERSONAL or TEACHER
    private Long classId; // nullable
    private Long personalSubjectId; // nullable
    private long createdByUserId;
    private String attachmentPath; // Dành cho file bài tập tải về (Docx, PDF...)
    private String quizFilePath;   // Dành cho file Trắc nghiệm (.txt)
    private String taskType;
    private double passingScore = 0.0; // Mức điểm cần đạt (0.0 = Không yêu cầu)
    private boolean requiresSubmission = false; // Yêu cầu sinh viên nộp file bài làm

    public Task() {}

    public Task(long id, String title, String description, String dueDate, String sourceType, Long classId, Long personalSubjectId, long createdByUserId, String attachmentPath, String quizFilePath, String taskType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.sourceType = sourceType;
        this.classId = classId;
        this.personalSubjectId = personalSubjectId;
        this.createdByUserId = createdByUserId;
        this.attachmentPath = attachmentPath;
        this.quizFilePath = quizFilePath;
        this.taskType = taskType != null ? taskType : TYPE_NORMAL;
    }

    public Task(long id, String title, String description, String dueDate, String sourceType, Long classId, Long personalSubjectId, long createdByUserId, String attachmentPath, String quizFilePath, String taskType, double passingScore) {
        this(id, title, description, dueDate, sourceType, classId, personalSubjectId, createdByUserId, attachmentPath, quizFilePath, taskType);
        this.passingScore = passingScore;
    }

    public Task(long id, String title, String description, String dueDate, String sourceType, Long classId, Long personalSubjectId, long createdByUserId, String attachmentPath, String quizFilePath, String taskType, double passingScore, boolean requiresSubmission) {
        this(id, title, description, dueDate, sourceType, classId, personalSubjectId, createdByUserId, attachmentPath, quizFilePath, taskType, passingScore);
        this.requiresSubmission = requiresSubmission;
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

    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }

    public String getQuizFilePath() { return quizFilePath; }
    public void setQuizFilePath(String quizFilePath) { this.quizFilePath = quizFilePath; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public double getPassingScore() { return passingScore; }
    public void setPassingScore(double passingScore) { this.passingScore = passingScore; }

    public boolean isRequiresSubmission() { return requiresSubmission; }
    public void setRequiresSubmission(boolean requiresSubmission) { this.requiresSubmission = requiresSubmission; }

    public boolean isPersonal() {
        return SOURCE_PERSONAL.equalsIgnoreCase(sourceType);
    }
}
