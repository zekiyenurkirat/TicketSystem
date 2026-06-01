import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchAllTicketsByStatuses } from '../../api/ticket.api'
import { getActiveAgents } from '../../api/user.api'
import type { TicketResponse, TicketStatus, Priority } from '../../types/ticket.types'
import type { UserResponse } from '../../types/user.types'
import { CLOSED_TERMINAL, isOverdue, isSlaApproaching } from '../../utils/ticketUtils'

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

const PRIORITY_LABELS: Record<Priority, string> = {
  BLOCKER: 'Blocker',
  CRITICAL: 'Kritik',
  HIGH: 'Yüksek',
  MEDIUM: 'Orta',
  LOW: 'Düşük',
}

const PRIORITY_BADGE: Record<Priority, string> = {
  BLOCKER: 'bg-red-200 text-red-800',
  CRITICAL: 'bg-red-100 text-red-700',
  HIGH: 'bg-orange-100 text-orange-700',
  MEDIUM: 'bg-yellow-100 text-yellow-700',
  LOW: 'bg-slate-100 text-slate-600',
}

const PRIORITY_CONFIG: { key: Priority; label: string; barColor: string }[] = [
  { key: 'BLOCKER', label: 'Blocker', barColor: 'bg-red-600' },
  { key: 'CRITICAL', label: 'Kritik', barColor: 'bg-red-400' },
  { key: 'HIGH', label: 'Yüksek', barColor: 'bg-orange-400' },
  { key: 'MEDIUM', label: 'Orta', barColor: 'bg-yellow-400' },
  { key: 'LOW', label: 'Düşük', barColor: 'bg-slate-300' },
]

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
        <span className="text-sm font-medium text-slate-600">{title}</span>
        <span className={`w-2.5 h-2.5 rounded-full flex-shrink-0 ${accent}`} />
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

