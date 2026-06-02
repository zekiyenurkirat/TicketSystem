import client from './client'
import type { ApiResponse } from '../types/api.types'
import type {
  CreateRegistrationRequestRequest,
  RegistrationRequestResponse,
  RegistrationRequestStatus,
} from '../types/registrationRequest.types'

/** Yeni kayıt talebi oluşturur. Public endpoint — token gerekmez. */
export async function createRegistrationRequest(
  request: CreateRegistrationRequestRequest
): Promise<RegistrationRequestResponse> {
  const { data: apiResponse } = await client.post<ApiResponse<RegistrationRequestResponse>>(
    '/registration-requests',
    request
  )
  return apiResponse.data
}

/** Belirtilen statüdeki kayıt taleplerini getirir. Status verilmezse PENDING döner. */
export async function getRegistrationRequests(
  status?: RegistrationRequestStatus
): Promise<RegistrationRequestResponse[]> {
  const params = status ? { status } : undefined
  const response = await client.get<ApiResponse<RegistrationRequestResponse[]>>(
    '/registration-requests',
    { params }
  )
  return response.data.data
}

/** Belirtilen kayıt talebini onaylar ve kullanıcı hesabını oluşturur. */
export async function approveRegistrationRequest(
  id: number
): Promise<RegistrationRequestResponse> {
  const response = await client.patch<ApiResponse<RegistrationRequestResponse>>(
    `/registration-requests/${id}/approve`
  )
  return response.data.data
}

/** Belirtilen kayıt talebini reddeder. Note gönderilmez. */
export async function rejectRegistrationRequest(
  id: number
): Promise<RegistrationRequestResponse> {
  const response = await client.patch<ApiResponse<RegistrationRequestResponse>>(
    `/registration-requests/${id}/reject`
  )
  return response.data.data
}
