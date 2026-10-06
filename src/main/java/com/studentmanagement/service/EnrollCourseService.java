package com.studentmanagement.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.dto.response.EnrolledCourseResponse;
import com.studentmanagement.dto.response.EnrolledCoursesResponseInner;
import com.studentmanagement.dto.response.EnrollmentResponse;
import com.studentmanagement.entity.Course;
import com.studentmanagement.entity.Enrollment;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.AccessDeniedException;
import com.studentmanagement.exception.ResourceExistException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.specification.EnrollmentSpecification;


@Service 
public class EnrollCourseService {
    


    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private  final UserRepository userRepository;
   // private final StudentRepository studentRepository;
    public  EnrollCourseService(
        EnrollmentRepository enrollmentRepository,CourseRepository courseRepository,
        UserRepository userRepository,StudentRepository studentRepository){
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        //this.studentRepository = studentRepository;
    }

    @Transactional 
    public EnrollmentResponse enrollCourse(Long id,Authentication authentication){

        Course course = courseRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("course not found with id "+id)
        );
        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
            ()-> new ResourceNotFoundException("user not found with username "+username)
        );

        Student student = user.getStudent();

        Enrollment enrollment = new Enrollment();
        if(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), id)){
            throw new ResourceExistException("you are already enrolled in this course ");
        }
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStudent(student);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(savedEnrollment.getId());
        response.setCourseId(savedEnrollment.getCourse().getId());
        response.setCourseName(savedEnrollment.getCourse().getName());
        response.setStudentId(savedEnrollment.getStudent().getId());
        response.setStudentName(savedEnrollment.getStudent().getName());
        response.setTeacherId(course.getTeacher().getId());
        response.setTeacherName(course.getTeacher().getName());
        return response;

        
        
    }
    @Transactional 

    public String unEnroll(Long id,Authentication authentication){
        Enrollment enrollment = enrollmentRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("Enrollment not found with id "+id)
        );
        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
            ()-> new ResourceNotFoundException("user not found with username "+username)
        );

        boolean isTeacher = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("TEACHER"));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));
        if(isAdmin){
            enrollmentRepository.deleteById(id);
            return "suceefully deleted";
        }
        else if(isTeacher){
            throw new AccessDeniedException("you can't do it");
        }

        Student student = user.getStudent();
        if(!enrollment.getStudent().getId().equals(student.getId())){
            throw new AccessDeniedException("you don't have access ");
        }
           
        enrollmentRepository.deleteById(id);
        return "successfully deleted";
        
        

    }

    //for normal contains search we can use pagination provided one for filters we can use specification one
    public EnrolledCourseResponse enrolledCourses(List<String>courseNames,List<String>teacherNames,Authentication authentication,Pageable pageable){
        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
            ()-> new ResourceNotFoundException("username not found")
        );
        Long id = user.getStudent().getId();
        Specification<Enrollment>specification = EnrollmentSpecification.hasStudentId(id);
        Specification<Enrollment>filters = null;
        if(courseNames != null || !courseNames.isEmpty()){
            Specification<Enrollment>courseNamesSpecification = EnrollmentSpecification.hasCourseName(courseNames);
            if(filters==null){
                filters = courseNamesSpecification;
            }else {
                filters = specification.or(courseNamesSpecification);
            }
        }
        if(teacherNames != null || !teacherNames.isEmpty()){
            Specification<Enrollment>teacherNamesSpecification = EnrollmentSpecification.hasTeacherName(teacherNames);
            if(filters == null)filters = teacherNamesSpecification;
            else filters = specification.or(teacherNamesSpecification);
        }
        if(filters!=null){
            specification = specification.and(filters);
        }
        Page<Enrollment>page = enrollmentRepository.findAll(specification,pageable);
        EnrolledCourseResponse response = new EnrolledCourseResponse();
        List<EnrolledCoursesResponseInner>innerResponse = new ArrayList<>();
        for(Enrollment ele:page.getContent()){
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
    

}
