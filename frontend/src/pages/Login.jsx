import { useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import { Alert, Field } from '../components/ui'

const EMPTY_REGISTER = {
  userName: '',
  password: '',
  name: '',
  degree: '',
  dob: '',
  year: '',
}

export default function Login() {
  const { login } = useAuth()
  const [mode, setMode] = useState('login')
  const [creds, setCreds] = useState({ userName: '', password: '' })
  const [form, setForm] = useState(EMPTY_REGISTER)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [busy, setBusy] = useState(false)

  const submitLogin = async (event) => {
    event.preventDefault()
    setError('')
    setBusy(true)
    try {
      await login(creds.userName.trim(), creds.password)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const submitRegister = async (event) => {
    event.preventDefault()
    setError('')
    setNotice('')
    setBusy(true)
    try {
      await api.register({
        ...form,
        userName: form.userName.trim(),
        year: form.year ? Number(form.year) : null,
        dob: form.dob || null,
      })
      setNotice('Account created. You can sign in now.')
      setCreds({ userName: form.userName.trim(), password: '' })
      setForm(EMPTY_REGISTER)
      setMode('login')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const bind = (key) => ({
    value: form[key],
    onChange: (e) => setForm({ ...form, [key]: e.target.value }),
  })

  return (
    <div className="auth-wrap">
      <div className="glass auth-card">
        <div className="brand" style={{ padding: '0 0 22px' }}>
          <div className="brand-mark">CS</div>
          <div>
            <div className="brand-name">Campus Sphere</div>
            <div className="brand-sub">Student Portal</div>
          </div>
        </div>

        <h1 className="auth-title">{mode === 'login' ? 'Welcome back' : 'Create account'}</h1>
        <p className="auth-sub">
          {mode === 'login'
            ? 'Sign in to reach your courses and assignments.'
            : 'Registering creates a student account.'}
        </p>

        <Alert onClose={() => setError('')}>{error}</Alert>
        <Alert kind="ok" onClose={() => setNotice('')}>
          {notice}
        </Alert>

        {mode === 'login' ? (
          <form onSubmit={submitLogin}>
            <Field label="Username">
              <input
                className="input"
                autoComplete="username"
                placeholder="your username"
                value={creds.userName}
                onChange={(e) => setCreds({ ...creds, userName: e.target.value })}
                required
              />
            </Field>
            <Field label="Password">
              <input
                className="input"
                type="password"
                autoComplete="current-password"
                placeholder="••••••••"
                value={creds.password}
                onChange={(e) => setCreds({ ...creds, password: e.target.value })}
                required
              />
            </Field>
            <button className="btn btn-primary btn-block" disabled={busy}>
              {busy ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
        ) : (
          <form onSubmit={submitRegister}>
            <Field label="Full name">
              <input className="input" placeholder="Jane Doe" {...bind('name')} required />
            </Field>
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
                <input className="input" placeholder="B.Tech" {...bind('degree')} />
              </Field>
              <Field label="Year">
                <input className="input" type="number" min="1" max="6" {...bind('year')} />
              </Field>
            </div>
            <Field label="Date of birth">
              <input className="input" type="date" {...bind('dob')} />
            </Field>
            <button className="btn btn-primary btn-block" disabled={busy}>
              {busy ? 'Creating…' : 'Create account'}
            </button>
          </form>
        )}

        <div className="auth-switch">
          {mode === 'login' ? "Don't have an account? " : 'Already registered? '}
          <button
            onClick={() => {
              setMode(mode === 'login' ? 'register' : 'login')
              setError('')
            }}
          >
            {mode === 'login' ? 'Register' : 'Sign in'}
          </button>
        </div>

        <div className="hint">
          Seeded accounts — <strong>admin / admin123</strong> and{' '}
          <strong>teacher / teacher123</strong>.
        </div>
      </div>
    </div>
  )
}
