package com.example.quanlybaitapsinhvien.model;

public class ClassModel {
    private long id;
    private String classCode;
    private String className;
    private long teacherId;
    private boolean isArchived;

    public ClassModel() {}

    public ClassModel(long id, String classCode, String className, long teacherId, boolean isArchived) {
        this.id = id;
        this.classCode = classCode;
        this.className = className;
        this.teacherId = teacherId;
        this.isArchived = isArchived;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public long getTeacherId() { return teacherId; }
    public void setTeacherId(long teacherId) { this.teacherId = teacherId; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }
}
