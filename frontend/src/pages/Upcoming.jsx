import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { formatDateTime } from '../format'
import { Alert, Empty, Pager, Spinner, StatusPill } from '../components/ui'
import { FilterBar, MultiSelect, uniqueBy } from '../components/filters'
import { SubmitModal } from './CourseDetail'

export default function Upcoming() {
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [courseNames, setCourseNames] = useState([])
  const [statuses, setStatuses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [submitFor, setSubmitFor] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setData(await api.upcomingAssignments(page, 15))
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

  const all = data?.assignments ?? []
  const items = all.filter(
    (item) =>
      (!courseNames.length || courseNames.includes(item.courseName)) &&
      (!statuses.length || statuses.includes(item.submissionStatus ?? 'NOT SUBMITTED')),
  )

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>
      <Alert kind="ok" onClose={() => setNotice('')}>
        {notice}
      </Alert>

      {all.length > 0 && (
        <FilterBar
          instant
          active={courseNames.length + statuses.length}
          onReset={() => {
            setCourseNames([])
            setStatuses([])
          }}
          note="Narrows the assignments shown on this page."
        >
          <MultiSelect
            label="Course"
            options={uniqueBy(all, (a) => a.courseName)}
            value={courseNames}
            onChange={setCourseNames}
          />
          <MultiSelect
            label="Status"
            options={uniqueBy(all, (a) => a.submissionStatus ?? 'NOT SUBMITTED')}
            value={statuses}
            onChange={setStatuses}
          />
        </FilterBar>
      )}

      {items.length ? (
        <>
          <div className="grid">
            {items.map((item) => (
              <div key={item.assignmentId} className="glass course-card card-hover">
                <div className="row spread">
                  <h4 className="course-title">{item.title}</h4>
                  <span
                    className={`pill ${
                      item.daysRemaining <= 1
                        ? 'pill-danger'
                        : item.daysRemaining <= 3
                          ? 'pill-warn'
                          : 'pill-accent'
                    }`}
                  >
                    {item.daysRemaining}d left
                  </span>
                </div>
                <p className="course-desc">{item.description || 'No description.'}</p>
                <div className="course-meta">
                  <span
                    className="pill"
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/courses/${item.courseId}`)}
                  >
                    ▤ {item.courseName}
                  </span>
                  <span className="pill">✓ {item.maxMarks} marks</span>
                  <StatusPill status={item.submissionStatus} />
                </div>
                <div className="row spread" style={{ marginTop: 4 }}>
                  <span className="muted small">Due {formatDateTime(item.dueDate)}</span>
                  {item.submissionStatus !== 'SUBMITTED' && (
                    <button className="btn btn-primary btn-sm" onClick={() => setSubmitFor(item)}>
                      Submit
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
          <Pager page={page} totalPages={data?.totalPages} onChange={setPage} />
        </>
      ) : (
        <Empty icon="✓">
          {all.length ? 'No assignment matched those filters.' : 'Nothing due. You are all caught up.'}
        </Empty>
      )}

      {submitFor && (
        <SubmitModal
          assignment={submitFor}
          onClose={() => setSubmitFor(null)}
          onDone={() => {
            setSubmitFor(null)
            setNotice('Assignment submitted.')
            load()
          }}
        />
      )}
    </>
  )
}
