package com.studentmanagement.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.studentmanagement.dto.request.CourseRequest;
import com.studentmanagement.dto.request.EnrolledCourseSearchRequest;
import com.studentmanagement.dto.request.TeacherCreateRequest;
import com.studentmanagement.dto.request.TeacherUpdateByAdminRequest;
import com.studentmanagement.dto.response.CourseCreateResponse;
import com.studentmanagement.dto.response.CourseTeacher;
import com.studentmanagement.dto.response.DepartmentResponse;
import com.studentmanagement.dto.response.EnrolledCourseResponse;
import com.studentmanagement.dto.response.EnrolledCoursesResponseInner;
import com.studentmanagement.dto.response.TeacherCourseResponse;
import com.studentmanagement.dto.response.TeacherCreateResponse;
import com.studentmanagement.dto.response.TeacherResponse;
import com.studentmanagement.dto.response.TeacherUpdateByAdminResponse;
import com.studentmanagement.entity.Course;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Enrollment;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.ResourceExistException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.RoleRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.TeacherRepository;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.specification.EnrollmentSpecification;

import jakarta.transaction.Transactional;

@Service
public class AdminService {
    private TeacherRepository teacherRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;
    private CourseRepository courseRepository;
    private DepartmentRepository departmentRepository;
    private StudentRepository studentRepository;
    private EnrollmentRepository enrollmentRepository;

    public AdminService(TeacherRepository teacherRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder, RoleRepository roleRepository, CourseRepository courseRepository,
            DepartmentRepository departmentRepository, StudentRepository studentRepository,
            EnrollmentRepository enrollmentRepository) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public TeacherCreateResponse createTeacher(List<TeacherCreateRequest> request) {
        TeacherCreateResponse response = new TeacherCreateResponse();
        List<TeacherResponse> teacherResponse = new ArrayList<>();
        for (TeacherCreateRequest r : request) {
            if (userRepository.findByUserName(r.getUserName()).isPresent()) {
                throw new ResourceExistException("username already exists");
            }
            User user = new User();
            user.setUserName(r.getUserName());
            user.setPassword(passwordEncoder.encode(r.getPassword()));

            Department department = departmentRepository.findById(r.getDepartmentId()).orElseThrow(
                    () -> new ResourceNotFoundException(
                            "department doesn't exist with deparment id " + r.getDepartmentId()));

            Role teacherRole = roleRepository.findByName("TEACHER")
                    .orElseThrow(() -> new ResourceNotFoundException("TEACHER role not found"));

            user.getRoles().add(teacherRole);

            Teacher teacher = new Teacher();
            teacher.setDegree(r.getDegree());
            teacher.setName(r.getName());
            teacher.setPhoneNumber(r.getPhoneNumber());
            teacher.setUser(user);
            teacher.setDepartment(department);
            User savedUser = userRepository.save(user);
            Teacher savedTeacher = teacherRepository.save(teacher);
            TeacherResponse innTeacherResponse = new TeacherResponse();
            innTeacherResponse.setId(savedTeacher.getId());
            if (savedTeacher.getDepartment() != null)
                innTeacherResponse.setDepartment(savedTeacher.getDepartment().getName());
            innTeacherResponse.setName(savedTeacher.getName());
            innTeacherResponse.setUsername(savedUser.getUserName());
            List<TeacherCourseResponse> teacherCourseResponseList = new ArrayList<>();
            if (savedTeacher.getCourses() != null) {
                for (Course course : savedTeacher.getCourses()) {
                    TeacherCourseResponse teacherCourseResponse = new TeacherCourseResponse();
                    teacherCourseResponse.setId(course.getId());
                    teacherCourseResponse.setName(course.getName());
                    teacherCourseResponseList.add(teacherCourseResponse);
                }
            }
            innTeacherResponse.setCourses(teacherCourseResponseList);
            teacherResponse.add(innTeacherResponse);

        }
        response.setTeacher(teacherResponse);
        return response;

    }

    @Transactional
    public String deleteTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id " + id));

