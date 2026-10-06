package com.studentmanagement.dto.response;

public class CoursesTaughtByTeacherInnerResponse {
    private Long id;
    private String name;
    private String description;
    private Long enrolledCount;
    private Long assignmentCount;
    private Long pendingSubmissions;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Long getEnrolledCount() {
        return enrolledCount;
    }
    public void setEnrolledCount(Long enrolledCount) {
        this.enrolledCount = enrolledCount;
    }
    public Long getAssignmentCount() {
        return assignmentCount;
    }
    public void setAssignmentCount(Long assignmentCount) {
        this.assignmentCount = assignmentCount;
    }
    public Long getPendingSubmissions() {
        return pendingSubmissions;
    }
    public void setPendingSubmissions(Long pendingSubmissions) {
        this.pendingSubmissions = pendingSubmissions;
    }
    
}
