package com.studentmanagement.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    @JsonProperty ("teacher")
    private TeacherCourseResponse teacher;
    private Long enrolledCount;
    private Long assignmentCount;
    private Boolean isEnrolled;
    private Boolean canEdit;
    private LocalDateTime createdAt;
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
    public Boolean getIsEnrolled() {
        return isEnrolled;
    }
    public void setIsEnrolled(Boolean isEnrolled) {
        this.isEnrolled = isEnrolled;
    }
    public Boolean getCanEdit() {
        return canEdit;
    }
    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public TeacherCourseResponse getTeacher() {
        return teacher;
    }
    public void setTeacher(TeacherCourseResponse teacher) {
        this.teacher = teacher;
    }
    
}
