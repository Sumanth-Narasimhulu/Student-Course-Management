import { useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import { initials } from '../format'
import { Alert, Field, Section } from '../components/ui'

export default function Profile() {
  const { user, profile, roles, isStudent, isTeacher, refresh } = useAuth()
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>
      <Alert kind="ok" onClose={() => setNotice('')}>
        {notice}
      </Alert>

      <div className="glass card" style={{ marginBottom: 24 }}>
        <div className="row" style={{ gap: 18 }}>
          <div className="avatar" style={{ width: 64, height: 64, fontSize: 22 }}>
            {initials(profile?.name || user?.username)}
          </div>
          <div>
            <h2 style={{ margin: 0, fontSize: 21 }}>{profile?.name || user?.username}</h2>
            <div className="row" style={{ marginTop: 8 }}>
              <span className="pill">@{user?.username}</span>
              {roles.map((role) => (
                <span key={role} className="pill pill-accent">
                  {role}
                </span>
              ))}
              {profile?.department && <span className="pill">{profile.department}</span>}
            </div>
          </div>
        </div>
      </div>

      {isStudent && (
        <Section title="Update my details">
          <StudentForm onError={setError} onDone={() => { setNotice('Profile updated.'); refresh() }} />
        </Section>
      )}

      {isTeacher && (
        <Section title="Update my details">
          <TeacherForm onError={setError} onDone={() => { setNotice('Profile updated.'); refresh() }} />
        </Section>
      )}

      <Section title="Permissions">
        <div className="glass card row">
          {user?.permissions?.length ? (
            user.permissions.map((permission) => (
              <span key={permission} className="pill">
                {permission}
              </span>
            ))
          ) : (
            <span className="muted small">No permissions granted.</span>
          )}
        </div>
      </Section>
    </>
  )
}

function StudentForm({ onError, onDone }) {
  const [form, setForm] = useState({ name: '', degree: '', year: '', dob: '', password: '' })
  const [busy, setBusy] = useState(false)
  const bind = (key) => ({
    value: form[key],
    onChange: (e) => setForm({ ...form, [key]: e.target.value }),
  })

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    try {
      const body = {}
      Object.entries(form).forEach(([key, value]) => {
        if (value !== '') body[key] = key === 'year' ? Number(value) : value
      })
      await api.updateMyStudentProfile(body)
      onDone()
    } catch (err) {
      onError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="glass card" onSubmit={submit}>
      <p className="muted small" style={{ marginTop: 0 }}>
        Leave a field blank to keep its current value.
      </p>
      <div className="form-row">
        <Field label="Name">
          <input className="input" {...bind('name')} />
        </Field>
        <Field label="Degree">
          <input className="input" {...bind('degree')} />
        </Field>
      </div>
      <div className="form-row">
        <Field label="Year">
          <input className="input" type="number" min="1" max="6" {...bind('year')} />
        </Field>
        <Field label="Date of birth">
          <input className="input" type="date" {...bind('dob')} />
        </Field>
      </div>
      <Field label="New password">
        <input className="input" type="password" {...bind('password')} />
      </Field>
      <button className="btn btn-primary btn-sm" disabled={busy}>
        {busy ? 'Saving…' : 'Save changes'}
      </button>
    </form>
  )
}

function TeacherForm({ onError, onDone }) {
  const [form, setForm] = useState({ name: '', degree: '', phoneNumber: '', password: '' })
  const [busy, setBusy] = useState(false)
  const bind = (key) => ({
    value: form[key],
    onChange: (e) => setForm({ ...form, [key]: e.target.value }),
  })

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    try {
      const body = {}
      Object.entries(form).forEach(([key, value]) => {
        if (value !== '') body[key] = value
      })
      await api.updateMyTeacherProfile(body)
      onDone()
    } catch (err) {
      onError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="glass card" onSubmit={submit}>
      <p className="muted small" style={{ marginTop: 0 }}>
        Leave a field blank to keep its current value.
      </p>
      <div className="form-row">
        <Field label="Name">
          <input className="input" {...bind('name')} />
        </Field>
        <Field label="Degree">
          <input className="input" {...bind('degree')} />
        </Field>
      </div>
      <div className="form-row">
        <Field label="Phone">
          <input className="input" {...bind('phoneNumber')} />
        </Field>
        <Field label="New password">
          <input className="input" type="password" {...bind('password')} />
        </Field>
      </div>
      <button className="btn btn-primary btn-sm" disabled={busy}>
        {busy ? 'Saving…' : 'Save changes'}
      </button>
    </form>
  )
}
