import type { UserRole } from '../../types/auth.types'
import type { TicketResponse, TicketStatus } from '../../types/ticket.types'

export type QueueId =
  | 'all'
  | 'unassigned'
  | 'mine'
  | 'new'
  | 'in_progress'
  | 'waiting'
  | 'resolved'
  | 'overdue'
  | 'sla_approaching'

type QueueDefinition = {
  id: QueueId
  label: string
  predicate: ((ticket: TicketResponse, userId: number | null) => boolean) | null
}

const CLOSED_TERMINAL = new Set<TicketStatus>(['CLOSED', 'CANCELLED'])
const ALL_TERMINAL = new Set<TicketStatus>(['CLOSED', 'CANCELLED', 'RESOLVED'])
const SLA_APPROACHING_MS = 24 * 60 * 60 * 1000

function isOverdue(ticket: TicketResponse): boolean {
  if (!ticket.dueDate) return false
  if (CLOSED_TERMINAL.has(ticket.status)) return false
  return new Date(ticket.dueDate) < new Date()
}

function isSlaApproaching(ticket: TicketResponse): boolean {
  if (!ticket.dueDate) return false
  if (ALL_TERMINAL.has(ticket.status)) return false
  const remaining = new Date(ticket.dueDate).getTime() - Date.now()
  return remaining > 0 && remaining <= SLA_APPROACHING_MS
}

const CUSTOMER_QUEUES: QueueDefinition[] = [
  { id: 'all', label: 'Tüm Taleplerim', predicate: null },
  { id: 'new', label: 'Yeni', predicate: (t) => t.status === 'NEW' },
  {
    id: 'in_progress',
    label: 'İşlemdeki',
    predicate: (t) =>
      t.status === 'ASSIGNED' ||
      t.status === 'IN_PROGRESS' ||
      t.status === 'WAITING_FOR_CUSTOMER',
  },
  {
    id: 'resolved',
    label: 'Çözülenler',
    predicate: (t) => t.status === 'RESOLVED' || t.status === 'CLOSED',
  },
  { id: 'overdue', label: 'Gecikenler', predicate: (t) => isOverdue(t) },
]

const AGENT_QUEUES: QueueDefinition[] = [
  { id: 'all', label: 'Tüm Talepler', predicate: null },
  {
    id: 'mine',
    label: 'Bana Atananlar',
    predicate: (t, userId) => userId !== null && t.assignedToId === userId,
  },
  { id: 'unassigned', label: 'Atanmamış', predicate: (t) => t.assignedToId === null },
  { id: 'new', label: 'Yeni Talepler', predicate: (t) => t.status === 'NEW' },
  { id: 'in_progress', label: 'İşlemde', predicate: (t) => t.status === 'IN_PROGRESS' },
  {
    id: 'waiting',
    label: 'Müşteriden Yanıt Bekleyenler',
    predicate: (t) => t.status === 'WAITING_FOR_CUSTOMER',
  },
  { id: 'resolved', label: 'Çözülenler', predicate: (t) => t.status === 'RESOLVED' },
  { id: 'overdue', label: 'Gecikenler', predicate: (t) => isOverdue(t) },
  { id: 'sla_approaching', label: 'SLA Yaklaşanlar', predicate: (t) => isSlaApproaching(t) },
]

const MANAGER_QUEUES: QueueDefinition[] = [
  { id: 'all', label: 'Tüm Talepler', predicate: null },
  { id: 'unassigned', label: 'Atanmamış', predicate: (t) => t.assignedToId === null },
  { id: 'new', label: 'Yeni Talepler', predicate: (t) => t.status === 'NEW' },
  { id: 'in_progress', label: 'İşlemde', predicate: (t) => t.status === 'IN_PROGRESS' },
  {
    id: 'waiting',
    label: 'Müşteriden Yanıt Bekleyenler',
    predicate: (t) => t.status === 'WAITING_FOR_CUSTOMER',
  },
  { id: 'resolved', label: 'Çözülenler', predicate: (t) => t.status === 'RESOLVED' },
  { id: 'overdue', label: 'Gecikenler', predicate: (t) => isOverdue(t) },
  { id: 'sla_approaching', label: 'SLA Yaklaşanlar', predicate: (t) => isSlaApproaching(t) },
]

function getQueues(role: UserRole | null): QueueDefinition[] {
  if (role === 'CUSTOMER') return CUSTOMER_QUEUES
  if (role === 'AGENT') return AGENT_QUEUES
  return MANAGER_QUEUES
}

export function filterTickets(
  tickets: TicketResponse[],
  queueId: string,
  role: UserRole | null,
  userId: number | null,
): TicketResponse[] {
  const queues = getQueues(role)
  const queue = queues.find((q) => q.id === queueId)
  if (!queue || queue.predicate === null) return tickets
  return tickets.filter((t) => queue.predicate!(t, userId))
}

type TicketQueueFilterProps = {
  role: UserRole | null
  userId: number | null
  tickets: TicketResponse[]
  selectedQueueId: string
  onQueueChange: (queueId: string) => void
}

function TicketQueueFilter({
  role,
  userId,
  tickets,
  selectedQueueId,
  onQueueChange,
}: TicketQueueFilterProps) {
  const queues = getQueues(role)

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="px-4 py-3 border-b border-slate-100">
        <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wide">Kuyruklar</h3>
      </div>
      <nav className="py-1">
        {queues.map((queue) => {
          const count =
            queue.predicate !== null
              ? tickets.filter((t) => queue.predicate!(t, userId)).length
              : null
          const isSelected = selectedQueueId === queue.id

          return (
            <button
              key={queue.id}
              onClick={() => onQueueChange(queue.id)}
              className={`w-full flex items-start justify-between gap-2 px-4 py-2.5 text-left transition-colors border-l-2 ${
                isSelected
                  ? 'bg-violet-50 border-violet-500'
                  : 'border-transparent hover:bg-slate-50'
              }`}
            >
              <span
                className={`text-xs leading-snug ${
                  isSelected ? 'text-violet-700 font-medium' : 'text-slate-600'
                }`}
              >
                {queue.label}
              </span>
              {count !== null && (
                <span
                  className={`flex-shrink-0 inline-flex items-center justify-center min-w-[1.25rem] h-5 px-1.5 rounded-full text-xs font-medium ${
                    isSelected
                      ? 'bg-violet-100 text-violet-700'
                      : count === 0
                        ? 'bg-slate-100 text-slate-400'
                        : 'bg-slate-100 text-slate-600'
                  }`}
                >
                  {count}
                </span>
              )}
            </button>
          )
        })}
      </nav>
    </div>
  )
}

export default TicketQueueFilter
