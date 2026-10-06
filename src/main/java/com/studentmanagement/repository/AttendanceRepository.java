package com.studentmanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studentmanagement.entity.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @EntityGraph(attributePaths = { "student" })
    List<Attendance> findByCourseIdAndAttendanceDate(Long courseId, LocalDate attendanceDate);

    @EntityGraph(attributePaths = { "course" })
    Page<Attendance> findByStudentId(Long studentId, Pageable pageable);

    @EntityGraph(attributePaths = { "course" })
    List<Attendance> findByStudentId(Long studentId);
}
