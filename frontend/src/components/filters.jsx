import { useEffect, useMemo, useRef, useState } from 'react'

function useDismiss(onDismiss) {
  const ref = useRef(null)
  useEffect(() => {
    const onDown = (event) => {
      if (ref.current && !ref.current.contains(event.target)) onDismiss()
    }
    const onKey = (event) => event.key === 'Escape' && onDismiss()
    document.addEventListener('mousedown', onDown)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onDown)
      document.removeEventListener('keydown', onKey)
    }
  }, [onDismiss])
  return ref
}

/**
 * The API matches filter values with an exact `IN (...)`, never a LIKE, so every
 * option here has to come from a real value the backend already knows about.
 */
export function MultiSelect({ label, options, value, onChange, icon = '▾', emptyText }) {
  const [open, setOpen] = useState(false)
  const [term, setTerm] = useState('')
  const ref = useDismiss(() => setOpen(false))

  const items = useMemo(() => {
    const seen = new Map()
    options.forEach((option) => {
      const entry = typeof option === 'object' ? option : { value: option, label: String(option) }
      if (entry.value !== undefined && entry.value !== null && entry.label)
        seen.set(String(entry.value), entry)
    })
    const all = [...seen.values()]
    const needle = term.trim().toLowerCase()
    return needle ? all.filter((item) => item.label.toLowerCase().includes(needle)) : all
  }, [options, term])

  const toggle = (itemValue) => {
    const has = value.some((v) => String(v) === String(itemValue))
    onChange(has ? value.filter((v) => String(v) !== String(itemValue)) : [...value, itemValue])
  }

  const labelFor = (v) => {
    const match = options.find(
      (option) => String(typeof option === 'object' ? option.value : option) === String(v),
    )
    if (!match) return String(v)
    return typeof match === 'object' ? match.label : String(match)
  }

  return (
    <div className="filter-control" ref={ref}>
      <button
        type="button"
        className={`filter-trigger ${value.length ? 'is-active' : ''}`}
        onClick={() => setOpen((prev) => !prev)}
      >
        <span className="filter-trigger-label">{label}</span>
        {value.length > 0 && <span className="filter-count">{value.length}</span>}
        <span className="filter-caret">{icon}</span>
      </button>

      {open && (
        <div className="filter-pop">
          <input
            className="input filter-pop-search"
            autoFocus
            placeholder="Filter options…"
            value={term}
            onChange={(event) => setTerm(event.target.value)}
          />
          <div className="filter-pop-list">
            {items.length ? (
              items.map((item) => {
                const checked = value.some((v) => String(v) === String(item.value))
                return (
                  <label key={String(item.value)} className={`filter-opt ${checked ? 'is-on' : ''}`}>
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={() => toggle(item.value)}
                    />
                    <span className="filter-box">{checked ? '✓' : ''}</span>
                    <span className="filter-opt-label">{item.label}</span>
                  </label>
                )
              })
            ) : (
              <p className="muted small" style={{ padding: '10px 4px' }}>
                {emptyText || 'Nothing to choose from yet.'}
              </p>
            )}
          </div>
          {value.length > 0 && (
            <button
              type="button"
              className="btn btn-ghost btn-sm btn-block"
              onClick={() => onChange([])}
            >
              Clear {label.toLowerCase()}
            </button>
          )}
        </div>
      )}

      {value.length > 0 && (
        <div className="chip-row">
          {value.map((v) => (
            <span key={String(v)} className="chip">
              {labelFor(v)}
              <button type="button" onClick={() => toggle(v)} aria-label="Remove">
                ✕
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}

/** Free-typed exact values (usernames), committed with Enter or comma. */
export function TokenInput({ label, placeholder, value, onChange, suggestions = [] }) {
  const [draft, setDraft] = useState('')
  const listId = `tokens-${label.replace(/\s+/g, '-').toLowerCase()}`

  const commit = () => {
    const token = draft.trim()
    if (!token) return
    if (!value.some((v) => v.toLowerCase() === token.toLowerCase())) onChange([...value, token])
    setDraft('')
  }

  return (
    <div className="filter-control">
      <div className="filter-token-wrap">
        <input
          className="input filter-token-input"
          placeholder={placeholder || label}
          value={draft}
          list={suggestions.length ? listId : undefined}
          onChange={(event) => setDraft(event.target.value)}
          onBlur={commit}
          onKeyDown={(event) => {
            if (event.key === 'Enter' || event.key === ',') {
              event.preventDefault()
              commit()
            }
            if (event.key === 'Backspace' && !draft && value.length) onChange(value.slice(0, -1))
          }}
        />
        {suggestions.length > 0 && (
          <datalist id={listId}>
            {suggestions.map((s) => (
              <option key={s} value={s} />
            ))}
          </datalist>
        )}
      </div>
      {value.length > 0 && (
        <div className="chip-row">
          {value.map((token) => (
            <span key={token} className="chip">
              {token}
              <button
                type="button"
                onClick={() => onChange(value.filter((v) => v !== token))}
                aria-label="Remove"
              >
                ✕
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}

export function FilterBar({ children, active, onApply, onReset, note, right, instant }) {
  return (
    <form
      className="glass filter-bar"
      onSubmit={(event) => {
        event.preventDefault()
        if (onApply) onApply()
      }}
    >
      <div className="filter-row">
        {children}
        <div className="filter-actions">
          {!instant && (
            <button className="btn btn-primary btn-sm" type="submit">
              Apply
            </button>
          )}
          {active > 0 && (
            <button type="button" className="btn btn-ghost btn-sm" onClick={onReset}>
              Reset
            </button>
          )}
          {right}
        </div>
      </div>
      {note && <p className="filter-note">{note}</p>}
    </form>
  )
}

export const uniqueBy = (rows, pick) =>
  [...new Set(rows.map(pick).filter((v) => v !== undefined && v !== null && v !== ''))].sort(
    (a, b) => String(a).localeCompare(String(b), undefined, { numeric: true }),
  )
