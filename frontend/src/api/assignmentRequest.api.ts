import client from './client'
import type { ApiResponse } from '../types/api.types'
import type {
  AssignmentRequestStatus,
  AssignmentRequestResponse,
  CreateAssignmentRequestRequest,
} from '../types/assignmentRequest.types'

/** Oturum açmış agent adına yeni atama isteği oluşturur. */
export async function createAssignmentRequest(
  request: CreateAssignmentRequestRequest
): Promise<AssignmentRequestResponse> {
  const response = await client.post<ApiResponse<AssignmentRequestResponse>>(
    '/assignment-requests',
    request
  )
  return response.data.data
}

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
