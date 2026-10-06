package com.studentmanagement.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.DepartmentRequest;
import com.studentmanagement.dto.response.AllDepartmentResponse;
import com.studentmanagement.dto.response.DepartmentResponse;
import com.studentmanagement.service.DepartmentService;

@RestController
@RequestMapping("/api/v1/department")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping()
    public ResponseEntity<DepartmentResponse> createDepartment(@RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.createDepartment(request));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(@RequestBody DepartmentRequest request,
            @PathVariable Long id) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/allDepartments")
    public ResponseEntity<AllDepartmentResponse> getAllDepartments(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) List<String> names,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(departmentService.getAllDepartments(ids, names, pageable));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.delete(id));
    }

}
