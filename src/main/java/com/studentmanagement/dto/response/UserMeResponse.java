package com.studentmanagement.dto.response;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;


public class UserMeResponse {
    private Long id;
    private String username;
    private Set<String>roles;
    private Set<String>permissions;
    @JsonProperty ("Profile")
    private ProfileResponse profileResponse;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public Set<String> getRoles() {
        return roles;
    }
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    public Set<String> getPermissions() {
        return permissions;
    }
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
    public ProfileResponse getProfileResponse() {
        return profileResponse;
    }
    public void setProfileResponse(ProfileResponse profileResponse) {
        this.profileResponse = profileResponse;
    }
    
}
