import client from './client'
import type { ApiResponse } from '../types/api.types'
import type { AuthResponse, LoginRequest, VerifyTotpRequest } from '../types/auth.types'

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data: apiResponse } = await client.post<ApiResponse<AuthResponse>>(
    '/auth/login',
    request
  )
  return apiResponse.data
}

export async function verifyTotp(request: VerifyTotpRequest): Promise<AuthResponse> {
  const { data: apiResponse } = await client.post<ApiResponse<AuthResponse>>(
    '/auth/2fa/verify',
    request
  )
  return apiResponse.data
}
