package com.studentmanagement.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.CourseRequest;
import com.studentmanagement.dto.request.EnrolledCourseSearchRequest;
import com.studentmanagement.dto.request.TeacherCreateRequest;
import com.studentmanagement.dto.request.TeacherUpdateByAdminRequest;
import com.studentmanagement.dto.response.CourseCreateResponse;
import com.studentmanagement.dto.response.EnrolledCourseResponse;
import com.studentmanagement.dto.response.TeacherCreateResponse;
import com.studentmanagement.dto.response.TeacherUpdateByAdminResponse;
import com.studentmanagement.service.AdminService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // admin access
    @PreAuthorize ("hasAuthority('ADMIN')")
    @PostMapping("/teachers")
    public ResponseEntity<TeacherCreateResponse> createTeachers(@RequestBody List<TeacherCreateRequest> request) {
        return ResponseEntity.ok(adminService.createTeacher(request));
    }

    // admin access
    @PreAuthorize ("hasAuthority('ADMIN')")
    @DeleteMapping("teacher/{id}")
    public ResponseEntity<String> deleteTeacherById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deleteTeacherById(id));

    }

    // admin access
    @PreAuthorize ("hasAuthority('ADMIN')")
    @PostMapping("/createCourse")
    public ResponseEntity<CourseCreateResponse> createCourseByAdmin(@RequestBody CourseRequest request) {
        return ResponseEntity.ok(adminService.createCourseByAdmin(request));
    }

    // get courses for a student both admin and teacher can access
    @PreAuthorize ("hasAuthority('ADMIN') or hasAuthority('TEACHER')")
    @PostMapping("{studentId}/enrolledCourses")
    public ResponseEntity<EnrolledCourseResponse> getCoursesOfStudent(@PathVariable Long studentId,
            @RequestBody(required = false) EnrolledCourseSearchRequest request,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminService.getCoursesOfStudent(studentId, request, pageable));
    }

    // admin role required
    @PreAuthorize ("hasAuthority('ADMIN')")
    @PatchMapping("/teachers/{teacherId}")
    public ResponseEntity<TeacherUpdateByAdminResponse> teacherUpdateByAdmin(@PathVariable Long teacherId,
            @RequestBody TeacherUpdateByAdminRequest request) {
        return ResponseEntity.ok(adminService.teacherUpdateByAdmin(teacherId, request));
    }

}
