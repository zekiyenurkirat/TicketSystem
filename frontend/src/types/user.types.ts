import type { UserRole } from './auth.types'

export interface UserResponse {
  id: number
  firstName: string
  lastName: string
  email: string
  role: UserRole
  active: boolean
  createdAt: string
  updatedAt: string
}
