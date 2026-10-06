package com.studentmanagement.service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Permission;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.PermissionRepository;
import com.studentmanagement.repository.RoleRepository;
import com.studentmanagement.repository.TeacherRepository;
import com.studentmanagement.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class DataInitializerService {

    private RoleRepository roleRepository;
    private PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializerService(RoleRepository roleRepository, PermissionRepository permissionRepository,
            UserRepository userRepository, TeacherRepository teacherRepository,
            DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void initializeData() {

        HashMap<String, Permission> permissions = new HashMap<>();

        Permission userRead = createPermission("USER_READ");
        Permission userCreate = createPermission("USER_CREATE");
        Permission userUpdate = createPermission("USER_UPDATE");
        Permission userDelete = createPermission("USER_DELETE");
        permissions.put("userRead", userRead);
        permissions.put("userCreate", userCreate);
        permissions.put("userUpdate", userUpdate);
        permissions.put("userDelete", userDelete);

        // student permissions
        Permission studentRead = createPermission("STUDENT_READ");
        Permission studentCreate = createPermission("STUDENT_CREATE");
        Permission studentUpdate = createPermission("STUDENT_UPDATE");
        Permission studentDelete = createPermission("STUDENT_DELETE");

        permissions.put("studentRead", studentRead);
        permissions.put("studentCreate", studentCreate);
        permissions.put("studentUpdate", studentUpdate);
        permissions.put("studentDelete", studentDelete);

        // Teacher permissions
        Permission teacherRead = createPermission("TEACHER_READ");
        Permission teacherCreate = createPermission("TEACHER_CREATE");
        Permission teacherUpdate = createPermission("TEACHER_UPDATE");
        Permission teacherDelete = createPermission("TEACHER_DELETE");

        permissions.put("teacherRead", teacherRead);
        permissions.put("teacherCreate", teacherCreate);
        permissions.put("teacherUpdate", teacherUpdate);
        permissions.put("teacherDelete", teacherDelete);

        // Course permissions
        Permission courseRead = createPermission("COURSE_READ");
        Permission courseCreate = createPermission("COURSE_CREATE");
        Permission courseUpdate = createPermission("COURSE_UPDATE");
        Permission courseDelete = createPermission("COURSE_DELETE");

        permissions.put("courseRead", courseRead);
        permissions.put("courseCreate", courseCreate);
        permissions.put("courseUpdate", courseUpdate);
        permissions.put("courseDelete", courseDelete);

        // Enrollment permissions
        Permission enrollmentRead = createPermission("ENROLLMENT_READ");
        Permission enrollmentUpdate = createPermission("ENROLLMENT_UPDATE");
        Permission enrollmentCreate = createPermission("ENROLLMENT_CREATE");
        Permission enrollmentDelete = createPermission("ENROLLMENT_DELETE");

        permissions.put("enrollementRead", enrollmentRead);
        permissions.put("enrollmentUpdate", enrollmentUpdate);
        permissions.put("enrollmentCreate", enrollmentCreate);
        permissions.put("enrollementDelete", enrollmentDelete);

        // Assignment permissions
        Permission assignmentRead = createPermission("ASSIGNMENT_READ");
        Permission assignmentCreate = createPermission("ASSIGNMENT_CREATE");
        Permission assignmentUpdate = createPermission("ASSIGNMENT_UPDATE");
        Permission assignmentDelete = createPermission("ASSIGNMENT_DELETE");
        Permission submitAssignment = createPermission("SUBMIT_ASSIGNMENT");

        permissions.put("assignmentRead", assignmentRead);
        permissions.put("assignmentCreate", assignmentCreate);
        permissions.put("assignmentUpdate", assignmentUpdate);
        permissions.put("assignmentDelete", assignmentDelete);
        permissions.put("submitAssignment", submitAssignment);

        // grade permissions
        Permission gradeRead = createPermission("GRADE_READ");
        Permission gradeSubmit = createPermission("GRADE_SUBMIT");
        permissions.put("gradeRead", gradeRead);
        permissions.put("gradeSubmit", gradeSubmit);

        // attendance permissions
        Permission attendanceRead = createPermission("ATTENDANCE_READ");
        Permission attendanceMark = createPermission("ATTENDANCE_MARK");
        permissions.put("attendanceRead", attendanceRead);
        permissions.put("attendanceMark", attendanceMark);

        /*
         * ADMIN
         * TEACHER
         * STUDENT
         */

        /*
         * USER_READ
         * USER_CREATE
         * USER_UPDATE
         * USER_DELETE
         * 
         * STUDENT_READ
         * STUDENT_CREATE
         * STUDENT_UPDATE
         * STUDENT_DELETE
         * 
         * TEACHER_READ
         * TEACHER_CREATE
         * TEACHER_UPDATE
         * TEACHER_DELETE
         * 
         * COURSE_READ
         * COURSE_CREATE
         * COURSE_UPDATE
         * COURSE_DELETE
         * 
         * ENROLLMENT_READ
         * ENROLLMENT_CREATE
         * ENROLLMENT_DELETE
         * 
         * ASSIGNMENT_READ
         * ASSIGNMENT_CREATE
         * ASSIGNMENT_UPDATE
         * ASSIGNMENT_DELETE
         * 
         * SUBMISSION_READ
         * ASSIGNMENT_SUBMIT
         * 
         * GRADE_READ
         * GRADE_SUBMISSION
         * 
         * ATTENDANCE_READ
         * ATTENDANCE_MARK
         */

        Role adminRole = createRole("ADMIN");
        Role teacherRole = createRole("TEACHER");
        Role studentRole = createRole("STUDENT");

        adminRole.getPermissions().addAll(permissions.values());

        teacherRole.getPermissions().addAll(List.of(
                studentRead,
                teacherRead,
                courseRead, courseUpdate,
                enrollmentRead,
                assignmentRead, assignmentCreate, assignmentUpdate, assignmentDelete,
                gradeRead, gradeSubmit,
                attendanceRead, attendanceMark));

        studentRole.getPermissions().addAll(List.of(
                courseRead,
                teacherRead,
                enrollmentRead,
                assignmentRead, submitAssignment,
                gradeRead,
                attendanceRead));

        roleRepository.save(adminRole);
        roleRepository.save(teacherRole);
        roleRepository.save(studentRole);

        seedAdminUser(adminRole);
        seedTeacherUser(teacherRole);

    }

    private void seedAdminUser(Role adminRole) {
        if (userRepository.findByUserName("admin").isPresent()) {
            return;
        }
        User admin = new User();
        admin.setUserName("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);
    }

    private void seedTeacherUser(Role teacherRole) {
        if (userRepository.findByUserName("teacher").isPresent()) {
            return;
        }
        Department department = departmentRepository.findByName("Computer Science")
                .orElseGet(() -> {
                    Department created = new Department();
                    created.setName("Computer Science");
                    return departmentRepository.save(created);
                });

        User user = new User();
        user.setUserName("teacher");
        user.setPassword(passwordEncoder.encode("teacher123"));
        user.setRoles(Set.of(teacherRole));
        userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setName("Default Teacher");
        teacher.setDegree("M.Tech");
        teacher.setPhoneNumber("0000000000");
        teacher.setUser(user);
        teacher.setDepartment(department);
        teacherRepository.save(teacher);
    }

    public Permission createPermission(String permissionName) {
        return permissionRepository.findByName(permissionName)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(permissionName);
                    return permissionRepository.save(permission);
                });
    }

    public Role createRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });
    }

}
