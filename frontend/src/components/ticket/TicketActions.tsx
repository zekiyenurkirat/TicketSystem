import { useEffect, useState } from 'react'
import type { UserRole } from '../../types/auth.types'
import type { Priority, TicketResponse, TicketStatus } from '../../types/ticket.types'
import type { UserResponse } from '../../types/user.types'
import { assignTicket, changeTicketStatus, reviewTicketPriority } from '../../api/ticket.api'
import { getActiveAgents } from '../../api/user.api'
import { createAssignmentRequest } from '../../api/assignmentRequest.api'

const STATUS_LABELS: Record<TicketStatus, string> = {
  NEW: 'Yeni',
  ASSIGNED: 'Atandı',
  IN_PROGRESS: 'İşlemde',
  WAITING_FOR_CUSTOMER: 'Müşteri Bekleniyor',
  RESOLVED: 'Çözüldü',
  CLOSED: 'Kapatıldı',
  CANCELLED: 'İptal',
}

const PRIORITY_OPTIONS: { value: Priority; label: string }[] = [
  { value: 'BLOCKER', label: 'Blocker' },
  { value: 'CRITICAL', label: 'Kritik' },
  { value: 'HIGH', label: 'Yüksek' },
  { value: 'MEDIUM', label: 'Orta' },
  { value: 'LOW', label: 'Düşük' },
]

const AGENT_MANAGER_TRANSITIONS: Record<TicketStatus, TicketStatus[]> = {
  NEW: ['CANCELLED'],
  ASSIGNED: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['WAITING_FOR_CUSTOMER', 'RESOLVED', 'CANCELLED'],
  WAITING_FOR_CUSTOMER: ['IN_PROGRESS', 'CANCELLED'],
  RESOLVED: ['CLOSED', 'IN_PROGRESS'],
  CLOSED: [],
  CANCELLED: [],
}

function getAvailableTransitions(status: TicketStatus, role: UserRole): TicketStatus[] {
  if (role === 'CUSTOMER') {
    return status === 'RESOLVED' ? ['CLOSED'] : []
  }
  return AGENT_MANAGER_TRANSITIONS[status] ?? []
}

type TicketActionsProps = {
  ticket: TicketResponse
  role: UserRole
  onUpdated: (updated: TicketResponse) => void
}

