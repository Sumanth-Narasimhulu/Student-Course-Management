package com.studentmanagement.specification;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import com.studentmanagement.entity.Assignment;
import com.studentmanagement.entity.Course;
import com.studentmanagement.entity.Enrollment;
import com.studentmanagement.entity.Student;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class AssignmentSpecification {
    public static Specification<Assignment>getCourseAssignments(Long studentId){
        return new Specification<Assignment>() {

            @Override
            public  Predicate toPredicate(Root<Assignment> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                
                        Join<Assignment,Course>courseJoin = root.join("course");
                        Join<Course,Enrollment>enrollmentJoin = courseJoin.join("enrollments");
                        Join<Enrollment,Student>studentJoin = enrollmentJoin.join("student");
                        query.distinct(true);
                        return criteriaBuilder.equal(studentJoin.get("id"), studentId);
            }
            
        };
    }
    public static Specification<Assignment>dueDateBetween(LocalDateTime start,LocalDateTime end){
        return new Specification<Assignment>() {

            @Override
            public @Nullable Predicate toPredicate(Root<Assignment> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("duedate"),start),
                    criteriaBuilder.lessThanOrEqualTo(
                        root.get("duedate"),end
                    )
                );
            }
            
        };
    }
}
