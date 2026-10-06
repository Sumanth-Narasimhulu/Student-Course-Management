package com.studentmanagement.repository;

import com.studentmanagement.entity.Student;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    Optional<Student> findByUserId(Long id);

    Page<Student> findAllByIsDeleted(Pageable pageable, boolean b);

    Optional<Student> findByIdAndIsDeleted(Long id, boolean b);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Student> findAll(Specification<Student> spec, Pageable pageable);
}
