import client from './client'
import type { ApiResponse } from '../types/api.types'
import type { UserResponse } from '../types/user.types'

export async function getUserByEmail(email: string): Promise<UserResponse> {
  const response = await client.get<ApiResponse<UserResponse>>(
    `/users/email/${encodeURIComponent(email)}`
  )
  return response.data.data
}
