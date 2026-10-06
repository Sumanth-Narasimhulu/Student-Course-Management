import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth'
import { initials } from '../format'

const NAV = [
  { to: '/', label: 'Dashboard', icon: '◉', roles: ['STUDENT', 'TEACHER', 'ADMIN'], end: true },
  { to: '/courses', label: 'Browse Courses', icon: '▤', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
  { to: '/my-courses', label: 'My Courses', icon: '★', roles: ['STUDENT'] },
  { to: '/upcoming', label: 'Upcoming Work', icon: '⏱', roles: ['STUDENT'] },
  { to: '/my-grades', label: 'My Grades', icon: '✓', roles: ['STUDENT'] },
  { to: '/my-attendance', label: 'My Attendance', icon: '⊙', roles: ['STUDENT'] },
  { to: '/teaching', label: 'My Teaching', icon: '◈', roles: ['TEACHER'] },
  { to: '/attendance', label: 'Take Attendance', icon: '⊞', roles: ['TEACHER', 'ADMIN'] },
  { to: '/students', label: 'Students', icon: '⚇', roles: ['ADMIN', 'TEACHER'] },
  { to: '/teachers', label: 'Faculty', icon: '⚈', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
  { to: '/departments', label: 'Departments', icon: '⌂', roles: ['ADMIN'] },
  { to: '/admin', label: 'Admin Console', icon: '⚙', roles: ['ADMIN'] },
  { to: '/profile', label: 'Profile', icon: '☺', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
]

const TITLES = {
  '/': ['Dashboard', 'Your campus at a glance'],
  '/courses': ['Browse Courses', 'Every course in the catalogue'],
  '/my-courses': ['My Courses', 'Courses you are enrolled in'],
  '/upcoming': ['Upcoming Work', 'Assignments due soon'],
  '/my-grades': ['My Grades', 'Marks and feedback across your courses'],
  '/my-attendance': ['My Attendance', 'Your class attendance record'],
  '/teaching': ['My Teaching', 'Courses you run'],
  '/attendance': ['Take Attendance', 'Mark the roster for a class date'],
  '/students': ['Students', 'Student directory'],
  '/teachers': ['Faculty', 'Teacher directory'],
  '/departments': ['Departments', 'Organisational units'],
  '/admin': ['Admin Console', 'Manage teachers and courses'],
  '/profile': ['Profile', 'Your account details'],
}

export default function Layout() {
  const { user, roles, logout, profile } = useAuth()
  const { pathname } = useLocation()
  const [title, sub] = TITLES[pathname] ?? ['Campus Sphere', '']
  const displayName = profile?.name || user?.username || 'User'

  const links = NAV.filter((item) => item.roles.some((role) => roles.includes(role)))

  return (
    <div className="shell">
      <aside className="glass sidebar">
        <div className="brand">
          <div className="brand-mark">CS</div>
          <div>
            <div className="brand-name">Campus Sphere</div>
            <div className="brand-sub">Student Portal</div>
          </div>
        </div>

        {links.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
          >
            <span className="nav-icon">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}

        <div className="sidebar-footer">
          <button className="btn btn-ghost btn-block btn-sm" onClick={logout}>
            Sign out
          </button>
        </div>
      </aside>

      <main className="main">
        <header className="glass topbar">
          <div>
            <h2 className="page-title">{title}</h2>
            {sub && <p className="page-sub">{sub}</p>}
          </div>
          <div className="user-chip">
            <div className="avatar">{initials(displayName)}</div>
            <div>
              <div style={{ fontSize: 13, fontWeight: 600 }}>{displayName}</div>
              <div className="muted" style={{ fontSize: 11 }}>
                {roles.join(' · ') || 'No role'}
              </div>
            </div>
          </div>
        </header>

        <Outlet />
      </main>
    </div>
  )
}
