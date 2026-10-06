package com.studentmanagement.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentResponse {
    
    private Long id;
    private String name;
    private LocalDate dob;
    private String degree;
    private Integer year;
    private String username;
    private Integer enrolledCourseCount;
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
    public LocalDate getDob() {
        return dob;
    }
    public void setDob(LocalDate dob) {
        this.dob = dob;
    }
    public String getDegree() {
        return degree;
    }
    public void setDegree(String degree) {
        this.degree = degree;
    }
    public Integer getYear() {
        return year;
    }
    public void setYear(Integer year) {
        this.year = year;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public Integer getEnrolledCourseCount() {
        return enrolledCourseCount;
    }
    public void setEnrolledCourseCount(Integer enrolledCourseCount) {
        this.enrolledCourseCount = enrolledCourseCount;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
}
