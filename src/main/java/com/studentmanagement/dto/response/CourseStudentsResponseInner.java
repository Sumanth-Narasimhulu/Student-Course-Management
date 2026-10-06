package com.studentmanagement.dto.response;

public class CourseStudentsResponseInner {
    
    private Long studentId;
    private String name;
    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
}
