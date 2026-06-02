export type NotificationType =
  | 'SLA_BREACHED'
  | 'SLA_APPROACHING'
  | 'UNASSIGNED_CRITICAL'
  | 'TICKET_ASSIGNED'
  | 'ASSIGNMENT_REQUEST_CREATED'
  | 'ASSIGNMENT_REQUEST_APPROVED'
  | 'ASSIGNMENT_REQUEST_REJECTED'
  | 'REGISTRATION_REQUEST_CREATED'
  | 'REGISTRATION_REQUEST_APPROVED'
  | 'REGISTRATION_REQUEST_REJECTED'

export interface NotificationResponse {
  id: number
  type: NotificationType
  message: string
  referenceId: number | null
  seen: boolean
  createdAt: string
}
