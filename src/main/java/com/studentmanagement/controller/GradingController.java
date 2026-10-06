package com.studentmanagement.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.GradeSubmissionRequest;
import com.studentmanagement.dto.response.CourseGradesResponse;
import com.studentmanagement.dto.response.GradeSubmissionResponse;
import com.studentmanagement.dto.response.MyGradesResponse;
import com.studentmanagement.dto.response.MySubmissionsResponse;
import com.studentmanagement.service.GradingService;

@RestController
@RequestMapping("/api/v1/grading")
public class GradingController {

    private final GradingService gradingService;

    public GradingController(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    @PreAuthorize("hasAuthority('TEACHER') or hasAuthority('ADMIN')")
    @PutMapping("/submissions/{submissionId}")
    public ResponseEntity<GradeSubmissionResponse> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody GradeSubmissionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(gradingService.gradeSubmission(submissionId, request, authentication));
    }

    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping("/me/submissions")
    public ResponseEntity<MySubmissionsResponse> mySubmissions(
            @PageableDefault(page = 0, size = 20, sort = "createdAt") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(gradingService.mySubmissions(pageable, authentication));
    }

    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping("/me/grades")
    public ResponseEntity<MyGradesResponse> myGrades(
            @PageableDefault(page = 0, size = 20, sort = "gradedAt") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(gradingService.myGrades(pageable, authentication));
    }

    @PreAuthorize("hasAuthority('TEACHER') or hasAuthority('ADMIN')")
    @GetMapping("/courses/{courseId}/grades")
    public ResponseEntity<CourseGradesResponse> courseGrades(
            @PathVariable Long courseId,
            @PageableDefault(page = 0, size = 20, sort = "gradedAt") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(gradingService.courseGrades(courseId, pageable, authentication));
    }
}
