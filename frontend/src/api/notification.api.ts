import client from './client'
import type { ApiResponse } from '../types/api.types'
import type { NotificationResponse } from '../types/notification.types'

/** Auth kullanıcısının son 50 bildirimini getirir. */
export async function fetchMyNotifications(): Promise<NotificationResponse[]> {
  const response = await client.get<ApiResponse<NotificationResponse[]>>('/notifications/me')
  return response.data.data
}

/** Belirtilen bildirimi okundu olarak işaretler. */
export async function markNotificationSeen(id: number): Promise<void> {
  await client.patch<ApiResponse<void>>(`/notifications/${id}/seen`)
}

/** Auth kullanıcısının tüm okunmamış bildirimlerini okundu olarak işaretler. */
export async function markAllNotificationsSeen(): Promise<void> {
  await client.patch<ApiResponse<void>>('/notifications/seen-all')
}
