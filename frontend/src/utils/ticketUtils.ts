import type { TicketResponse, TicketStatus } from '../types/ticket.types'

export const CLOSED_TERMINAL = new Set<TicketStatus>(['CLOSED', 'CANCELLED'])
export const ALL_TERMINAL = new Set<TicketStatus>(['CLOSED', 'CANCELLED', 'RESOLVED'])
export const SLA_APPROACHING_MS = 24 * 60 * 60 * 1000

export function isOverdue(ticket: TicketResponse): boolean {
  if (!ticket.dueDate) return false
  if (CLOSED_TERMINAL.has(ticket.status)) return false
  return new Date(ticket.dueDate) < new Date()
}

export function isSlaApproaching(ticket: TicketResponse): boolean {
  if (!ticket.dueDate) return false
  if (ALL_TERMINAL.has(ticket.status)) return false
  const remaining = new Date(ticket.dueDate).getTime() - Date.now()
  return remaining > 0 && remaining <= SLA_APPROACHING_MS
}
