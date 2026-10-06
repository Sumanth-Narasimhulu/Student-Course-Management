# Student Course Management System — API Specification

## Authentication APIs

### Register
`POST /auth/register`

Request:
```json
{
  "username": "sumanth",
  "password": "password123",
  "role": "STUDENT"
}
```

Response:
```json
{
  "message": "User registered successfully"
}
```

Authorization: Public.

> In production, public registration should not allow arbitrary privileged roles such as `ADMIN`.

### Login
`POST /auth/login`

Request:
```json
{
  "username": "sumanth",
  "password": "password123"
}
```

Response:
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "abc..."
}
```

Authorization: Public.

### Refresh
`POST /auth/refresh`

Request:
```json
{
  "refreshToken": "abc..."
}
```

Response:
```json
{
  "accessToken": "new-eyJ...",
  "refreshToken": "new-abc..."
}
```

Authorization: Public at URL level; the refresh token is the credential.

### Logout
`POST /auth/logout`

Request:
```json
{
  "refreshToken": "abc..."
}
```

Response:
```json
{
  "message": "Logged out successfully"
}
```

Authorization: Valid refresh token that can be revoked.

### Current User
`GET /auth/me`

Response:
```json
{
  "id": 1,
  "username": "sumanth",
  "roles": ["STUDENT"]
}
```

Authorization: Authenticated user.

---

# Student APIs

## Create Student
`POST /api/students`

Request:
```json
{
  "name": "Sumanth",
  "dob": "2002-05-17",
  "degree": "B.Tech CSE",
  "year": 4,
  "userId": 10
}
```

Response:
```json
{
  "id": 101,
  "name": "Sumanth",
  "dob": "2002-05-17",
  "degree": "B.Tech CSE",
  "year": 4
}
```

Authorization: `STUDENT_CREATE`

- ADMIN → allowed
- TEACHER → denied
- STUDENT → denied

## Get All Students
`GET /api/students`

Response:
```json
[
  {
    "id": 101,
    "name": "Sumanth",
    "degree": "B.Tech CSE",
    "year": 4
  },
  {
    "id": 102,
    "name": "Rahul",
    "degree": "B.Tech CSE",
    "year": 3
  }
]
```

Authorization: `STUDENT_READ`

Suggested access: ADMIN and TEACHER; student access can be restricted according to requirements.

## Get Student By ID
`GET /api/students/{studentId}`

Response:
```json
{
  "id": 101,
  "name": "Sumanth",
  "dob": "2002-05-17",
  "degree": "B.Tech CSE",
  "year": 4
}
```

Authorization:
- ADMIN → any student
- TEACHER → according to business rules
- STUDENT → own record only

Requires `STUDENT_READ` plus ownership/business rules.

## Update Student
`PUT /api/students/{studentId}`

Request:
```json
{
  "name": "Sumanth Mamillapalli",
  "degree": "B.Tech CSE",
  "year": 4
}
```

Response:
```json
{
  "id": 101,
  "name": "Sumanth Mamillapalli",
  "degree": "B.Tech CSE",
  "year": 4
}
```

Authorization:
- ADMIN → any student
- STUDENT → own profile
- TEACHER → normally denied

Requires `STUDENT_UPDATE` plus ownership.

## Delete Student
`DELETE /api/students/{studentId}`

Response:
```json
{
  "message": "Student deleted successfully"
}
```

Authorization: `STUDENT_DELETE` — normally ADMIN only.

---

# Teacher APIs

## Create Teacher
`POST /api/teachers`

Request:
```json
{
  "name": "Ravi Kumar",
  "department": "Computer Science",
  "userId": 20
}
```

Response:
```json
{
  "id": 201,
  "name": "Ravi Kumar",
  "department": "Computer Science"
}
```

Authorization: `TEACHER_CREATE` — normally ADMIN only.

## Get All Teachers
`GET /api/teachers`

Response:
```json
[
  {
    "id": 201,
    "name": "Ravi Kumar",
    "department": "Computer Science"
  }
]
```

Authorization: `TEACHER_READ`

Suggested access: ADMIN, TEACHER, STUDENT.

## Get Teacher By ID
`GET /api/teachers/{id}`

Response:
```json
{
  "id": 201,
  "name": "Ravi Kumar",
  "department": "Computer Science",
  "coursesTaught": [
    {
      "id": 10,
      "name": "Java"
    }
  ]
}
```

Authorization: `TEACHER_READ`.

## Update Teacher
`PUT /api/teachers/{id}`

Authorization:
- ADMIN → any teacher
- TEACHER → own profile
- STUDENT → denied

Requires `TEACHER_UPDATE` plus ownership.

## Delete Teacher
`DELETE /api/teachers/{id}`

Authorization: `TEACHER_DELETE` — normally ADMIN only.

---

# Course APIs

## Create Course
`POST /api/courses`

Request:
```json
{
  "name": "Spring Boot",
  "description": "Spring Boot fundamentals"
}
```

The backend should identify the teacher from the authenticated user. Do not trust a client-supplied teacher ID for ownership.

Response:
```json
{
  "id": 10,
  "name": "Spring Boot",
  "description": "Spring Boot fundamentals",
  "teacher": {
    "id": 201,
    "name": "Ravi Kumar"
  }
}
```

Authorization: `COURSE_CREATE`

- ADMIN → allowed
- TEACHER → allowed
- STUDENT → denied

## Get All Courses
`GET /api/courses`

Response:
```json
[
  {
    "id": 10,
    "name": "Java",
    "description": "Java Programming",
    "teacherName": "Ravi"
  },
  {
    "id": 11,
    "name": "Database",
    "description": "PostgreSQL",
    "teacherName": "Priya"
  }
]
```

Authorization: `COURSE_READ`.

Usually ADMIN, TEACHER and STUDENT can read.

## Get Course
`GET /api/courses/{courseId}`

Response:
```json
{
  "id": 10,
  "name": "Java",
  "description": "Java Programming",
  "teacher": {
    "id": 201,
    "name": "Ravi"
  }
}
```

Authorization: `COURSE_READ`.

## Update Course
`PUT /api/courses/{courseId}`

Request:
```json
{
  "name": "Advanced Java",
  "description": "Advanced Java Programming"
}
```

Response:
```json
{
  "id": 10,
  "name": "Advanced Java",
  "description": "Advanced Java Programming"
}
```

Authorization: `COURSE_UPDATE` plus ownership.

- ADMIN → any course
- TEACHER → own course
- STUDENT → denied

## Delete Course
`DELETE /api/courses/{courseId}`

Response:
```json
{
  "message": "Course deleted successfully"
}
```

Authorization: `COURSE_DELETE` plus ownership.

- ADMIN → any
- TEACHER → own course
- STUDENT → denied

## Get Course Students
`GET /api/courses/{courseId}/students`

Response:
```json
[
  {
    "studentId": 101,
    "name": "Sumanth"
  },
  {
    "studentId": 102,
    "name": "Rahul"
  }
]
```

Authorization:
- ADMIN → allowed
- TEACHER → only if they teach the course
- STUDENT → normally denied

Requires `ENROLLMENT_READ` plus course ownership/business rules.

## Get Course Teacher
`GET /api/courses/{courseId}/teacher`

Response:
```json
{
  "id": 201,
  "name": "Ravi Kumar",
  "department": "Computer Science"
}
```

Authorization: `COURSE_READ`.

## Get Course Assignments
`GET /api/courses/{courseId}/assignments`

Response:
```json
[
  {
    "id": 1001,
    "title": "Spring Security Assignment",
    "dueDate": "2026-09-15",
    "maxMarks": 100
  }
]
```

Authorization: `ASSIGNMENT_READ` plus relevant ownership/enrollment rules.

---

# Enrollment APIs

## Enroll In Course
`POST /api/courses/{courseId}/enroll`

No student ID is needed; derive the current student from the authenticated user.

Response:
```json
{
  "id": 500,
  "courseId": 10,
  "courseName": "Java",
  "studentId": 101,
  "studentName": "Sumanth",
  "status": "ACTIVE"
}
```

Authorization:
- `ENROLLMENT_CREATE`
- STUDENT
- course exists
- not already enrolled

## Unenroll From Course
`DELETE /api/courses/{courseId}/enroll`

Response:
```json
{
  "message": "Successfully unenrolled"
}
```

Authorization: `ENROLLMENT_DELETE` plus ownership of the enrollment.

## Get My Courses
`GET /api/students/me/courses`

Response:
```json
[
  {
    "courseId": 10,
    "courseName": "Java",
    "teacherName": "Ravi",
    "status": "ACTIVE"
  },
  {
    "courseId": 11,
    "courseName": "Spring Boot",
    "teacherName": "Priya",
    "status": "ACTIVE"
  }
]
```

Authorization: Authenticated STUDENT.

## Get Courses For A Student
`GET /api/students/{studentId}/courses`

Response:
```json
[
  {
    "courseId": 10,
    "courseName": "Java",
    "status": "ACTIVE"
  }
]
```

Authorization:
- ADMIN → allowed
- TEACHER → according to business rules
- STUDENT → own courses only

---

# Assignment APIs

## Create Assignment
`POST /api/courses/{courseId}/assignments`

Request:
```json
{
  "title": "Spring Security Assignment",
  "description": "Implement JWT authentication",
  "dueDate": "2026-09-15",
  "maxMarks": 100
}
```

Response:
```json
{
  "id": 1001,
  "title": "Spring Security Assignment",
  "description": "Implement JWT authentication",
  "dueDate": "2026-09-15",
  "maxMarks": 100,
  "courseId": 10
}
```

Authorization: `ASSIGNMENT_CREATE` plus teacher owns the course.

## Get Assignments For A Course
`GET /api/courses/{courseId}/assignments`

Authorization: `ASSIGNMENT_READ` plus teacher ownership OR student enrollment.

## Get Assignment
`GET /api/assignments/{assignmentId}`

Response:
```json
{
  "id": 1001,
  "title": "Spring Security Assignment",
  "description": "Implement JWT authentication",
  "dueDate": "2026-09-15",
  "maxMarks": 100,
  "course": {
    "id": 10,
    "name": "Spring Boot"
  }
}
```

Authorization: `ASSIGNMENT_READ` plus relevant course access.

## Update Assignment
`PUT /api/assignments/{assignmentId}`

Authorization: `ASSIGNMENT_UPDATE` plus teacher owns the assignment's course.

## Delete Assignment
`DELETE /api/assignments/{assignmentId}`

Authorization: `ASSIGNMENT_DELETE` plus teacher owns the assignment's course.

---

# Submission APIs

## Submit Assignment
`POST /api/assignments/{assignmentId}/submit`

Request:
```json
{
  "content": "My solution..."
}
```

Response:
```json
{
  "id": 9001,
  "assignmentId": 1001,
  "studentId": 101,
  "submittedAt": "2026-09-03T10:30:00",
  "status": "SUBMITTED"
}
```

Authorization:
- `ASSIGNMENT_SUBMIT`
- STUDENT
- student enrolled in assignment course
- assignment is open
- submission rules satisfied

## Get Assignment Submissions
`GET /api/assignments/{assignmentId}/submissions`

Response:
```json
[
  {
    "id": 9001,
    "studentId": 101,
    "studentName": "Sumanth",
    "submittedAt": "2026-09-03T10:30:00",
    "status": "SUBMITTED"
  }
]
```

Authorization:
- ADMIN → allowed
- TEACHER → only for their course
- STUDENT → denied

## Get My Submissions
`GET /api/students/me/submissions`

Response:
```json
[
  {
    "submissionId": 9001,
    "assignmentId": 1001,
    "assignmentTitle": "Spring Security Assignment",
    "status": "SUBMITTED",
    "marks": 85
  }
]
```

Authorization: STUDENT, own submissions only.

---

# Grading APIs

## Grade Submission
`PUT /api/submissions/{submissionId}/grade`

Request:
```json
{
  "marks": 85,
  "feedback": "Good implementation"
}
```

Response:
```json
{
  "submissionId": 9001,
  "marks": 85,
  "feedback": "Good implementation",
  "gradedBy": "Ravi"
}
```

Authorization: `GRADE_SUBMISSION` plus teacher owns submission's course.

- ADMIN → allowed
- TEACHER → own course only
- STUDENT → denied

## Get My Grades
`GET /api/students/me/grades`

Response:
```json
[
  {
    "course": "Spring Boot",
    "assignment": "Spring Security Assignment",
    "marks": 85,
    "maxMarks": 100
  }
]
```

Authorization: STUDENT, own grades only.

## Get Course Grades
`GET /api/courses/{courseId}/grades`

Response:
```json
[
  {
    "studentId": 101,
    "studentName": "Sumanth",
    "assignment": "Spring Security Assignment",
    "marks": 85
  }
]
```

Authorization:
- ADMIN → allowed
- TEACHER → own course
- STUDENT → denied

---

# Attendance APIs

## Mark Attendance
`POST /api/courses/{courseId}/attendance`

Request:
```json
{
  "date": "2026-09-03",
  "records": [
    {
      "studentId": 101,
      "status": "PRESENT"
    },
    {
      "studentId": 102,
      "status": "ABSENT"
    }
  ]
}
```

Response:
```json
{
  "courseId": 10,
  "date": "2026-09-03",
  "message": "Attendance marked successfully"
}
```

Authorization: `ATTENDANCE_MARK` plus teacher owns course.

## Get Course Attendance
`GET /api/courses/{courseId}/attendance?date=2026-09-03`

Response:
```json
[
  {
    "studentId": 101,
    "studentName": "Sumanth",
    "status": "PRESENT"
  },
  {
    "studentId": 102,
    "studentName": "Rahul",
    "status": "ABSENT"
  }
]
```

Authorization:
- ADMIN → allowed
- TEACHER → own course
- STUDENT → normally denied

## Get My Attendance
`GET /api/students/me/attendance`

Response:
```json
[
  {
    "course": "Java",
    "date": "2026-09-01",
    "status": "PRESENT"
  },
  {
    "course": "Java",
    "date": "2026-09-02",
    "status": "ABSENT"
  }
]
```

Authorization: STUDENT, own attendance only.

## Get My Attendance Summary
`GET /api/students/me/attendance/summary`

Response:
```json
[
  {
    "course": "Java",
    "totalClasses": 20,
    "present": 17,
    "absent": 3,
    "percentage": 85.0
  },
  {
    "course": "Spring Boot",
    "totalClasses": 18,
    "present": 16,
    "absent": 2,
    "percentage": 88.89
  }
]
```

Authorization: STUDENT, own data only.

---

# Admin APIs

## Get All Users
`GET /api/admin/users`

Response:
```json
[
  {
    "id": 1,
    "username": "sumanth",
    "roles": ["STUDENT"]
  },
  {
    "id": 2,
    "username": "ravi",
    "roles": ["TEACHER"]
  }
]
```

Authorization:
```java
@PreAuthorize("hasRole('ADMIN')")
```

## Get All Students
`GET /api/admin/students`

Authorization: ADMIN.

## Get All Teachers
`GET /api/admin/teachers`

Authorization: ADMIN.

## Get All Courses
`GET /api/admin/courses`

Authorization: ADMIN.

This is optional if `GET /api/courses` already provides everything needed.

## Delete User
`DELETE /api/admin/users/{userId}`

Response:
```json
{
  "message": "User deleted successfully"
}
```

Authorization: ADMIN.

---

# Authorization Model

Roles:

```text
ADMIN
TEACHER
STUDENT
```

Example permissions:

```text
USER_READ
USER_CREATE
USER_UPDATE
USER_DELETE

