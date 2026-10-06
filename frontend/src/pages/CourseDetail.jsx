import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api, uploadToSas } from '../api'
import { useAuth } from '../auth'
import { formatDateTime, toLocalDateTime } from '../format'
import { Alert, Empty, Field, Modal, Section, Spinner, Stat } from '../components/ui'
import { CourseModal } from './Courses'

export default function CourseDetail() {
  const { courseId } = useParams()
  const navigate = useNavigate()
  const { isStudent, isTeacher, isAdmin } = useAuth()

  const [course, setCourse] = useState(null)
  const [assignments, setAssignments] = useState([])
  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [editing, setEditing] = useState(false)
  const [assignmentModal, setAssignmentModal] = useState(null)
  const [submitFor, setSubmitFor] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const detail = await api.course(courseId)
      setCourse(detail)

      // Both are permission-gated; a 403 here should not blank the page.
      const [assignmentsRes, studentsRes] = await Promise.all([
        api.courseAssignments(courseId, 0, 50).catch(() => null),
        detail.canEdit ? api.courseStudents(courseId, 0, 100).catch(() => null) : null,
      ])
      setAssignments(assignmentsRes?.assignments ?? [])
      setStudents(studentsRes?.students ?? [])
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [courseId])

  useEffect(() => {
    load()
  }, [load])

  const enroll = async () => {
    try {
      await api.enroll(courseId)
      setNotice('Enrolled successfully.')
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const removeAssignment = async (id) => {
    if (!window.confirm('Delete this assignment?')) return
    try {
      await api.deleteAssignment(id)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const removeCourse = async () => {
    if (!window.confirm(`Delete "${course.name}"?`)) return
    try {
      await api.deleteCourse(courseId)
      navigate('/courses')
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading) return <Spinner />
  if (!course)
    return (
      <>
        <Alert>{error}</Alert>
        <Empty icon="⚠">Course unavailable.</Empty>
      </>
    )

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>
      <Alert kind="ok" onClose={() => setNotice('')}>
        {notice}
      </Alert>

      <div className="glass card" style={{ marginBottom: 22 }}>
        <div className="row spread" style={{ alignItems: 'flex-start' }}>
          <div style={{ maxWidth: 640 }}>
            <button className="btn btn-sm btn-ghost" onClick={() => navigate(-1)}>
              ← Back
            </button>
            <h2 style={{ margin: '14px 0 8px', fontSize: 24 }}>{course.name}</h2>
            <p className="muted" style={{ margin: 0, lineHeight: 1.7, fontSize: 14 }}>
              {course.description || 'No description provided.'}
            </p>
            <div className="row" style={{ marginTop: 16 }}>
              <span className="pill">👤 {course.teacher?.name || 'Unassigned'}</span>
              {course.teacher?.departmentName && (
                <span className="pill pill-accent">{course.teacher.departmentName}</span>
              )}
              {course.isEnrolled && <span className="pill pill-ok">Enrolled</span>}
            </div>
          </div>
          <div className="row">
            {isStudent && !course.isEnrolled && (
              <button className="btn btn-primary btn-sm" onClick={enroll}>
                Enroll
              </button>
            )}
            {course.canEdit && (
              <button className="btn btn-sm" onClick={() => setEditing(true)}>
                Edit
              </button>
            )}
            {isAdmin && (
              <button className="btn btn-danger btn-sm" onClick={removeCourse}>
                Delete
              </button>
            )}
          </div>
        </div>
      </div>

      <div className="stat-grid">
        <Stat label="Enrolled students" value={course.enrolledCount} />
        <Stat label="Assignments" value={course.assignmentCount} color="#33d6d0" />
        <Stat label="Created" value={formatDateTime(course.createdAt).split(',')[0]} color="#ff7ac6" />
      </div>

      <Section
        title="Assignments"
        action={
          course.canEdit && (
            <button className="btn btn-primary btn-sm" onClick={() => setAssignmentModal({})}>
              + New assignment
            </button>
          )
        }
      >
        {assignments.length ? (
          <div className="glass table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Due</th>
                  <th>Max marks</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {assignments.map((item) => (
                  <tr key={item.id}>
                    <td style={{ fontWeight: 600 }}>{item.title}</td>
                    <td className="muted">{formatDateTime(item.dueDate)}</td>
                    <td>
                      <span className="pill">{item.maxMarks}</span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div className="row" style={{ justifyContent: 'flex-end' }}>
                        {isStudent && course.isEnrolled && (
                          <button className="btn btn-sm" onClick={() => setSubmitFor(item)}>
                            Submit
                          </button>
                        )}
                        {(isTeacher || isAdmin) && (
                          <button
                            className="btn btn-sm"
                            onClick={() => navigate(`/assignments/${item.id}/submissions`)}
                          >
                            Submissions
                          </button>
                        )}
                        {course.canEdit && (
                          <>
                            <button
                              className="btn btn-sm btn-ghost"
                              onClick={() => setAssignmentModal(item)}
                            >
                              Edit
                            </button>
                            <button
                              className="btn btn-sm btn-danger"
                              onClick={() => removeAssignment(item.id)}
                            >
                              ✕
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <Empty icon="✎">No assignments yet.</Empty>
        )}
      </Section>

      {course.canEdit && (
        <Section title={`Enrolled students (${students.length})`}>
          {students.length ? (
            <div className="glass table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                  </tr>
                </thead>
                <tbody>
                  {students.map((student) => (
                    <tr key={student.studentId}>
                      <td className="muted">#{student.studentId}</td>
                      <td style={{ fontWeight: 600 }}>{student.name}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <Empty icon="⚇">Nobody has enrolled yet.</Empty>
          )}
        </Section>
      )}

      {editing && (
        <CourseModal
          isAdmin={isAdmin}
          course={course}
          onClose={() => setEditing(false)}
          onSaved={() => {
            setEditing(false)
            load()
          }}
        />
      )}

      {assignmentModal && (
        <AssignmentModal
          courseId={courseId}
          assignment={assignmentModal.id ? assignmentModal : null}
          onClose={() => setAssignmentModal(null)}
          onSaved={() => {
            setAssignmentModal(null)
            load()
          }}
        />
      )}

      {submitFor && (
        <SubmitModal
          assignment={submitFor}
          onClose={() => setSubmitFor(null)}
          onDone={() => {
            setSubmitFor(null)
            setNotice('Assignment submitted.')
          }}
        />
      )}
    </>
  )
}

function AssignmentModal({ courseId, assignment, onClose, onSaved }) {
  const [form, setForm] = useState({
    title: assignment?.title ?? '',
    description: assignment?.description ?? '',
    dueDate: assignment?.dueDate ? String(assignment.dueDate).slice(0, 16) : '',
    maxMarks: assignment?.maxMarks ?? 100,
  })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    const body = {
      title: form.title.trim(),
      description: form.description.trim(),
      dueDate: toLocalDateTime(form.dueDate),
      maxMarks: Number(form.maxMarks),
      courseId: Number(courseId),
    }
    try {
      if (assignment) await api.updateAssignment(assignment.id, body)
      else await api.createAssignment(courseId, body)
      onSaved()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal title={assignment ? 'Edit assignment' : 'New assignment'} onClose={onClose}>
      <Alert onClose={() => setError('')}>{error}</Alert>
      <form onSubmit={submit}>
        <Field label="Title">
          <input
            className="input"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
          />
        </Field>
        <Field label="Description">
          <textarea
            className="textarea"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </Field>
        <div className="form-row">
          <Field label="Due date">
            <input
              className="input"
              type="datetime-local"
              value={form.dueDate}
              onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
              required
            />
          </Field>
          <Field label="Max marks">
            <input
              className="input"
              type="number"
              min="1"
              value={form.maxMarks}
              onChange={(e) => setForm({ ...form, maxMarks: e.target.value })}
              required
            />
          </Field>
        </div>
        <div className="row" style={{ justifyContent: 'flex-end', marginTop: 20 }}>
          <button type="button" className="btn btn-ghost btn-sm" onClick={onClose}>
            Cancel
          </button>
          <button className="btn btn-primary btn-sm" disabled={busy}>
            {busy ? 'Saving…' : 'Save'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

export function SubmitModal({ assignment, onClose, onDone }) {
  const [file, setFile] = useState(null)
  const [stage, setStage] = useState('')
  const [error, setError] = useState('')

  const submit = async (event) => {
    event.preventDefault()
    if (!file) return
    setError('')
    try {
      setStage('Requesting upload link…')
      const ticket = await api.initiateUpload(assignment.id ?? assignment.assignmentId, {
        fileName: file.name,
        contentType: file.type || 'application/octet-stream',
        fileSize: file.size,
      })
      setStage('Uploading to storage…')
      await uploadToSas(ticket.uploadUrl, file)
      setStage('Confirming…')
      await api.completeUpload(ticket.submissionId)
      onDone()
    } catch (err) {
      setError(err.message)
      setStage('')
    }
  }

  return (
    <Modal title={`Submit — ${assignment.title}`} onClose={onClose}>
      <Alert onClose={() => setError('')}>{error}</Alert>
      <form onSubmit={submit}>
        <label className="file-drop">
          <input type="file" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
          {file ? (
            <>
              <strong>{file.name}</strong>
              <div className="muted small" style={{ marginTop: 6 }}>
                {(file.size / 1024).toFixed(1)} KB — click to change
              </div>
            </>
          ) : (
            <>
              <div style={{ fontSize: 26, marginBottom: 8 }}>⬆</div>
              <div className="muted small">Click to choose a file</div>
            </>
          )}
        </label>
        {stage && (
          <p className="muted small" style={{ marginTop: 14 }}>
            {stage}
          </p>
        )}
        <div className="row" style={{ justifyContent: 'flex-end', marginTop: 20 }}>
          <button type="button" className="btn btn-ghost btn-sm" onClick={onClose}>
            Cancel
          </button>
          <button className="btn btn-primary btn-sm" disabled={!file || !!stage}>
            Submit
          </button>
        </div>
      </form>
    </Modal>
  )
}
