package com.studentmanagement.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.dto.request.AssignmentCreateRequest;
import com.studentmanagement.dto.response.AssignmentCreateResponse;
import com.studentmanagement.dto.response.UpcomingAssignmentsResponse;
import com.studentmanagement.dto.response.UpcomingAssignmentsResponseInner;
import com.studentmanagement.entity.Assignment;
import com.studentmanagement.entity.AssignmentSubmission;
import com.studentmanagement.entity.AssignmentSubmissionStatus;
import com.studentmanagement.entity.Course;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.AccessDeniedException;
import com.studentmanagement.exception.FeildNotProvidedException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.AssignmentRepository;
import com.studentmanagement.repository.AssignmentSubmissionRepository;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.specification.AssignmentSpecification;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, CourseRepository courseRepository,
            UserRepository userRepository, AssignmentSubmissionRepository assignmentSubmissionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
    }

    public AssignmentCreateResponse createAssignment(Long courseId, AssignmentCreateRequest request,
            Authentication authentication) {
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("course not found with id " + courseId));
        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
                () -> new ResourceNotFoundException("user not found"));
        Teacher userTeacher = user.getTeacher();
        Teacher teacher = course.getTeacher();
        if (!userTeacher.getId().equals(teacher.getId())) {
            throw new AccessDeniedException("you are not the teacher of this course");
        }
        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setDuedate(request.getDueDate());
        assignment.setMaxMarks(request.getMaxMarks());
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());

        Assignment savedAssignment = assignmentRepository.save(assignment);
        AssignmentCreateResponse response = new AssignmentCreateResponse();
        response.setCourseId(savedAssignment.getCourse().getId());
        response.setCourseName(savedAssignment.getCourse().getName());
        response.setDescription(savedAssignment.getDescription());
        response.setDueDate(savedAssignment.getDuedate());
        response.setMaxMarks(savedAssignment.getMaxMarks());
        response.setTitle(savedAssignment.getTitle());
        response.setId(savedAssignment.getId());
        return response;
    }

    public AssignmentCreateResponse getAssignmentById(Long id, Authentication authentication) {
        Assignment savedAssignment = assignmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("assignment not found with id " + id));
        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
                () -> new ResourceNotFoundException("user not found"));
        Teacher userTeacher = user.getTeacher();
        Teacher teacher = savedAssignment.getCourse().getTeacher();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
        boolean isOwner = userTeacher != null && teacher != null
                && userTeacher.getId().equals(teacher.getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("you are not the teacher of this course");
        }
        AssignmentCreateResponse response = new AssignmentCreateResponse();
        response.setCourseId(savedAssignment.getCourse().getId());
        response.setCourseName(savedAssignment.getCourse().getName());
        response.setDescription(savedAssignment.getDescription());
        response.setDueDate(savedAssignment.getDuedate());
        response.setMaxMarks(savedAssignment.getMaxMarks());
        response.setTitle(savedAssignment.getTitle());
        return response;
    }

    @Transactional
    public AssignmentCreateResponse updateAssignment(Long id, AssignmentCreateRequest request,
            Authentication authentication) {

        Assignment assignment = assignmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("assignment not found with id " + id));
        Course course = assignment.getCourse();

        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
                () -> new ResourceNotFoundException("user not found"));
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));
        Teacher userTeacher = user.getTeacher();
        Teacher teacher = course.getTeacher();
        boolean isOwner = userTeacher != null && teacher != null
                && userTeacher.getId().equals(teacher.getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("you are not the teacher of this course");
        }

        // PUT replaces the whole assignment, so every field is required.
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new FeildNotProvidedException("title is required");
        }
        if (request.getDueDate() == null) {
            throw new FeildNotProvidedException("dueDate is required");
        }
        if (request.getMaxMarks() == null) {
            throw new FeildNotProvidedException("maxMarks is required");
        }

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDuedate(request.getDueDate());
        assignment.setMaxMarks(request.getMaxMarks());

        AssignmentCreateResponse response = new AssignmentCreateResponse();
        response.setId(assignment.getId());
        response.setCourseId(course.getId());
        response.setCourseName(course.getName());
        response.setDescription(assignment.getDescription());
        response.setDueDate(assignment.getDuedate());
        response.setMaxMarks(assignment.getMaxMarks());
        response.setTitle(assignment.getTitle());
        return response;
    }

    public String deleteAssignment(Long assignmentId, Authentication authentication) {
        Assignment savedAssignment = assignmentRepository.findById(assignmentId).orElseThrow(
                () -> new ResourceNotFoundException("assignment not found with id " + assignmentId));

        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
                () -> new ResourceNotFoundException("user not found"));
        Teacher userTeacher = user.getTeacher();
        Teacher teacher = savedAssignment.getCourse().getTeacher();
        if (!userTeacher.getId().equals(teacher.getId())) {
            throw new AccessDeniedException("you are not the teacher of this course");
        }
        assignmentRepository.deleteById(assignmentId);
        return "successfully deleted";

    }

    @Transactional(readOnly = true)
    public UpcomingAssignmentsResponse upcomingAssignments(
            int days,
            Authentication authentication,
            Pageable pageable) {

        String username = authentication.getName();

        User user = userRepository
                .findByUserName(username)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "user not found"));

        Student student = user.getStudent();

        if (student == null
                || Boolean.TRUE.equals(student.getIsDeleted())) {

            throw new ResourceNotFoundException(
                    "student not found");
        }

        if (days < 0) {
            throw new IllegalArgumentException(
                    "days cannot be negative");
        }

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime end = now.plusDays(days);

        Specification<Assignment> specification = AssignmentSpecification
                .getCourseAssignments(
                        student.getId())
                .and(
                        AssignmentSpecification
                                .dueDateBetween(
                                        now,
                                        end));

        Page<Assignment> page = assignmentRepository.findAll(
                specification,
                pageable);

        List<Long> assignmentIds = new ArrayList<>();

        for (Assignment assignment : page.getContent()) {
            assignmentIds.add(
                    assignment.getId());
        }

        List<AssignmentSubmission> assignmentSubmissions = new ArrayList<>();

        if (!assignmentIds.isEmpty()) {

            assignmentSubmissions = assignmentSubmissionRepository
                    .findByStudentIdAndAssignmentIdIn(
                            student.getId(),
                            assignmentIds);
        }

        HashMap<Long, AssignmentSubmission> submissions = new HashMap<>();

        for (AssignmentSubmission submission : assignmentSubmissions) {

            submissions.put(
                    submission.getAssignment().getId(),
                    submission);
        }

        UpcomingAssignmentsResponse response = new UpcomingAssignmentsResponse();

        List<UpcomingAssignmentsResponseInner> innerResponse = new ArrayList<>();

        for (Assignment assignment : page.getContent()) {

            UpcomingAssignmentsResponseInner inner = new UpcomingAssignmentsResponseInner();

            inner.setAssignmentId(
                    assignment.getId());

            inner.setTitle(
                    assignment.getTitle());

            inner.setDescription(
                    assignment.getDescription());

            inner.setCourseId(
                    assignment.getCourse().getId());

            inner.setCourseName(
                    assignment.getCourse().getName());

            inner.setMaxMarks(
                    assignment.getMaxMarks());

            inner.setDueDate(
                    assignment.getDuedate());

            Long remainingDays = Duration.between(
                    now,
                    assignment.getDuedate()).toDays();

            inner.setDaysRemaining(
                    remainingDays);

            AssignmentSubmission submission = submissions.get(
                    assignment.getId());

            if (submission == null) {

                inner.setSubmissionStatus(
                        AssignmentSubmissionStatus.PENDING);

                inner.setSubmittedAt(null);

            } else if (submission.getStatus() == AssignmentSubmissionStatus.SUBMITTED) {

                inner.setSubmissionStatus(
                        AssignmentSubmissionStatus.SUBMITTED);

                inner.setSubmittedAt(
                        submission.getSubmittedAt());

            } else {

                inner.setSubmissionStatus(
                        AssignmentSubmissionStatus.PENDING);

                inner.setSubmittedAt(null);
            }

            innerResponse.add(inner);
        }

        response.setAssignments(innerResponse);

        response.setPage(
                Long.valueOf(page.getNumber()));

        response.setSize(
                Long.valueOf(page.getSize()));

        response.setTotalPages(
                Long.valueOf(page.getTotalPages()));

        response.setTotalElements(
                Long.valueOf(page.getTotalElements()));

        response.setTotalNumberOfElements(
                Long.valueOf(page.getTotalElements()));

        return response;
    }
}