STUDENT_READ
STUDENT_CREATE
STUDENT_UPDATE
STUDENT_DELETE

TEACHER_READ
TEACHER_CREATE
TEACHER_UPDATE
TEACHER_DELETE

COURSE_READ
COURSE_CREATE
COURSE_UPDATE
COURSE_DELETE

ENROLLMENT_READ
ENROLLMENT_CREATE
ENROLLMENT_DELETE

ASSIGNMENT_READ
ASSIGNMENT_CREATE
ASSIGNMENT_UPDATE
ASSIGNMENT_DELETE

SUBMISSION_READ
ASSIGNMENT_SUBMIT

GRADE_READ
GRADE_SUBMISSION

ATTENDANCE_READ
ATTENDANCE_MARK
```

---

# Authorization Rule

For sensitive operations, use three layers:

```text
1. Authentication
   Is the user logged in?

2. RBAC / permission
   Does the user have the required permission?

3. Resource ownership / business rule
   Is the user allowed to operate on this particular resource?
```

Example:

```text
Ravi
 ↓
ROLE_TEACHER
 ↓
COURSE_UPDATE ✅
 ↓
Does Course 20 belong to Ravi?
 ↓
NO
 ↓
403 Forbidden
```

Another example:

```text
Sumanth
 ↓
ASSIGNMENT_SUBMIT ✅
 ↓
