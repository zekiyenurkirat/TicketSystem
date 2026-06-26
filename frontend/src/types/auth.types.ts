export type UserRole = 'CUSTOMER' | 'AGENT' | 'MANAGER'

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string | null          // null ise requiresTwoFactor=true
  email: string
  role: UserRole | null         // null ise requiresTwoFactor=true
  expiresIn: number | null      // null ise requiresTwoFactor=true
  requiresTwoFactor: boolean    // true ise TOTP ekranına geçilir
  challengeToken: string | null // yalnızca requiresTwoFactor=true durumunda dolu
}

export interface VerifyTotpRequest {
  challengeToken: string
  code: string
}
