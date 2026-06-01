export type CommentType = 'EXTERNAL' | 'INTERNAL'

export interface CommentResponse {
  id: number
  ticketId: number
  authorId: number
  authorFullName: string
  content: string
  type: CommentType
  createdAt: string
}

export interface AddCommentRequest {
  ticketId: number
  authorId: number
  content: string
  type: CommentType
}
