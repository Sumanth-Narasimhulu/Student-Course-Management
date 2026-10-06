package com.studentmanagement.dto.response;

import java.util.List;



public class TeacherResponse {
    private Long id;
    private String name;
    private String department;
    private String username;
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    private List<TeacherCourseResponse>courses;
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
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public List<TeacherCourseResponse> getCourses() {
        return courses;
    }
    public void setCourses(List<TeacherCourseResponse> courses) {
        this.courses = courses;
    }
    


}
