import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth'
import Layout from './components/Layout'
import { Spinner } from './components/ui'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Courses from './pages/Courses'
import CourseDetail from './pages/CourseDetail'
import MyCourses from './pages/MyCourses'
import Upcoming from './pages/Upcoming'
import Teaching from './pages/Teaching'
import Submissions from './pages/Submissions'
import MyGrades from './pages/MyGrades'
import MyAttendance from './pages/MyAttendance'
import Attendance from './pages/Attendance'
import CourseGrades from './pages/CourseGrades'
import Students from './pages/Students'
import Teachers from './pages/Teachers'
import Departments from './pages/Departments'
import Admin from './pages/Admin'
import Profile from './pages/Profile'

function Guard({ roles, children }) {
  const auth = useAuth()
  if (auth.loading) return <Spinner />
  if (!auth.user) return <Navigate to="/login" replace />
  if (roles && !roles.some((role) => auth.roles.includes(role)))
    return <Navigate to="/" replace />
  return children
}

function Shell() {
  const { user, loading } = useAuth()
  if (loading) return <Spinner />
  if (!user) return <Navigate to="/login" replace />
  return <Layout />
}

function LoginRoute() {
  const { user, loading } = useAuth()
  if (loading) return <Spinner />
  return user ? <Navigate to="/" replace /> : <Login />
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginRoute />} />
          <Route element={<Shell />}>
            <Route index element={<Dashboard />} />
            <Route path="courses" element={<Courses />} />
            <Route path="courses/:courseId" element={<CourseDetail />} />
            <Route
              path="my-courses"
              element={
                <Guard roles={['STUDENT']}>
                  <MyCourses />
                </Guard>
              }
            />
            <Route
              path="upcoming"
              element={
                <Guard roles={['STUDENT']}>
                  <Upcoming />
                </Guard>
              }
            />
            <Route
              path="teaching"
              element={
                <Guard roles={['TEACHER']}>
                  <Teaching />
                </Guard>
              }
            />
            <Route
              path="assignments/:assignmentId/submissions"
              element={
                <Guard roles={['TEACHER', 'ADMIN']}>
                  <Submissions />
                </Guard>
              }
            />
            <Route
              path="courses/:courseId/grades"
              element={
                <Guard roles={['TEACHER', 'ADMIN']}>
                  <CourseGrades />
                </Guard>
              }
            />
            <Route
              path="attendance"
              element={
                <Guard roles={['TEACHER', 'ADMIN']}>
                  <Attendance />
                </Guard>
              }
            />
            <Route
              path="my-grades"
              element={
                <Guard roles={['STUDENT']}>
                  <MyGrades />
                </Guard>
              }
            />
            <Route
              path="my-attendance"
              element={
                <Guard roles={['STUDENT']}>
                  <MyAttendance />
                </Guard>
              }
            />
            <Route
              path="students"
              element={
                <Guard roles={['TEACHER', 'ADMIN']}>
                  <Students />
                </Guard>
              }
            />
            <Route path="teachers" element={<Teachers />} />
            <Route
              path="departments"
              element={
                <Guard roles={['ADMIN']}>
                  <Departments />
                </Guard>
              }
            />
            <Route
              path="admin"
              element={
                <Guard roles={['ADMIN']}>
                  <Admin />
                </Guard>
              }
            />
            <Route path="profile" element={<Profile />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
