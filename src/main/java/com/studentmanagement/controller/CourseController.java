package com.studentmanagement.controller;

import java.nio.file.AccessDeniedException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.CourseRequest;
import com.studentmanagement.dto.request.CourseSearchRequest;
import com.studentmanagement.dto.response.AllCourseResponse;
import com.studentmanagement.dto.response.CourseAssignmentsResponse;
import com.studentmanagement.dto.response.CourseCreateResponse;
import com.studentmanagement.dto.response.CourseResponse;
import com.studentmanagement.dto.response.CourseStudentsResponse;
import com.studentmanagement.dto.response.CourseTeacher;
import com.studentmanagement.service.CourseService;

@RestController 
@RequestMapping ("/api/v1/course")
public class CourseController {

    private final CourseService courseService;
    public CourseController(CourseService courseService){
        this.courseService = courseService;
    }

    //course_create and self relation
    @PreAuthorize ("hasAuthority('TEACHER')")
    @PostMapping()
    public ResponseEntity<CourseCreateResponse>createCourse(@RequestBody CourseRequest request,Authentication authentication){
        return ResponseEntity.ok(courseService.createCourse(request, authentication));
    }

    // get all courses
    //student/techer/admin courseView
    @PostMapping  ("/allCourses")
    public ResponseEntity<AllCourseResponse>getAllCourses( @RequestBody(required = false)  CourseSearchRequest request,@PageableDefault (
        page = 0,
        size = 4
    )Pageable pageable){
        return ResponseEntity.ok(courseService.getAllCourses(request.getCourseNames(), request.getTeacherNames(), request.getTeacherUsernames(), pageable));
    }

    //update course
    //teacher_update,admin
    @PreAuthorize ("hasAuthority('TEACHER') or hasAuthority('ADMIN')")
    @PostMapping ("/{id}")
    public ResponseEntity<CourseCreateResponse>updateCourse(@RequestBody CourseRequest request,@PathVariable Long id,Authentication authentication) throws AccessDeniedException{
        return ResponseEntity.ok(courseService.updateCourse(request, id, authentication));
    }

    //admin,course_delete
    @PreAuthorize ("hasAuthority('ADMIN')")
    @DeleteMapping ("/{id}")
    public ResponseEntity<String>deleteCourse(@PathVariable  Long id,Authentication authentication) throws AccessDeniedException{
        return ResponseEntity.ok(courseService.deleteCourse(id, authentication));
    }

    //teacher and admin (teacher also if they are teaching)
    //for student searching with or name we will utilize pagination
    @PreAuthorize ("hasAuthority('TEACHER') or hasAuthority('ADMIN')")
    @GetMapping ("/{id}/students")
    public ResponseEntity<CourseStudentsResponse>getCourseStudents(@PathVariable Long id,@PageableDefault (
        page = 0,
        size =10
    )Pageable pageable){

        return ResponseEntity.ok(courseService.getCourseStudents(id,pageable));

    }

    //course_read
    @GetMapping ("{id}/teacher")
    public ResponseEntity<CourseTeacher>getCourseTeacher(@PathVariable  Long id){
        return ResponseEntity.ok(courseService.getCourseTeacher(id));
    }

    //assignment read
    @GetMapping ("{id}/assignments")
    public ResponseEntity<CourseAssignmentsResponse>getCourseAssignments(@PathVariable Long id,Authentication authentication,@PageableDefault (
        page = 0,
        size = 10
    )Pageable pageable) throws AccessDeniedException{
        return ResponseEntity.ok(courseService.getCourseAssignments(id, authentication, pageable));
    }

    //teacher,student,admin allowed
    @GetMapping ("/{courseId}")
    public ResponseEntity<CourseResponse>getCourse(@PathVariable Long courseId,Authentication authentication){
        return ResponseEntity.ok(courseService.getCourse(courseId,authentication));
    }

    
    

    
}
