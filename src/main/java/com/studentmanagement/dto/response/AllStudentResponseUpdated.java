package com.studentmanagement.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AllStudentResponseUpdated {
    
    @JsonProperty ("students")
    private List<StudentResponse>studentResponse;
    private Long totalElements;
    private Long page;
    private Long size;
    private Long totalPages;
    private Long totalNumberOfElements;
    public List<StudentResponse> getStudentResponse() {
        return studentResponse;
    }
    public void setStudentResponse(List<StudentResponse> studentResponse) {
        this.studentResponse = studentResponse;
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
    public Long getTotalPages() {
        return totalPages;
    }
    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }
    public Long getTotalNumberOfElements() {
        return totalNumberOfElements;
    }
    public void setTotalNumberOfElements(Long totalNumberOfElements) {
        this.totalNumberOfElements = totalNumberOfElements;
    }
    
}
