const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

export const tokens = {
  get: () => localStorage.getItem('accessToken'),
  getRefresh: () => localStorage.getItem('refreshToken'),
  set: ({ jwtToken, refreshToken }) => {
    if (jwtToken) localStorage.setItem('accessToken', jwtToken)
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken)
  },
  clear: () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  },
}

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = tokens.get()
  if (auth && token) headers.Authorization = `Bearer ${token}`

  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  const raw = await response.text()
  let data = raw
  try {
    data = raw ? JSON.parse(raw) : null
  } catch {
    /* several endpoints return a bare string body */
  }

  if (!response.ok) {
    const message =
      (data && typeof data === 'object' && data.message) ||
      (typeof data === 'string' && data) ||
      `Request failed with ${response.status}`
    throw new ApiError(message, response.status)
  }
  return data
}

const qs = (params) => {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    if (Array.isArray(value)) value.forEach((v) => search.append(key, v))
    else search.append(key, value)
  })
  const out = search.toString()
  return out ? `?${out}` : ''
}

export const api = {
  // ---- auth ----
  login: (userName, password) =>
    request('/auth/login', { method: 'POST', auth: false, body: { userName, password } }),
  register: (body) => request('/auth/register', { method: 'POST', auth: false, body }),
  logout: (refreshToken) =>
    request('/auth/logout', { method: 'POST', auth: false, body: { refreshToken } }),

  me: () => request('/users/me'),

  // ---- courses ----
  allCourses: (filters = {}, page = 0, size = 12) =>
    request(`/api/v1/course/allCourses${qs({ page, size })}`, {
      method: 'POST',
      body: {
        courseNames: filters.courseNames ?? [],
        teacherNames: filters.teacherNames ?? [],
        teacherUsernames: filters.teacherUsernames ?? [],
      },
    }),
  course: (courseId) => request(`/api/v1/course/${courseId}`),
  courseTeacher: (courseId) => request(`/api/v1/course/${courseId}/teacher`),
  courseAssignments: (courseId, page = 0, size = 20) =>
    request(`/api/v1/course/${courseId}/assignments${qs({ page, size })}`),
  courseStudents: (courseId, page = 0, size = 20) =>
    request(`/api/v1/course/${courseId}/students${qs({ page, size })}`),
  createCourse: (body) => request('/api/v1/course', { method: 'POST', body }),
  updateCourse: (courseId, body) => request(`/api/v1/course/${courseId}`, { method: 'POST', body }),
  deleteCourse: (courseId) => request(`/api/v1/course/${courseId}`, { method: 'DELETE' }),

  // ---- enrollment ----
  enroll: (courseId) => request(`/api/v1/enrollCourse/${courseId}/enroll`, { method: 'POST' }),
  unenroll: (enrollmentId) =>
    request(`/api/v1/enrollCourse/${enrollmentId}/unenroll`, { method: 'DELETE' }),
  myEnrolledCourses: (filters = {}, page = 0, size = 20) =>
    request(
      `/api/v1/enrollCourse/me/courses${qs({
        page,
        size,
        courseNames: filters.courseNames,
        teachernames: filters.teacherNames,
      })}`,
    ),

  // ---- assignments ----
  assignment: (assignmentId) => request(`/api/v1/assignments/${assignmentId}`),
  createAssignment: (courseId, body) =>
    request(`/api/v1/assignments/${courseId}`, { method: 'POST', body }),
  updateAssignment: (assignmentId, body) =>
    request(`/api/v1/assignments/${assignmentId}`, { method: 'PUT', body }),
  deleteAssignment: (assignmentId) =>
    request(`/api/v1/assignments/${assignmentId}`, { method: 'DELETE' }),
  upcomingAssignments: (page = 0, size = 20) =>
    request(`/api/v1/assignments/me/upcoming${qs({ page, size })}`),

  // ---- submissions ----
  initiateUpload: (assignmentId, body) =>
    request(`/api/v1/assignment/${assignmentId}/initiateUpload`, { method: 'POST', body }),
  completeUpload: (submissionId) =>
    request(`/api/v1/assignment/submissions/${submissionId}/complete`, { method: 'POST' }),
  submissionFile: (submissionId) =>
    request(`/api/v1/assignment/submissions/${submissionId}/file`),
  assignmentSubmissions: (assignmentId, page = 0, size = 20, status) =>
    request(`/api/v1/assignment/${assignmentId}/submissions${qs({ page, size, status })}`),

  // ---- grading ----
  gradeSubmission: (submissionId, body) =>
    request(`/api/v1/grading/submissions/${submissionId}`, { method: 'PUT', body }),
  mySubmissions: (page = 0, size = 20) =>
    request(`/api/v1/grading/me/submissions${qs({ page, size })}`),
  myGrades: (page = 0, size = 20) => request(`/api/v1/grading/me/grades${qs({ page, size })}`),
  courseGrades: (courseId, page = 0, size = 50) =>
    request(`/api/v1/grading/courses/${courseId}/grades${qs({ page, size })}`),

  // ---- attendance ----
  markAttendance: (courseId, body) =>
    request(`/api/v1/attendance/courses/${courseId}`, { method: 'POST', body }),
  courseAttendance: (courseId, date) =>
    request(`/api/v1/attendance/courses/${courseId}${qs({ date })}`),
  myAttendance: (page = 0, size = 50) => request(`/api/v1/attendance/me${qs({ page, size })}`),
  myAttendanceSummary: () => request('/api/v1/attendance/me/summary'),

  // ---- students ----
  allStudents: (page = 0, size = 20) => request(`/api/v1/students${qs({ page, size })}`),
  student: (id) => request(`/api/v1/students/${id}`),
  searchStudents: (body, page = 0, size = 20) =>
    request(`/api/v1/students/search${qs({ page, size })}`, { method: 'POST', body }),
  updateMyStudentProfile: (body) => request('/api/v1/students', { method: 'PATCH', body }),
  deleteStudent: (id) => request(`/api/v1/students/${id}`, { method: 'DELETE' }),

  // ---- teachers ----
  allTeachers: (page = 0, size = 20) => request(`/api/v1/teachers${qs({ page, size })}`),
  teacher: (id) => request(`/api/v1/teachers/${id}`),
  searchTeachers: (body, page = 0, size = 20) =>
    request(`/api/v1/teachers/search${qs({ page, size })}`, { method: 'POST', body }),
  updateMyTeacherProfile: (body) => request('/api/v1/teachers', { method: 'PATCH', body }),
  myTaughtCourses: (page = 0, size = 20) =>
    request(`/api/v1/teachers/me/courses${qs({ page, size })}`),

  // ---- departments ----
  allDepartments: (page = 0, size = 50) =>
    request(`/api/v1/department/allDepartments${qs({ page, size })}`, { method: 'POST' }),
  createDepartment: (name) => request('/api/v1/department', { method: 'POST', body: { name } }),
  updateDepartment: (id, name) =>
    request(`/api/v1/department/${id}`, { method: 'POST', body: { name } }),
  deleteDepartment: (id) => request(`/api/v1/department/${id}`, { method: 'DELETE' }),

  // ---- admin ----
  createTeachers: (list) => request('/api/v1/admin/teachers', { method: 'POST', body: list }),
  deleteTeacher: (id) => request(`/api/v1/admin/teacher/${id}`, { method: 'DELETE' }),
  adminCreateCourse: (body) => request('/api/v1/admin/createCourse', { method: 'POST', body }),
  adminUpdateTeacher: (teacherId, body) =>
    request(`/api/v1/admin/teachers/${teacherId}`, { method: 'PATCH', body }),
  coursesOfStudent: (studentId, page = 0, size = 20) =>
    request(`/api/v1/admin/${studentId}/enrolledCourses${qs({ page, size })}`, { method: 'POST' }),
}

// Azure Blob direct PUT against the SAS url the backend hands back.
export async function uploadToSas(uploadUrl, file) {
  const response = await fetch(uploadUrl, {
    method: 'PUT',
    headers: {
      'x-ms-blob-type': 'BlockBlob',
      'Content-Type': file.type || 'application/octet-stream',
    },
    body: file,
  })
  if (!response.ok) throw new Error(`Azure upload failed (${response.status})`)
}
