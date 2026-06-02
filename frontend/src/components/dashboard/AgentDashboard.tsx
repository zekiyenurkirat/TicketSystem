import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchTicketsByAgent } from '../../api/ticket.api'
import type { TicketResponse, TicketStatus } from '../../types/ticket.types'
import { CLOSED_TERMINAL, isSlaApproaching } from '../../utils/ticketUtils'

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
  NEW: 'bg-blue-100 text-blue-700',
  ASSIGNED: 'bg-violet-100 text-violet-700',
  IN_PROGRESS: 'bg-amber-100 text-amber-700',
  WAITING_FOR_CUSTOMER: 'bg-orange-100 text-orange-700',
  RESOLVED: 'bg-green-100 text-green-700',
  CLOSED: 'bg-slate-100 text-slate-600',
  CANCELLED: 'bg-red-100 text-red-600',
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('tr-TR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

type StatCardProps = {
  title: string
  value: number
  accent: string
  isLoading: boolean
  to?: string
}

function StatCard({ title, value, accent, isLoading, to }: StatCardProps) {
  const inner = (
    <div
      className={`bg-white rounded-xl border border-slate-200 p-5 shadow-sm ${
        to ? 'hover:border-violet-300 hover:shadow-md transition-all cursor-pointer' : ''
      }`}
    >
      <div className="flex items-center justify-between mb-3">
        <span className="text-sm font-medium text-slate-600 leading-snug">{title}</span>
        <span className={`w-2.5 h-2.5 rounded-full flex-shrink-0 ml-2 ${accent}`} />
      </div>
      {isLoading ? (
        <div className="h-8 w-16 bg-slate-100 rounded animate-pulse" />
      ) : (
        <p className="text-3xl font-bold text-slate-800">{value}</p>
      )}
    </div>
  )
  if (to) return <Link to={to} className="block">{inner}</Link>
  return inner
}

type AgentDashboardProps = {
  userId: number | null
}

function AgentDashboard({ userId }: AgentDashboardProps) {
  const [tickets, setTickets] = useState<TicketResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (userId === null) {
      setIsLoading(false)
      return
    }

    async function load() {
      setIsLoading(true)
      setError(null)
      try {
        const id = userId as number
        const data = await fetchTicketsByAgent(id)
        setTickets(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Veriler yüklenirken bir hata oluştu.')
      } finally {
        setIsLoading(false)
      }
    }

    load()
  }, [userId])

  const activeCount = useMemo(
    () => tickets.filter((t) => !CLOSED_TERMINAL.has(t.status)).length,
    [tickets],
  )

  const inProgressCount = useMemo(
    () => tickets.filter((t) => t.status === 'IN_PROGRESS').length,
    [tickets],
  )

  const slaApproachingCount = useMemo(
    () => tickets.filter((t) => isSlaApproaching(t)).length,
    [tickets],
  )

  const waitingForCustomerCount = useMemo(
    () => tickets.filter((t) => t.status === 'WAITING_FOR_CUSTOMER').length,
    [tickets],
  )

  const recentTickets = useMemo(
    () =>
      [...tickets]
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 5),
    [tickets],
  )

  if (userId === null) {
    return (
      <div className="px-4 py-3 rounded-lg bg-amber-50 border border-amber-200 max-w-md">
        <p className="text-sm text-amber-700">
          Kullanıcı kimliği alınamadı. Lütfen çıkış yapıp tekrar giriş yapın.
        </p>
      </div>
    )
  }

  return (
    <>
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-slate-800">Hoş geldiniz</h2>
        <p className="text-sm text-slate-500 mt-1">Atanan taleplerinize genel bakış</p>
      </div>

      {error && (
        <div className="mb-5 px-4 py-3 rounded-lg bg-red-50 border border-red-200">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard
          title="Bana Atananlar"
          value={activeCount}
          accent="bg-violet-500"
          isLoading={isLoading}
          to="/tickets?queue=mine"
        />
        <StatCard
          title="İşlemdeki"
          value={inProgressCount}
          accent="bg-amber-400"
          isLoading={isLoading}
          to="/tickets?queue=mine_in_progress"
        />
        <StatCard
          title="SLA Yaklaşan"
          value={slaApproachingCount}
          accent="bg-amber-400"
          isLoading={isLoading}
          to="/tickets?queue=mine_sla_approaching"
        />
        <StatCard
          title="Müşteri Yanıtı Bekleyen"
          value={waitingForCustomerCount}
          accent="bg-orange-400"
          isLoading={isLoading}
          to="/tickets?queue=mine_waiting"
        />
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm">
        <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-slate-700">Son Atanan Talepler</h3>
          <Link
            to="/tickets"
            className="text-xs font-medium text-violet-600 hover:text-violet-800 transition-colors"
          >
            Tümünü görüntüle →
          </Link>
        </div>

        {isLoading ? (
          <div className="px-6 py-4 space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-8 bg-slate-100 rounded animate-pulse" />
            ))}
          </div>
        ) : recentTickets.length === 0 ? (
          <div className="px-6 py-12 text-center">
            <p className="text-sm text-slate-400">Henüz atanmış talep bulunamadı.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-100">
                  <th className="px-5 py-2.5 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">
                    Numara
                  </th>
                  <th className="px-5 py-2.5 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">
                    Başlık
                  </th>
                  <th className="px-5 py-2.5 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">
                    Statü
                  </th>
                  <th className="px-5 py-2.5 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">
                    Tarih
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {recentTickets.map((ticket) => (
                  <tr key={ticket.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-5 py-3 font-mono text-xs text-slate-500 whitespace-nowrap">
                      {ticket.ticketNumber}
                    </td>
                    <td className="px-5 py-3 text-slate-700 max-w-xs truncate">{ticket.title}</td>
                    <td className="px-5 py-3 whitespace-nowrap">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[ticket.status]}`}
                      >
                        {STATUS_LABELS[ticket.status]}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-slate-500 whitespace-nowrap">
                      {formatDate(ticket.createdAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  )
}

export default AgentDashboard
