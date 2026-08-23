package com.studentmanagement.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.studentmanagement.dto.response.UserMeResponse;

@Service
public class UserService {
    
    public UserMeResponse me(Authentication authentication){
        if(!authentication.isAuthenticated()){
            throw new IllegalArgumentException("user not authenticated");
        }
        UserMeResponse userMeResponse = new UserMeResponse();
        userMeResponse.setUsername(authentication.getName());
        userMeResponse.setAuthorities(authentication.getAuthorities());
        return userMeResponse;
    }
}
