package com.studentmanagement.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.response.EnrolledCourseResponse;
import com.studentmanagement.dto.response.EnrollmentResponse;
import com.studentmanagement.service.EnrollCourseService;

@RestController
@RequestMapping("/api/v1/enrollCourse")
public class CourseEnrollmentController {

    private final EnrollCourseService enrollCourseService;

    public CourseEnrollmentController(EnrollCourseService enrollCourseService) {
        this.enrollCourseService = enrollCourseService;
    }

    // enrollment_create,student
    @PreAuthorize("hasAuthority('STUDENT')")
    @PostMapping("{courseId}/enroll")
    public ResponseEntity<EnrollmentResponse> enrollInCourse(@PathVariable(name = "courseId") Long id,
            Authentication authentication) {
        return ResponseEntity.ok(enrollCourseService.enrollCourse(id, authentication));
    }

    // enrollment_delete
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('STUDENT')")
    @DeleteMapping("{enrollmentId}/unenroll")
    public ResponseEntity<String> unEnroll(@PathVariable(name = "enrollmentId") Long id,
            Authentication authentication) {
        return ResponseEntity.ok(enrollCourseService.unEnroll(id, authentication));
    }

    // student
    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping("/me/courses")
    public ResponseEntity<EnrolledCourseResponse> enrolledCourses(
            @RequestParam(required = false) List<String> courseNames,
            @RequestParam(required = false) List<String> teachernames, Authentication authentication,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        if (courseNames == null) {
            courseNames = new ArrayList<>();
        }
        if (teachernames == null) {
            teachernames = new ArrayList<>();
        }
        return ResponseEntity
                .ok(enrollCourseService.enrolledCourses(courseNames, teachernames, authentication, pageable));
    }

}
