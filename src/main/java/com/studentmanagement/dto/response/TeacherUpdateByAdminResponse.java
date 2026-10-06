package com.studentmanagement.dto.response;

public class TeacherUpdateByAdminResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private String degree;
    private DepartmentResponse department;
    private String username;
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
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getDegree() {
        return degree;
    }
    public void setDegree(String degree) {
        this.degree = degree;
    }
    public DepartmentResponse getDepartment() {
        return department;
    }
    public void setDepartment(DepartmentResponse department) {
        this.department = department;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    
}
