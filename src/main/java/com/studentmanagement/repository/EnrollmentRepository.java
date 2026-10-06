package com.studentmanagement.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.studentmanagement.entity.Enrollment;
import com.studentmanagement.projection.CourseEnrollementCount;
import com.studentmanagement.projection.StudentEnrollmentCount;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment> {

    @Override
    @EntityGraph(attributePaths = { "student", "course", "course.teacher" })
    Page<Enrollment> findAll(Specification<Enrollment> specification, Pageable pageable);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @EntityGraph(attributePaths = { "student" })
    List<Enrollment> findByCourseId(Long courseId);

    Integer countByStudentId(Long studentId);

    long countByCourseId(Long courseId);

    @Query("select e.student.id as studentId,count(e) as count from Enrollment e where e.student.id in:ids group by e.student.id")
    List<StudentEnrollmentCount> countByStudentIds(List<Long> ids);

    @Query("select e.course.id as courseId,count(e) as count from Enrollment e where e.course.id in :courseIds group by e.course.id")
    List<CourseEnrollementCount> countByCourseId(List<Long> courseIds);
}
