package com.studentmanagement.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.dto.request.GradeSubmissionRequest;
import com.studentmanagement.dto.response.CourseGradesResponse;
import com.studentmanagement.dto.response.CourseGradesResponseInner;
import com.studentmanagement.dto.response.GradeSubmissionResponse;
import com.studentmanagement.dto.response.MyGradesResponse;
import com.studentmanagement.dto.response.MyGradesResponseInner;
import com.studentmanagement.dto.response.MySubmissionsResponse;
import com.studentmanagement.dto.response.MySubmissionsResponseInner;
import com.studentmanagement.entity.Assignment;
import com.studentmanagement.entity.AssignmentSubmission;
import com.studentmanagement.entity.AssignmentSubmissionStatus;
import com.studentmanagement.entity.Course;
import com.studentmanagement.entity.FileAsset;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.AccessDeniedException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.AssignmentSubmissionRepository;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.UserRepository;

@Service
public class GradingService {

    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public GradingService(
            AssignmentSubmissionRepository assignmentSubmissionRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GradeSubmissionResponse gradeSubmission(
            Long submissionId,
            GradeSubmissionRequest request,
            Authentication authentication) {

        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId).orElseThrow(
                () -> new ResourceNotFoundException("submission not found " + submissionId));

        Assignment assignment = submission.getAssignment();
        Course course = assignment.getCourse();

        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(
                () -> new ResourceNotFoundException("user not found " + authentication.getName()));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
        Teacher teacher = user.getTeacher();
        boolean ownsCourse = teacher != null
                && course.getTeacher() != null
                && teacher.getId().equals(course.getTeacher().getId());

        if (!isAdmin && !ownsCourse) {
            throw new AccessDeniedException("you are not the teacher of this assignment's course");
        }
        if (!submission.getStatus().equals(AssignmentSubmissionStatus.SUBMITTED)) {
            throw new AccessDeniedException("this submission hasn't been submitted yet");
        }
        if (request.getMarks() == null) {
            throw new ResourceNotFoundException("marks are required");
        }
        if (request.getMarks() < 0 || request.getMarks() > assignment.getMaxMarks()) {
            throw new AccessDeniedException(
                    "marks must be between 0 and " + assignment.getMaxMarks());
        }

        submission.setMarks(request.getMarks());
        submission.setFeedback(request.getFeedback());
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(teacher);
        AssignmentSubmission saved = assignmentSubmissionRepository.save(submission);

        GradeSubmissionResponse response = new GradeSubmissionResponse();
        response.setSubmissionId(saved.getId());
        response.setStudentId(saved.getStudent().getId());
        response.setStudentName(saved.getStudent().getName());
        response.setAssignmentId(assignment.getId());
        response.setAssignmentTitle(assignment.getTitle());
        response.setMarks(saved.getMarks());
        response.setMaxMarks(assignment.getMaxMarks());
        response.setFeedback(saved.getFeedback());
        response.setGradedBy(teacher == null ? user.getUserName() : teacher.getName());
        response.setGradedAt(saved.getGradedAt());
        return response;
    }

    @Transactional(readOnly = true)
    public MySubmissionsResponse mySubmissions(Pageable pageable, Authentication authentication) {
        Student student = currentStudent(authentication);
        Page<AssignmentSubmission> page = assignmentSubmissionRepository
                .findByStudentId(student.getId(), pageable);

        List<MySubmissionsResponseInner> rows = new ArrayList<>();
        for (AssignmentSubmission submission : page.getContent()) {
            Assignment assignment = submission.getAssignment();
            MySubmissionsResponseInner row = new MySubmissionsResponseInner();
            row.setSubmissionId(submission.getId());
            row.setAssignmentId(assignment.getId());
            row.setAssignmentTitle(assignment.getTitle());
            row.setCourseId(assignment.getCourse().getId());
            row.setCourseName(assignment.getCourse().getName());
            row.setStatus(submission.getStatus());
            row.setSubmittedAt(submission.getSubmittedAt());
            row.setDueDate(assignment.getDuedate());
            row.setMarks(submission.getMarks());
            row.setMaxMarks(assignment.getMaxMarks());
            row.setFeedback(submission.getFeedback());
            FileAsset fileAsset = submission.getFileAsset();
            row.setHasFile(fileAsset != null);
            row.setFileName(fileAsset == null ? null : fileAsset.getOriginalFileName());
            rows.add(row);
        }

        MySubmissionsResponse response = new MySubmissionsResponse();
        response.setSubmissions(rows);
        response.setPage(Long.valueOf(page.getNumber()));
        response.setSize(Long.valueOf(page.getSize()));
        response.setTotalPages(Long.valueOf(page.getTotalPages()));
        response.setTotalElements(page.getTotalElements());
        response.setTotalNumberOfElements(Long.valueOf(page.getNumberOfElements()));
        return response;
    }

