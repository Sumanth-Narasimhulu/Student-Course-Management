package com.studentmanagement.dto.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CourseAttendanceResponse {
    private Long courseId;
    private String courseName;
    private LocalDate date;
    private Integer totalEnrolled;
    private Integer totalMarked;
    private List<CourseAttendanceResponseInner> records = new ArrayList<>();

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getTotalEnrolled() {
        return totalEnrolled;
    }

    public void setTotalEnrolled(Integer totalEnrolled) {
        this.totalEnrolled = totalEnrolled;
    }

    public Integer getTotalMarked() {
        return totalMarked;
    }

    public void setTotalMarked(Integer totalMarked) {
        this.totalMarked = totalMarked;
    }

    public List<CourseAttendanceResponseInner> getRecords() {
        return records;
    }

    public void setRecords(List<CourseAttendanceResponseInner> records) {
        this.records = records;
    }
}
