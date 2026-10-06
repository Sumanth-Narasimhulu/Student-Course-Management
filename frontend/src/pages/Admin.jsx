import { useEffect, useState } from 'react'
import { api } from '../api'
import { Alert, Empty, Field, Section, Spinner } from '../components/ui'

const EMPTY_TEACHER = {
  userName: '',
  password: '',
  name: '',
  degree: '',
  phoneNumber: '',
  departmentId: '',
}

export default function Admin() {
  const [departments, setDepartments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    api
      .allDepartments(0, 100)
      .then((res) => setDepartments(res.departments ?? []))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Spinner />

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>
      <Alert kind="ok" onClose={() => setNotice('')}>
        {notice}
      </Alert>

      <Section title="Create a teacher account">
        <CreateTeacher
          departments={departments}
          onError={setError}
          onDone={() => setNotice('Teacher created.')}
        />
      </Section>

      <Section title="Look up a student's enrolments">
        <StudentEnrolments onError={setError} />
      </Section>
    </>
  )
}

function CreateTeacher({ departments, onError, onDone }) {
  const [form, setForm] = useState(EMPTY_TEACHER)
  const [busy, setBusy] = useState(false)

  const bind = (key) => ({
    value: form[key],
    onChange: (e) => setForm({ ...form, [key]: e.target.value }),
  })

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    try {
      // The endpoint takes a list, so send a single-element batch.
      await api.createTeachers([
        {
          ...form,
          userName: form.userName.trim(),
          departmentId: form.departmentId ? Number(form.departmentId) : null,
        },
      ])
      setForm(EMPTY_TEACHER)
      onDone()
    } catch (err) {
      onError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="glass card" onSubmit={submit}>
      <div className="form-row">
        <Field label="Full name">
          <input className="input" {...bind('name')} required />
        </Field>
        <Field label="Department">
          <select className="select" {...bind('departmentId')}>
            <option value="">— none —</option>
            {departments.map((dept) => (
              <option key={dept.id} value={dept.id}>
                {dept.name}
              </option>
            ))}
          </select>
        </Field>
      </div>
      <div className="form-row">
        <Field label="Username">
          <input className="input" {...bind('userName')} required />
        </Field>
        <Field label="Password">
          <input className="input" type="password" {...bind('password')} required />
        </Field>
      </div>
      <div className="form-row">
        <Field label="Degree">
          <input className="input" placeholder="M.Tech" {...bind('degree')} />
        </Field>
        <Field label="Phone">
          <input className="input" {...bind('phoneNumber')} />
        </Field>
      </div>
      <button className="btn btn-primary btn-sm" disabled={busy}>
        {busy ? 'Creating…' : 'Create teacher'}
      </button>
    </form>
  )
}

function StudentEnrolments({ onError }) {
  const [studentId, setStudentId] = useState('')
  const [result, setResult] = useState(null)
  const [busy, setBusy] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    try {
      setResult(await api.coursesOfStudent(studentId, 0, 50))
    } catch (err) {
      onError(err.message)
      setResult(null)
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <form className="glass card row" onSubmit={submit} style={{ marginBottom: 16 }}>
        <input
          className="input"
          style={{ maxWidth: 220 }}
          type="number"
          placeholder="Student ID"
          value={studentId}
          onChange={(e) => setStudentId(e.target.value)}
          required
        />
        <button className="btn btn-sm" disabled={busy}>
          {busy ? 'Loading…' : 'Look up'}
        </button>
      </form>

      {result &&
        (result.enrolledCourses?.length ? (
          <div className="glass table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Course</th>
                  <th>Teacher</th>
                </tr>
              </thead>
              <tbody>
                {result.enrolledCourses.map((course) => (
                  <tr key={course.courseId}>
                    <td style={{ fontWeight: 600 }}>{course.courseName}</td>
                    <td className="muted">{course.teacherName || 'Unassigned'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <Empty icon="▤">That student has no enrolments.</Empty>
        ))}
    </>
  )
}
