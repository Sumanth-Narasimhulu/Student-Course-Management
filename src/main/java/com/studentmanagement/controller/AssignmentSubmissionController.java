package com.studentmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.AssignmentSubmissionUploadRequest;
import com.studentmanagement.dto.response.AssignmentSubmissionUploadResponse;
import com.studentmanagement.dto.response.FileAccessResponse;
import com.studentmanagement.dto.response.AssignmentSubmissionsResponse;
import com.studentmanagement.entity.AssignmentSubmissionStatus;
import com.studentmanagement.service.AssignmentSubmissionService;

@RestController
@RequestMapping("api/v1/assignment")
public class AssignmentSubmissionController {
    private final AssignmentSubmissionService assignmentSubmissionService;

    public AssignmentSubmissionController(AssignmentSubmissionService assignmentSubmissionService) {
        this.assignmentSubmissionService = assignmentSubmissionService;
    }

    @PreAuthorize("hasAuthority('STUDENT')")
    @PostMapping("/{assignmentId}/initiateUpload")
    public ResponseEntity<AssignmentSubmissionUploadResponse> initiateAssignmentSubmissionUpload(
            @PathVariable Long assignmentId, @RequestBody AssignmentSubmissionUploadRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(assignmentSubmissionService.initiateSubmission(assignmentId, request, authentication));
    }

    @PreAuthorize("hasAuthority('STUDENT')")
    @PostMapping("/submissions/{submissionId}/complete")
    public ResponseEntity<String> completeSubmission(@PathVariable Long submissionId, Authentication authentication) {
        return ResponseEntity
                .ok(assignmentSubmissionService.completeAssignmentSubmission(submissionId, authentication));
    }

    @PreAuthorize("hasAuthority('STUDENT') or hasAuthority('TEACHER') or hasAuthority('ADMIN')")
    @GetMapping("/submissions/{submissionId}/file")
    public ResponseEntity<FileAccessResponse> getSubmissionFile(@PathVariable Long submissionId,
            Authentication authentication) {
        return ResponseEntity.ok(assignmentSubmissionService.getAssignmentSubmission(submissionId, authentication));
    }

    @PreAuthorize("hasAuthority('TEACHER') or hasAuthority('ADMIN')")
    @GetMapping("/{assignmentId}/submissions")
    public ResponseEntity<AssignmentSubmissionsResponse> getAssignmentSubmissions(
            @PathVariable Long assignmentId,
            @RequestParam(defaultValue = "SUBMITTED") AssignmentSubmissionStatus status,
            @PageableDefault(page = 0, size = 20, sort = "submittedAt") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(assignmentSubmissionService.getAssignmentSubmissions(
                assignmentId, status, pageable, authentication));
    }
}
