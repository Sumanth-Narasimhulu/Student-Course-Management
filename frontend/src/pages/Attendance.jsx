import { useCallback, useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import { Alert, Empty, Field, Spinner, Stat } from '../components/ui'

const STATUSES = ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED']

const TONE = {
  PRESENT: '#3ddc97',
  ABSENT: '#ff6b81',
  LATE: '#ffc857',
  EXCUSED: '#33d6d0',
}

function today() {
  return new Date().toISOString().slice(0, 10)
}

export default function Attendance() {
  const { roles } = useAuth()
  const isAdmin = roles.includes('ADMIN')
  const [courses, setCourses] = useState([])
  const [courseId, setCourseId] = useState('')
  const [date, setDate] = useState(today())
  const [sheet, setSheet] = useState(null)
  const [marks, setMarks] = useState({})
  const [loadingCourses, setLoadingCourses] = useState(true)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    const fetchCourses = isAdmin
      ? api.allCourses({}, 0, 200).then((res) => res.course ?? [])
      : api.myTaughtCourses(0, 200).then((res) => res.courses ?? [])

    fetchCourses
      .then((list) => {
        setCourses(list)
        if (list.length) setCourseId(String(list[0].id))
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoadingCourses(false))
  }, [isAdmin])

  const load = useCallback(async () => {
    if (!courseId) return
    setLoading(true)
    try {
      const data = await api.courseAttendance(courseId, date)
      setSheet(data)
      setMarks(
        Object.fromEntries(
          (data.records ?? []).map((row) => [row.studentId, row.status ?? 'PRESENT']),
        ),
      )
      setError('')
    } catch (err) {
      setError(err.message)
      setSheet(null)
    } finally {
      setLoading(false)
    }
  }, [courseId, date])

  useEffect(() => {
    load()
  }, [load])

  const setAll = (status) =>
    setMarks(Object.fromEntries((sheet?.records ?? []).map((row) => [row.studentId, status])))

  const save = async () => {
    setSaving(true)
    try {
      const result = await api.markAttendance(courseId, {
        date,
        records: Object.entries(marks).map(([studentId, status]) => ({
          studentId: Number(studentId),
          status,
        })),
      })
      setNotice(`${result.created} created · ${result.updated} updated`)
      setError('')
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  if (loadingCourses) return <Spinner />
  if (!courses.length)
    return (
      <>
        <Alert onClose={() => setError('')}>{error}</Alert>
        <Empty icon="◈">You have no courses to take attendance for.</Empty>
      </>
    )

  const rows = sheet?.records ?? []
  const tally = STATUSES.map((status) => ({
    status,
    count: Object.values(marks).filter((value) => value === status).length,
  }))

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>
      {notice && (
        <Alert kind="ok" onClose={() => setNotice('')}>
          {notice}
        </Alert>
      )}

      <div className="glass card" style={{ marginBottom: 22 }}>
        <div className="filter-row">
          <Field label="Course">
            <select
              className="input"
              value={courseId}
              onChange={(event) => setCourseId(event.target.value)}
            >
              {courses.map((course) => (
                <option key={course.id} value={course.id}>
                  {course.name}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Date">
            <input
              className="input"
              type="date"
              value={date}
              max={today()}
              onChange={(event) => setDate(event.target.value)}
            />
          </Field>
        </div>
      </div>

      {loading ? (
        <Spinner />
      ) : !rows.length ? (
        <Empty icon="⚇">Nobody is enrolled in this course yet.</Empty>
      ) : (
        <>
          <div className="stat-grid">
            <Stat label="Enrolled" value={sheet.totalEnrolled} />
            {tally.map((item) => (
              <Stat
                key={item.status}
                label={item.status.charAt(0) + item.status.slice(1).toLowerCase()}
                value={item.count}
                color={TONE[item.status]}
              />
            ))}
          </div>

          <div className="row spread" style={{ marginBottom: 16 }}>
            <h3 className="section-title">
              Roster
              <span className="muted small" style={{ marginLeft: 10, fontWeight: 400 }}>
                {sheet.totalMarked} already marked for this date
              </span>
            </h3>
            <div className="row">
              {STATUSES.map((status) => (
                <button
                  key={status}
                  className="btn btn-sm btn-ghost"
                  onClick={() => setAll(status)}
                >
                  All {status.toLowerCase()}
                </button>
              ))}
            </div>
          </div>

          <div className="glass table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Student</th>
                  <th style={{ textAlign: 'right' }}>Status</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.studentId}>
                    <td style={{ fontWeight: 600 }}>
                      {row.studentName}
                      <div className="muted small">
                        #{row.studentId}
                        {row.attendanceId ? ' · already recorded' : ''}
                      </div>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div className="row" style={{ justifyContent: 'flex-end' }}>
                        {STATUSES.map((status) => {
                          const on = marks[row.studentId] === status
                          return (
                            <button
                              key={status}
                              className={`btn btn-sm ${on ? '' : 'btn-ghost'}`}
                              style={
                                on
                                  ? { background: TONE[status], color: '#0b1020', borderColor: 'transparent' }
                                  : undefined
                              }
                              onClick={() =>
                                setMarks((prev) => ({ ...prev, [row.studentId]: status }))
                              }
                            >
                              {status.charAt(0)}
                            </button>
                          )
                        })}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="row" style={{ marginTop: 18, justifyContent: 'flex-end' }}>
            <button className="btn btn-primary" disabled={saving} onClick={save}>
              {saving ? 'Saving…' : 'Save attendance'}
            </button>
          </div>
        </>
      )}
    </>
  )
}
