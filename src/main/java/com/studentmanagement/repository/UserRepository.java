package com.studentmanagement.repository;

import com.studentmanagement.entity.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    @EntityGraph(attributePaths = {"roles","roles.permissions"})
    Optional<User> findByUserName(String userName);
}