        teacher.setIsDeleted(true);
        teacherRepository.save(teacher);
        return "Successfully deleted";
    }

    // create course
    @Transactional
    public CourseCreateResponse createCourseByAdmin(CourseRequest request) {

        if (courseRepository.findByName(request.getName()).isPresent()) {
            throw new ResourceExistException("course already exists with name " + request.getName());
        }
        Teacher teacher = teacherRepository.findById(request.getTeacherId()).orElseThrow(
                () -> new ResourceNotFoundException("teacher not found with id " + request.getTeacherId()));

        Course course = new Course();
        course.setDescription(request.getDescription());
        course.setName(request.getName());
        course.setTeacher(teacher);
        Course savedCourse = courseRepository.save(course);
        CourseCreateResponse response = new CourseCreateResponse();
        CourseTeacher courseTeacher = new CourseTeacher();
        courseTeacher.setId(teacher.getId());
        courseTeacher.setName(teacher.getName());
        if (teacher.getDepartment() != null) {
            courseTeacher.setDepartment(teacher.getDepartment().getName());
        }
        response.setCourseTeacher(courseTeacher);
        response.setDescription(savedCourse.getDescription());
        response.setId(savedCourse.getId());
        response.setName(savedCourse.getName());

        return response;
    }

    public EnrolledCourseResponse getCoursesOfStudent(Long studentId, EnrolledCourseSearchRequest request,
            Pageable pageable) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("student not found with id " + studentId);
        }
        Specification<Enrollment> specification = EnrollmentSpecification.hasStudentId(studentId);
        Specification<Enrollment> filters = null;
        if (request != null) {
            List<String> courseNames = request.getCourseNames();
            List<String> teacherNames = request.getTeacherNames();
            if (courseNames != null && !courseNames.isEmpty()) {
                filters = EnrollmentSpecification.hasCourseName(courseNames);
            }
            if (teacherNames != null && !teacherNames.isEmpty()) {
                Specification<Enrollment> teacherNamesFilter = EnrollmentSpecification.hasTeacherName(teacherNames);
                if (filters == null)
                    filters = teacherNamesFilter;
                else
                    filters = filters.and(teacherNamesFilter);
            }
        }
        if (filters != null)
            specification = specification.and(filters);
        Page<Enrollment> page = enrollmentRepository.findAll(specification, pageable);
        EnrolledCourseResponse response = new EnrolledCourseResponse();
        List<EnrolledCoursesResponseInner> innerResponse = new ArrayList<>();
        for (Enrollment ele : page.getContent()) {
            EnrolledCoursesResponseInner inner = new EnrolledCoursesResponseInner();
            inner.setCourseId(ele.getCourse().getId());
            inner.setCourseName(ele.getCourse().getName());
            inner.setTeacherName(ele.getCourse().getTeacher().getName());
            innerResponse.add(inner);
        }
        response.setEnrolledCourses(innerResponse);
        response.setPage(Long.valueOf(page.getNumber()));
        response.setSize(Long.valueOf(page.getSize()));
        response.setTotalPages(Long.valueOf(page.getTotalPages()));
        response.setTotalElements(Long.valueOf(page.getTotalElements()));
        response.setTotalNumberOfElements(Long.valueOf(page.getNumberOfElements()));

        return response;

    }

    @Transactional

    public TeacherUpdateByAdminResponse teacherUpdateByAdmin(Long teacherId, TeacherUpdateByAdminRequest request) {
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(
                () -> new ResourceNotFoundException("teacher not found:" + teacherId));
        if (Boolean.TRUE.equals(teacher.getIsDeleted())) {
            throw new ResourceNotFoundException("teacher not found");
        }
        User user = teacher.getUser();

        if (request.getDegree() != null && !request.getDegree().isBlank()) {
            teacher.setDegree(request.getDegree());
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId()).orElseThrow(
                    () -> new ResourceNotFoundException("department not found " + request.getDepartmentId()));
            if (Boolean.TRUE.equals(department.getIsDeleted())) {
                throw new ResourceNotFoundException("department not found " + request.getDepartmentId());
            }
            teacher.setDepartment(department);
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            teacher.setName(request.getName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            teacher.setPhoneNumber(request.getPhoneNumber());
        }
        Teacher savedTeacher = teacherRepository.save(teacher);
        TeacherUpdateByAdminResponse response = new TeacherUpdateByAdminResponse();
        response.setDegree(savedTeacher.getDegree());
        if (savedTeacher.getDepartment() != null) {
            DepartmentResponse departmentResponse = new DepartmentResponse();
            departmentResponse.setId(savedTeacher.getDepartment().getId());
            departmentResponse.setName(savedTeacher.getDepartment().getName());
            response.setDepartment(departmentResponse);

        }
        response.setId(savedTeacher.getId());
        response.setName(savedTeacher.getName());
        response.setPhoneNumber(savedTeacher.getPhoneNumber());
        response.setUsername(user.getUserName());
        return response;
    }

}
