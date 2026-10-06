import { useCallback, useEffect, useState } from 'react'
import { api } from '../api'
import { Alert, Empty, Field, Modal, Spinner } from '../components/ui'

export default function Departments() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setData(await api.allDepartments(0, 100))
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const remove = async (id, name) => {
    if (!window.confirm(`Delete department "${name}"?`)) return
    try {
      await api.deleteDepartment(id)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading) return <Spinner />

  const departments = data?.departments ?? []

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>

      <div className="row spread" style={{ marginBottom: 16 }}>
        <h3 className="section-title">All departments</h3>
        <button className="btn btn-primary btn-sm" onClick={() => setEditing({})}>
          + New department
        </button>
      </div>

      {departments.length ? (
        <div className="grid">
          {departments.map((dept) => (
            <div key={dept.id} className="glass course-card card-hover">
              <h4 className="course-title">{dept.name}</h4>
              <span className="muted small">ID #{dept.id}</span>
              <div className="row">
                <button className="btn btn-sm" onClick={() => setEditing(dept)}>
                  Rename
                </button>
                <button
                  className="btn btn-sm btn-danger"
                  onClick={() => remove(dept.id, dept.name)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <Empty icon="⌂">No departments yet.</Empty>
      )}

      {editing && (
        <DepartmentModal
          department={editing.id ? editing : null}
          onClose={() => setEditing(null)}
          onSaved={() => {
            setEditing(null)
            load()
          }}
        />
      )}
    </>
  )
}

function DepartmentModal({ department, onClose, onSaved }) {
  const [name, setName] = useState(department?.name ?? '')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      if (department) await api.updateDepartment(department.id, name.trim())
      else await api.createDepartment(name.trim())
      onSaved()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal title={department ? 'Rename department' : 'New department'} onClose={onClose}>
      <Alert onClose={() => setError('')}>{error}</Alert>
      <form onSubmit={submit}>
        <Field label="Name">
          <input
            className="input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            autoFocus
          />
        </Field>
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
