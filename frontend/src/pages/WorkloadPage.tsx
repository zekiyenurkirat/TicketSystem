import { useEffect, useState } from 'react'
import { getActiveAgents } from '../api/user.api'
import { fetchTicketsByAgent } from '../api/ticket.api'
import type { UserResponse } from '../types/user.types'
import type { TicketResponse } from '../types/ticket.types'
import { useAuth } from '../context/AuthContext'
import TicketTable from '../components/ticket/TicketTable'
import TicketDetailPanel from '../components/ticket/TicketDetailPanel'

type WorkloadMetrics = {
  activeCount: number
  inProgressCount: number
  resolvedCount: number
  overdueCount: number
}

const CLOSED_STATUSES = new Set(['CLOSED', 'CANCELLED'])

function computeMetrics(tickets: TicketResponse[]): WorkloadMetrics {
  const now = new Date()
  return {
    activeCount: tickets.filter((t) => !CLOSED_STATUSES.has(t.status)).length,
    inProgressCount: tickets.filter((t) => t.status === 'IN_PROGRESS').length,
    resolvedCount: tickets.filter((t) => t.status === 'RESOLVED').length,
    overdueCount: tickets.filter(
      (t) =>
        !CLOSED_STATUSES.has(t.status) &&
        t.dueDate !== null &&
        new Date(t.dueDate) < now
    ).length,
  }
}

function MetricBadge({
  label,
  value,
  accent,
}: {
  label: string
  value: number
  accent: string
}) {
  return (
    <div className="flex flex-col items-center">
      <span className={`text-lg font-bold leading-none ${accent}`}>{value}</span>
      <span className="text-xs text-slate-500 mt-1">{label}</span>
    </div>
  )
}

function WorkloadPage() {
  const { role } = useAuth()
  const [agents, setAgents] = useState<UserResponse[]>([])
  const [agentTickets, setAgentTickets] = useState<Record<number, TicketResponse[]>>({})
  const [selectedAgentId, setSelectedAgentId] = useState<number | null>(null)
  const [selectedTicket, setSelectedTicket] = useState<TicketResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      setError(null)
      try {
        const agentList = await getActiveAgents()
        setAgents(agentList)
        const ticketResults = await Promise.all(
          agentList.map((agent) => fetchTicketsByAgent(agent.id))
        )
        const ticketMap: Record<number, TicketResponse[]> = {}
        agentList.forEach((agent, index) => {
          ticketMap[agent.id] = ticketResults[index]
        })
        setAgentTickets(ticketMap)
      } catch (err) {
        setError(
          err instanceof Error ? err.message : 'İş yükü verisi yüklenirken bir hata oluştu.'
        )
      } finally {
        setIsLoading(false)
      }
    }

    load()
  }, [])

  function handleSelectAgent(agentId: number) {
    if (selectedAgentId === agentId) return
    setSelectedAgentId(agentId)
    setSelectedTicket(null)
  }

  function handleTicketUpdated(updated: TicketResponse) {
    setSelectedTicket(updated)
    if (selectedAgentId === null) return
    setAgentTickets((prev) => ({
      ...prev,
      [selectedAgentId]: (prev[selectedAgentId] ?? []).map((t) =>
        t.id === updated.id ? updated : t
      ),
    }))
  }

  const selectedAgent = agents.find((a) => a.id === selectedAgentId) ?? null
  const selectedAgentTickets =
    selectedAgentId !== null ? (agentTickets[selectedAgentId] ?? []) : []

  return (
    <div className="flex items-start gap-4">
      <div className="flex-1 min-w-0 space-y-4">
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-4">
          <h2 className="text-sm font-semibold text-slate-700 mb-3">Aktif Agentlar</h2>

          {error && (
            <div className="px-4 py-3 rounded-lg bg-red-50 border border-red-200 mb-3">
              <p className="text-sm text-red-700">{error}</p>
            </div>
          )}

          {isLoading ? (
            <div className="grid grid-cols-2 gap-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="h-28 bg-slate-100 rounded-lg animate-pulse" />
              ))}
            </div>
          ) : agents.length === 0 ? (
            <p className="text-sm text-slate-400 py-8 text-center">Aktif agent bulunamadı.</p>
          ) : (
            <div className="grid grid-cols-2 xl:grid-cols-3 gap-3">
              {agents.map((agent) => {
                const tickets = agentTickets[agent.id] ?? []
                const metrics = computeMetrics(tickets)
                const isSelected = selectedAgentId === agent.id
                return (
                  <button
                    key={agent.id}
                    onClick={() => handleSelectAgent(agent.id)}
                    className={`text-left rounded-lg border p-4 transition-colors ${
                      isSelected
                        ? 'border-violet-400 bg-violet-50'
                        : 'border-slate-200 bg-white hover:border-violet-200 hover:bg-violet-50/40'
                    }`}
                  >
                    <p className="text-sm font-semibold text-slate-800 truncate mb-3">
                      {agent.firstName} {agent.lastName}
                    </p>
                    <div className="flex justify-between">
                      <MetricBadge
                        label="Aktif"
                        value={metrics.activeCount}
                        accent="text-violet-700"
                      />
                      <MetricBadge
                        label="İşlemde"
                        value={metrics.inProgressCount}
                        accent="text-amber-600"
                      />
                      <MetricBadge
                        label="Çözüldü"
                        value={metrics.resolvedCount}
                        accent="text-green-600"
                      />
                      <MetricBadge
                        label="Geciken"
                        value={metrics.overdueCount}
                        accent={metrics.overdueCount > 0 ? 'text-red-600' : 'text-slate-400'}
                      />
                    </div>
                  </button>
                )
              })}
            </div>
          )}
        </div>

        {selectedAgent !== null && (
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm">
            <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
              <h2 className="text-sm font-semibold text-slate-700">
                {selectedAgent.firstName} {selectedAgent.lastName} — Talepler
              </h2>
              <span className="text-xs text-slate-400">
                {selectedAgentTickets.length} kayıt
              </span>
            </div>
            <TicketTable
              tickets={selectedAgentTickets}
              isLoading={false}
              selectedId={selectedTicket?.id ?? null}
              onSelect={setSelectedTicket}
            />
          </div>
        )}
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

export default WorkloadPage
