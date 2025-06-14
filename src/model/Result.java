package model;

public class Result {
    private int id;
    private int studentId;
    private int examId;
    private int score;
    private String takenAt;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getTakenAt() { return takenAt; }
    public void setTakenAt(String takenAt) { this.takenAt = takenAt; }
}
