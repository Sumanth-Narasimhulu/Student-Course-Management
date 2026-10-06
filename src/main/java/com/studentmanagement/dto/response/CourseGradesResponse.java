package com.studentmanagement.dto.response;

import java.util.ArrayList;
import java.util.List;

public class CourseGradesResponse {
    private Long courseId;
    private String courseName;
    private List<CourseGradesResponseInner> grades = new ArrayList<>();
    private Long page;
    private Long size;
    private Long totalPages;
    private Long totalElements;
    private Long totalNumberOfElements;

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

    public List<CourseGradesResponseInner> getGrades() {
        return grades;
    }

    public void setGrades(List<CourseGradesResponseInner> grades) {
        this.grades = grades;
    }

    public Long getPage() {
        return page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Long getTotalNumberOfElements() {
        return totalNumberOfElements;
    }

    public void setTotalNumberOfElements(Long totalNumberOfElements) {
        this.totalNumberOfElements = totalNumberOfElements;
    }
}
