package com.example.quanlybaitapsinhvien.model;

public class StudentReportItem {
    private long studentId;
    private String studentName;
    private String username;
    private String status;
    private int progressPercentage;
    private String submittedFilePath;

    public StudentReportItem() {}

    public StudentReportItem(long studentId, String studentName, String username, String status, int progressPercentage, String submittedFilePath) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.username = username;
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.submittedFilePath = submittedFilePath;
    }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

    public String getSubmittedFilePath() { return submittedFilePath; }
    public void setSubmittedFilePath(String submittedFilePath) { this.submittedFilePath = submittedFilePath; }
}
