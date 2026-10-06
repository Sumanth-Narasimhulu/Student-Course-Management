import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../api'
import { formatDateTime } from '../format'
import { Alert, Empty, Pager, Spinner, Stat } from '../components/ui'
import { FilterBar, MultiSelect, uniqueBy } from '../components/filters'

export default function CourseGrades() {
  const { courseId } = useParams()
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [students, setStudents] = useState([])
  const [assignments, setAssignments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setData(await api.courseGrades(courseId, page, 50))
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [courseId, page])

  useEffect(() => {
    load()
  }, [load])

  if (loading) return <Spinner />
  if (!data)
    return (
      <>
        <Alert>{error}</Alert>
        <Empty icon="⚠">Gradebook unavailable.</Empty>
      </>
    )

  const rows = data.grades ?? []
  const visible = rows.filter(
    (row) =>
      (!students.length || students.includes(row.studentName)) &&
      (!assignments.length || assignments.includes(row.assignment)),
  )

  const scored = rows.filter((row) => row.marks != null && row.maxMarks)
  const average = scored.length
    ? Math.round(
        (scored.reduce((acc, row) => acc + (row.marks / row.maxMarks) * 100, 0) / scored.length) *
          10,
      ) / 10
    : null

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>

      <div className="glass card" style={{ marginBottom: 22 }}>
        <button className="btn btn-sm btn-ghost" onClick={() => navigate(-1)}>
          ← Back
        </button>
        <h2 style={{ margin: '14px 0 6px', fontSize: 22 }}>{data.courseName}</h2>
        <span
          className="pill"
          style={{ cursor: 'pointer' }}
          onClick={() => navigate(`/courses/${data.courseId}`)}
        >
          ▤ Course page
        </span>
      </div>

      <div className="stat-grid">
        <Stat label="Graded entries" value={data.totalElements ?? rows.length} />
        <Stat label="Class average" value={average != null ? `${average}%` : '—'} color="#3ddc97" />
        <Stat
          label="Students graded"
          value={new Set(rows.map((row) => row.studentId)).size}
          color="#33d6d0"
        />
      </div>

      {rows.length > 0 && (
        <FilterBar
          instant
          active={students.length + assignments.length}
          onReset={() => {
            setStudents([])
            setAssignments([])
          }}
          note="Narrows the rows shown on this page."
        >
          <MultiSelect
            label="Student"
            options={uniqueBy(rows, (r) => r.studentName)}
            value={students}
            onChange={setStudents}
          />
          <MultiSelect
            label="Assignment"
            options={uniqueBy(rows, (r) => r.assignment)}
            value={assignments}
            onChange={setAssignments}
          />
        </FilterBar>
      )}

      {visible.length ? (
        <>
          <div className="glass table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Assignment</th>
                  <th>Score</th>
                  <th>Feedback</th>
                  <th>Graded</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((row) => (
                  <tr key={row.submissionId}>
                    <td style={{ fontWeight: 600 }}>
                      {row.studentName}
                      <div className="muted small">#{row.studentId}</div>
                    </td>
                    <td className="muted">{row.assignment}</td>
                    <td>
                      <span className="pill pill-ok">
                        {row.marks} / {row.maxMarks}
                      </span>
                    </td>
                    <td className="muted small">{row.feedback || '—'}</td>
                    <td className="muted">{formatDateTime(row.gradedAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pager page={page} totalPages={data.totalPages} onChange={setPage} />
        </>
      ) : (
        <Empty icon="✓">
          {rows.length ? 'No rows matched those filters.' : 'Nothing has been graded yet.'}
        </Empty>
      )}
    </>
  )
}
