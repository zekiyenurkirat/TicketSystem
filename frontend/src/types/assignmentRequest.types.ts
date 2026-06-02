export type AssignmentRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface AssignmentRequestResponse {
  id: number
  ticketId: number
  ticketNumber: string
  requestedById: number
  requestedByFullName: string
  status: AssignmentRequestStatus
  note: string | null
  reviewedById: number | null
  reviewedByFullName: string | null
  reviewedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateAssignmentRequestRequest {
  ticketId: number
  note?: string
}
