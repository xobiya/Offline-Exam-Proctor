package model;

public class Screenshot {
    private int id;
    private int studentId;
    private int examId;
    private String imagePath;
    private String capturedAt;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getCapturedAt() { return capturedAt; }
    public void setCapturedAt(String capturedAt) { this.capturedAt = capturedAt; }
}