Is Sumanth enrolled in the assignment's course?
 ↓
NO
 ↓
403 Forbidden
```

---

# HTTP Status Codes

```text
200 OK
    Successful request

201 Created
    Resource successfully created

400 Bad Request
    Invalid input or business request

401 Unauthorized
    Missing/invalid/expired authentication

403 Forbidden
    Authenticated but not authorized

404 Not Found
    Resource does not exist

409 Conflict
    Request conflicts with existing state

500 Internal Server Error
    Unexpected server-side failure
```

---

# Exception Handling

Application exceptions:

```text
ResourceNotFoundException → 404
AlreadyEnrolledException  → 409
BadRequestException       → 400
Unexpected exception      → 500
```

Security failures:

```text
AuthenticationEntryPoint → 401
AccessDeniedHandler      → 403
```

---

# Recommended Implementation Order

Build in dependency order:

```text
1. Student CRUD
2. Teacher CRUD
3. Course CRUD
4. Enrollment
5. Assignment
6. Submission
7. Grading
8. Attendance
9. Admin APIs
```

Dependency chain:

```text
Teacher
   ↓
Course
   ↓
Enrollment
   ↓
Assignment
   ↓
Submission
   ↓
Grading

Course
   ↓
Attendance
```

For each feature:

```text
Controller
    ↓
DTO
    ↓
Service
    ↓
Repository
    ↓
Entity
    ↓
Database
```

Then add:

```text
@PreAuthorize
    ↓
RBAC
    ↓
Ownership/business checks
    ↓
GlobalExceptionHandler
```

---

# Current Progress

Already completed:

```text
✅ Register
✅ Login
✅ Logout
✅ Refresh Token
✅ /me
✅ JWT authentication
✅ Global exception handling
✅ 401 / 403 handling
✅ RBAC
✅ @PreAuthorize
✅ Ownership authorization
✅ CORS
```

Next:

```text
→ Student CRUD
→ Teacher CRUD
→ Course CRUD
→ Enrollment
→ Assignment
→ Submission
→ Grading
→ Attendance
→ Admin APIs
```
