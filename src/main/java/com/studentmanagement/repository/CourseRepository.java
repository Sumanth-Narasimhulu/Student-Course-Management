package com.studentmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.studentmanagement.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    Optional<Course> findByName(String name);

    @Override
    @EntityGraph(attributePaths = { "teacher", "teacher.department" })
    Page<Course> findAll(Specification<Course> specification, Pageable pageable);

    @EntityGraph(attributePaths = { "teacher", "teacher.department", "teacher.user" })
    Optional<Course> findById(Long id);

    Page<Course> findByTeacherIdAndIsDeletedFalse(Long id, Pageable pageable);
}
