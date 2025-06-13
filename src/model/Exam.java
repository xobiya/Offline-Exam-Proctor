package model;

public class Exam {
    private int id;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private int durationMinutes;
    private int createdBy;
    private String entryPassword;
    private String exitPassword;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public String getEntryPassword() { return entryPassword; }
    public void setEntryPassword(String entryPassword) { this.entryPassword = entryPassword; }
    public String getExitPassword() { return exitPassword; }
    public void setExitPassword(String exitPassword) { this.exitPassword = exitPassword; }

    // Add a constructor to match usage in ExamManagementPanel
    public Exam(int id, String title, String description, String startTime, String endTime, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
    }
    // Optionally, add a constructor with all fields if needed by other usages
    public Exam(int id, String title, String description, String startTime, String endTime, int durationMinutes, int createdBy, String entryPassword, String exitPassword) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.createdBy = createdBy;
        this.entryPassword = entryPassword;
        this.exitPassword = exitPassword;
    }
}
