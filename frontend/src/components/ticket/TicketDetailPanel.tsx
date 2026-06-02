import type { ReactNode } from 'react'
import type { UserRole } from '../../types/auth.types'
import type { TicketResponse, TicketStatus, Priority, Impact, Urgency } from '../../types/ticket.types'
import TicketActions from './TicketActions'
import TicketAttachments from './TicketAttachments'
import TicketComments from './TicketComments'

const STATUS_LABELS: Record<TicketStatus, string> = {
  NEW: 'Yeni',
  ASSIGNED: 'Atandı',
  IN_PROGRESS: 'İşlemde',
  WAITING_FOR_CUSTOMER: 'Müşteri Bekleniyor',
  RESOLVED: 'Çözüldü',
  CLOSED: 'Kapatıldı',
  CANCELLED: 'İptal',
}

const STATUS_BADGE: Record<TicketStatus, string> = {
  NEW: 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300',
  ASSIGNED: 'bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300',
  IN_PROGRESS: 'bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300',
  WAITING_FOR_CUSTOMER: 'bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-300',
  RESOLVED: 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300',
  CLOSED: 'bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-400',
  CANCELLED: 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400',
}

const PRIORITY_LABELS: Record<Priority, string> = {
  BLOCKER: 'Blocker',
  CRITICAL: 'Kritik',
  HIGH: 'Yüksek',
  MEDIUM: 'Orta',
  LOW: 'Düşük',
}

const PRIORITY_BADGE: Record<Priority, string> = {
  BLOCKER: 'bg-red-200 dark:bg-red-900/40 text-red-800 dark:text-red-300',
  CRITICAL: 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400',
  HIGH: 'bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-400',
  MEDIUM: 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400',
  LOW: 'bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-400',
}

const IMPACT_LABELS: Record<Impact, string> = {
  LOW: 'Düşük',
  MEDIUM: 'Orta',
  HIGH: 'Yüksek',
}

const URGENCY_LABELS: Record<Urgency, string> = {
  LOW: 'Düşük',
  MEDIUM: 'Orta',
  HIGH: 'Yüksek',
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('tr-TR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

type DetailFieldProps = {
  label: string
  value: string
}

function DetailField({ label, value }: DetailFieldProps) {
  return (
    <div>
      <dt className="text-xs font-medium text-slate-400 uppercase tracking-wide mb-0.5">
        {label}
      </dt>
      <dd className="text-sm text-slate-700 dark:text-slate-200">{value}</dd>
    </div>
  )
}

type SectionProps = {
  title: string
  children: ReactNode
}

function Section({ title, children }: SectionProps) {
  return (
    <div>
      <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wide mb-2">
        {title}
      </h4>
      {children}
    </div>
  )
}

type TicketDetailPanelProps = {
  ticket: TicketResponse | null
  role: UserRole | null
  onTicketUpdated: (updated: TicketResponse) => void
}

function TicketDetailPanel({ ticket, role, onTicketUpdated }: TicketDetailPanelProps) {
  if (!ticket) {
    return (
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm flex items-center justify-center py-20">
        <p className="text-sm text-slate-400">← Listeden bir talep seçin</p>
      </div>
    )
  }

  return (
    <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden flex flex-col max-h-[calc(100vh-5.5rem)]">
      <div className="px-5 py-4 border-b border-slate-100 dark:border-slate-700 flex-shrink-0">
        <p className="font-mono text-xs text-slate-400 mb-1">{ticket.ticketNumber}</p>
        <h3 className="text-sm font-semibold text-slate-800 dark:text-slate-100 leading-snug mb-3">
          {ticket.title}
        </h3>
        <div className="flex items-center gap-2 flex-wrap">
          <span
            className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[ticket.status]}`}
          >
            {STATUS_LABELS[ticket.status]}
          </span>
          <span
            className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${PRIORITY_BADGE[ticket.priority]}`}
          >
            {PRIORITY_LABELS[ticket.priority]}
          </span>
        </div>
      </div>

      <div className="px-5 py-4 space-y-5 overflow-y-auto flex-1">
        <Section title="Açıklama">
          <p className="text-sm text-slate-700 dark:text-slate-200 leading-relaxed whitespace-pre-wrap">
            {ticket.description}
          </p>
        </Section>

        <Section title="Kişiler">
          <dl className="grid grid-cols-2 gap-x-4 gap-y-3">
            <DetailField label="Oluşturan" value={ticket.createdByFullName} />
            <DetailField label="Atanan" value={ticket.assignedToFullName ?? '—'} />
          </dl>
        </Section>

        <Section title="Tarihler">
          <dl className="grid grid-cols-2 gap-x-4 gap-y-3">
            <DetailField label="Oluşturulma" value={formatDateTime(ticket.createdAt)} />
            <DetailField label="Güncelleme" value={formatDateTime(ticket.updatedAt)} />
            <DetailField
              label="Son Tarih"
              value={ticket.dueDate ? formatDateTime(ticket.dueDate) : '—'}
            />
          </dl>
        </Section>

        <Section title="Öncelik Analizi">
          <dl className="grid grid-cols-2 gap-x-4 gap-y-3">
            <DetailField
              label="Müşteri Önceliği"
              value={PRIORITY_LABELS[ticket.customerPriority]}
            />
            <DetailField label="Etki" value={IMPACT_LABELS[ticket.impact]} />
            <DetailField label="Aciliyet" value={URGENCY_LABELS[ticket.urgency]} />
            <DetailField
              label="Önerilen Öncelik"
              value={ticket.suggestedPriority ? PRIORITY_LABELS[ticket.suggestedPriority] : '—'}
            />
          </dl>
        </Section>

        {ticket.priorityReviewNote && (
          <Section title="Öncelik İncelemesi">
            <p className="text-sm text-slate-700 dark:text-slate-200 leading-relaxed mb-3">
              {ticket.priorityReviewNote}
            </p>
            <dl className="grid grid-cols-2 gap-x-4 gap-y-3">
              <DetailField
                label="İnceleyen"
                value={ticket.priorityReviewedByFullName ?? '—'}
              />
              <DetailField
                label="İnceleme Tarihi"
                value={
                  ticket.priorityReviewedAt ? formatDateTime(ticket.priorityReviewedAt) : '—'
                }
              />
            </dl>
          </Section>
        )}

        <TicketAttachments key={`att-${ticket.id}`} ticketId={ticket.id} />

        <TicketComments key={`cmt-${ticket.id}`} ticketId={ticket.id} />
      </div>

      {role && (
        <TicketActions
          key={ticket.id}
          ticket={ticket}
          role={role}
          onUpdated={onTicketUpdated}
        />
      )}
    </div>
  )
}

export default TicketDetailPanel
