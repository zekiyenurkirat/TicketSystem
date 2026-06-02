import client from './client'
import type { ApiResponse } from '../types/api.types'
import type {
  CreateRegistrationRequestRequest,
  RegistrationRequestResponse,
} from '../types/registrationRequest.types'

export async function createRegistrationRequest(
  request: CreateRegistrationRequestRequest
): Promise<RegistrationRequestResponse> {
  const { data: apiResponse } = await client.post<ApiResponse<RegistrationRequestResponse>>(
    '/registration-requests',
    request
  )
  return apiResponse.data
}
