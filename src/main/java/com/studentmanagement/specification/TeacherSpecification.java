package com.studentmanagement.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Component
public class TeacherSpecification {

    public static Specification<Teacher> hasNames(List<String> names) {
        return new Specification<Teacher>() {

            @Override
            public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                return root.get("name").in(names);
            }

        };
    }

    public static Specification<Teacher> hasUsernames(List<String> usernames) {

        return new Specification<Teacher>() {

            @Override
            public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                query.distinct(true);
                Join<Teacher, User> userJoin = root.join("user", JoinType.LEFT);

                return userJoin.get("userName").in(usernames);
            }

        };
    }

    public static Specification<Teacher> isDeleted(Boolean isDeleted) {
        return new Specification<Teacher>() {

            @Override
            public Predicate toPredicate(Root<Teacher> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                return criteriaBuilder.equal(root.get("isDeleted"), isDeleted);
            }

        };
    }

}
