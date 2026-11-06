package com.example.quanlybaitapsinhvien.model;

public class TaskProgress {
    public static final String STATUS_NOT_STARTED = "Chưa làm";
    public static final String STATUS_IN_PROGRESS = "Đang làm";
    public static final String STATUS_COMPLETED = "Hoàn thành";
    public static final String STATUS_OVERDUE = "Quá hạn";

    public static final String PRIORITY_LOW = "Thấp";
    public static final String PRIORITY_MEDIUM = "Trung bình";
    public static final String PRIORITY_HIGH = "Cao";

    private long id;
    private long taskId;
    private long studentId;
    private String status; // Chưa làm, Đang làm, Hoàn thành
    private String priority; // Thấp, Trung bình, Cao
    private String reminderTime;

    public TaskProgress() {}

    public TaskProgress(long id, long taskId, long studentId, String status, String priority, String reminderTime) {
        this.id = id;
        this.taskId = taskId;
        this.studentId = studentId;
        this.status = status;
        this.priority = priority;
        this.reminderTime = reminderTime;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
}
