package com.studentmanagement.specification;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.studentmanagement.entity.Assignment;
import com.studentmanagement.entity.Course;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Component
public class CourseAssignmentsSpecification {

    public static Specification<Assignment> getAssignmentsOfCourseId(Long id) {
        return new Specification<Assignment>() {

            @Override
            public Predicate toPredicate(Root<Assignment> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                Join<Assignment, Course> courseJoin = root.join("course");

                return criteriaBuilder.and(

                        criteriaBuilder.equal(courseJoin.get("id"), id),
                        criteriaBuilder.equal(courseJoin.get("isDeleted"), false));

            }

        };

    }
}
