import client from './client'
import type { ApiResponse } from '../types/api.types'
import type { AttachmentResponse } from '../types/attachment.types'

export async function getAttachmentsByTicket(ticketId: number): Promise<AttachmentResponse[]> {
  const response = await client.get<ApiResponse<AttachmentResponse[]>>(
    `/attachments/ticket/${ticketId}`
  )
  return response.data.data
}

export async function uploadAttachment(
  ticketId: number,
  file: File
): Promise<AttachmentResponse> {
  const formData = new FormData()
  formData.append('ticketId', String(ticketId))
  formData.append('file', file)

  const response = await client.post<ApiResponse<AttachmentResponse>>(
    '/attachments/upload',
    formData,
    {
      headers: {
        'Content-Type': undefined
      }
    }
  )
  return response.data.data
}

export async function downloadAttachment(
  attachmentId: number,
  fileName: string
): Promise<void> {
  const response = await client.get(
    `/attachments/${attachmentId}/download`,
    { responseType: 'blob' }
  )
  const url = window.URL.createObjectURL(response.data as Blob)
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', fileName)
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
