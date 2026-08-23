package com.studentmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.response.UserMeResponse;
import com.studentmanagement.service.UserService;
@RestController
@RequestMapping("/users")

public class UserController {
    private UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }
    
    
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse>me(Authentication authentication){
        return ResponseEntity.ok(userService.me(authentication));

    }
}
