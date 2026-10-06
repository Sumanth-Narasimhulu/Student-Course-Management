import { useCallback, useEffect, useMemo, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import { formatDate } from '../format'
import { Alert, Empty, Modal, Pager, Spinner } from '../components/ui'
import { FilterBar, MultiSelect, uniqueBy } from '../components/filters'

const EMPTY = { names: [], degrees: [], years: [], courseIds: [] }
const count = (f) => f.names.length + f.degrees.length + f.years.length + f.courseIds.length

export default function Students() {
  const { isAdmin } = useAuth()
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [draft, setDraft] = useState(EMPTY)
  const [applied, setApplied] = useState(EMPTY)
  const [roster, setRoster] = useState([])
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [detail, setDetail] = useState(null)

  const activeCount = count(applied)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setData(
        activeCount
          ? await api.searchStudents(
              {
                names: applied.names,
                degrees: applied.degrees,
                years: applied.years.map(Number),
                courseIds: applied.courseIds.map(Number),
              },
              page,
              15,
            )
          : await api.allStudents(page, 15),
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

  // Options come from stored values because the API matches them exactly.
  useEffect(() => {
    api
      .allStudents(0, 300)
      .then((res) => setRoster(res.students ?? []))
      .catch(() => setRoster([]))
    api
      .allCourses({}, 0, 200)
      .then((res) => setCourses(res.course ?? []))
      .catch(() => setCourses([]))
  }, [])

  const nameOptions = useMemo(() => uniqueBy(roster, (s) => s.name), [roster])
  const degreeOptions = useMemo(() => uniqueBy(roster, (s) => s.degree), [roster])
  const yearOptions = useMemo(
    () => uniqueBy(roster, (s) => s.year).map((y) => ({ value: y, label: `Year ${y}` })),
    [roster],
  )
  const courseOptions = useMemo(
    () => courses.map((c) => ({ value: c.id, label: c.name })),
    [courses],
  )

  const open = async (id) => {
    try {
      setDetail(await api.student(id))
    } catch (err) {
      setError(err.message)
    }
  }

  const remove = async (id, name) => {
    if (!window.confirm(`Delete student "${name}"?`)) return
    try {
      await api.deleteStudent(id)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const students = data?.students ?? []

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>

      <FilterBar
        active={count(draft)}
        onApply={() => {
          setPage(0)
          setApplied(draft)
        }}
        onReset={() => {
          setDraft(EMPTY)
          setApplied(EMPTY)
          setPage(0)
        }}
        note="Filters match whole values and are combined — a student must satisfy every one of them."
      >
        <MultiSelect
          label="Name"
          options={nameOptions}
          value={draft.names}
          onChange={(names) => setDraft({ ...draft, names })}
        />
        <MultiSelect
          label="Degree"
          options={degreeOptions}
          value={draft.degrees}
          onChange={(degrees) => setDraft({ ...draft, degrees })}
        />
        <MultiSelect
          label="Year"
          options={yearOptions}
          value={draft.years}
          onChange={(years) => setDraft({ ...draft, years })}
        />
        <MultiSelect
          label="Enrolled in"
          options={courseOptions}
          value={draft.courseIds}
          onChange={(courseIds) => setDraft({ ...draft, courseIds })}
          emptyText="No courses to filter by."
        />
      </FilterBar>

      {loading ? (
        <Spinner />
      ) : (
        <>
          <p className="result-note">
            {data?.totalElements ?? students.length} student
            {(data?.totalElements ?? students.length) === 1 ? '' : 's'}
            {activeCount > 0 ? ` matching ${activeCount} filter${activeCount === 1 ? '' : 's'}` : ''}
          </p>

          {students.length ? (
            <>
              <div className="glass table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Degree</th>
                      <th>Year</th>
                      <th style={{ textAlign: 'right' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {students.map((student) => (
                      <tr key={student.id}>
                        <td className="muted">#{student.id}</td>
                        <td style={{ fontWeight: 600 }}>{student.name}</td>
                        <td className="muted">{student.degree || '—'}</td>
                        <td>
                          <span className="pill">Year {student.year}</span>
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <div className="row" style={{ justifyContent: 'flex-end' }}>
                            <button className="btn btn-sm" onClick={() => open(student.id)}>
                              View
                            </button>
                            {isAdmin && (
                              <button
                                className="btn btn-sm btn-danger"
                                onClick={() => remove(student.id, student.name)}
                              >
                                ✕
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pager page={page} totalPages={data?.totalPages} onChange={setPage} />
            </>
          ) : (
            <Empty icon="⚇">
              {activeCount ? 'No students matched those filters.' : 'No students registered.'}
            </Empty>
          )}
        </>
      )}

      {detail && (
        <Modal title={detail.name} onClose={() => setDetail(null)}>
          <Row label="Username" value={detail.username} />
          <Row label="Degree" value={detail.degree} />
          <Row label="Year" value={detail.year} />
          <Row label="Date of birth" value={formatDate(detail.dob)} />
          <Row label="Enrolled courses" value={detail.enrolledCourseCount} />
          <Row label="Joined" value={formatDate(detail.createdAt)} />
        </Modal>
      )}
    </>
  )
}

function Row({ label, value }) {
  return (
    <div className="row spread" style={{ padding: '10px 0', borderBottom: '1px solid var(--stroke-soft)' }}>
      <span className="muted small">{label}</span>
      <strong style={{ fontSize: 13.5 }}>{value ?? '—'}</strong>
    </div>
  )
}
