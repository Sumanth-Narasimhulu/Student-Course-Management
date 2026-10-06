import { useEffect } from 'react'

export const Spinner = () => <div className="spinner" />

export function Alert({ kind = 'error', children, onClose }) {
  if (!children) return null
  return (
    <div className={`alert alert-${kind}`}>
      <div className="row spread">
        <span>{children}</span>
        {onClose && (
          <button className="btn btn-sm btn-ghost" onClick={onClose}>
            ✕
          </button>
        )}
      </div>
    </div>
  )
}

export function Empty({ icon = '✦', children }) {
  return (
    <div className="glass card empty">
      <span className="empty-icon">{icon}</span>
      {children}
    </div>
  )
}

export function Stat({ label, value, color }) {
  return (
    <div className="glass stat card-hover">
      <div className="stat-glow" style={color ? { background: color } : undefined} />
      <div className="stat-value">{value ?? 0}</div>
      <div className="stat-label">{label}</div>
    </div>
  )
}

export function Section({ title, action, children }) {
  return (
    <div className="section">
      <div className="section-head">
        <h3 className="section-title">{title}</h3>
        {action}
      </div>
      {children}
    </div>
  )
}

export function Modal({ title, onClose, children, footer }) {
  useEffect(() => {
    const onKey = (event) => event.key === 'Escape' && onClose()
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="glass modal">
        <div className="row spread" style={{ marginBottom: 20 }}>
          <h3 className="section-title">{title}</h3>
          <button className="btn btn-sm btn-ghost" onClick={onClose}>
            ✕
          </button>
        </div>
        {children}
        {footer && (
          <div className="row" style={{ marginTop: 22, justifyContent: 'flex-end' }}>
            {footer}
          </div>
        )}
      </div>
    </div>
  )
}

export function Field({ label, children }) {
  return (
    <div className="field">
      <label className="label">{label}</label>
      {children}
    </div>
  )
}

export function Pager({ page, totalPages, onChange }) {
  const total = Number(totalPages) || 0
  if (total <= 1) return null
  return (
    <div className="pager">
      <button className="btn btn-sm" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        ← Prev
      </button>
      <span className="muted small">
        Page {page + 1} of {total}
      </span>
      <button
        className="btn btn-sm"
        disabled={page >= total - 1}
        onClick={() => onChange(page + 1)}
      >
        Next →
      </button>
    </div>
  )
}

export function StatusPill({ status }) {
  const map = {
    SUBMITTED: 'pill-ok',
    PENDING: 'pill-warn',
    FAILED: 'pill-danger',
    PRESENT: 'pill-ok',
    ABSENT: 'pill-danger',
    LATE: 'pill-warn',
    EXCUSED: '',
  }
  return <span className={`pill ${map[status] || ''}`}>{status || 'NOT SUBMITTED'}</span>
}
