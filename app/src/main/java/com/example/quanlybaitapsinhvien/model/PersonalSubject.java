package com.example.quanlybaitapsinhvien.model;

public class PersonalSubject {
    private long id;
    private String subjectCode;
    private String subjectName;
    private long studentId;

    public PersonalSubject() {}

    public PersonalSubject(long id, String subjectCode, String subjectName, long studentId) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.studentId = studentId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    @Override
    public String toString() {
        return subjectCode + " - " + subjectName;
    }
}
