import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { fetchTicketsByUser, fetchAllTicketsByStatuses } from '../api/ticket.api'
import type { TicketResponse } from '../types/ticket.types'
import TicketTable from '../components/ticket/TicketTable'
import TicketDetailPanel from '../components/ticket/TicketDetailPanel'
import TicketQueueFilter, { filterTickets } from '../components/ticket/TicketQueueFilter'

function TicketListPage() {
  const navigate = useNavigate()
  const { role, userId } = useAuth()
  const [tickets, setTickets] = useState<TicketResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedTicket, setSelectedTicket] = useState<TicketResponse | null>(null)
  const [selectedQueueId, setSelectedQueueId] = useState('all')

  const filteredTickets = useMemo(
    () => filterTickets(tickets, selectedQueueId, role, userId),
    [tickets, selectedQueueId, role, userId],
  )

  function handleTicketUpdated(updated: TicketResponse) {
    setSelectedTicket(updated)
    setTickets((prev) => prev.map((t) => (t.id === updated.id ? updated : t)))
  }

  function handleQueueChange(queueId: string) {
    setSelectedQueueId(queueId)
    setSelectedTicket(null)
  }

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      setError(null)
      try {
        let data: TicketResponse[]
        if (role === 'CUSTOMER') {
          if (userId === null) {
            setError('Kullanıcı kimliği alınamadı. Lütfen çıkış yapıp tekrar giriş yapın.')
            return
          }
          data = await fetchTicketsByUser(userId)
        } else {
          data = await fetchAllTicketsByStatuses()
        }
        setTickets(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Talepler yüklenirken bir hata oluştu.')
      } finally {
        setIsLoading(false)
      }
    }

    load()
  }, [role, userId])

  return (
    <div className="flex items-start gap-4">
      <div className="w-52 flex-shrink-0 sticky top-6">
        <TicketQueueFilter
          role={role}
          userId={userId}
          tickets={tickets}
          selectedQueueId={selectedQueueId}
          onQueueChange={handleQueueChange}
        />
      </div>

      <div className="flex-1 min-w-0 bg-white rounded-xl border border-slate-200 shadow-sm">
        <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
          <h2 className="text-sm font-semibold text-slate-700">Talepler</h2>
          <div className="flex items-center gap-3">
            {!isLoading && !error && (
              <span className="text-xs text-slate-400">{filteredTickets.length} kayıt</span>
            )}
            <button
              onClick={() => navigate('/tickets/create')}
              className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-white bg-violet-600 hover:bg-violet-700 rounded-lg transition-colors"
            >
              + Yeni Talep
            </button>
          </div>
        </div>

        {error && (
          <div className="mx-6 mt-4 px-4 py-3 rounded-lg bg-red-50 border border-red-200">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        <TicketTable
          tickets={filteredTickets}
          isLoading={isLoading}
          selectedId={selectedTicket?.id ?? null}
          onSelect={setSelectedTicket}
        />
      </div>

      <div className="w-[380px] flex-shrink-0 sticky top-6">
        <TicketDetailPanel
          ticket={selectedTicket}
          role={role}
          onTicketUpdated={handleTicketUpdated}
        />
      </div>
    </div>
  )
}

export default TicketListPage
