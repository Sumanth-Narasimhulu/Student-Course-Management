import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { Alert, Empty, Field, Modal, Pager, Spinner } from '../components/ui'
import { FilterBar, MultiSelect, TokenInput, uniqueBy } from '../components/filters'

const EMPTY = { courseNames: [], teacherNames: [], teacherUsernames: [] }

export default function Courses() {
  const { isTeacher, isAdmin } = useAuth()
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [draft, setDraft] = useState(EMPTY)
  const [applied, setApplied] = useState(EMPTY)
  const [catalogue, setCatalogue] = useState([])
  const [teachers, setTeachers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [creating, setCreating] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setData(await api.allCourses(applied, page, 12))
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [page, applied])

  useEffect(() => {
    load()
  }, [load])

  // Filter options have to be real stored values — the API matches them exactly.
  useEffect(() => {
    api
      .allCourses({}, 0, 200)
      .then((res) => setCatalogue(res.course ?? []))
      .catch(() => setCatalogue([]))
    api
      .allTeachers(0, 200)
      .then((res) => setTeachers(res.teachers ?? []))
      .catch(() => setTeachers([]))
  }, [])

  const courseNameOptions = useMemo(() => uniqueBy(catalogue, (c) => c.name), [catalogue])
  const teacherNameOptions = useMemo(() => uniqueBy(teachers, (t) => t.name), [teachers])

  const activeCount =
    applied.courseNames.length + applied.teacherNames.length + applied.teacherUsernames.length

  const apply = () => {
    setPage(0)
    setApplied(draft)
  }

  const reset = () => {
    setDraft(EMPTY)
    setApplied(EMPTY)
    setPage(0)
  }

  const courses = data?.course ?? []

  return (
    <>
      <Alert onClose={() => setError('')}>{error}</Alert>

      <FilterBar
        active={draft.courseNames.length + draft.teacherNames.length + draft.teacherUsernames.length}
        onApply={apply}
        onReset={reset}
        note="Filters match whole values, and a course is listed when it matches any one of them."
        right={
          (isTeacher || isAdmin) && (
            <button type="button" className="btn btn-sm" onClick={() => setCreating(true)}>
              + New course
            </button>
          )
        }
      >
        <MultiSelect
          label="Course"
          options={courseNameOptions}
          value={draft.courseNames}
          onChange={(courseNames) => setDraft({ ...draft, courseNames })}
          emptyText="No courses to filter by."
        />
        <MultiSelect
          label="Teacher"
          options={teacherNameOptions}
          value={draft.teacherNames}
          onChange={(teacherNames) => setDraft({ ...draft, teacherNames })}
          emptyText="No faculty to filter by."
        />
        <TokenInput
          label="Teacher username"
          placeholder="Teacher username, then Enter…"
          value={draft.teacherUsernames}
          onChange={(teacherUsernames) => setDraft({ ...draft, teacherUsernames })}
        />
      </FilterBar>

      {!loading && (
        <p className="result-note">
          {data?.totalElements ?? courses.length} course
          {(data?.totalElements ?? courses.length) === 1 ? '' : 's'}
          {activeCount > 0 ? ` matching ${activeCount} filter${activeCount === 1 ? '' : 's'}` : ''}
        </p>
      )}

      {loading ? (
        <Spinner />
      ) : courses.length ? (
        <>
          <div className="grid">
            {courses.map((course) => (
              <div
                key={course.id}
                className="glass course-card card-hover"
                onClick={() => navigate(`/courses/${course.id}`)}
              >
                <h4 className="course-title">{course.name}</h4>
                <p className="course-desc">{course.description || 'No description provided.'}</p>
                <div className="course-meta">
                  <span className="pill">👤 {course.courseTeacher?.name || 'Unassigned'}</span>
                  {course.courseTeacher?.department && (
                    <span className="pill pill-accent">{course.courseTeacher.department}</span>
                  )}
                </div>
              </div>
            ))}
          </div>
          <Pager page={page} totalPages={data?.totalPages} onChange={setPage} />
        </>
      ) : (
        <Empty icon="▤">
          {activeCount ? 'No courses matched those filters.' : 'No courses yet.'}
        </Empty>
      )}

      {creating && (
        <CourseModal
          isAdmin={isAdmin}
          onClose={() => setCreating(false)}
          onSaved={() => {
            setCreating(false)
            load()
          }}
        />
      )}
    </>
  )
}

export function CourseModal({ isAdmin, course, onClose, onSaved }) {
  const [form, setForm] = useState({
    name: course?.name ?? '',
    description: course?.description ?? '',
    teacherId: course?.teacher?.id ?? '',
  })
  const [teachers, setTeachers] = useState([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    api
      .allTeachers(0, 100)
      .then((res) => setTeachers(res.teachers ?? []))
      .catch(() => setTeachers([]))
  }, [])

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    const body = {
      name: form.name.trim(),
      description: form.description.trim(),
      teacherId: form.teacherId ? Number(form.teacherId) : null,
    }
    try {
      if (course) await api.updateCourse(course.id, body)
      else if (isAdmin) await api.adminCreateCourse(body)
      else await api.createCourse(body)
      onSaved()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal title={course ? 'Edit course' : 'New course'} onClose={onClose}>
      <Alert onClose={() => setError('')}>{error}</Alert>
      <form onSubmit={submit}>
        <Field label="Course name">
          <input
            className="input"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
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
        <Field label="Teacher">
          <select
            className="select"
            value={form.teacherId}
            onChange={(e) => setForm({ ...form, teacherId: e.target.value })}
          >
            <option value="">— unassigned —</option>
            {teachers.map((teacher) => (
              <option key={teacher.id} value={teacher.id}>
                {teacher.name} {teacher.department ? `(${teacher.department})` : ''}
              </option>
            ))}
          </select>
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
