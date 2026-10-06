import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { formatDateTime } from '../format'
import { Alert, Empty, Section, Spinner, Stat, StatusPill } from '../components/ui'

export default function Dashboard() {
  const { isAdmin, isTeacher, isStudent, profile, user } = useAuth()
  const navigate = useNavigate()
  const [state, setState] = useState({ loading: true, error: '' })
  const [enrolled, setEnrolled] = useState(null)
  const [upcoming, setUpcoming] = useState(null)
  const [taught, setTaught] = useState(null)
  const [catalogue, setCatalogue] = useState(null)

  const load = useCallback(async () => {
    setState({ loading: true, error: '' })
    try {
      const jobs = [api.allCourses({}, 0, 6).then(setCatalogue)]
      if (isStudent) {
        jobs.push(api.myEnrolledCourses({}, 0, 6).then(setEnrolled))
        jobs.push(api.upcomingAssignments(0, 5).then(setUpcoming))
      }
      if (isTeacher) jobs.push(api.myTaughtCourses(0, 6).then(setTaught))
      await Promise.all(jobs)
      setState({ loading: false, error: '' })
    } catch (err) {
      setState({ loading: false, error: err.message })
    }
  }, [isStudent, isTeacher])

  useEffect(() => {
    load()
  }, [load])

  if (state.loading) return <Spinner />

  const taughtCourses = taught?.courses ?? []
  const pendingTotal = taughtCourses.reduce((sum, c) => sum + (c.pendingSubmissions ?? 0), 0)

  return (
    <>
      <Alert onClose={() => setState((s) => ({ ...s, error: '' }))}>{state.error}</Alert>

      <div className="stat-grid">
        <Stat label="Courses in catalogue" value={catalogue?.totalElements} />
        {isStudent && (
          <>
            <Stat label="Enrolled" value={enrolled?.totalElements} color="#33d6d0" />
            <Stat label="Upcoming work" value={upcoming?.totalElements} color="#ffc857" />
          </>
        )}
        {isTeacher && (
          <>
            <Stat label="Courses taught" value={taught?.totalElements} color="#33d6d0" />
            <Stat label="Pending submissions" value={pendingTotal} color="#ff7ac6" />
          </>
        )}
        {isAdmin && <Stat label="Role" value="ADMIN" color="#ff6b81" />}
      </div>

      {isStudent && (
        <Section
          title="Due soon"
          action={
            <button className="btn btn-sm" onClick={() => navigate('/upcoming')}>
              View all
            </button>
          }
        >
          {upcoming?.assignments?.length ? (
            <div className="glass table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Assignment</th>
                    <th>Course</th>
                    <th>Due</th>
                    <th>Left</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {upcoming.assignments.map((item) => (
                    <tr key={item.assignmentId}>
                      <td style={{ fontWeight: 600 }}>{item.title}</td>
                      <td className="muted">{item.courseName}</td>
                      <td className="muted">{formatDateTime(item.dueDate)}</td>
                      <td>
                        <span
                          className={`pill ${item.daysRemaining <= 2 ? 'pill-danger' : 'pill-accent'}`}
                        >
                          {item.daysRemaining}d
                        </span>
                      </td>
                      <td>
                        <StatusPill status={item.submissionStatus} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <Empty icon="✓">Nothing due right now.</Empty>
          )}
        </Section>
      )}

      {isStudent && (
        <Section
          title="My courses"
          action={
            <button className="btn btn-sm" onClick={() => navigate('/my-courses')}>
              View all
            </button>
          }
        >
          {enrolled?.enrolledCourses?.length ? (
            <div className="grid">
              {enrolled.enrolledCourses.map((course) => (
                <div
                  key={course.courseId}
                  className="glass course-card card-hover"
                  onClick={() => navigate(`/courses/${course.courseId}`)}
                >
                  <h4 className="course-title">{course.courseName}</h4>
                  <div className="course-meta">
                    <span className="pill">👤 {course.teacherName || 'Unassigned'}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <Empty icon="▤">
              You have not enrolled in anything yet — browse the catalogue to get started.
            </Empty>
          )}
        </Section>
      )}

      {isTeacher && (
        <Section
          title="Courses I teach"
          action={
            <button className="btn btn-sm" onClick={() => navigate('/teaching')}>
              Manage
            </button>
          }
        >
          {taughtCourses.length ? (
            <div className="grid">
              {taughtCourses.map((course) => (
                <div
                  key={course.id}
                  className="glass course-card card-hover"
                  onClick={() => navigate(`/courses/${course.id}`)}
                >
                  <h4 className="course-title">{course.name}</h4>
                  <p className="course-desc">{course.description || 'No description.'}</p>
                  <div className="course-meta">
                    <span className="pill">⚇ {course.enrolledCount} enrolled</span>
                    <span className="pill">✎ {course.assignmentCount} tasks</span>
                    {course.pendingSubmissions > 0 && (
                      <span className="pill pill-warn">{course.pendingSubmissions} pending</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <Empty icon="◈">You are not assigned to any course yet.</Empty>
          )}
        </Section>
      )}

      <Section
        title="Explore the catalogue"
        action={
          <button className="btn btn-sm" onClick={() => navigate('/courses')}>
            Browse all
          </button>
        }
      >
        {catalogue?.course?.length ? (
          <div className="grid">
            {catalogue.course.map((course) => (
              <div
                key={course.id}
                className="glass course-card card-hover"
                onClick={() => navigate(`/courses/${course.id}`)}
              >
                <h4 className="course-title">{course.name}</h4>
                <p className="course-desc">{course.description || 'No description.'}</p>
                <div className="course-meta">
                  <span className="pill">👤 {course.courseTeacher?.name || 'Unassigned'}</span>
                  {course.courseTeacher?.department && (
                    <span className="pill pill-accent">{course.courseTeacher.department}</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <Empty icon="▤">No courses published yet.</Empty>
        )}
      </Section>

      {!isStudent && !isTeacher && !isAdmin && (
        <Empty icon="⚠">
          Your account ({user?.username}) has no role assigned{profile ? '' : ' and no profile'}.
        </Empty>
      )}
    </>
  )
}
