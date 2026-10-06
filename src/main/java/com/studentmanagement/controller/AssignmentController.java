package com.studentmanagement.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.AssignmentCreateRequest;
import com.studentmanagement.dto.response.AssignmentCreateResponse;
import com.studentmanagement.dto.response.UpcomingAssignmentsResponse;
import com.studentmanagement.service.AssignmentService;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PreAuthorize ("hasAuthority('TEACHER')")
    @PostMapping("/{courseId}")
    public ResponseEntity<AssignmentCreateResponse> createAssignment(
            @PathVariable Long courseId,
            @RequestBody AssignmentCreateRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                assignmentService.createAssignment(
                        courseId,
                        request,
                        authentication
                )
        );
    }

    @PreAuthorize ("hasAuthority('TEACHER')")
    @GetMapping("/{assignmentId}")
    public ResponseEntity<AssignmentCreateResponse> getAssignment(
            @PathVariable Long assignmentId,
            Authentication authentication) {

        return ResponseEntity.ok(
                assignmentService.getAssignmentById(
                        assignmentId,
                        authentication
                )
        );
    }

    @PreAuthorize ("hasAuthority('TEACHER') or hasAuthority('ADMIN')")
    @PutMapping("/{assignmentId}")
    public ResponseEntity<AssignmentCreateResponse> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody AssignmentCreateRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                assignmentService.updateAssignment(
                        assignmentId,
                        request,
                        authentication
                )
        );
    }

    @PreAuthorize ("hasAuthority('TEACHER') or hasAuthority('ADMIN')")
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<String> deleteAssignment(
            @PathVariable Long assignmentId,
            Authentication authentication) {

        return ResponseEntity.ok(
                assignmentService.deleteAssignment(
                        assignmentId,
                        authentication
                )
        );
    }

    
    @GetMapping("/me/upcoming")
    public ResponseEntity<UpcomingAssignmentsResponse> upcomingAssignments(
            @RequestParam(defaultValue = "7") int days,
            Authentication authentication,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            ) Pageable pageable) {

        return ResponseEntity.ok(
                assignmentService.upcomingAssignments(
                        days,
                        authentication,
                        pageable
                )
        );
    }
}
