export type NotificationType =
  | 'SLA_BREACHED'
  | 'SLA_APPROACHING'
  | 'UNASSIGNED_CRITICAL'
  | 'TICKET_ASSIGNED'

export interface NotificationResponse {
  id: number
  type: NotificationType
  message: string
  referenceId: number | null
  seen: boolean
  createdAt: string
}
