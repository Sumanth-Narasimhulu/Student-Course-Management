package com.studentmanagement.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;

@Repository 
public interface TeacherRepository extends JpaRepository<Teacher,Long>,JpaSpecificationExecutor<Teacher> {

    @EntityGraph (attributePaths = {"courses","department","user"})
    Optional<Teacher>findById(Long id);

    @EntityGraph (attributePaths = "department")
    Page<Teacher> findAllByIsDeleted(Pageable pageable,Boolean isDeleted);

    @EntityGraph(attributePaths = {"user","department","courses"})
    Page<Teacher>findAll(Specification<Teacher>specification,Pageable pageable);


    @EntityGraph (attributePaths = {"department"})
    Optional<Teacher>findByUser(@Param("user") User user);



    

    
}
