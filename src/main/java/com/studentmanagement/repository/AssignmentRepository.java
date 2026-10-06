package com.studentmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.studentmanagement.entity.Assignment;
import com.studentmanagement.projection.AssignmentCount;

@Repository 
public interface AssignmentRepository extends JpaRepository<Assignment,Long>,JpaSpecificationExecutor<Assignment> {
    
    @Query(
        "select a.course.id as courseId,count(a) as count from Assignment a where a.course.id in:courseIds group by a.course.id"
    )
    List<AssignmentCount>countByCourseIds(List<Long>courseIds);
}
