import { createContext, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { login as loginApi } from '../api/auth.api'
import { getUserByEmail } from '../api/user.api'
import { TOKEN_KEY } from '../api/client'
import type { LoginRequest, UserRole } from '../types/auth.types'

const USER_KEY = 'ticketSystemUser'

type AuthContextValue = {
  token: string | null
  email: string | null
  role: UserRole | null
  userId: number | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (request: LoginRequest) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null)
  const [email, setEmail] = useState<string | null>(null)
  const [role, setRole] = useState<UserRole | null>(null)
  const [userId, setUserId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const storedToken = localStorage.getItem(TOKEN_KEY)
    const storedUser = localStorage.getItem(USER_KEY)

    if (storedToken && storedUser) {
      try {
        const user = JSON.parse(storedUser) as { email: string; role: UserRole; userId?: number }
        setToken(storedToken)
        setEmail(user.email)
        setRole(user.role)
        setUserId(user.userId ?? null)
      } catch {
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
      }
    }

    setIsLoading(false)
  }, [])

  async function login(request: LoginRequest): Promise<void> {
    const authResponse = await loginApi(request)

    // Token interceptor'ın getUserByEmail isteğinde okuyabilmesi için önce localStorage'a yazılır
    localStorage.setItem(TOKEN_KEY, authResponse.token)

    let fetchedUserId: number | null = null
    try {
      const userProfile = await getUserByEmail(authResponse.email)
      fetchedUserId = userProfile.id
    } catch {
      localStorage.removeItem(TOKEN_KEY)
      throw new Error('Kullanıcı profili alınamadı. Lütfen tekrar deneyin.')
    }

    localStorage.setItem(
      USER_KEY,
      JSON.stringify({ email: authResponse.email, role: authResponse.role, userId: fetchedUserId })
    )

    setToken(authResponse.token)
    setEmail(authResponse.email)
    setRole(authResponse.role)
    setUserId(fetchedUserId)
  }

  function logout(): void {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)

    setToken(null)
    setEmail(null)
    setRole(null)
    setUserId(null)
  }

  return (
    <AuthContext.Provider
      value={{
        token,
        email,
        role,
        userId,
        isLoading,
        isAuthenticated: token !== null,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth bir AuthProvider içinde kullanılmalıdır.')
  }
  return context
}
