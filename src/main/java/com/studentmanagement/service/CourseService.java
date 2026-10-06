package com.studentmanagement.service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.dto.request.CourseRequest;
import com.studentmanagement.dto.response.AllCourseResponse;
import com.studentmanagement.dto.response.CourseAssignmentsInnerResponse;
import com.studentmanagement.dto.response.CourseAssignmentsResponse;
import com.studentmanagement.dto.response.CourseCreateResponse;
import com.studentmanagement.dto.response.CourseResponse;
import com.studentmanagement.dto.response.CourseStudentsResponse;
import com.studentmanagement.dto.response.CourseStudentsResponseInner;
import com.studentmanagement.dto.response.CourseTeacher;
import com.studentmanagement.dto.response.TeacherCourseResponse;
import com.studentmanagement.dto.response.TeacherResponse;
import com.studentmanagement.entity.Assignment;
import com.studentmanagement.entity.Course;
import com.studentmanagement.entity.Enrollment;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.FeildNotProvidedException;
import com.studentmanagement.exception.ResourceExistException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.projection.AssignmentCount;
import com.studentmanagement.projection.CourseEnrollementCount;
import com.studentmanagement.repository.AssignmentRepository;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.TeacherRepository;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.specification.CourseAssignmentsSpecification;
import com.studentmanagement.specification.CourseSpecification;
import com.studentmanagement.specification.CourseStudentTeacherSpecification;

