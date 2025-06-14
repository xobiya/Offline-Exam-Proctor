package model;

public class StudentExam {
    private int id;
    private int studentId;
    private int examId;
    private String assignedAt;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }
    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }
}
