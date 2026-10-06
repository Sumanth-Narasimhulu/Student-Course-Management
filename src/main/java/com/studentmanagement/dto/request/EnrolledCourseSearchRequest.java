package com.studentmanagement.dto.request;

import java.util.List;

public class EnrolledCourseSearchRequest {
    private List<String> courseNames;
    private List<String> teacherNames;

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
}
