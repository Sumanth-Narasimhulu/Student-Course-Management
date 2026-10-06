package com.studentmanagement.dto.request;

import java.util.List;

public class CourseSearchRequest {
    
    List<String>courseNames;
    List<String>teacherNames;
    List<String>teacherUsernames;
    public List<String> getCourseNames() {
        return courseNames;
    }
    public void setCourseNames(List<String> courseNames) {
        this.courseNames = courseNames;
    }
    public List<String> getTeacherNames() {
        return teacherNames;
    }
    public void setTeacherNames(List<String> teacherNames) {
        this.teacherNames = teacherNames;
    }
    public List<String> getTeacherUsernames() {
        return teacherUsernames;
    }
    public void setTeacherUsernames(List<String> teacherUsernames) {
        this.teacherUsernames = teacherUsernames;
    }
    
}
