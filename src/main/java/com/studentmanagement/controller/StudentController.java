package com.studentmanagement.controller;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.StudentRegisterRequest;
import com.studentmanagement.dto.request.StudentSearchRequest;
import com.studentmanagement.dto.request.StudentUpdateRequest;
import com.studentmanagement.dto.response.AllStudentResponseUpdated;
import com.studentmanagement.dto.response.StudentResponse;
import com.studentmanagement.dto.response.StudentUpdateResponse;
import com.studentmanagement.dto.response.StudentsResponse;
import com.studentmanagement.service.StudentService;

@RestController 
@RequestMapping ("/api/v1/students")
public class StudentController {
    private final StudentService studentService;
    public StudentController(StudentService studentService){
        this.studentService = studentService;
    }
    //teacher,admin role and student read permission
    @GetMapping ()
    public ResponseEntity<StudentsResponse>getAllStudents(@PageableDefault (
        page = 0,
        size = 10
    )Pageable pageable){
        StudentsResponse studentsResponse = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(studentsResponse);

    }
    
    //admin access
    @PreAuthorize ("hasAuthority('ADMIN')")
    @PostMapping()
    public ResponseEntity<String>createStudents(@RequestBody List<StudentRegisterRequest>request){

        return ResponseEntity.ok(studentService.createStudents(request));
    }

    //student_update authorization
    @PreAuthorize ("hasAuthority('STUDENT')")
    @PatchMapping ()
    public ResponseEntity<StudentUpdateResponse>updateProfile(Authentication authentication,@RequestBody StudentUpdateRequest request){
        StudentUpdateResponse response = studentService.updateProfile(authentication,request);
        return ResponseEntity.ok(response);
    }
    //admin access
    @PreAuthorize ("hasAuthority('ADMIN')")
    @PatchMapping ("/{id}")
    public ResponseEntity<StudentUpdateResponse>updateProfileFromAdminSide(@PathVariable long id,@RequestBody StudentUpdateRequest request){
        return ResponseEntity.ok(studentService.updateProfileFromAdminSide(id,request));
    }

    //admin access
    @PreAuthorize ("hasAuthority('ADMIN')")
    @DeleteMapping ("/{id}")
    public ResponseEntity<String>deleteStudent(@PathVariable Long id){
        return ResponseEntity.ok(studentService.deleteStudent(id));
    }

    // later will update student,admin,teacher
    
    @GetMapping ("/{id}")
    public ResponseEntity<StudentResponse>getStudentById(@PathVariable Long id,Authentication authentication){
        return ResponseEntity.ok(studentService.getStudentById(id,authentication));
    }

    //we can use this api for both get all students and for filters as well
    //teacher and admin allowed
    @PreAuthorize ("hasAuthority('ADMIN') or hasAuthority('TEACHER')")
    @PostMapping ("/search")
    public ResponseEntity<AllStudentResponseUpdated>getAllStudentsAlongWithFilters(@RequestBody (required = false)StudentSearchRequest request,Authentication authentication,Pageable pageable){
        return ResponseEntity.ok(studentService.allStudentResponseAlongWithFilters(request, pageable, authentication));
    }




    
}
