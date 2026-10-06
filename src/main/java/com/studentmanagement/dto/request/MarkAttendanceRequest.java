package com.studentmanagement.dto.request;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class MarkAttendanceRequest {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private List<AttendanceRecordRequest> records = new ArrayList<>();

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<AttendanceRecordRequest> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordRequest> records) {
        this.records = records;
    }
}
