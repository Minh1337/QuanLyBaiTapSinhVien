package com.example.quanlybaitapsinhvien.model;

public class TaskWithProgress {
    private Task task;
    private TaskProgress progress;
    private String subjectOrClassName;
    private int completedChecklistCount;
    private int totalChecklistCount;

    public TaskWithProgress(Task task, TaskProgress progress, String subjectOrClassName, int completedChecklistCount, int totalChecklistCount) {
        this.task = task;
        this.progress = progress;
        this.subjectOrClassName = subjectOrClassName;
        this.completedChecklistCount = completedChecklistCount;
        this.totalChecklistCount = totalChecklistCount;
    }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public TaskProgress getProgress() { return progress; }
    public void setProgress(TaskProgress progress) { this.progress = progress; }

    public String getSubjectOrClassName() { return subjectOrClassName; }
    public void setSubjectOrClassName(String subjectOrClassName) { this.subjectOrClassName = subjectOrClassName; }

    public int getCompletedChecklistCount() { return completedChecklistCount; }
    public void setCompletedChecklistCount(int completedChecklistCount) { this.completedChecklistCount = completedChecklistCount; }

    public int getTotalChecklistCount() { return totalChecklistCount; }
    public void setTotalChecklistCount(int totalChecklistCount) { this.totalChecklistCount = totalChecklistCount; }

    public int getProgressPercentage() {
        if (totalChecklistCount == 0) {
            if (progress != null && TaskProgress.STATUS_COMPLETED.equalsIgnoreCase(progress.getStatus())) {
                return 100;
            }
            return 0;
        }
        return (int) Math.round((double) completedChecklistCount / totalChecklistCount * 100.0);
    }
}
