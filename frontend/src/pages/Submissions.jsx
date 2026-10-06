import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../api'
import { formatDateTime } from '../format'
import { Alert, Empty, Field, Modal, Pager, Spinner, Stat, StatusPill } from '../components/ui'
import { FilterBar, MultiSelect, uniqueBy } from '../components/filters'

export default function Submissions() {
  const { assignmentId } = useParams()
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [statuses, setStatuses] = useState([])
  const [students, setStudents] = useState([])
  const [grading, setGrading] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setData(await api.assignmentSubmissions(assignmentId, page, 20))
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [assignmentId, page])

  useEffect(() => {
    load()
  }, [load])

  const openFile = async (submissionId) => {
    try {
      const file = await api.submissionFile(submissionId)
      window.open(file.accessFile, '_blank', 'noopener,noreferrer')
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading) return <Spinner />
  if (!data)
    return (
      <>
        <Alert>{error}</Alert>
        <Empty icon="⚠">Submissions unavailable.</Empty>
      </>
    )

  const rows = data.submissions ?? []
  const visible = rows.filter(
    (row) =>
      (!statuses.length || statuses.includes(row.status ?? 'NOT SUBMITTED')) &&
      (!students.length || students.includes(row.studentName)),
  )

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>

      <div className="glass card" style={{ marginBottom: 22 }}>
        <button className="btn btn-sm btn-ghost" onClick={() => navigate(-1)}>
          ← Back
        </button>
        <h2 style={{ margin: '14px 0 6px', fontSize: 22 }}>{data.assignmentTitle}</h2>
        <div className="row">
          <span
            className="pill"
            style={{ cursor: 'pointer' }}
            onClick={() => navigate(`/courses/${data.courseId}`)}
          >
            ▤ {data.courseName}
          </span>
          <span className="pill">✓ {data.maxMarks} marks</span>
          <span
            className="pill"
            style={{ cursor: 'pointer' }}
            onClick={() => navigate(`/courses/${data.courseId}/grades`)}
          >
            ▦ Gradebook
          </span>
        </div>
      </div>

      <div className="stat-grid">
        <Stat label="Enrolled" value={data.totalEnrolled} />
        <Stat label="Submitted" value={data.totalSubmitted} color="#3ddc97" />
        <Stat
          label="Missing"
          value={Math.max(0, (data.totalEnrolled ?? 0) - (data.totalSubmitted ?? 0))}
          color="#ff6b81"
        />
      </div>

      {rows.length > 0 && (
        <FilterBar
          instant
          active={statuses.length + students.length}
          onReset={() => {
            setStatuses([])
            setStudents([])
          }}
          note="Narrows the submissions shown on this page."
        >
          <MultiSelect
            label="Status"
            options={uniqueBy(rows, (r) => r.status ?? 'NOT SUBMITTED')}
            value={statuses}
            onChange={setStatuses}
          />
          <MultiSelect
            label="Student"
            options={uniqueBy(rows, (r) => r.studentName)}
            value={students}
            onChange={setStudents}
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
                  <th>Status</th>
                  <th>Submitted at</th>
                  <th>Score</th>
                  <th>File</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((row) => (
                  <tr key={row.submissionId}>
                    <td style={{ fontWeight: 600 }}>
                      {row.studentName}
                      <div className="muted small">#{row.studentId}</div>
                    </td>
                    <td>
                      <StatusPill status={row.status} />
                    </td>
                    <td className="muted">{formatDateTime(row.submittedAt)}</td>
                    <td>
                      {row.marks != null ? (
                        <span className="pill pill-ok">
                          {row.marks} / {row.maxMarks ?? data.maxMarks}
                        </span>
                      ) : (
                        <span className="muted small">Not graded</span>
                      )}
                    </td>
                    <td className="muted small">{row.fileName || '—'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <div className="row" style={{ justifyContent: 'flex-end' }}>
                        <button
                          className="btn btn-sm"
                          disabled={!row.hasFile}
                          onClick={() => openFile(row.submissionId)}
                        >
                          Open file
                        </button>
                        <button
                          className="btn btn-sm btn-primary"
                          onClick={() => setGrading({ ...row, maxMarks: row.maxMarks ?? data.maxMarks })}
                        >
                          {row.marks != null ? 'Regrade' : 'Grade'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pager page={page} totalPages={data.totalPages} onChange={setPage} />
        </>
      ) : (
        <Empty icon="✎">
          {rows.length ? 'No submissions matched those filters.' : 'No submissions yet.'}
        </Empty>
      )}

      {grading && (
        <GradeModal
          submission={grading}
          onClose={() => setGrading(null)}
          onSaved={() => {
            setGrading(null)
            load()
          }}
        />
      )}
    </>
  )
}

function GradeModal({ submission, onClose, onSaved }) {
  const [marks, setMarks] = useState(submission.marks ?? '')
  const [feedback, setFeedback] = useState(submission.feedback ?? '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const save = async () => {
    const value = Number(marks)
    if (marks === '' || Number.isNaN(value)) return setError('Enter a mark.')
    if (value < 0 || value > submission.maxMarks)
      return setError(`Mark must be between 0 and ${submission.maxMarks}.`)
    setSaving(true)
    try {
      await api.gradeSubmission(submission.submissionId, { marks: value, feedback })
      onSaved()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal
      title={`Grade · ${submission.studentName}`}
      onClose={onClose}
      footer={
        <>
          <button className="btn btn-ghost" onClick={onClose}>
            Cancel
          </button>
          <button className="btn btn-primary" disabled={saving} onClick={save}>
            {saving ? 'Saving…' : 'Save grade'}
          </button>
        </>
      }
    >
      <Alert onClose={() => setError('')}>{error}</Alert>
      <Field label={`Marks (out of ${submission.maxMarks})`}>
        <input
          className="input"
          type="number"
          min={0}
          max={submission.maxMarks}
          value={marks}
          onChange={(event) => setMarks(event.target.value)}
        />
      </Field>
      <Field label="Feedback">
        <textarea
          className="input"
          rows={4}
          value={feedback}
          placeholder="Optional notes for the student"
          onChange={(event) => setFeedback(event.target.value)}
        />
      </Field>
    </Modal>
  )
}
