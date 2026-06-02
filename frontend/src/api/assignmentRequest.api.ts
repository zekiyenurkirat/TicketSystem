import client from './client'
import type { ApiResponse } from '../types/api.types'
import type {
  AssignmentRequestStatus,
  AssignmentRequestResponse,
} from '../types/assignmentRequest.types'

/** Belirtilen statüdeki atama isteklerini getirir. Status verilmezse PENDING döner. */
export async function getAssignmentRequests(
  status?: AssignmentRequestStatus
): Promise<AssignmentRequestResponse[]> {
  const params = status ? { status } : undefined
  const response = await client.get<ApiResponse<AssignmentRequestResponse[]>>(
    '/assignment-requests',
    { params }
  )
  return response.data.data
}

/** Belirtilen atama isteğini onaylar. */
export async function approveAssignmentRequest(
  id: number
): Promise<AssignmentRequestResponse> {
  const response = await client.patch<ApiResponse<AssignmentRequestResponse>>(
    `/assignment-requests/${id}/approve`
  )
  return response.data.data
}

/** Belirtilen atama isteğini reddeder. */
export async function rejectAssignmentRequest(
  id: number
): Promise<AssignmentRequestResponse> {
  const response = await client.patch<ApiResponse<AssignmentRequestResponse>>(
    `/assignment-requests/${id}/reject`
  )
  return response.data.data
}
