import { useCallback, useEffect, useState } from 'react'
import { api } from '../api'
import { formatDateTime } from '../format'
import { Alert, Empty, Pager, Spinner, Stat, StatusPill } from '../components/ui'
import { FilterBar, MultiSelect, uniqueBy } from '../components/filters'

function pct(marks, maxMarks) {
  if (marks == null || !maxMarks) return null
  return Math.round((marks / maxMarks) * 1000) / 10
}

export default function MyGrades() {
  const [tab, setTab] = useState('grades')
  const [grades, setGrades] = useState(null)
  const [subs, setSubs] = useState(null)
  const [page, setPage] = useState(0)
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    setPage(0)
    setCourses([])
  }, [tab])

  const load = useCallback(async () => {
    setLoading(true)
    try {
      if (tab === 'grades') setGrades(await api.myGrades(page, 20))
      else setSubs(await api.mySubmissions(page, 20))
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [tab, page])

  useEffect(() => {
    load()
  }, [load])

  const gradeRows = grades?.grades ?? []
  const subRows = subs?.submissions ?? []
  const rows = tab === 'grades' ? gradeRows : subRows
  const visible = rows.filter(
    (row) => !courses.length || courses.includes(row.course ?? row.courseName),
  )

  const graded = gradeRows.filter((row) => row.marks != null)
  const best = graded.reduce(
    (acc, row) => Math.max(acc, pct(row.marks, row.maxMarks) ?? 0),
    0,
  )

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>

      <div className="stat-grid">
        <Stat
          label="Overall"
          value={grades?.overallPercentage != null ? `${grades.overallPercentage}%` : '—'}
          color="#3ddc97"
        />
        <Stat label="Graded items" value={grades?.totalElements ?? graded.length} color="#33d6d0" />
        <Stat label="Best score" value={best ? `${best}%` : '—'} color="#7c5cff" />
        <Stat label="Submissions" value={subs?.totalElements ?? '—'} color="#ffc857" />
      </div>

      <div className="row" style={{ marginBottom: 16 }}>
        <button
          className={`btn btn-sm ${tab === 'grades' ? 'btn-primary' : 'btn-ghost'}`}
          onClick={() => setTab('grades')}
        >
          Grades
        </button>
        <button
          className={`btn btn-sm ${tab === 'submissions' ? 'btn-primary' : 'btn-ghost'}`}
          onClick={() => setTab('submissions')}
        >
          All submissions
        </button>
      </div>

      {loading ? (
        <Spinner />
      ) : (
        <>
          {rows.length > 0 && (
            <FilterBar
              instant
              active={courses.length}
              onReset={() => setCourses([])}
              note="Narrows the rows shown on this page."
            >
              <MultiSelect
                label="Course"
                options={uniqueBy(rows, (r) => r.course ?? r.courseName)}
                value={courses}
                onChange={setCourses}
              />
            </FilterBar>
          )}

          {visible.length === 0 ? (
            <Empty icon="✓">
              {rows.length ? 'Nothing matched that filter.' : 'Nothing here yet.'}
            </Empty>
          ) : tab === 'grades' ? (
            <div className="glass table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Assignment</th>
                    <th>Course</th>
                    <th>Score</th>
                    <th>Feedback</th>
                    <th>Graded</th>
                  </tr>
                </thead>
                <tbody>
                  {visible.map((row) => (
                    <tr key={row.submissionId}>
                      <td style={{ fontWeight: 600 }}>{row.assignment}</td>
                      <td className="muted">{row.course}</td>
                      <td>
                        <span className="pill pill-ok">
                          {row.marks} / {row.maxMarks}
                        </span>
                        <div className="muted small">{pct(row.marks, row.maxMarks)}%</div>
                      </td>
                      <td className="muted small">{row.feedback || '—'}</td>
                      <td className="muted">{formatDateTime(row.gradedAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="glass table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Assignment</th>
                    <th>Course</th>
                    <th>Status</th>
                    <th>Submitted</th>
                    <th>Score</th>
                  </tr>
                </thead>
                <tbody>
                  {visible.map((row) => (
                    <tr key={row.submissionId}>
                      <td style={{ fontWeight: 600 }}>
                        {row.assignmentTitle}
                        <div className="muted small">{row.fileName || 'No file'}</div>
                      </td>
                      <td className="muted">{row.courseName}</td>
                      <td>
                        <StatusPill status={row.status} />
                      </td>
                      <td className="muted">{formatDateTime(row.submittedAt)}</td>
                      <td>
                        {row.marks != null ? (
                          <span className="pill pill-ok">
                            {row.marks} / {row.maxMarks}
                          </span>
                        ) : (
                          <span className="muted small">Not graded</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <Pager
            page={page}
            totalPages={tab === 'grades' ? grades?.totalPages : subs?.totalPages}
            onChange={setPage}
          />
        </>
      )}
    </>
  )
}
