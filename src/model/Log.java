package model;

public class Log {
    private int id;
    private int studentId;
    private int examId;
    private String activityType;
    private String activityDetail;
    private String timestamp;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public String getActivityDetail() { return activityDetail; }
    public void setActivityDetail(String activityDetail) { this.activityDetail = activityDetail; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
