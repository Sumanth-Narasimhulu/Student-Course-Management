package com.studentmanagement.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.studentmanagement.entity.Enrollment;
import com.studentmanagement.entity.Student;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class StudentSpecification {
    public static Specification<Student> hasNames(List<String> names) {
        return new Specification<Student>() {

            @Override
            public Predicate toPredicate(Root<Student> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                List<String> lowerCaseNames = new ArrayList<>();
                for (String ele : names)
                    lowerCaseNames.add(ele.toLowerCase());
                return criteriaBuilder.lower(root.get("name")).in(lowerCaseNames);
            }

        };
    }

    public static Specification<Student> hasDegrees(List<String> degrees) {
        return new Specification<Student>() {

            @Override
            public Predicate toPredicate(Root<Student> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                List<String> lowerCaseDegrees = new ArrayList<>();
                for (String ele : degrees)
                    lowerCaseDegrees.add(ele.toLowerCase());
                return criteriaBuilder.lower(root.get("degree")).in(lowerCaseDegrees);
            }
        };
    }

    public static Specification<Student> hasYears(List<Integer> years) {
        return new Specification<Student>() {

            @Override
            public Predicate toPredicate(Root<Student> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                return root.get("year").in(years);
            }
        };
    }

    public static Specification<Student> hasCourseIds(List<Long> courseIds) {
        return new Specification<Student>() {

            @Override
            public Predicate toPredicate(Root<Student> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                // Must also apply to the count query, otherwise a student matching several
                // courseIds is counted once per matching enrollment row.
                if (query != null) {
                    query.distinct(true);
                }
                Join<Student, Enrollment> enrollmentJoin = root.join("enrollments");
                return enrollmentJoin.get("course").get("id").in(courseIds);
            }
        };
    }

    public static Specification<Student> isDeleted(Boolean isDeleted) {
        return new Specification<Student>() {

            @Override
            public Predicate toPredicate(Root<Student> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("isDeleted"), isDeleted);
            }

        };
    }

}
