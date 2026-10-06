package com.studentmanagement.dto.response;


public class ProfileResponse {
    private String type;
    private Long profileId;
    private String name;
    private String department;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Long getProfileId() {
        return profileId;
    }
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
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
    
}
