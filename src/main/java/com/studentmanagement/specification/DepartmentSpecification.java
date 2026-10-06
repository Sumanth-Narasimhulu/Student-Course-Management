package com.studentmanagement.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.studentmanagement.entity.Department;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class DepartmentSpecification {

    public static Specification<Department> hasIdsOrHasNames(List<Long> ids, List<String> names) {
        return new Specification<Department>() {

            @Override
            public Predicate toPredicate(Root<Department> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {

                Predicate notDeleted = criteriaBuilder.equal(
                        root.get("isDeleted"),
                        false);

                boolean hasIds = ids != null && !ids.isEmpty();
                boolean hasNames = names != null && !names.isEmpty();

                if (!hasIds && !hasNames) {
                    return notDeleted;
                }

                List<Predicate> any = new ArrayList<>();
                if (hasIds) {
                    any.add(root.get("id").in(ids));
                }
                if (hasNames) {
                    List<String> lowerNames = new ArrayList<>();
                    for (String ele : names)
                        lowerNames.add(ele.toLowerCase());
                    any.add(criteriaBuilder.lower(root.get("name")).in(lowerNames));
                }

                return criteriaBuilder.and(
                        notDeleted,
                        criteriaBuilder.or(any.toArray(new Predicate[0])));
            }

        };
    }
}
