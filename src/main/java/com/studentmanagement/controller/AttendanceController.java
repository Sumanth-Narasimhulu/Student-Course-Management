package com.studentmanagement.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.MarkAttendanceRequest;
import com.studentmanagement.dto.response.CourseAttendanceResponse;
import com.studentmanagement.dto.response.MarkAttendanceResponse;
import com.studentmanagement.dto.response.MyAttendanceResponse;
import com.studentmanagement.dto.response.MyAttendanceSummaryResponse;
import com.studentmanagement.service.AttendanceService;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PreAuthorize("hasAuthority('TEACHER') or hasAuthority('ADMIN')")
    @PostMapping("/courses/{courseId}")
    public ResponseEntity<MarkAttendanceResponse> markAttendance(
            @PathVariable Long courseId,
            @RequestBody MarkAttendanceRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(attendanceService.markAttendance(courseId, request, authentication));
    }

    @PreAuthorize("hasAuthority('TEACHER') or hasAuthority('ADMIN')")
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseAttendanceResponse> courseAttendance(
            @PathVariable Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        return ResponseEntity.ok(attendanceService.courseAttendance(courseId, date, authentication));
    }

    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping("/me")
    public ResponseEntity<MyAttendanceResponse> myAttendance(
            @PageableDefault(page = 0, size = 50, sort = "attendanceDate") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(attendanceService.myAttendance(pageable, authentication));
    }

    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping("/me/summary")
    public ResponseEntity<MyAttendanceSummaryResponse> myAttendanceSummary(Authentication authentication) {
        return ResponseEntity.ok(attendanceService.myAttendanceSummary(authentication));
    }
}
