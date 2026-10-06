package com.studentmanagement.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.dto.request.AttendanceRecordRequest;
import com.studentmanagement.dto.request.MarkAttendanceRequest;
import com.studentmanagement.dto.response.CourseAttendanceResponse;
import com.studentmanagement.dto.response.CourseAttendanceResponseInner;
import com.studentmanagement.dto.response.MarkAttendanceResponse;
import com.studentmanagement.dto.response.MyAttendanceResponse;
import com.studentmanagement.dto.response.MyAttendanceResponseInner;
import com.studentmanagement.dto.response.MyAttendanceSummaryResponse;
import com.studentmanagement.dto.response.MyAttendanceSummaryResponseInner;
import com.studentmanagement.entity.Attendance;
import com.studentmanagement.entity.AttendanceStatus;
import com.studentmanagement.entity.Course;
import com.studentmanagement.entity.Enrollment;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.AccessDeniedException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.AttendanceRepository;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.UserRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MarkAttendanceResponse markAttendance(
            Long courseId,
            MarkAttendanceRequest request,
            Authentication authentication) {

        Course course = requireCourseAccess(courseId, authentication);

        LocalDate date = request.getDate() == null ? LocalDate.now() : request.getDate();
        if (date.isAfter(LocalDate.now())) {
            throw new AccessDeniedException("you can't mark attendance for a future date");
        }
        if (request.getRecords() == null || request.getRecords().isEmpty()) {
            throw new ResourceNotFoundException("no attendance records supplied");
        }

        // only enrolled students may be marked, so build the roster once
        Map<Long, Student> roster = new HashMap<>();
        for (Enrollment enrollment : enrollmentRepository.findByCourseId(courseId)) {
            roster.put(enrollment.getStudent().getId(), enrollment.getStudent());
        }

        Map<Long, Attendance> existing = new HashMap<>();
        for (Attendance attendance : attendanceRepository.findByCourseIdAndAttendanceDate(courseId, date)) {
            existing.put(attendance.getStudent().getId(), attendance);
        }

        int created = 0;
        int updated = 0;
        List<Attendance> toSave = new ArrayList<>();
        for (AttendanceRecordRequest record : request.getRecords()) {
            if (record.getStudentId() == null || record.getStatus() == null) {
                throw new ResourceNotFoundException("each record needs a studentId and a status");
            }
            Student student = roster.get(record.getStudentId());
            if (student == null) {
                throw new ResourceNotFoundException(
                        "student " + record.getStudentId() + " is not enrolled in this course");
            }
            Attendance attendance = existing.get(record.getStudentId());
            if (attendance == null) {
                attendance = new Attendance();
                attendance.setCourse(course);
                attendance.setStudent(student);
                attendance.setAttendanceDate(date);
                created++;
            } else {
                updated++;
            }
            attendance.setStatus(record.getStatus());
            toSave.add(attendance);
        }
        attendanceRepository.saveAll(toSave);

        MarkAttendanceResponse response = new MarkAttendanceResponse();
        response.setCourseId(course.getId());
        response.setCourseName(course.getName());
        response.setDate(date);
        response.setCreated(created);
        response.setUpdated(updated);
        response.setMessage("Attendance marked successfully");
        return response;
    }

    @Transactional(readOnly = true)
    public CourseAttendanceResponse courseAttendance(
            Long courseId,
            LocalDate date,
            Authentication authentication) {

        Course course = requireCourseAccess(courseId, authentication);
        LocalDate day = date == null ? LocalDate.now() : date;

        Map<Long, Attendance> marked = new HashMap<>();
        for (Attendance attendance : attendanceRepository.findByCourseIdAndAttendanceDate(courseId, day)) {
            marked.put(attendance.getStudent().getId(), attendance);
        }

        List<CourseAttendanceResponseInner> rows = new ArrayList<>();
        for (Enrollment enrollment : enrollmentRepository.findByCourseId(courseId)) {
            Student student = enrollment.getStudent();
            Attendance attendance = marked.get(student.getId());
            CourseAttendanceResponseInner row = new CourseAttendanceResponseInner();
            row.setStudentId(student.getId());
            row.setStudentName(student.getName());
            row.setAttendanceId(attendance == null ? null : attendance.getId());
            row.setStatus(attendance == null ? null : attendance.getStatus());
            rows.add(row);
        }

        CourseAttendanceResponse response = new CourseAttendanceResponse();
        response.setCourseId(course.getId());
        response.setCourseName(course.getName());
        response.setDate(day);
        response.setTotalEnrolled(rows.size());
        response.setTotalMarked(marked.size());
        response.setRecords(rows);
        return response;
    }

    @Transactional(readOnly = true)
    public MyAttendanceResponse myAttendance(Pageable pageable, Authentication authentication) {
        Student student = currentStudent(authentication);
        Page<Attendance> page = attendanceRepository.findByStudentId(student.getId(), pageable);

        List<MyAttendanceResponseInner> rows = new ArrayList<>();
        for (Attendance attendance : page.getContent()) {
            MyAttendanceResponseInner row = new MyAttendanceResponseInner();
            row.setAttendanceId(attendance.getId());
            row.setCourseId(attendance.getCourse().getId());
            row.setCourse(attendance.getCourse().getName());
            row.setDate(attendance.getAttendanceDate());
            row.setStatus(attendance.getStatus());
            rows.add(row);
        }

        MyAttendanceResponse response = new MyAttendanceResponse();
        response.setAttendance(rows);
        response.setPage(Long.valueOf(page.getNumber()));
        response.setSize(Long.valueOf(page.getSize()));
        response.setTotalPages(Long.valueOf(page.getTotalPages()));
        response.setTotalElements(page.getTotalElements());
        response.setTotalNumberOfElements(Long.valueOf(page.getNumberOfElements()));
        return response;
    }

    @Transactional(readOnly = true)
    public MyAttendanceSummaryResponse myAttendanceSummary(Authentication authentication) {
        Student student = currentStudent(authentication);

        Map<Long, MyAttendanceSummaryResponseInner> byCourse = new LinkedHashMap<>();
        for (Attendance attendance : attendanceRepository.findByStudentId(student.getId())) {
            Course course = attendance.getCourse();
            MyAttendanceSummaryResponseInner row = byCourse.get(course.getId());
            if (row == null) {
                row = new MyAttendanceSummaryResponseInner();
                row.setCourseId(course.getId());
                row.setCourse(course.getName());
                row.setTotalClasses(0);
                row.setPresent(0);
                row.setAbsent(0);
                row.setLate(0);
                row.setExcused(0);
                byCourse.put(course.getId(), row);
            }
            row.setTotalClasses(row.getTotalClasses() + 1);
            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                row.setPresent(row.getPresent() + 1);
            } else if (attendance.getStatus() == AttendanceStatus.ABSENT) {
                row.setAbsent(row.getAbsent() + 1);
            } else if (attendance.getStatus() == AttendanceStatus.LATE) {
                row.setLate(row.getLate() + 1);
            } else if (attendance.getStatus() == AttendanceStatus.EXCUSED) {
                row.setExcused(row.getExcused() + 1);
            }
        }

        int attended = 0;
        int total = 0;
        for (MyAttendanceSummaryResponseInner row : byCourse.values()) {
            // LATE and EXCUSED still count as attended
            int courseAttended = row.getPresent() + row.getLate() + row.getExcused();
            row.setPercentage(row.getTotalClasses() == 0
                    ? 0.0
                    : round2(courseAttended * 100.0 / row.getTotalClasses()));
            attended += courseAttended;
            total += row.getTotalClasses();
        }

        MyAttendanceSummaryResponse response = new MyAttendanceSummaryResponse();
        response.setSummary(new ArrayList<>(byCourse.values()));
        response.setOverallPercentage(total == 0 ? null : round2(attended * 100.0 / total));
        return response;
    }

    private Course requireCourseAccess(Long courseId, Authentication authentication) {
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("course not found " + courseId));

        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(
                () -> new ResourceNotFoundException("user not found " + authentication.getName()));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
        Teacher teacher = user.getTeacher();
        boolean ownsCourse = teacher != null
                && course.getTeacher() != null
                && teacher.getId().equals(course.getTeacher().getId());

        if (!isAdmin && !ownsCourse) {
            throw new AccessDeniedException("you are not the teacher of this course");
        }
        return course;
    }

    private Student currentStudent(Authentication authentication) {
        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(
                () -> new ResourceNotFoundException("user not found " + authentication.getName()));
        Student student = user.getStudent();
        if (student == null) {
            throw new ResourceNotFoundException("no student profile for " + authentication.getName());
        }
        return student;
    }

    private Double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
