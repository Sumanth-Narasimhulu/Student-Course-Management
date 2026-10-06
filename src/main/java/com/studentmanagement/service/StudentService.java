package com.studentmanagement.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.studentmanagement.dto.request.StudentRegisterRequest;
import com.studentmanagement.dto.request.StudentSearchRequest;
import com.studentmanagement.dto.request.StudentUpdateRequest;
import com.studentmanagement.dto.response.AllStudentResponse;
import com.studentmanagement.dto.response.AllStudentResponseUpdated;
import com.studentmanagement.dto.response.StudentResponse;
import com.studentmanagement.dto.response.StudentUpdateResponse;
import com.studentmanagement.dto.response.StudentsResponse;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.AccessDeniedException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.projection.StudentEnrollmentCount;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.specification.StudentSpecification;

import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnrollmentRepository enrollmentRepository;

    public StudentService(StudentRepository studentRepository, AuthService authService, UserRepository userRepository,
            PasswordEncoder passwordEncoder, EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public StudentsResponse getAllStudents(Pageable pageable) {

        Page<Student> students = studentRepository.findAllByIsDeleted(pageable, false);
        List<AllStudentResponse> allStudentResponse = new ArrayList<>();
        for (Student student : students) {
            AllStudentResponse dto = new AllStudentResponse();
            dto.setId(student.getId());
            dto.setDegree(student.getDegree());
            dto.setName(student.getName());
            dto.setYear(student.getYear());
            allStudentResponse.add(dto);
        }
        StudentsResponse studentsResponse = new StudentsResponse();
        studentsResponse.setStudents(allStudentResponse);
        studentsResponse.setPage(students.getNumber());
        studentsResponse.setSize(students.getSize());
        studentsResponse.setTotalPages(students.getTotalPages());
        studentsResponse.setTotalElements(students.getTotalElements());
        return studentsResponse;

    }

    @Transactional
    public String createStudents(List<StudentRegisterRequest> students) {
        for (StudentRegisterRequest request : students) {
            authService.register(request);
        }
        return "Successfully created";
    }

    @Transactional
    public StudentUpdateResponse updateProfile(Authentication authentication, StudentUpdateRequest request) {
        String username = authentication.getName();
        User userEntity = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("404 " + username));
        Student studentEntity = studentRepository.findByUserId(userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("404 " + username));
        if (request.getDegree() != null) {
            studentEntity.setDegree(request.getDegree());
        }
        if (request.getDob() != null) {
            studentEntity.setDob(request.getDob());
        }
        if (request.getName() != null) {
            studentEntity.setName(request.getName());
        }
        if (request.getPassword() != null) {
            userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getYear() != null) {
            studentEntity.setYear(request.getYear());
        }
        Student savedEntity = studentRepository.save(studentEntity);
        userRepository.save(userEntity);
        StudentUpdateResponse response = new StudentUpdateResponse();
        response.setDegree(savedEntity.getDegree());
        response.setDob(savedEntity.getDob());
        response.setName(savedEntity.getName());
        response.setYear(savedEntity.getYear());
        return response;

    }

    @Transactional
    public StudentUpdateResponse updateProfileFromAdminSide(long id, StudentUpdateRequest request) {
        Student studentEntity = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("student 404 " + id));
        if (request.getDegree() != null)
            studentEntity.setDegree(request.getDegree());
        if (request.getDob() != null)
            studentEntity.setDob(request.getDob());
        if (request.getName() != null)
            studentEntity.setName(request.getName());
        if (request.getYear() != null)
            studentEntity.setYear(request.getYear());
        Student updatedEntity = studentRepository.save(studentEntity);
        StudentUpdateResponse response = new StudentUpdateResponse();
        response.setDegree(updatedEntity.getDegree());
        response.setDob(updatedEntity.getDob());
        response.setName(updatedEntity.getName());
        response.setYear(updatedEntity.getYear());
        return response;
    }

    @Transactional
    public String deleteStudent(Long id) {
        Student studentEntity = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("student 404" + id));
        studentEntity.setIsDeleted(true);
        studentRepository.save(studentEntity);
        return "successfully deleted";
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id, Authentication authentication) {
        boolean isStudent = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("STUDENT"));
        boolean isStaff = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("TEACHER")
                        || role.getAuthority().equals("ADMIN"));
        if (isStudent) {
            String username = authentication.getName();
            User user = userRepository.findByUserName(username).orElseThrow(
                    () -> new ResourceNotFoundException("username not found:" + username));
            if (user.getStudent() == null) {
                throw new ResourceNotFoundException("student not found");
            }
            if (!user.getStudent().getId().equals(id)) {
                throw new AccessDeniedException("access denied");
            }
        } else if (!isStaff) {
            throw new AccessDeniedException("access denied");
        }
        Student student = studentRepository.findByIdAndIsDeleted(id, false).orElseThrow(
                () -> new ResourceNotFoundException("student not found:" + id));
        Integer enrolledCourseCount = enrollmentRepository.countByStudentId(id);
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setName(student.getName());
        response.setDob(student.getDob());
        response.setYear(student.getYear());
        response.setUsername(student.getUser().getUserName());
        response.setEnrolledCourseCount(enrolledCourseCount);
        response.setCreatedAt(student.getCreatedAt());
        response.setDegree(student.getDegree());
        return response;

    }

    @Transactional (readOnly = true)
    public AllStudentResponseUpdated allStudentResponseAlongWithFilters(StudentSearchRequest request,Pageable pageable,Authentication authentication){

        Specification<Student>specification = StudentSpecification.isDeleted(false);
        Specification<Student>filters = null;
        if(request!=null){
        if( request.getCourseIds()!=null && !request.getCourseIds().isEmpty()){
            Specification<Student>courseIdsSpecification = StudentSpecification.hasCourseIds(request.getCourseIds());
            if(filters==null)filters = courseIdsSpecification;
            else filters=filters.and(courseIdsSpecification);
        }
        if(request.getDegrees()!=null && !request.getDegrees().isEmpty()){
            Specification<Student>degreesSpecification =StudentSpecification.hasDegrees(request.getDegrees());
            if(filters == null)filters = degreesSpecification;
            else filters = filters.and(degreesSpecification);
        }
        if(request.getNames()!=null && !request.getNames().isEmpty()){
            Specification<Student>namesSpecification = StudentSpecification.hasNames(request.getNames());
            if(filters==null)filters=namesSpecification;
            else filters = filters.and(namesSpecification);
        }
        if(request.getYears()!=null && !request.getYears().isEmpty()){
            Specification<Student>yearsSpecification = StudentSpecification.hasYears(request.getYears());
            if(filters==null)filters = yearsSpecification;
            else filters = filters.and(yearsSpecification);
        }
    }
        if(filters!=null){
            specification=specification.and(filters);
        }
        Page<Student>page = studentRepository.findAll(specification,pageable);
        List<Student>students = page.getContent();
        List<Long>studentIds = new ArrayList<>();
        for(Student student:students)studentIds.add(student.getId());
        HashMap<Long,Long>counts = new HashMap<>();
        for(StudentEnrollmentCount studentEnrollmentCount:enrollmentRepository.countByStudentIds(studentIds)){
            counts.put(studentEnrollmentCount.getStudentId(),studentEnrollmentCount.getCount());
        }
        AllStudentResponseUpdated response = new AllStudentResponseUpdated();
        List<StudentResponse>innerResponseList = new ArrayList<>();
        for(Student student:students){
            StudentResponse innerResponse = new StudentResponse();
            innerResponse.setCreatedAt(student.getCreatedAt());
            innerResponse.setDegree(student.getDegree());
            innerResponse.setDob(student.getDob());
            innerResponse.setEnrolledCourseCount(counts.getOrDefault(student.getId(),0L).intValue());
            innerResponse.setId(student.getId());
            innerResponse.setName(student.getName());
            innerResponse.setUsername(student.getUser().getUserName());
            innerResponse.setYear(student.getYear());
            innerResponseList.add(innerResponse);
            
        }
        response.setStudentResponse(innerResponseList);
         response.setPage(Long.valueOf(page.getNumber()));
        response.setSize(Long.valueOf(page.getSize()));
        response.setTotalElements(page.getTotalElements());
        response.setTotalNumberOfElements(Long.valueOf(page.getNumberOfElements()));
        response.setTotalPages(Long.valueOf(page.getTotalPages()));
        
        return response;
        
    }
    

}