    @Transactional(readOnly = true)
    public MyGradesResponse myGrades(Pageable pageable, Authentication authentication) {
        Student student = currentStudent(authentication);
        Page<AssignmentSubmission> page = assignmentSubmissionRepository
                .findByStudentIdAndMarksIsNotNull(student.getId(), pageable);

        List<MyGradesResponseInner> rows = new ArrayList<>();
        long scored = 0;
        long possible = 0;
        for (AssignmentSubmission submission : page.getContent()) {
            Assignment assignment = submission.getAssignment();
            MyGradesResponseInner row = new MyGradesResponseInner();
            row.setSubmissionId(submission.getId());
            row.setCourseId(assignment.getCourse().getId());
            row.setCourse(assignment.getCourse().getName());
            row.setAssignmentId(assignment.getId());
            row.setAssignment(assignment.getTitle());
            row.setMarks(submission.getMarks());
            row.setMaxMarks(assignment.getMaxMarks());
            row.setFeedback(submission.getFeedback());
            row.setGradedAt(submission.getGradedAt());
            rows.add(row);
            scored += submission.getMarks();
            possible += assignment.getMaxMarks();
        }

        MyGradesResponse response = new MyGradesResponse();
        response.setGrades(rows);
        response.setOverallPercentage(possible == 0 ? null : round2(scored * 100.0 / possible));
        response.setPage(Long.valueOf(page.getNumber()));
        response.setSize(Long.valueOf(page.getSize()));
        response.setTotalPages(Long.valueOf(page.getTotalPages()));
        response.setTotalElements(page.getTotalElements());
        response.setTotalNumberOfElements(Long.valueOf(page.getNumberOfElements()));
        return response;
    }

    @Transactional(readOnly = true)
    public CourseGradesResponse courseGrades(Long courseId, Pageable pageable, Authentication authentication) {
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("course not found " + courseId));

        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(
                () -> new ResourceNotFoundException("user not found " + authentication.getName()));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
        Teacher teacher = user.getTeacher();
        boolean ownsCourse = teacher != null
                && course.getTeacher() != null
                && teacher.getId().equals(course.getTeacher().getId());

        if (!isAdmin && !ownsCourse) {
            throw new AccessDeniedException("you are not the teacher of this course");
        }

        Page<AssignmentSubmission> page = assignmentSubmissionRepository
                .findByAssignmentCourseIdAndMarksIsNotNull(courseId, pageable);

        List<CourseGradesResponseInner> rows = new ArrayList<>();
        for (AssignmentSubmission submission : page.getContent()) {
            Assignment assignment = submission.getAssignment();
            CourseGradesResponseInner row = new CourseGradesResponseInner();
            row.setSubmissionId(submission.getId());
            row.setStudentId(submission.getStudent().getId());
            row.setStudentName(submission.getStudent().getName());
            row.setAssignmentId(assignment.getId());
            row.setAssignment(assignment.getTitle());
            row.setMarks(submission.getMarks());
            row.setMaxMarks(assignment.getMaxMarks());
            row.setFeedback(submission.getFeedback());
            row.setGradedAt(submission.getGradedAt());
            rows.add(row);
        }

        CourseGradesResponse response = new CourseGradesResponse();
        response.setCourseId(course.getId());
        response.setCourseName(course.getName());
        response.setGrades(rows);
        response.setPage(Long.valueOf(page.getNumber()));
        response.setSize(Long.valueOf(page.getSize()));
        response.setTotalPages(Long.valueOf(page.getTotalPages()));
        response.setTotalElements(page.getTotalElements());
        response.setTotalNumberOfElements(Long.valueOf(page.getNumberOfElements()));
        return response;
    }

    private Student currentStudent(Authentication authentication) {
        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(
                () -> new ResourceNotFoundException("user not found " + authentication.getName()));
        Student student = user.getStudent();
        if (student == null) {
            throw new ResourceNotFoundException("no student profile for " + authentication.getName());
        }
        return student;
    }

    private Double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
