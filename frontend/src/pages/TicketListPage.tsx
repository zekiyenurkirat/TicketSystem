import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { fetchTicketsByUser, fetchAllTicketsByStatuses } from '../api/ticket.api'
import type { TicketResponse } from '../types/ticket.types'
import TicketTable from '../components/ticket/TicketTable'
import TicketDetailPanel from '../components/ticket/TicketDetailPanel'

function TicketListPage() {
  const { role, userId } = useAuth()
  const [tickets, setTickets] = useState<TicketResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedTicket, setSelectedTicket] = useState<TicketResponse | null>(null)

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
      <div className="flex-1 min-w-0 bg-white rounded-xl border border-slate-200 shadow-sm">
        <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
          <h2 className="text-sm font-semibold text-slate-700">Talepler</h2>
          {!isLoading && !error && (
            <span className="text-xs text-slate-400">{tickets.length} kayıt</span>
          )}
        </div>

        {error && (
          <div className="mx-6 mt-4 px-4 py-3 rounded-lg bg-red-50 border border-red-200">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        <TicketTable
          tickets={tickets}
          isLoading={isLoading}
          selectedId={selectedTicket?.id ?? null}
          onSelect={setSelectedTicket}
        />
      </div>

      <div className="w-[380px] flex-shrink-0 sticky top-6">
        <TicketDetailPanel ticket={selectedTicket} />
      </div>
    </div>
  )
}

export default TicketListPage
