import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import {
  fetchMyNotifications,
  markNotificationSeen,
  markAllNotificationsSeen,
} from '../api/notification.api'
import type { NotificationType } from '../types/notification.types'
import type { UserRole } from '../types/auth.types'

export type NotificationSeverity = 'critical' | 'warning' | 'info'

export type NotificationItem = {
  id: number
  severity: NotificationSeverity
  message: string
  to: string
  seen: boolean
}

function resolveSeverity(type: NotificationType): NotificationSeverity {
  switch (type) {
    case 'SLA_BREACHED':
    case 'UNASSIGNED_CRITICAL':
      return 'critical'
    case 'SLA_APPROACHING':
      return 'warning'
    case 'TICKET_ASSIGNED':
      return 'info'
    default:
      return 'info'
  }
}

function resolveLink(type: NotificationType, role: UserRole | null): string {
  if (role === 'MANAGER') {
    switch (type) {
      case 'SLA_BREACHED':        return '/tickets?queue=overdue'
      case 'SLA_APPROACHING':     return '/tickets?queue=sla_approaching'
      case 'UNASSIGNED_CRITICAL': return '/tickets?queue=unassigned_critical'
      default:                    return '/tickets'
    }
  }
  if (role === 'AGENT') {
    switch (type) {
      case 'SLA_BREACHED':    return '/tickets?queue=mine_overdue'
      case 'SLA_APPROACHING': return '/tickets?queue=mine_sla_approaching'
      case 'TICKET_ASSIGNED': return '/tickets?queue=mine'
      default:                return '/tickets'
    }
  }
  // CUSTOMER şu an backend'den bildirim almıyor; fallback güvenli.
  return '/tickets'
}

export function useNotifications(): {
  items: NotificationItem[]
  unseenCount: number
  isLoading: boolean
  markSeen: (id: number) => Promise<void>
  markAllSeen: () => Promise<void>
  refresh: () => Promise<void>
} {
  const { role } = useAuth()
  const [items, setItems] = useState<NotificationItem[]>([])
  const [isLoading, setIsLoading] = useState(true)

  const load = useCallback(async () => {
    setIsLoading(true)
    try {
      const data = await fetchMyNotifications()
      const mapped: NotificationItem[] = data.map((n) => ({
        id: n.id,
        severity: resolveSeverity(n.type),
        message: n.message,
        to: resolveLink(n.type, role),
        seen: n.seen,
      }))
      setItems(mapped)
    } catch {
      setItems([])
    } finally {
      setIsLoading(false)
    }
  }, [role])

  useEffect(() => {
    load()
  }, [load])

  const markSeen = useCallback(async (id: number) => {
    // Optimistik güncelleme — kullanıcı anında tepki alır
    setItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, seen: true } : item)),
    )
    try {
      await markNotificationSeen(id)
    } catch {
      // Hata durumunda geri al
      setItems((prev) =>
        prev.map((item) => (item.id === id ? { ...item, seen: false } : item)),
      )
    }
  }, [])

  const markAllSeen = useCallback(async () => {
    // Optimistik güncelleme
    setItems((prev) => prev.map((item) => ({ ...item, seen: true })))
    try {
      await markAllNotificationsSeen()
    } catch {
      // Hata durumunda sunucudan taze veri çek
      await load()
    }
  }, [load])

  const unseenCount = items.filter((item) => !item.seen).length

  return { items, unseenCount, isLoading, markSeen, markAllSeen, refresh: load }
}
