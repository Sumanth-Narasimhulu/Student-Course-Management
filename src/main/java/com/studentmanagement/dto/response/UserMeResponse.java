package com.studentmanagement.dto.response;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

public class UserMeResponse {
    private String username;
    private Collection<? extends GrantedAuthority> authorities;
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public Collection<? extends GrantedAuthority>  getAuthorities() {
        return authorities;
    }
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }
    

}
