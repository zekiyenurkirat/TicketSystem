import client from './client'
import type { ApiResponse } from '../types/api.types'
import type { AddCommentRequest, CommentResponse } from '../types/comment.types'

export async function getCommentsForTicket(
  ticketId: number,
  requesterId: number
): Promise<CommentResponse[]> {
  const response = await client.get<ApiResponse<CommentResponse[]>>(
    `/comments/ticket/${ticketId}`,
    { params: { requesterId } }
  )
  return response.data.data
}

export async function addComment(request: AddCommentRequest): Promise<CommentResponse> {
  const response = await client.post<ApiResponse<CommentResponse>>('/comments', request)
  return response.data.data
}
