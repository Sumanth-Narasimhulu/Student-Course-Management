package com.studentmanagement.specification;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import com.studentmanagement.entity.Enrollment;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class EnrollmentSpecification {
    
    public static Specification<Enrollment> hasCourseName(List<String>courseNames){
        
        return new Specification<Enrollment>() {

            @Override
            public  Predicate toPredicate(Root<Enrollment> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
               
                       List<String>lowerCourseNames = new ArrayList<>();
                       for(String ele:courseNames){
                        lowerCourseNames.add(ele.toLowerCase());
                       }
                       return criteriaBuilder.lower(root.get("course").get("name")).in(lowerCourseNames);
            }
            
        };
    }

    public static Specification<Enrollment>hasTeacherName(List<String>teacherNames){

        return  new Specification<Enrollment>() {

            @Override
            public @Nullable Predicate toPredicate(Root<Enrollment> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
               
                        List<String>lowerTeacherNames = new ArrayList<>();
                       for(String ele:teacherNames){
                        lowerTeacherNames.add(ele.toLowerCase());
                       }
                       return criteriaBuilder.lower(root.get("course").get("teacher").get("name")).in(lowerTeacherNames);
            }
            
        };
    }
    public static Specification<Enrollment>hasStudentId(Long id){
        return new Specification<Enrollment>() {

            @Override
            public @Nullable Predicate toPredicate(Root<Enrollment> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                
                        return criteriaBuilder.equal(root.get("student").get("id"), id);
            }
            
        };

    }
}
