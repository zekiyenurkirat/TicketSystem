export type RegistrationRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface CreateRegistrationRequestRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  note?: string
}

export interface RegistrationRequestResponse {
  id: number
  firstName: string
  lastName: string
  email: string
  requestedRole: 'CUSTOMER'
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  note: string | null
  reviewedById: number | null
  reviewedByFullName: string | null
  reviewedAt: string | null
  createdAt: string
  updatedAt: string
}
