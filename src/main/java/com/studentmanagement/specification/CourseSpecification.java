package com.studentmanagement.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.studentmanagement.entity.Course;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Component
public class CourseSpecification {

    public static Specification<Course> hasCourseName(List<String> courseNames) {

        return new Specification<Course>() {

            @Override
            public Predicate toPredicate(Root<Course> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                List<String> lowercaseCourseNames = new ArrayList<>();
                for (String ele : courseNames) {
                    lowercaseCourseNames.add(ele.toLowerCase());
                }
                return criteriaBuilder.lower(root.get("name")).in(lowercaseCourseNames);
            }

        };
    }

    public static Specification<Course> hasTeacherName(List<String> teacherNames) {
        return new Specification<Course>() {

            @Override
            public Predicate toPredicate(Root<Course> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                query.distinct(true);
                Join<Course, Teacher> teacherJoin = root.join("teacher", JoinType.LEFT);
                List<String> lowercaseTeacherNames = new ArrayList<>();
                for (String ele : teacherNames) {
                    lowercaseTeacherNames.add(ele.toLowerCase());
                }

                return criteriaBuilder.lower(teacherJoin.get("name")).in(lowercaseTeacherNames);
            }

        };

    }

    public static Specification<Course> hasUsername(List<String> usernames) {
        return new Specification<Course>() {

            @Override
            public Predicate toPredicate(Root<Course> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                query.distinct(true);
                Join<Course, Teacher> teacherJoin = root.join("teacher", JoinType.LEFT);
                Join<Teacher, User> userJoin = teacherJoin.join("user", JoinType.LEFT);
                List<String> lowercaseUsernames = new ArrayList<>();
                for (String ele : usernames) {
                    lowercaseUsernames.add(ele.toLowerCase());
                }

                return criteriaBuilder.lower(userJoin.get("userName")).in(lowercaseUsernames);
            }

        };
    }

    public static Specification<Course> isDeleted(Boolean isDeleted) {
        return new Specification<Course>() {

            @Override
            public Predicate toPredicate(Root<Course> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("isDeleted"), isDeleted);
            }

        };
    }
}
