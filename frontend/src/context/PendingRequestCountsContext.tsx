import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { useAuth } from './AuthContext'
import { getAssignmentRequests } from '../api/assignmentRequest.api'
import { getRegistrationRequests } from '../api/registrationRequest.api'

type PendingRequestCountsContextValue = {
  agentCount: number
  regCount: number
  total: number
  updateAgentCount: (n: number) => void
  updateRegCount: (n: number) => void
}

const PendingRequestCountsContext =
  createContext<PendingRequestCountsContextValue | null>(null)

export function PendingRequestCountsProvider({ children }: { children: ReactNode }) {
  const { role } = useAuth()
  const [agentCount, setAgentCount] = useState(0)
  const [regCount, setRegCount] = useState(0)

  const load = useCallback(async () => {
    if (role !== 'MANAGER') return
    try {
      const [agents, regs] = await Promise.all([
        getAssignmentRequests('PENDING'),
        getRegistrationRequests('PENDING'),
      ])
      setAgentCount(agents.length)
      setRegCount(regs.length)
    } catch {
      // badge cosmetic — fail silently
    }
  }, [role])

  useEffect(() => {
    load()
  }, [load])

  return (
    <PendingRequestCountsContext.Provider
      value={{
        agentCount,
        regCount,
        total: agentCount + regCount,
        updateAgentCount: setAgentCount,
        updateRegCount: setRegCount,
      }}
    >
      {children}
    </PendingRequestCountsContext.Provider>
  )
}

export function usePendingRequestCounts(): PendingRequestCountsContextValue {
  const ctx = useContext(PendingRequestCountsContext)
  if (!ctx) {
    throw new Error(
      'usePendingRequestCounts bir PendingRequestCountsProvider içinde kullanılmalıdır.'
    )
  }
  return ctx
}
