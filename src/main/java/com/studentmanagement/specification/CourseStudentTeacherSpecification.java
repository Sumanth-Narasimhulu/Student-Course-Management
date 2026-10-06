package com.studentmanagement.specification;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.studentmanagement.entity.Course;
import com.studentmanagement.entity.Enrollment;
import com.studentmanagement.entity.Teacher;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Component 
public class CourseStudentTeacherSpecification {
    
    public static Specification<Enrollment>hasCourseId(Long id){
        return new Specification<Enrollment>() {

            @Override
            public  Predicate toPredicate(Root<Enrollment> root, CriteriaQuery<?> query,CriteriaBuilder criteriaBuilder) {
                
                return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("course").get("id"), id),
                    criteriaBuilder.equal(root.get("course").get("isDeleted"), false),
                    criteriaBuilder.equal(root.get("student").get("isDeleted"), false)

                );

            }
            
        };
    }

    public static Specification<Enrollment>hasCourseIdAndActiveTeacher(Long id){
        return new Specification<Enrollment>() {

            @Override
            public Predicate toPredicate(Root<Enrollment> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                

                    Join<Enrollment,Course>courseJoin = root.join("course");
                    Join<Course,Teacher>teacherJoin = courseJoin.join("teacher");

                   return criteriaBuilder.and(
                    
                    criteriaBuilder.equal(courseJoin.get("id"), id),
                    criteriaBuilder.equal(courseJoin.get("isDeleted"), false),
                    criteriaBuilder.equal(teacherJoin.get("isDeleted"), false)
                   );
            }
            
        };
    }
}