function ManagerDashboard() {
  const [allTickets, setAllTickets] = useState<TicketResponse[]>([])
  const [agents, setAgents] = useState<UserResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      setError(null)
      try {
        const [ticketData, agentData] = await Promise.all([
          fetchAllTicketsByStatuses(),
          getActiveAgents(),
        ])
        setAllTickets(ticketData)
        setAgents(agentData)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Veriler yüklenirken bir hata oluştu.')
      } finally {
        setIsLoading(false)
      }
    }

    load()
  }, [])

  const activeTickets = useMemo(
    () => allTickets.filter((t) => !CLOSED_TERMINAL.has(t.status)),
    [allTickets],
  )

  const totalOpenCount = activeTickets.length

  const unassignedCount = useMemo(
    () => activeTickets.filter((t) => t.assignedToId === null).length,
    [activeTickets],
  )

  const overdueCount = useMemo(
    () => allTickets.filter((t) => isOverdue(t)).length,
    [allTickets],
  )

  const slaApproachingCount = useMemo(
    () => allTickets.filter((t) => isSlaApproaching(t)).length,
    [allTickets],
  )

  const criticalUnassigned = useMemo(
    () =>
      activeTickets.filter(
        (t) =>
          t.assignedToId === null &&
          (t.priority === 'BLOCKER' || t.priority === 'CRITICAL'),
      ),
    [activeTickets],
  )

  const agentWorkload = useMemo(
    () =>
      agents
        .map((agent) => ({
          id: agent.id,
          fullName: `${agent.firstName} ${agent.lastName}`,
          activeCount: activeTickets.filter((t) => t.assignedToId === agent.id).length,
        }))
        .sort((a, b) => b.activeCount - a.activeCount)
        .slice(0, 5),
    [agents, activeTickets],
  )

  const recentTickets = useMemo(() => {
    const source = criticalUnassigned.length > 0 ? criticalUnassigned : activeTickets
    return [...source]
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 5)
  }, [criticalUnassigned, activeTickets])

  const recentListTitle =
    criticalUnassigned.length > 0
      ? 'Atanmamış Kritik / Blocker Talepler'
      : 'Son Aktif Talepler'

  return (
    <>
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-slate-800">Hoş geldiniz</h2>
        <p className="text-sm text-slate-500 mt-1">Sistem geneli talep durumuna genel bakış</p>
      </div>

      {!isLoading && criticalUnassigned.length > 0 && (
        <div className="mb-5 flex items-center justify-between px-4 py-3 rounded-lg bg-red-50 border border-red-200">
          <p className="text-sm text-red-700">
            <span className="font-semibold">{criticalUnassigned.length}</span> adet atanmamış
            Kritik / Blocker talep var. Acilen atama yapılması gerekiyor.
          </p>
          <Link
            to="/tickets?queue=unassigned_critical"
            className="ml-4 flex-shrink-0 text-xs font-medium text-red-700 underline underline-offset-2"
          >
            Görüntüle →
          </Link>
        </div>
      )}

      {error && (
        <div className="mb-5 px-4 py-3 rounded-lg bg-red-50 border border-red-200">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard
          title="Toplam Açık"
          value={totalOpenCount}
          accent="bg-blue-500"
          isLoading={isLoading}
          to="/tickets?queue=all"
        />
        <StatCard
          title="Atanmamış"
          value={unassignedCount}
          accent="bg-slate-400"
          isLoading={isLoading}
          to="/tickets?queue=unassigned"
        />
        <StatCard
          title="Geciken"
          value={overdueCount}
          accent="bg-red-500"
          isLoading={isLoading}
          to="/tickets?queue=overdue"
        />
        <StatCard
          title="SLA Yaklaşan"
          value={slaApproachingCount}
          accent="bg-amber-400"
          isLoading={isLoading}
          to="/tickets?queue=sla_approaching"
        />
      </div>

      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-5">
          <h3 className="text-sm font-semibold text-slate-700 mb-4">Öncelik Dağılımı</h3>
          {isLoading ? (
            <div className="space-y-3">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="h-5 bg-slate-100 rounded animate-pulse" />
              ))}
            </div>
          ) : activeTickets.length === 0 ? (
            <p className="text-sm text-slate-400 py-4 text-center">Aktif talep bulunamadı.</p>
          ) : (
            <div className="space-y-3">
              {PRIORITY_CONFIG.map(({ key, label, barColor }) => {
                const count = activeTickets.filter((t) => t.priority === key).length
                const pct =
                  activeTickets.length > 0
                    ? Math.round((count / activeTickets.length) * 100)
                    : 0
                return (
                  <div key={key} className="flex items-center gap-3">
                    <span className="text-xs text-slate-600 w-14 flex-shrink-0">{label}</span>
                    <div className="flex-1 bg-slate-100 rounded-full h-2 overflow-hidden">
                      <div
                        className={`h-2 rounded-full transition-all ${barColor}`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                    <span className="text-xs font-semibold text-slate-700 w-5 text-right flex-shrink-0">
                      {count}
                    </span>
                  </div>
                )
              })}
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-5">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-slate-700">Agent İş Yükü</h3>
            <Link
              to="/workload"
              className="text-xs font-medium text-violet-600 hover:text-violet-800 transition-colors"
            >
              Detaylı görünüm →
            </Link>
          </div>
          {isLoading ? (
            <div className="space-y-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="h-8 bg-slate-100 rounded animate-pulse" />
              ))}
            </div>
          ) : agentWorkload.length === 0 ? (
            <p className="text-sm text-slate-400 py-4 text-center">Aktif agent bulunamadı.</p>
          ) : (
            <div className="space-y-1">
              {agentWorkload.map((item) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between py-2 border-b border-slate-50 last:border-b-0"
                >
                  <span className="text-sm text-slate-700 truncate">{item.fullName}</span>
                  <span
                    className={`ml-3 flex-shrink-0 inline-flex items-center justify-center min-w-[1.5rem] h-6 px-2 rounded-full text-xs font-semibold ${
                      item.activeCount > 0
                        ? 'bg-violet-100 text-violet-700'
                        : 'bg-slate-100 text-slate-500'
                    }`}
                  >
                    {item.activeCount}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm">
        <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-slate-700">{recentListTitle}</h3>
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
            <p className="text-sm text-slate-400">Henüz aktif talep bulunamadı.</p>
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
                    Öncelik
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
                    <td className="px-5 py-3 whitespace-nowrap">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${PRIORITY_BADGE[ticket.priority]}`}
                      >
                        {PRIORITY_LABELS[ticket.priority]}
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

export default ManagerDashboard
