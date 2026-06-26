import { createContext, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { login as loginApi, verifyTotp as verifyTotpApi } from '../api/auth.api'
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
  /** true ise TOTP ekranına geçilir. */
  requiresTwoFactor: boolean
  /** 2FA beklenen kullanıcının emaili — TOTP ekranında gösterim için. */
  pendingEmail: string | null
  login: (request: LoginRequest) => Promise<{ requiresTwoFactor: boolean }>
  /** Yalnızca 6 haneli TOTP kodunu alır; challengeToken context içinde gizlidir. */
  verifyTotp: (code: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken]   = useState<string | null>(null)
  const [email, setEmail]   = useState<string | null>(null)
  const [role, setRole]     = useState<UserRole | null>(null)
  const [userId, setUserId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  // 2FA geçiş state'i — localStorage'a yazılmaz; challenge token 5 dk geçerli
  const [requiresTwoFactor, setRequiresTwoFactor]         = useState(false)
  const [pendingEmail, setPendingEmail]                   = useState<string | null>(null)
  const [pendingChallengeToken, setPendingChallengeToken] = useState<string | null>(null)

  useEffect(() => {
    const storedToken = localStorage.getItem(TOKEN_KEY)
    const storedUser  = localStorage.getItem(USER_KEY)

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

  async function login(request: LoginRequest): Promise<{ requiresTwoFactor: boolean }> {
    const authResponse = await loginApi(request)

    if (authResponse.requiresTwoFactor) {
      setPendingEmail(authResponse.email)
      setPendingChallengeToken(authResponse.challengeToken)
      setRequiresTwoFactor(true)
      return { requiresTwoFactor: true }
    }

    await applyFullAuth(authResponse.token!, authResponse.email, authResponse.role!)
    return { requiresTwoFactor: false }
  }

  async function verifyTotp(code: string): Promise<void> {
    if (!pendingChallengeToken) {
      throw new Error('2FA oturumu bulunamadı. Lütfen tekrar giriş yapın.')
    }
    const authResponse = await verifyTotpApi({ challengeToken: pendingChallengeToken, code })
    setRequiresTwoFactor(false)
    setPendingEmail(null)
    setPendingChallengeToken(null)
    await applyFullAuth(authResponse.token!, authResponse.email, authResponse.role!)
  }

  async function applyFullAuth(tok: string, userEmail: string, userRole: UserRole): Promise<void> {
    // Token interceptor'ın getUserByEmail isteğinde okuyabilmesi için önce localStorage'a yazılır
    localStorage.setItem(TOKEN_KEY, tok)

    let fetchedUserId: number | null = null
    try {
      const userProfile = await getUserByEmail(userEmail)
      fetchedUserId = userProfile.id
    } catch {
      localStorage.removeItem(TOKEN_KEY)
      throw new Error('Kullanıcı profili alınamadı. Lütfen tekrar deneyin.')
    }

    localStorage.setItem(
      USER_KEY,
      JSON.stringify({ email: userEmail, role: userRole, userId: fetchedUserId })
    )

    setToken(tok)
    setEmail(userEmail)
    setRole(userRole)
    setUserId(fetchedUserId)
  }

  function logout(): void {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)

    setToken(null)
    setEmail(null)
    setRole(null)
    setUserId(null)
    setRequiresTwoFactor(false)
    setPendingEmail(null)
    setPendingChallengeToken(null)
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
        requiresTwoFactor,
        pendingEmail,
        login,
        verifyTotp,
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
