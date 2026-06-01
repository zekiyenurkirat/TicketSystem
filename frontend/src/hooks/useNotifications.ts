import { useEffect, useMemo, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import {
  fetchAllTicketsByStatuses,
  fetchTicketsByAgent,
  fetchTicketsByUser,
} from '../api/ticket.api'
import type { TicketResponse } from '../types/ticket.types'
import { CLOSED_TERMINAL, isOverdue, isSlaApproaching } from '../utils/ticketUtils'

export type NotificationSeverity = 'critical' | 'warning' | 'info'

export type NotificationItem = {
  id: string
  severity: NotificationSeverity
  message: string
  to: string
  count: number
}

function buildManagerNotifications(tickets: TicketResponse[]): NotificationItem[] {
  const unassignedCritical = tickets.filter(
    (t) =>
      t.assignedToId === null &&
      (t.priority === 'BLOCKER' || t.priority === 'CRITICAL') &&
      !CLOSED_TERMINAL.has(t.status),
  ).length

  const overdueCount = tickets.filter((t) => isOverdue(t)).length
  const slaApproachingCount = tickets.filter((t) => isSlaApproaching(t)).length

  return [
    {
      id: 'mgr_unassigned_critical',
      severity: 'critical',
      message: `${unassignedCritical} atanmamış Kritik/Blocker talep`,
      to: '/tickets?queue=unassigned_critical',
      count: unassignedCritical,
    },
    {
      id: 'mgr_overdue',
      severity: 'critical',
      message: `${overdueCount} geciken talep`,
      to: '/tickets?queue=overdue',
      count: overdueCount,
    },
    {
      id: 'mgr_sla_approaching',
      severity: 'warning',
      message: `${slaApproachingCount} talep SLA'ya yaklaşıyor`,
      to: '/tickets?queue=sla_approaching',
      count: slaApproachingCount,
    },
  ]
}

function buildAgentNotifications(tickets: TicketResponse[]): NotificationItem[] {
  const overdueCount = tickets.filter((t) => isOverdue(t)).length
  const slaApproachingCount = tickets.filter((t) => isSlaApproaching(t)).length
  const waitingCount = tickets.filter((t) => t.status === 'WAITING_FOR_CUSTOMER').length

  return [
    {
      id: 'agent_overdue',
      severity: 'critical',
      message: `${overdueCount} atanan talep gecikmiş`,
      to: '/tickets?queue=mine_overdue',
      count: overdueCount,
    },
    {
      id: 'agent_sla_approaching',
      severity: 'warning',
      message: `${slaApproachingCount} atanan talep SLA'ya yaklaşıyor`,
      to: '/tickets?queue=mine_sla_approaching',
      count: slaApproachingCount,
    },
    {
      id: 'agent_waiting',
      severity: 'info',
      message: `${waitingCount} talep müşteri yanıtı bekliyor`,
      to: '/tickets?queue=mine_waiting',
      count: waitingCount,
    },
  ]
}

function buildCustomerNotifications(tickets: TicketResponse[]): NotificationItem[] {
  const waitingCount = tickets.filter((t) => t.status === 'WAITING_FOR_CUSTOMER').length
  const resolvedCount = tickets.filter((t) => t.status === 'RESOLVED').length

  return [
    {
      id: 'cust_waiting',
      severity: 'warning',
      message: `${waitingCount} talep yanıtınızı bekliyor`,
      to: '/tickets?queue=my_waiting',
      count: waitingCount,
    },
    {
      id: 'cust_resolved',
      severity: 'info',
      message: `${resolvedCount} talep kapatılmayı bekliyor`,
      to: '/tickets?queue=resolved',
      count: resolvedCount,
    },
  ]
}

export function useNotifications(): {
  items: NotificationItem[]
  totalCount: number
  isLoading: boolean
} {
  const { role, userId } = useAuth()
  const [tickets, setTickets] = useState<TicketResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      try {
        let data: TicketResponse[] = []
        if (role === 'MANAGER') {
          data = await fetchAllTicketsByStatuses()
        } else if (role === 'AGENT' && userId !== null) {
          data = await fetchTicketsByAgent(userId)
        } else if (role === 'CUSTOMER' && userId !== null) {
          data = await fetchTicketsByUser(userId)
        }
        setTickets(data)
      } catch {
        setTickets([])
      } finally {
        setIsLoading(false)
      }
    }
    load()
  }, [role, userId])

  const items = useMemo((): NotificationItem[] => {
    let all: NotificationItem[] = []
    if (role === 'MANAGER') all = buildManagerNotifications(tickets)
    else if (role === 'AGENT') all = buildAgentNotifications(tickets)
    else if (role === 'CUSTOMER') all = buildCustomerNotifications(tickets)
    return all.filter((item) => item.count > 0)
  }, [tickets, role])

  return { items, totalCount: items.length, isLoading }
}
