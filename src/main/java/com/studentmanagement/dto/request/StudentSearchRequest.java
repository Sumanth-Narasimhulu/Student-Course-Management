package com.studentmanagement.dto.request;

import java.util.List;

public class StudentSearchRequest {
   
    private List<String>names;
    private List<String>degrees;
    private List<Integer>years;
    private List<Long>courseIds;
    public List<String> getNames() {
        return names;
    }
    public void setNames(List<String> names) {
        this.names = names;
    }
    public List<String> getDegrees() {
        return degrees;
    }
    public void setDegrees(List<String> degrees) {
        this.degrees = degrees;
    }
    public List<Integer> getYears() {
        return years;
    }
    public void setYears(List<Integer> years) {
        this.years = years;
    }
    public List<Long> getCourseIds() {
        return courseIds;
    }
    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }
    
}
