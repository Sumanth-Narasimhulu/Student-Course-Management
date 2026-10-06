import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, tokens } from './api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadMe = useCallback(async () => {
    if (!tokens.get()) {
      setUser(null)
      setLoading(false)
      return
    }
    try {
      setUser(await api.me())
    } catch {
      tokens.clear()
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadMe()
  }, [loadMe])

  const login = useCallback(
    async (userName, password) => {
      tokens.set(await api.login(userName, password))
      setLoading(true)
      await loadMe()
    },
    [loadMe],
  )

  const logout = useCallback(async () => {
    const refresh = tokens.getRefresh()
    if (refresh) await api.logout(refresh).catch(() => {})
    tokens.clear()
    setUser(null)
  }, [])

  const value = useMemo(() => {
    const roles = user?.roles ?? []
    return {
      user,
      loading,
      login,
      logout,
      refresh: loadMe,
      roles,
      isAdmin: roles.includes('ADMIN'),
      isTeacher: roles.includes('TEACHER'),
      isStudent: roles.includes('STUDENT'),
      profile: user?.Profile ?? null,
    }
  }, [user, loading, login, logout, loadMe])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}
