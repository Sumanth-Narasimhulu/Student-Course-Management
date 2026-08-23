package com.studentmanagement.controller;

import com.studentmanagement.dto.request.StudentLoginRequest;
import com.studentmanagement.dto.request.StudentRegisterRequest;
import com.studentmanagement.dto.response.StudentLoginResponse;
import com.studentmanagement.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService){
        this.authService = authService;
    }
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody StudentRegisterRequest studentRegisterRequest){
        String response = authService.register(studentRegisterRequest);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/login")
    public ResponseEntity<StudentLoginResponse>login(@RequestBody StudentLoginRequest studentLoginRequest){
        StudentLoginResponse response = authService.login(studentLoginRequest);
        return ResponseEntity.ok(response);
    }

    


}
