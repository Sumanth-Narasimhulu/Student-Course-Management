package com.studentmanagement.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.dto.request.TeacherUpdateRequest;
import com.studentmanagement.dto.response.AllTeacherResponse;
import com.studentmanagement.dto.response.CourseAssignmentsInnerResponse;
import com.studentmanagement.dto.response.CoursesTaughtByTeacherInnerResponse;
import com.studentmanagement.dto.response.CoursesTaughtByTeacherResponse;
import com.studentmanagement.dto.response.InnerAllTeacherResponse;
import com.studentmanagement.dto.response.TeacherCourseResponse;
import com.studentmanagement.dto.response.TeacherCreateResponse;
import com.studentmanagement.dto.response.TeacherResponse;
import com.studentmanagement.dto.response.TeacherUpdateResponse;
import com.studentmanagement.entity.Course;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.entity.AssignmentSubmissionStatus;
import com.studentmanagement.projection.AssignmentCount;
import com.studentmanagement.projection.CourseEnrollementCount;
import com.studentmanagement.projection.CourseSubmissionCount;
import com.studentmanagement.repository.AssignmentRepository;
import com.studentmanagement.repository.AssignmentSubmissionRepository;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.RefreshTokenRepository;
import com.studentmanagement.repository.TeacherRepository;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.specification.TeacherSpecification;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final TeacherSpecification teacherSpecification;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;

    public TeacherService(TeacherRepository teacherRepository, TeacherSpecification teacherSpecification,
            UserRepository userRepository, PasswordEncoder passwordEncoder,
            RefreshTokenRepository refreshTokenRepository, CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository, AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository assignmentSubmissionRepository) {
        this.teacherRepository = teacherRepository;
        this.teacherSpecification = teacherSpecification;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
    }

    @Transactional(readOnly = true)
    public AllTeacherResponse getAllTeachers(Pageable pageable) {
        AllTeacherResponse response = new AllTeacherResponse();
        List<InnerAllTeacherResponse> innerResponse = new ArrayList<>();
        Page<Teacher> page = teacherRepository.findAllByIsDeleted(pageable, false);
        for (Teacher teacher : page.getContent()) {
            InnerAllTeacherResponse element = new InnerAllTeacherResponse();
            element.setId(teacher.getId());
            if (teacher.getDepartment() != null)
                element.setDepartment(teacher.getDepartment().getName());
            element.setName(teacher.getName());
            innerResponse.add(element);
        }
        response.setPage(Long.valueOf(page.getNumber()));
        response.setSize(Long.valueOf(page.getSize()));
        response.setTotalElements(page.getTotalElements());
        response.setTotalNumberOfElements(Long.valueOf(page.getNumberOfElements()));
        response.setTotalPages(Long.valueOf(page.getTotalPages()));
        response.setTeachers(innerResponse);
        return response;

    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with Id " + id));
        TeacherResponse response = new TeacherResponse();
        List<TeacherCourseResponse> teacherCourseResponseList = new ArrayList<>();
        for (Course course : teacher.getCourses()) {
            TeacherCourseResponse teacherCourseResponse = new TeacherCourseResponse();
            teacherCourseResponse.setId(course.getId());
            teacherCourseResponse.setName(course.getName());
            teacherCourseResponseList.add(teacherCourseResponse);

        }

        response.setCourses(teacherCourseResponseList);
        if (teacher.getDepartment() != null)
            response.setDepartment(teacher.getDepartment().getName());
        response.setId(teacher.getId());
        response.setName(teacher.getName());
        response.setUsername(teacher.getUser().getUserName());
        return response;
    }

    @Transactional(readOnly = true)
    public AllTeacherResponse search(List<String> names, List<String> usernames, Pageable pageable) {

        Specification<Teacher> specification = null;

        if (names != null && !names.isEmpty()) {
            Specification hasNames = teacherSpecification.hasNames(names);
            if (specification == null) {
                specification = hasNames;
            } else {
                specification = specification.and(hasNames);
            }
        }
        if (usernames != null && !usernames.isEmpty()) {
            Specification hasUsernames = teacherSpecification.hasUsernames(usernames);
            if (specification == null) {
                specification = hasUsernames;
            } else {
                specification = specification.or(hasUsernames);
            }
        }

        Specification<Teacher> mainSpecification = teacherSpecification.isDeleted(false);
        if (specification != null) {
            mainSpecification = mainSpecification.and(specification);
        }

        Page<Teacher> teacher = teacherRepository.findAll(mainSpecification, pageable);

        AllTeacherResponse response = new AllTeacherResponse();
        List<InnerAllTeacherResponse> innerAllTeacherResponseList = new ArrayList<>();
        for (Teacher ele : teacher.getContent()) {
            InnerAllTeacherResponse innerAllTeacherResponse = new InnerAllTeacherResponse();
            innerAllTeacherResponse.setId(ele.getId());
            innerAllTeacherResponse.setName(ele.getName());
            if (ele.getDepartment() != null)
                innerAllTeacherResponse.setDepartment(ele.getDepartment().getName());
            innerAllTeacherResponseList.add(innerAllTeacherResponse);
        }
        response.setTeachers(innerAllTeacherResponseList);
        response.setPage(Long.valueOf(teacher.getNumber()));
        response.setSize(Long.valueOf(teacher.getSize()));
        response.setTotalPages(Long.valueOf(teacher.getTotalPages()));
        response.setTotalElements(Long.valueOf(teacher.getTotalElements()));
        response.setTotalNumberOfElements(Long.valueOf(teacher.getNumberOfElements()));

        return response;

    }

    @Transactional
    public TeacherUpdateResponse updateTeacherSelf(TeacherUpdateRequest request, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("username not found"));
        Teacher teacher = user.getTeacher();
        if (teacher == null || Boolean.TRUE.equals(teacher.getIsDeleted())) {
            throw new ResourceNotFoundException("teacher not found");
        }
        if (request.getDegree() != null && !request.getDegree().isBlank())
            teacher.setDegree(request.getDegree());
        if (request.getName() != null && !request.getName().isBlank())
            teacher.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            refreshTokenRepository.revokeRefreshToken(user.getId());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank())
            teacher.setPhoneNumber(request.getPhoneNumber());
        Teacher savedTeacher = teacherRepository.save(teacher);
        userRepository.save(user);
        TeacherUpdateResponse response = new TeacherUpdateResponse();
        response.setDegree(savedTeacher.getDegree());
        if (savedTeacher.getDepartment() != null)
            response.setDepartment(savedTeacher.getDepartment().getName());
        response.setId(savedTeacher.getId());
        response.setName(savedTeacher.getName());
        response.setPhoneNumber(savedTeacher.getPhoneNumber());
        return response;

    }

    @Transactional(readOnly = true)
    public CoursesTaughtByTeacherResponse courses(Authentication authentication, Pageable pageable) {
        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
                () -> new ResourceNotFoundException("user not found"));
        Teacher teacher = user.getTeacher();
        if (teacher == null || Boolean.TRUE.equals(teacher.getIsDeleted())) {
            throw new ResourceNotFoundException("teacher not found");
        }
        Page<Course> coursesTaughtByYou = courseRepository.findByTeacherIdAndIsDeletedFalse(teacher.getId(), pageable);
        List<Long> courseIds = new ArrayList<>();
        for (Course courses : coursesTaughtByYou.getContent())
            courseIds.add(courses.getId());

        HashMap<Long, Long> enrollmetCount = new HashMap<>();
        HashMap<Long, Long> assignmentCount = new HashMap<>();
        HashMap<Long, Long> submissionCount = new HashMap<>();
        if (!courseIds.isEmpty()) {
            for (CourseEnrollementCount courseEnrollementCount : enrollmentRepository.countByCourseId(courseIds)) {
                enrollmetCount.put(courseEnrollementCount.getCourseId(), courseEnrollementCount.getCount());
            }
            for (AssignmentCount count : assignmentRepository.countByCourseIds(courseIds)) {
                assignmentCount.put(count.getCourseId(), count.getCount());
            }
            for (CourseSubmissionCount count : assignmentSubmissionRepository.countSubmittedByCourseIds(courseIds,
                    AssignmentSubmissionStatus.SUBMITTED)) {
                submissionCount.put(count.getCourseId(), count.getCount());
            }
        }

        CoursesTaughtByTeacherResponse response = new CoursesTaughtByTeacherResponse();
        List<CoursesTaughtByTeacherInnerResponse> coursesTaughtByTeacherInnerResponsesList = new ArrayList<>();
        for (Course course : coursesTaughtByYou.getContent()) {
            CoursesTaughtByTeacherInnerResponse coursesTaughtByTeacherInnerResponse = new CoursesTaughtByTeacherInnerResponse();
            coursesTaughtByTeacherInnerResponse.setDescription(course.getDescription());
            coursesTaughtByTeacherInnerResponse.setId(course.getId());
            coursesTaughtByTeacherInnerResponse.setName(course.getName());
            long assignments = assignmentCount.getOrDefault(course.getId(), 0L);
            long enrolled = enrollmetCount.getOrDefault(course.getId(), 0L);
            long submitted = submissionCount.getOrDefault(course.getId(), 0L);
            coursesTaughtByTeacherInnerResponse.setAssignmentCount(assignments);
            coursesTaughtByTeacherInnerResponse.setEnrolledCount(enrolled);
            // submissions can outlive an unenrolment, so the difference can go negative
            coursesTaughtByTeacherInnerResponse.setPendingSubmissions(Math.max(0, enrolled * assignments - submitted));

            coursesTaughtByTeacherInnerResponsesList.add(coursesTaughtByTeacherInnerResponse);
        }
        response.setCourses(coursesTaughtByTeacherInnerResponsesList);
        response.setPage(Long.valueOf(coursesTaughtByYou.getNumber()));
        response.setSize(Long.valueOf(coursesTaughtByYou.getSize()));
        response.setTotalPages(Long.valueOf(coursesTaughtByYou.getTotalPages()));
        response.setTotalElements(Long.valueOf(coursesTaughtByYou.getTotalElements()));
        response.setTotalNumberOfElements(Long.valueOf(coursesTaughtByYou.getNumberOfElements()));

        return response;

    }

}
