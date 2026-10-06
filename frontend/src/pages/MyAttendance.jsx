import { useEffect, useState } from 'react'
import { api } from '../api'
import { formatDate } from '../format'
import { Alert, Empty, Pager, Spinner, Stat, StatusPill } from '../components/ui'
import { FilterBar, MultiSelect, uniqueBy } from '../components/filters'

function barColor(percentage) {
  if (percentage == null) return '#8892b0'
  if (percentage >= 75) return '#3ddc97'
  if (percentage >= 60) return '#ffc857'
  return '#ff6b81'
}

export default function MyAttendance() {
  const [summary, setSummary] = useState(null)
  const [records, setRecords] = useState(null)
  const [page, setPage] = useState(0)
  const [courses, setCourses] = useState([])
  const [statuses, setStatuses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    Promise.all([api.myAttendanceSummary(), api.myAttendance(page, 50)])
      .then(([sum, recs]) => {
        if (cancelled) return
        setSummary(sum)
        setRecords(recs)
        setError('')
      })
      .catch((err) => !cancelled && setError(err.message))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [page])

  if (loading) return <Spinner />

  const rows = records?.attendance ?? []
  const visible = rows.filter(
    (row) =>
      (!courses.length || courses.includes(row.course)) &&
      (!statuses.length || statuses.includes(row.status)),
  )
  const perCourse = summary?.summary ?? []

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>

      <div className="stat-grid">
        <Stat
          label="Overall attendance"
          value={summary?.overallPercentage != null ? `${summary.overallPercentage}%` : '—'}
          color={barColor(summary?.overallPercentage)}
        />
        <Stat label="Courses tracked" value={perCourse.length} color="#33d6d0" />
        <Stat
          label="Classes attended"
          value={perCourse.reduce((acc, item) => acc + (item.present ?? 0), 0)}
          color="#7c5cff"
        />
        <Stat
          label="Classes missed"
          value={perCourse.reduce((acc, item) => acc + (item.absent ?? 0), 0)}
          color="#ff6b81"
        />
      </div>

      <h3 className="section-title" style={{ marginBottom: 16 }}>
        By course
      </h3>
      {perCourse.length ? (
        <div className="grid" style={{ marginBottom: 26 }}>
          {perCourse.map((item) => (
            <div key={item.courseId} className="glass course-card">
              <h4 className="course-title">{item.course}</h4>
              <div
                style={{
                  height: 8,
                  borderRadius: 999,
                  background: 'rgba(255,255,255,0.08)',
                  overflow: 'hidden',
                  margin: '12px 0',
                }}
              >
                <div
                  style={{
                    width: `${Math.min(100, item.percentage ?? 0)}%`,
                    height: '100%',
                    background: barColor(item.percentage),
                  }}
                />
              </div>
              <div className="row spread">
                <span style={{ fontWeight: 700, color: barColor(item.percentage) }}>
                  {item.percentage != null ? `${item.percentage}%` : '—'}
                </span>
                <span className="muted small">{item.totalClasses} classes</span>
              </div>
              <div className="course-meta" style={{ marginTop: 10 }}>
                <span className="pill pill-ok">{item.present} present</span>
                <span className="pill pill-danger">{item.absent} absent</span>
                {item.late > 0 && <span className="pill pill-warn">{item.late} late</span>}
                {item.excused > 0 && <span className="pill">{item.excused} excused</span>}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <Empty icon="⊙">No attendance has been recorded for you yet.</Empty>
      )}

      <h3 className="section-title" style={{ marginBottom: 16 }}>
        Class log
      </h3>

      {rows.length > 0 && (
        <FilterBar
          instant
          active={courses.length + statuses.length}
          onReset={() => {
            setCourses([])
            setStatuses([])
          }}
          note="Narrows the records shown on this page."
        >
          <MultiSelect
            label="Course"
            options={uniqueBy(rows, (r) => r.course)}
            value={courses}
            onChange={setCourses}
          />
          <MultiSelect
            label="Status"
            options={uniqueBy(rows, (r) => r.status)}
            value={statuses}
            onChange={setStatuses}
          />
        </FilterBar>
      )}

      {visible.length ? (
        <>
          <div className="glass table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Course</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((row) => (
                  <tr key={row.attendanceId}>
                    <td style={{ fontWeight: 600 }}>{formatDate(row.date)}</td>
                    <td className="muted">{row.course}</td>
                    <td>
                      <StatusPill status={row.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pager page={page} totalPages={records?.totalPages} onChange={setPage} />
        </>
      ) : (
        <Empty icon="⊙">
          {rows.length ? 'No records matched those filters.' : 'No class records yet.'}
        </Empty>
      )}
    </>
  )
}
