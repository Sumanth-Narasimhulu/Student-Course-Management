package com.studentmanagement.dto.response;

import java.util.List;

public class StudentsResponse {
    private List<AllStudentResponse>students;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    public List<AllStudentResponse> getStudents() {
        return students;
    }
    public void setStudents(List<AllStudentResponse> students) {
        this.students = students;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public long getTotalElements() {
        return totalElements;
    }
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    public int getTotalPages() {
        return totalPages;
    }
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
}
