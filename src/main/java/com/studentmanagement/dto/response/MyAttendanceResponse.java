package com.studentmanagement.dto.response;

import java.util.ArrayList;
import java.util.List;

public class MyAttendanceResponse {
    private List<MyAttendanceResponseInner> attendance = new ArrayList<>();
    private Long page;
    private Long size;
    private Long totalPages;
    private Long totalElements;
    private Long totalNumberOfElements;

    public List<MyAttendanceResponseInner> getAttendance() {
        return attendance;
    }

    public void setAttendance(List<MyAttendanceResponseInner> attendance) {
        this.attendance = attendance;
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
