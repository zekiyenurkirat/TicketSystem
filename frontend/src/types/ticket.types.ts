export type TicketStatus =
  | 'NEW'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'WAITING_FOR_CUSTOMER'
  | 'RESOLVED'
  | 'CLOSED'
  | 'CANCELLED'

export type Priority = 'BLOCKER' | 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'

export type Impact = 'LOW' | 'MEDIUM' | 'HIGH'

export type Urgency = 'LOW' | 'MEDIUM' | 'HIGH'

export interface AssignTicketRequest {
  agentId: number
}

export interface ChangeStatusRequest {
  newStatus: TicketStatus
}

export interface PriorityReviewRequest {
  priority: Priority
  reviewNote?: string
}

export interface CreateTicketRequest {
  createdById: number
  title: string
  description: string
  customerPriority: Priority
  impact: Impact
  urgency: Urgency
}

export interface TicketResponse {
  id: number
  ticketNumber: string
  title: string
  description: string
  status: TicketStatus
  priority: Priority
  customerPriority: Priority
  impact: Impact
  urgency: Urgency
  suggestedPriority: Priority | null
  createdById: number
  createdByFullName: string
  assignedToId: number | null
  assignedToFullName: string | null
  dueDate: string | null
  resolvedAt: string | null
  closedAt: string | null
  createdAt: string
  updatedAt: string
  priorityReviewNote: string | null
  priorityReviewedAt: string | null
  priorityReviewedById: number | null
  priorityReviewedByFullName: string | null
}
