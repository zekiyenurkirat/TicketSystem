import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchTicketsByUser } from '../../api/ticket.api'
import type { TicketResponse, TicketStatus } from '../../types/ticket.types'

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
  NEW: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300',
  ASSIGNED: 'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300',
  IN_PROGRESS: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300',
  WAITING_FOR_CUSTOMER: 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-300',
  RESOLVED: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  CLOSED: 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-400',
  CANCELLED: 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400',
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
      className={`bg-white rounded-xl border border-slate-200 p-5 shadow-sm dark:bg-slate-800 dark:border-slate-700 ${
        to ? 'hover:border-violet-300 hover:shadow-md transition-all cursor-pointer' : ''
      }`}
    >
      <div className="flex items-center justify-between mb-3">
        <span className="text-sm font-medium text-slate-600 dark:text-slate-300">{title}</span>
        <span className={`w-2.5 h-2.5 rounded-full flex-shrink-0 ${accent}`} />
      </div>
      {isLoading ? (
        <div className="h-8 w-16 bg-slate-100 rounded animate-pulse dark:bg-slate-700" />
      ) : (
        <p className="text-3xl font-bold text-slate-800 dark:text-slate-100">{value}</p>
      )}
    </div>
  )
  if (to) return <Link to={to} className="block">{inner}</Link>
  return inner
}

type CustomerDashboardProps = {
  userId: number | null
}

function CustomerDashboard({ userId }: CustomerDashboardProps) {
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
        const data = await fetchTicketsByUser(id)
        setTickets(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Veriler yüklenirken bir hata oluştu.')
      } finally {
        setIsLoading(false)
      }
    }

    load()
  }, [userId])

  const totalCount = tickets.length

  const waitingCount = useMemo(
    () =>
      tickets.filter(
        (t) => t.status !== 'RESOLVED' && t.status !== 'CLOSED' && t.status !== 'CANCELLED',
      ).length,
    [tickets],
  )

  const closedCount = useMemo(
    () => tickets.filter((t) => t.status === 'RESOLVED' || t.status === 'CLOSED').length,
    [tickets],
  )

  const resolvedPending = useMemo(
    () => tickets.filter((t) => t.status === 'RESOLVED'),
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
      <div className="px-4 py-3 rounded-lg bg-amber-50 border border-amber-200 max-w-md dark:bg-amber-900/20 dark:border-amber-800">
        <p className="text-sm text-amber-700 dark:text-amber-400">
          Kullanıcı kimliği alınamadı. Lütfen çıkış yapıp tekrar giriş yapın.
        </p>
      </div>
    )
  }

  return (
    <>
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-slate-800 dark:text-slate-100">Hoş geldiniz</h2>
        <p className="text-sm text-slate-500 mt-1 dark:text-slate-400">Taleplerinize genel bakış</p>
      </div>

      {!isLoading && resolvedPending.length > 0 && (
        <div className="mb-5 flex items-center justify-between px-4 py-3 rounded-lg bg-amber-50 border border-amber-200 dark:bg-amber-900/20 dark:border-amber-800">
          <p className="text-sm text-amber-700 dark:text-amber-400">
            <span className="font-semibold">{resolvedPending.length}</span> talebin onayınızı
            bekliyor. Çözümlenen talepleri kapatmayı unutmayın.
          </p>
          <Link
            to="/tickets?queue=resolved"
            className="ml-4 flex-shrink-0 text-xs font-medium text-amber-700 underline underline-offset-2 dark:text-amber-400"
          >
            Görüntüle →
          </Link>
        </div>
      )}

      {error && (
        <div className="mb-5 px-4 py-3 rounded-lg bg-red-50 border border-red-200 dark:bg-red-900/20 dark:border-red-800">
          <p className="text-sm text-red-700 dark:text-red-400">{error}</p>
        </div>
      )}

      <div className="grid grid-cols-3 gap-4 mb-6">
        <StatCard
          title="Açtığım Talepler"
          value={totalCount}
          accent="bg-blue-500"
          isLoading={isLoading}
          to="/tickets?queue=all"
        />
        <StatCard
          title="Çözüm Bekleyenler"
          value={waitingCount}
          accent="bg-amber-400"
          isLoading={isLoading}
          to="/tickets?queue=active"
        />
        <StatCard
          title="Kapananlar"
          value={closedCount}
          accent="bg-green-500"
          isLoading={isLoading}
          to="/tickets?queue=resolved"
        />
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm dark:bg-slate-800 dark:border-slate-700">
        <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-slate-700 dark:text-slate-200">Son Talepler</h3>
          <Link
            to="/tickets"
            className="text-xs font-medium text-violet-600 hover:text-violet-800 transition-colors dark:text-violet-400 dark:hover:text-violet-300"
          >
            Tümünü görüntüle →
          </Link>
        </div>

        {isLoading ? (
          <div className="px-6 py-4 space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-8 bg-slate-100 rounded animate-pulse dark:bg-slate-700" />
            ))}
          </div>
        ) : recentTickets.length === 0 ? (
          <div className="px-6 py-12 text-center">
            <p className="text-sm text-slate-400">Henüz talep bulunamadı.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-100 dark:border-slate-700">
                  <th className="px-5 py-2.5 text-left text-xs font-medium text-slate-500 uppercase tracking-wide dark:text-slate-400">
                    Numara
                  </th>
                  <th className="px-5 py-2.5 text-left text-xs font-medium text-slate-500 uppercase tracking-wide dark:text-slate-400">
                    Başlık
                  </th>
                  <th className="px-5 py-2.5 text-left text-xs font-medium text-slate-500 uppercase tracking-wide dark:text-slate-400">
                    Statü
                  </th>
                  <th className="px-5 py-2.5 text-left text-xs font-medium text-slate-500 uppercase tracking-wide dark:text-slate-400">
                    Tarih
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50 dark:divide-slate-700">
                {recentTickets.map((ticket) => (
                  <tr key={ticket.id} className="hover:bg-slate-50 transition-colors dark:hover:bg-slate-700/50">
                    <td className="px-5 py-3 font-mono text-xs text-slate-500 whitespace-nowrap dark:text-slate-400">
                      {ticket.ticketNumber}
                    </td>
                    <td className="px-5 py-3 text-slate-700 max-w-xs truncate dark:text-slate-200">{ticket.title}</td>
                    <td className="px-5 py-3 whitespace-nowrap">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[ticket.status]}`}
                      >
                        {STATUS_LABELS[ticket.status]}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-slate-500 whitespace-nowrap dark:text-slate-400">
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

export default CustomerDashboard
