import client from './client'
import type { ApiResponse } from '../types/api.types'
import type { TicketResponse, TicketStatus, CreateTicketRequest } from '../types/ticket.types'

const ALL_STATUSES: TicketStatus[] = [
  'NEW',
  'ASSIGNED',
  'IN_PROGRESS',
  'WAITING_FOR_CUSTOMER',
  'RESOLVED',
  'CLOSED',
  'CANCELLED',
]

export async function createTicket(request: CreateTicketRequest): Promise<TicketResponse> {
  const response = await client.post<ApiResponse<TicketResponse>>('/tickets', request)
  return response.data.data
}

export async function fetchTicketsByUser(userId: number): Promise<TicketResponse[]> {
  const response = await client.get<ApiResponse<TicketResponse[]>>(
    `/tickets/created-by/${userId}`
  )
  return response.data.data
}

export async function fetchTicketsByStatus(status: TicketStatus): Promise<TicketResponse[]> {
  const response = await client.get<ApiResponse<TicketResponse[]>>(
    `/tickets/status/${status}`
  )
  return response.data.data
}

export async function fetchAllTicketsByStatuses(): Promise<TicketResponse[]> {
  const results = await Promise.all(
    ALL_STATUSES.map((status) => fetchTicketsByStatus(status))
  )
  const combined = results.flat()

  const seen = new Set<number>()
  const unique = combined.filter((ticket) => {
    if (seen.has(ticket.id)) return false
    seen.add(ticket.id)
    return true
  })

  return unique.sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  )
}
