import client from './client'
import type { ApiResponse } from '../types/api.types'
import type { UserResponse, CreateUserRequest } from '../types/user.types'

export async function getUserByEmail(email: string): Promise<UserResponse> {
  const response = await client.get<ApiResponse<UserResponse>>(
    `/users/email/${encodeURIComponent(email)}`
  )
  return response.data.data
}

export async function getActiveAgents(): Promise<UserResponse[]> {
  const response = await client.get<ApiResponse<UserResponse[]>>('/users/role/AGENT/active')
  return response.data.data
}

export async function createUser(data: CreateUserRequest): Promise<UserResponse> {
  const response = await client.post<ApiResponse<UserResponse>>('/users/admin', data)
  return response.data.data
}
