import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { Alert, Empty, Pager, Spinner, Stat } from '../components/ui'
import { CourseModal } from './Courses'

export default function Teaching() {
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [creating, setCreating] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setData(await api.myTaughtCourses(page, 12))
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [page])

  useEffect(() => {
    load()
  }, [load])

  if (loading) return <Spinner />

  const courses = data?.courses ?? []
  const totals = courses.reduce(
    (acc, course) => ({
      enrolled: acc.enrolled + (course.enrolledCount ?? 0),
      assignments: acc.assignments + (course.assignmentCount ?? 0),
      pending: acc.pending + (course.pendingSubmissions ?? 0),
    }),
    { enrolled: 0, assignments: 0, pending: 0 },
  )

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>

      <div className="stat-grid">
        <Stat label="Courses" value={data?.totalElements} />
        <Stat label="Total students" value={totals.enrolled} color="#33d6d0" />
        <Stat label="Assignments" value={totals.assignments} color="#7c5cff" />
        <Stat label="Pending submissions" value={totals.pending} color="#ffc857" />
      </div>

      <div className="row spread" style={{ marginBottom: 16 }}>
        <h3 className="section-title">My courses</h3>
        <button className="btn btn-primary btn-sm" onClick={() => setCreating(true)}>
          + New course
        </button>
      </div>

      {courses.length ? (
        <>
          <div className="grid">
            {courses.map((course) => (
              <div
                key={course.id}
                className="glass course-card card-hover"
                onClick={() => navigate(`/courses/${course.id}`)}
              >
                <h4 className="course-title">{course.name}</h4>
                <p className="course-desc">{course.description || 'No description.'}</p>
                <div className="course-meta">
                  <span className="pill">⚇ {course.enrolledCount}</span>
                  <span className="pill">✎ {course.assignmentCount}</span>
                  {course.pendingSubmissions > 0 && (
                    <span className="pill pill-warn">{course.pendingSubmissions} pending</span>
                  )}
                </div>
              </div>
            ))}
          </div>
          <Pager page={page} totalPages={data?.totalPages} onChange={setPage} />
        </>
      ) : (
        <Empty icon="◈">You are not assigned to any course yet.</Empty>
      )}

      {creating && (
        <CourseModal
          onClose={() => setCreating(false)}
          onSaved={() => {
            setCreating(false)
            load()
          }}
        />
      )}
    </>
  )
}
