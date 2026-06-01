import client from './client'
import type { ApiResponse } from '../types/api.types'
import type { AuthResponse, LoginRequest } from '../types/auth.types'

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data: apiResponse } = await client.post<ApiResponse<AuthResponse>>(
    '/auth/login',
    request
  )
  return apiResponse.data
}
