package com.studentmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.studentmanagement.entity.AssignmentSubmission;
import com.studentmanagement.entity.AssignmentSubmissionStatus;
import com.studentmanagement.projection.CourseSubmissionCount;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    Optional<AssignmentSubmission> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    @Query("select s.assignment.course.id as courseId,count(s) as count from AssignmentSubmission s where s.assignment.course.id in :courseIds and s.status = :status group by s.assignment.course.id")
    List<CourseSubmissionCount> countSubmittedByCourseIds(List<Long> courseIds, AssignmentSubmissionStatus status);

    List<AssignmentSubmission> findByStudentIdAndAssignmentIdIn(Long studentId, List<Long> assignmentIds);

    Page<AssignmentSubmission> findByAssignmentIdAndStatus(
            Long assignmentId,
            AssignmentSubmissionStatus status,
            Pageable pageable);

    long countByAssignmentIdAndStatus(Long assignmentId, AssignmentSubmissionStatus status);

    @EntityGraph(attributePaths = { "assignment", "assignment.course", "fileAsset" })
    Page<AssignmentSubmission> findByStudentId(Long studentId, Pageable pageable);

    @EntityGraph(attributePaths = { "assignment", "assignment.course" })
    Page<AssignmentSubmission> findByStudentIdAndMarksIsNotNull(Long studentId, Pageable pageable);

    @EntityGraph(attributePaths = { "assignment", "student" })
    Page<AssignmentSubmission> findByAssignmentCourseIdAndMarksIsNotNull(Long courseId, Pageable pageable);

}
