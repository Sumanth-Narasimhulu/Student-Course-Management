package com.studentmanagement.dto.response;

import java.time.LocalDateTime;

import com.studentmanagement.entity.AssignmentSubmissionStatus;

public class UpcomingAssignmentsResponseInner {
    private Long assignmentId;
    private String title;
    private String description;
    private Long courseId;
    private String courseName;
    private Integer maxMarks;
    private LocalDateTime dueDate;
    private Long daysRemaining;
    private AssignmentSubmissionStatus submissionStatus;
    private LocalDateTime submittedAt;
    public Long getAssignmentId() {
        return assignmentId;
    }
    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Long getCourseId() {
        return courseId;
    }
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    public String getCourseName() {
        return courseName;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    public Integer getMaxMarks() {
        return maxMarks;
    }
    public void setMaxMarks(Integer maxMarks) {
        this.maxMarks = maxMarks;
    }
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    public Long getDaysRemaining() {
        return daysRemaining;
    }
    public void setDaysRemaining(Long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }
    public AssignmentSubmissionStatus getSubmissionStatus() {
        return submissionStatus;
    }
    public void setSubmissionStatus(AssignmentSubmissionStatus submissionStatus) {
        this.submissionStatus = submissionStatus;
    }
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
}
