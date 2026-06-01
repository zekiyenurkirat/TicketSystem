export type UserRole = 'CUSTOMER' | 'AGENT' | 'MANAGER'

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  email: string
  role: UserRole
  expiresIn: number // milisaniye
}
