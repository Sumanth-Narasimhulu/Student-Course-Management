package com.studentmanagement.dto.response;

import java.util.List;

public class AllTeacherResponse {
    private List<InnerAllTeacherResponse>teachers;
    private Long totalElements;
    private Long page;
    private Long size;
    private Long totalPages;
    private Long totalNumberOfElements;
    

    public Long getTotalPages() {
        return totalPages;
    }
    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }
    public List<InnerAllTeacherResponse> getTeachers() {
        return teachers;
    }
    public void setTeachers(List<InnerAllTeacherResponse> teachers) {
        this.teachers = teachers;
    }
    public Long getTotalElements() {
        return totalElements;
    }
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
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
    public Long getTotalNumberOfElements() {
        return totalNumberOfElements;
    }
    public void setTotalNumberOfElements(Long totalNumberOfElements) {
        this.totalNumberOfElements = totalNumberOfElements;
    }
    

}
