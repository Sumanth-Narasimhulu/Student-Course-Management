package com.studentmanagement.dto.request;

import com.studentmanagement.entity.AttendanceStatus;

public class AttendanceRecordRequest {
    private Long studentId;
    private AttendanceStatus status;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}
