import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { Alert, Empty, Pager, Spinner } from '../components/ui'
import { FilterBar, MultiSelect, uniqueBy } from '../components/filters'

export default function MyCourses() {
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [courseNames, setCourseNames] = useState([])
  const [teacherNames, setTeacherNames] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setData(await api.myEnrolledCourses({}, page, 12))
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

  const unenroll = async (enrollmentId, name) => {
    if (!window.confirm(`Leave "${name}"?`)) return
    try {
      await api.unenroll(enrollmentId)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading) return <Spinner />

  const all = data?.enrolledCourses ?? []
  const courseOptions = uniqueBy(all, (c) => c.courseName)
  const teacherOptions = uniqueBy(all, (c) => c.teacherName)
  const courses = all.filter(
    (course) =>
      (!courseNames.length || courseNames.includes(course.courseName)) &&
      (!teacherNames.length || teacherNames.includes(course.teacherName)),
  )

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>

      {all.length > 0 && (
        <FilterBar
          instant
          active={courseNames.length + teacherNames.length}
          onReset={() => {
            setCourseNames([])
            setTeacherNames([])
          }}
          note="Narrows the courses shown on this page."
        >
          <MultiSelect
            label="Course"
            options={courseOptions}
            value={courseNames}
            onChange={setCourseNames}
          />
          <MultiSelect
            label="Teacher"
            options={teacherOptions}
            value={teacherNames}
            onChange={setTeacherNames}
          />
        </FilterBar>
      )}

      {courses.length ? (
        <>
          <div className="grid">
            {courses.map((course) => (
              <div key={course.courseId} className="glass course-card card-hover">
                <h4 className="course-title">{course.courseName}</h4>
                <div className="course-meta">
                  <span className="pill">👤 {course.teacherName || 'Unassigned'}</span>
                </div>
                <div className="row" style={{ marginTop: 6 }}>
                  <button
                    className="btn btn-sm"
                    onClick={() => navigate(`/courses/${course.courseId}`)}
                  >
                    Open
                  </button>
                  {course.enrollmentId && (
                    <button
                      className="btn btn-sm btn-danger"
                      onClick={() => unenroll(course.enrollmentId, course.courseName)}
                    >
                      Leave
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
          <Pager page={page} totalPages={data?.totalPages} onChange={setPage} />
        </>
      ) : all.length ? (
        <Empty icon="★">No enrolled course matched those filters.</Empty>
      ) : (
        <Empty icon="★">
          You have not enrolled in any course yet.
          <div style={{ marginTop: 16 }}>
            <button className="btn btn-primary btn-sm" onClick={() => navigate('/courses')}>
              Browse catalogue
            </button>
          </div>
        </Empty>
      )}
    </>
  )
}
