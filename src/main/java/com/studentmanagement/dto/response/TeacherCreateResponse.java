package com.studentmanagement.dto.response;

import java.util.List;



public class TeacherCreateResponse {
    List<TeacherResponse>teachers;

    public List<TeacherResponse> getTeacher() {
        return teachers;
    }

    public void setTeacher(List<TeacherResponse> teacher) {
        this.teachers = teacher;
    }


    
}