import io.jsonwebtoken.lang.Arrays;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final CourseSpecification courseSpecification;
    private final CourseStudentTeacherSpecification courseStudentTeacherSpecification;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseAssignmentsSpecification courseAssignmentsSpecification;
    private final AssignmentRepository assignmentRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository,
            TeacherRepository teacherRepository, CourseSpecification courseSpecification,
            CourseStudentTeacherSpecification courseStudentTeacherSpecification,
            EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
            CourseAssignmentsSpecification courseAssignmentsSpecification, AssignmentRepository assignmentRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.courseSpecification = courseSpecification;
        this.courseStudentTeacherSpecification = courseStudentTeacherSpecification;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseAssignmentsSpecification = courseAssignmentsSpecification;
        this.assignmentRepository = assignmentRepository;
    }

    // create course
    @Transactional
    public CourseCreateResponse createCourse(CourseRequest request, Authentication authentication) {
        if (courseRepository.findByName(request.getName()).isPresent()) {
            throw new ResourceExistException("course already exist with this name " + request.getName());
        }

        String username = authentication.getName();
        System.out.println(username);
        System.out.println(authentication.getAuthorities().toString());
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("username not found " + username));

        Teacher teacher = teacherRepository.findByUser(user).orElseThrow(
                () -> new ResourceNotFoundException("teacher not found " + username));

        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setTeacher(teacher);

        Course savedCourse = courseRepository.save(course);
        CourseCreateResponse response = new CourseCreateResponse();
        response.setId(savedCourse.getId());
        response.setDescription(savedCourse.getDescription());
        response.setName(savedCourse.getName());
        CourseTeacher courseTeacher = new CourseTeacher();
        if (savedCourse.getTeacher() != null) {
            courseTeacher.setId(savedCourse.getTeacher().getId());
            if (savedCourse.getTeacher().getDepartment() != null) {
                courseTeacher.setDepartment(savedCourse.getTeacher().getDepartment().getName());
            }
            courseTeacher.setName(savedCourse.getTeacher().getName());

        }
        response.setCourseTeacher(courseTeacher);

        return response;

    }

    @Transactional(readOnly = true)
    // this can be used to get perticular course with name as well no need to build
    // another api
    public AllCourseResponse getAllCourses(List<String> courseNames, List<String> teacherNames,
            List<String> teacherUsernames, Pageable pageable) {
        Specification<Course> specification = null;

        if (courseNames != null && !courseNames.isEmpty()) {
            Specification<Course> courseNamesSpecification = courseSpecification.hasCourseName(courseNames);
            if (specification == null)
                specification = courseNamesSpecification;
            else {
                specification = specification.or(courseNamesSpecification);
            }
        }
        if (teacherNames != null && !teacherNames.isEmpty()) {
            Specification<Course> teacherNamesSpecification = courseSpecification.hasTeacherName(teacherNames);
            if (specification == null)
                specification = teacherNamesSpecification;
            else
                specification = specification.or(teacherNamesSpecification);
        }
        if (teacherUsernames != null && !teacherUsernames.isEmpty()) {
            Specification<Course> teacherUsernamesSpecification = courseSpecification.hasUsername(teacherUsernames);
            if (specification == null)
                specification = teacherUsernamesSpecification;
            else
                specification = specification.or(teacherUsernamesSpecification);
        }

        Specification<Course> mainSpecification = courseSpecification.isDeleted(false);

        if (specification != null) {
            mainSpecification = mainSpecification.and(specification);
        }

        Page<Course> allCourses = courseRepository.findAll(mainSpecification, pageable);

        List<CourseCreateResponse> innerCourses = new ArrayList<>();
        for (Course course : allCourses.getContent()) {
            CourseCreateResponse ele = new CourseCreateResponse();
            CourseTeacher courseTeacher = new CourseTeacher();
            if (course.getTeacher().getDepartment() != null)
                courseTeacher.setDepartment(course.getTeacher().getDepartment().getName());
            courseTeacher.setId(course.getTeacher().getId());
            courseTeacher.setName(course.getTeacher().getName());
            ele.setCourseTeacher(courseTeacher);
            ele.setDescription(course.getDescription());
            ele.setId(course.getId());
            ele.setName(course.getName());
            innerCourses.add(ele);
        }
        AllCourseResponse response = new AllCourseResponse();
        response.setCourse(innerCourses);
        response.setPage(Long.valueOf(allCourses.getNumber()));
        response.setSize(Long.valueOf(allCourses.getSize()));
        response.setTotalPages(Long.valueOf(allCourses.getTotalPages()));
        response.setTotalElements(Long.valueOf(allCourses.getTotalElements()));
        response.setTotalNumberOfElements(Long.valueOf(allCourses.getNumberOfElements()));

        return response;

    }

    public CourseCreateResponse updateCourse(
            CourseRequest request,
            Long id,
            Authentication authentication) throws AccessDeniedException {
        System.out.println("Username: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "course not found with id " + id));

        boolean isTeacher = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("TEACHER"));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        // TEACHER
        if (isTeacher) {

            if (course.getTeacher() == null ||
                    course.getTeacher().getUser() == null) {

                throw new AccessDeniedException(
                        "This course is not assigned to a teacher");
            }

            String courseTeacherUsername = course.getTeacher()
                    .getUser()
                    .getUserName();

            if (!authentication.getName()
                    .equals(courseTeacherUsername)) {

                throw new AccessDeniedException(
                        "You can't update this course " + id);
            }

            // Teacher can update only name and description
            if (request.getName() != null) {
                course.setName(request.getName());
            }

            if (request.getDescription() != null) {
                course.setDescription(request.getDescription());
            }
        }

        // ADMIN
        else if (isAdmin) {

            if (request.getName() != null) {
                course.setName(request.getName());
            }

            if (request.getDescription() != null) {
                course.setDescription(request.getDescription());
            }

            // Admin can change teacher
            if (request.getTeacherId() != null) {

                Teacher teacher = teacherRepository
                        .findById(request.getTeacherId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "teacher not found with id "
                                        + request.getTeacherId()));

                course.setTeacher(teacher);
            }
        }

        else {
            throw new AccessDeniedException(
                    "You don't have permission to update this course");
        }

        Course savedCourse = courseRepository.save(course);

        return buildCourseResponse(savedCourse);
    }

    private CourseCreateResponse buildCourseResponse(Course course) {

        CourseCreateResponse response = new CourseCreateResponse();

        response.setId(course.getId());
        response.setName(course.getName());
        response.setDescription(course.getDescription());

        if (course.getTeacher() != null) {

            CourseTeacher courseTeacher = new CourseTeacher();

            courseTeacher.setId(
                    course.getTeacher().getId());

            courseTeacher.setName(
                    course.getName());

            if (course.getTeacher().getDepartment() != null) {
                courseTeacher.setDepartment(
                        course.getTeacher()
                                .getDepartment()
                                .getName());
            }

            response.setCourseTeacher(courseTeacher);
        }

        return response;
    }

    public String deleteCourse(Long id, Authentication authentication) throws AccessDeniedException {

        Course course = courseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("course not found with id " + id));
        boolean isTeacher = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("TEACHER"));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (isTeacher) {
            String courseTeacherUsername = course.getTeacher()
                    .getUser()
                    .getUserName();

            if (!authentication.getName()
                    .equals(courseTeacherUsername)) {

                throw new AccessDeniedException(
                        "You can't delete this course " + id);
            } else {
                course.setIsDeleted(true);
                courseRepository.save(course);
                return "Successfully deleted";
            }
        }
        if (isAdmin) {
            course.setIsDeleted(true);
            courseRepository.save(course);
            return "Successfully deleted";
        }
        return "Something went wrong";

    }

    public CourseStudentsResponse getCourseStudents(Long id, Pageable pageable) {

        Specification<Enrollment> specification = CourseStudentTeacherSpecification.hasCourseId(id);
        Page<Enrollment> page = enrollmentRepository.findAll(specification, pageable);

        CourseStudentsResponse response = new CourseStudentsResponse();
        List<CourseStudentsResponseInner> courseStudentsResponseInnerList = new ArrayList<>();
        for (Enrollment enroll : page.getContent()) {
            CourseStudentsResponseInner courseStudentsResponseInner = new CourseStudentsResponseInner();
            if (enroll.getStudent() != null) {
                courseStudentsResponseInner.setName(enroll.getStudent().getName());

                courseStudentsResponseInner.setStudentId(enroll.getStudent().getId());

                courseStudentsResponseInnerList.add(courseStudentsResponseInner);
            }
        }
        response.setPage(Long.valueOf(page.getNumber()));
        response.setSize(Long.valueOf(page.getSize()));
        response.setTotalPages(Long.valueOf(page.getTotalPages()));
        response.setTotalElements(Long.valueOf(page.getTotalElements()));
        response.setTotalNumberOfElements(Long.valueOf(page.getNumberOfElements()));
        response.setStudents(courseStudentsResponseInnerList);
        return response;

    }

    @Transactional(readOnly = true)
    public CourseTeacher getCourseTeacher(Long id) {
        Course course = courseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("course not found with id " + id));

        CourseTeacher response = new CourseTeacher();
        Teacher teacher = course.getTeacher();
        if (teacher != null) {
            response.setId(teacher.getId());
            response.setName(teacher.getName());
            if (teacher.getDepartment() != null) {
                response.setDepartment(teacher.getDepartment().getName());
            }
        }
        return response;

    }

    // course Assignments

    public CourseAssignmentsResponse getCourseAssignments(Long id, Authentication authentication, Pageable pageable)
            throws AccessDeniedException {

        boolean isTeacher = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("TEACHER"));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));
        boolean isStudent = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("STUDENT"));

        if (isAdmin) {

            return getCourseAssignmentsHelper(id, pageable);
        }
        if (isStudent) {
            // he should be enrolled in this course
            User user = userRepository.findByUserName(authentication.getName()).get();
            Student student = studentRepository.findByUserId(user.getId()).get();
            Boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), id);
            if (!isEnrolled) {
                throw new AccessDeniedException("you haven't enrolled in this course ");
            }
        }
        if (isTeacher) {
            User user = userRepository.findByUserName(authentication.getName()).get();
            Teacher teacher = teacherRepository.findByUser(user).get();
            Course course = courseRepository.findById(id).orElseThrow(
                    () -> new ResourceNotFoundException("Course not exist with id " + id));
            Long courseTeacherId = null;
            if (course.getTeacher() == null) {
                throw new AccessDeniedException("no teacher assigned for this course");
            }
            if (course.getTeacher() != null) {
                courseTeacherId = course.getTeacher().getId();
            }
            if (courseTeacherId != null && !courseTeacherId.equals(teacher.getId())) {
                throw new AccessDeniedException("you are not the teacher of this course");
            }

        }

        return getCourseAssignmentsHelper(id, pageable);

    }

    public CourseAssignmentsResponse getCourseAssignmentsHelper(Long id, Pageable pageable) {
        Specification<Assignment> specification = courseAssignmentsSpecification.getAssignmentsOfCourseId(id);
        Page<Assignment> page = assignmentRepository.findAll(specification, pageable);
        CourseAssignmentsResponse response = new CourseAssignmentsResponse();
        List<CourseAssignmentsInnerResponse> innerList = new ArrayList<>();
        for (Assignment assignment : page.getContent()) {
            CourseAssignmentsInnerResponse inner = new CourseAssignmentsInnerResponse();
            inner.setId(assignment.getId());
            inner.setTitle(assignment.getTitle());
            inner.setDueDate(assignment.getDuedate());
            inner.setMaxMarks(assignment.getMaxMarks());
            innerList.add(inner);
        }
        response.setPage(Long.valueOf(page.getNumber()));
        response.setSize(Long.valueOf(page.getSize()));
        response.setTotalPages(Long.valueOf(page.getTotalPages()));
        response.setTotalElements(Long.valueOf(page.getTotalElements()));
        response.setTotalNumberOfElements(Long.valueOf(page.getNumberOfElements()));
        response.setAssignments(innerList);
        return response;

    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long courseId, Authentication authentication) {

        boolean isTeacher = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("TEACHER"));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));
        boolean isStudent = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("STUDENT"));

        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
                () -> new ResourceNotFoundException("username not found"));
        Teacher teacher = null;
        if (isTeacher) {
            teacher = user.getTeacher();
            if (teacher == null)
                throw new ResourceNotFoundException("teacher not found");
        }
        Student student = null;
        if (isStudent) {
            student = user.getStudent();
            if (student == null)
                throw new ResourceNotFoundException("student not found");
        }
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("course not found with id:" + courseId));
        if (Boolean.TRUE.equals(course.getIsDeleted())) {
            throw new ResourceNotFoundException("course not found with id:" + courseId);
        }
        List<AssignmentCount> assignmentCount = assignmentRepository.countByCourseIds(List.of(course.getId()));
        List<CourseEnrollementCount> courseEnrollmentCount = enrollmentRepository
                .countByCourseId(List.of(course.getId()));
        Boolean canEdit = (isAdmin
                || (teacher !=null && course.getTeacher() != null && course.getTeacher().getId().equals(teacher.getId()))) ? true : false;
        Boolean isEnrolled = student!=null && enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId()) ? true
                : false;
        CourseResponse response = new CourseResponse();
        response.setEnrolledCount(0L);
        response.setAssignmentCount(0L);
        TeacherCourseResponse teacherCourseResponse = new TeacherCourseResponse();
        response.setId(course.getId());
        response.setName(course.getName());
        response.setDescription(course.getDescription());
        if (course.getTeacher() != null) {
            teacherCourseResponse.setId(course.getTeacher().getId());
            teacherCourseResponse.setName(course.getTeacher().getName());
            if (course.getTeacher().getDepartment() != null)
                teacherCourseResponse.setDepartmentName(course.getTeacher().getDepartment().getName());

        }
        response.setTeacher(teacherCourseResponse);
        if (courseEnrollmentCount.size() > 0) {
            Long count = courseEnrollmentCount.get(0).getCount();
            response.setEnrolledCount(count);
        }
        if (assignmentCount.size() > 0) {
            Long count = assignmentCount.get(0).getCount();
            response.setAssignmentCount(count);

        }
        response.setIsEnrolled(isEnrolled);
        response.setCanEdit(canEdit);
        response.setCreatedAt(course.getCreatedAt());
        return response;

    }

}