function TicketActions({ ticket, role, onUpdated }: TicketActionsProps) {
  const [newStatus, setNewStatus] = useState<TicketStatus | null>(null)
  const [statusError, setStatusError] = useState<string | null>(null)
  const [statusSubmitting, setStatusSubmitting] = useState(false)

  const [agents, setAgents] = useState<UserResponse[]>([])
  const [agentsLoading, setAgentsLoading] = useState(false)
  const [agentsError, setAgentsError] = useState<string | null>(null)
  const [selectedAgentId, setSelectedAgentId] = useState<number | null>(null)
  const [assignError, setAssignError] = useState<string | null>(null)
  const [assignSubmitting, setAssignSubmitting] = useState(false)

  const [reviewPriority, setReviewPriority] = useState<Priority>(ticket.priority)
  const [reviewNote, setReviewNote] = useState(ticket.priorityReviewNote ?? '')
  const [reviewError, setReviewError] = useState<string | null>(null)
  const [reviewSubmitting, setReviewSubmitting] = useState(false)

  const [requestNote, setRequestNote] = useState('')
  const [requestSubmitting, setRequestSubmitting] = useState(false)
  const [requestError, setRequestError] = useState<string | null>(null)
  const [requestSent, setRequestSent] = useState(false)

  const showAssign =
    role === 'MANAGER' && (ticket.status === 'NEW' || ticket.status === 'ASSIGNED')
  const showReview = role === 'AGENT' || role === 'MANAGER'
  const showRequestAssignment =
    role === 'AGENT' && ticket.status === 'NEW' && ticket.assignedToId === null
  const availableTransitions = getAvailableTransitions(ticket.status, role)
  const showStatus = availableTransitions.length > 0

  useEffect(() => {
    if (!showAssign) return
    setAgentsLoading(true)
    getActiveAgents()
      .then(setAgents)
      .catch((err) =>
        setAgentsError(err instanceof Error ? err.message : 'Agent listesi alınamadı.')
      )
      .finally(() => setAgentsLoading(false))
  }, [])

  if (!showStatus && !showAssign && !showReview && !showRequestAssignment) return null

  async function handleStatusSubmit() {
    if (newStatus === null) return
    setStatusSubmitting(true)
    setStatusError(null)
    try {
      const updated = await changeTicketStatus(ticket.id, { newStatus })
      onUpdated(updated)
      setNewStatus(null)
    } catch (err) {
      setStatusError(err instanceof Error ? err.message : 'Statü güncellenirken bir hata oluştu.')
    } finally {
      setStatusSubmitting(false)
    }
  }

  async function handleAssignSubmit() {
    if (selectedAgentId === null) return
    setAssignSubmitting(true)
    setAssignError(null)
    try {
      const updated = await assignTicket(ticket.id, { agentId: selectedAgentId })
      onUpdated(updated)
      setSelectedAgentId(null)
    } catch (err) {
      setAssignError(err instanceof Error ? err.message : 'Atama yapılırken bir hata oluştu.')
    } finally {
      setAssignSubmitting(false)
    }
  }

  async function handleRequestAssignment() {
    setRequestSubmitting(true)
    setRequestError(null)
    try {
      await createAssignmentRequest({
        ticketId: ticket.id,
        note: requestNote.trim() || undefined,
      })
      setRequestSent(true)
    } catch (err) {
      setRequestError(
        err instanceof Error ? err.message : 'İstek gönderilirken bir hata oluştu.'
      )
    } finally {
      setRequestSubmitting(false)
    }
  }

  async function handleReviewSubmit() {
    setReviewSubmitting(true)
    setReviewError(null)
    try {
      const updated = await reviewTicketPriority(ticket.id, {
        priority: reviewPriority,
        reviewNote: reviewNote.trim() || undefined,
      })
      onUpdated(updated)
      setReviewPriority(updated.priority)
      setReviewNote(updated.priorityReviewNote ?? '')
    } catch (err) {
      setReviewError(
        err instanceof Error ? err.message : 'Öncelik incelemesi sırasında bir hata oluştu.'
      )
    } finally {
      setReviewSubmitting(false)
    }
  }

  return (
    <div className="border-t border-slate-100 px-5 py-4 space-y-4">
      <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wide">İşlemler</h4>

      {showStatus && (
        <div>
          <p className="text-xs font-medium text-slate-600 mb-1.5">Statü Değiştir</p>
          <div className="flex gap-2">
            <select
              value={newStatus ?? ''}
              disabled={statusSubmitting}
              onChange={(e) => {
                setNewStatus(e.target.value ? (e.target.value as TicketStatus) : null)
                setStatusError(null)
              }}
              className="flex-1 px-2 py-1.5 text-xs border border-slate-200 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-violet-400 disabled:bg-slate-50 disabled:text-slate-400"
            >
              <option value="">— Seçiniz —</option>
              {availableTransitions.map((s) => (
                <option key={s} value={s}>
                  {STATUS_LABELS[s]}
                </option>
              ))}
            </select>
            <button
              disabled={newStatus === null || statusSubmitting}
              onClick={handleStatusSubmit}
              className="px-3 py-1.5 text-xs font-medium text-white bg-violet-600 hover:bg-violet-700 rounded-lg transition-colors disabled:opacity-50"
            >
              {statusSubmitting ? '...' : 'Güncelle'}
            </button>
          </div>
          {statusError && <p className="mt-1 text-xs text-red-600">{statusError}</p>}
        </div>
      )}

      {showAssign && (
        <div>
          <p className="text-xs font-medium text-slate-600 mb-1.5">Agent Atama</p>
          {agentsError && <p className="mb-1 text-xs text-red-600">{agentsError}</p>}
          <div className="flex gap-2">
            <select
              value={selectedAgentId ?? ''}
              disabled={agentsLoading || assignSubmitting}
              onChange={(e) => {
                setSelectedAgentId(e.target.value ? Number(e.target.value) : null)
                setAssignError(null)
              }}
              className="flex-1 px-2 py-1.5 text-xs border border-slate-200 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-violet-400 disabled:bg-slate-50 disabled:text-slate-400"
            >
              <option value="">
                {agentsLoading ? 'Yükleniyor...' : '— Agent seçin —'}
              </option>
              {agents.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.firstName} {a.lastName}
                </option>
              ))}
            </select>
            <button
              disabled={selectedAgentId === null || assignSubmitting || agentsLoading}
              onClick={handleAssignSubmit}
              className="px-3 py-1.5 text-xs font-medium text-white bg-violet-600 hover:bg-violet-700 rounded-lg transition-colors disabled:opacity-50"
            >
              {assignSubmitting ? '...' : 'Ata'}
            </button>
          </div>
          {assignError && <p className="mt-1 text-xs text-red-600">{assignError}</p>}
        </div>
      )}

      {showReview && (
        <div>
          <p className="text-xs font-medium text-slate-600 mb-1.5">Öncelik İncelemesi</p>
          <select
            value={reviewPriority}
            disabled={reviewSubmitting}
            onChange={(e) => {
              setReviewPriority(e.target.value as Priority)
              setReviewError(null)
            }}
            className="w-full px-2 py-1.5 text-xs border border-slate-200 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-violet-400 disabled:bg-slate-50 disabled:text-slate-400 mb-2"
          >
            {PRIORITY_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          <textarea
            value={reviewNote}
            disabled={reviewSubmitting}
            maxLength={1000}
            rows={2}
            placeholder="İnceleme notu (opsiyonel)"
            onChange={(e) => setReviewNote(e.target.value)}
            className="w-full px-2 py-1.5 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-400 disabled:bg-slate-50 disabled:text-slate-400 resize-none mb-2"
          />
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">{reviewNote.length}/1000</span>
            <button
              disabled={reviewSubmitting}
              onClick={handleReviewSubmit}
              className="px-3 py-1.5 text-xs font-medium text-white bg-violet-600 hover:bg-violet-700 rounded-lg transition-colors disabled:opacity-50"
            >
              {reviewSubmitting ? '...' : 'Kaydet'}
            </button>
          </div>
          {reviewError && <p className="mt-1 text-xs text-red-600">{reviewError}</p>}
        </div>
      )}

      {showRequestAssignment && (
        <div>
          <p className="text-xs font-medium text-slate-600 mb-1.5">Atama İsteği</p>
          {requestSent ? (
            <div className="px-3 py-2 rounded-lg bg-green-50 border border-green-200">
              <p className="text-xs text-green-700">
                Talebiniz MANAGER onayına gönderildi.
              </p>
            </div>
          ) : (
            <>
              <textarea
                value={requestNote}
                disabled={requestSubmitting}
                maxLength={500}
                rows={2}
                placeholder="Not ekleyin (opsiyonel)"
                onChange={(e) => setRequestNote(e.target.value)}
                className="w-full px-2 py-1.5 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-400 disabled:bg-slate-50 disabled:text-slate-400 resize-none mb-2"
              />
              <button
                disabled={requestSubmitting}
                onClick={handleRequestAssignment}
                className="w-full px-3 py-1.5 text-xs font-medium text-white bg-violet-600 hover:bg-violet-700 rounded-lg transition-colors disabled:opacity-50"
              >
                {requestSubmitting ? '...' : 'Bu talebi almak istiyorum'}
              </button>
              {requestError && (
                <p className="mt-1 text-xs text-red-600">{requestError}</p>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}

export default TicketActions
