import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { Alert, Empty, Modal, Pager, Spinner } from '../components/ui'
import { FilterBar, MultiSelect, TokenInput, uniqueBy } from '../components/filters'

const EMPTY = { names: [], usernames: [] }

export default function Teachers() {
  const { isAdmin } = useAuth()
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [draft, setDraft] = useState(EMPTY)
  const [applied, setApplied] = useState(EMPTY)
  const [roster, setRoster] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [detail, setDetail] = useState(null)

  const activeCount = applied.names.length + applied.usernames.length

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setData(
        activeCount
          ? await api.searchTeachers({ names: applied.names, usernames: applied.usernames }, page, 15)
          : await api.allTeachers(page, 15),
      )
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [page, applied, activeCount])

  useEffect(() => {
    load()
  }, [load])

  // Teacher names are matched case-sensitively, so only offer stored spellings.
  useEffect(() => {
    api
      .allTeachers(0, 300)
      .then((res) => setRoster(res.teachers ?? []))
      .catch(() => setRoster([]))
  }, [])

  const nameOptions = useMemo(() => uniqueBy(roster, (t) => t.name), [roster])
  const departmentOptions = useMemo(() => uniqueBy(roster, (t) => t.department), [roster])
  const [departments, setDepartments] = useState([])

  const open = async (id) => {
    try {
      setDetail(await api.teacher(id))
    } catch (err) {
      setError(err.message)
    }
  }

  const remove = async (id, name) => {
    if (!window.confirm(`Delete teacher "${name}"?`)) return
    try {
      await api.deleteTeacher(id)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const all = data?.teachers ?? []
  // The API has no department filter, so that one narrows the current page locally.
  const teachers = departments.length
    ? all.filter((t) => departments.includes(t.department))
    : all

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>

      <FilterBar
        active={draft.names.length + draft.usernames.length + departments.length}
        onApply={() => {
          setPage(0)
          setApplied(draft)
        }}
        onReset={() => {
          setDraft(EMPTY)
          setApplied(EMPTY)
          setDepartments([])
          setPage(0)
        }}
        note="Name and username are matched exactly by the API; department narrows the results already on screen."
      >
        <MultiSelect
          label="Name"
          options={nameOptions}
          value={draft.names}
          onChange={(names) => setDraft({ ...draft, names })}
        />
        <MultiSelect
          label="Department"
          options={departmentOptions}
          value={departments}
          onChange={setDepartments}
          emptyText="No departments to filter by."
        />
        <TokenInput
          label="Username"
          placeholder="Username, then Enter…"
          value={draft.usernames}
          onChange={(usernames) => setDraft({ ...draft, usernames })}
        />
      </FilterBar>

      {loading ? (
        <Spinner />
      ) : teachers.length ? (
        <>
          <p className="result-note">
            {teachers.length} of {data?.totalElements ?? all.length} faculty
          </p>
          <div className="grid">
            {teachers.map((teacher) => (
              <div key={teacher.id} className="glass course-card card-hover">
                <h4 className="course-title">{teacher.name}</h4>
                <div className="course-meta">
                  <span className="pill pill-accent">{teacher.department || 'No department'}</span>
                </div>
                <div className="row">
                  <button className="btn btn-sm" onClick={() => open(teacher.id)}>
                    View
                  </button>
                  {isAdmin && (
                    <button
                      className="btn btn-sm btn-danger"
                      onClick={() => remove(teacher.id, teacher.name)}
                    >
                      Delete
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
          <Pager page={page} totalPages={data?.totalPages} onChange={setPage} />
        </>
      ) : (
        <Empty icon="⚈">
          {activeCount || departments.length
            ? 'No faculty matched those filters.'
            : 'No faculty registered.'}
        </Empty>
      )}

      {detail && (
        <Modal title={detail.name} onClose={() => setDetail(null)}>
          <div className="row" style={{ marginBottom: 18 }}>
            <span className="pill pill-accent">{detail.department || 'No department'}</span>
            <span className="pill">@{detail.username}</span>
          </div>
          <h4 className="section-title" style={{ marginBottom: 10 }}>
            Courses ({detail.courses?.length ?? 0})
          </h4>
          {detail.courses?.length ? (
            detail.courses.map((course) => (
              <button
                key={course.id}
                className="btn btn-ghost btn-block btn-sm"
                style={{ justifyContent: 'flex-start', marginBottom: 8 }}
                onClick={() => {
                  setDetail(null)
                  navigate(`/courses/${course.id}`)
                }}
              >
                ▤ {course.name}
              </button>
            ))
          ) : (
            <p className="muted small">Not teaching anything right now.</p>
          )}
        </Modal>
      )}
    </>
  )
}
