package com.studentmanagement.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.TeacherSearchRequest;
import com.studentmanagement.dto.request.TeacherUpdateRequest;
import com.studentmanagement.dto.response.AllTeacherResponse;
import com.studentmanagement.dto.response.CoursesTaughtByTeacherResponse;
import com.studentmanagement.dto.response.TeacherResponse;
import com.studentmanagement.dto.response.TeacherUpdateResponse;
import com.studentmanagement.service.TeacherService;

@RestController 
@RequestMapping ("api/v1/teachers")
public class TeacherController {
    
    private final TeacherService teacherService;
    public TeacherController(TeacherService teacherService){
        this.teacherService = teacherService;
    }
    //students,teachers,admin
    @GetMapping ()
    public ResponseEntity<AllTeacherResponse> getAllTeachers(@PageableDefault (
        page = 0,
        size = 2
    )Pageable pageable){
        return ResponseEntity.ok(teacherService.getAllTeachers(pageable));
    }

    //teacher_read
    @PreAuthorize ("hasAuthority('TEACHER_READ')")
    @GetMapping ("{id}")
    public ResponseEntity<TeacherResponse>getTeacherById(@PathVariable Long id ){
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }
    //teacher_read;
     @PreAuthorize ("hasAuthority('TEACHER_READ')")
    @PostMapping ("/search")
    public ResponseEntity<AllTeacherResponse>search(@RequestBody TeacherSearchRequest request, @PageableDefault (
        page = 0,
        size = 2
    )Pageable pageable){
        return ResponseEntity.ok(teacherService.search(request.getNames(),request.getUsernames(),pageable));
    }

    //teacher role
     @PreAuthorize ("hasAuthority('TEACHER')")
    @PatchMapping ()
    public ResponseEntity<TeacherUpdateResponse>updateTeacherSelf(@RequestBody TeacherUpdateRequest request,Authentication authentication){
        return ResponseEntity.ok(teacherService.updateTeacherSelf(request,authentication));
    }

    @PreAuthorize ("hasAuthority('TEACHER')")
    @GetMapping ("/me/courses")
    public ResponseEntity<CoursesTaughtByTeacherResponse>getCourseTaughtByTeacher(Authentication authentication,
        @PageableDefault (
            page = 0,
            size = 10,
            sort = {"id"}
        )Pageable pageable){
            return ResponseEntity.ok(teacherService.courses(authentication, pageable));
        }


}
