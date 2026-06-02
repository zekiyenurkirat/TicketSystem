import { useEffect, useState } from 'react'
import {
  getAssignmentRequests,
  approveAssignmentRequest,
  rejectAssignmentRequest,
} from '../api/assignmentRequest.api'
import {
  getRegistrationRequests,
  approveRegistrationRequest,
  rejectRegistrationRequest,
} from '../api/registrationRequest.api'
import { usePendingRequestCounts } from '../context/PendingRequestCountsContext'
import type { AssignmentRequestResponse } from '../types/assignmentRequest.types'
import type { RegistrationRequestResponse } from '../types/registrationRequest.types'

type ActiveTab = 'agent' | 'registration'

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('tr-TR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

function SkeletonRow({ cols }: { cols: number }) {
  return (
    <tr>
      {Array.from({ length: cols }).map((_, i) => (
        <td key={i} className="px-4 py-3">
          <div className="h-4 bg-slate-100 dark:bg-slate-700 rounded animate-pulse" />
        </td>
      ))}
    </tr>
  )
}

function RequestsPage() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('agent')
  const { updateAgentCount, updateRegCount } = usePendingRequestCounts()

  // ── Agent Talepleri state ──────────────────────────────────────────────────
  const [requests, setRequests] = useState<AssignmentRequestResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)

  // ── Kayıt Talepleri state ─────────────────────────────────────────────────
  const [regRequests, setRegRequests] = useState<RegistrationRequestResponse[]>([])
  const [regIsLoading, setRegIsLoading] = useState(false)
  const [regError, setRegError] = useState<string | null>(null)
  const [regProcessingId, setRegProcessingId] = useState<number | null>(null)

  // ── Agent tab yükleme ─────────────────────────────────────────────────────

  async function load() {
    setIsLoading(true)
    setError(null)
    try {
      const data = await getAssignmentRequests('PENDING')
      setRequests(data)
      updateAgentCount(data.length)
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Atama istekleri yüklenirken bir hata oluştu.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function handleApprove(id: number) {
    setProcessingId(id)
    setError(null)
    try {
      await approveAssignmentRequest(id)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'İstek onaylanırken bir hata oluştu.')
    } finally {
      setProcessingId(null)
    }
  }

  async function handleReject(id: number) {
    setProcessingId(id)
    setError(null)
    try {
      await rejectAssignmentRequest(id)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'İstek reddedilirken bir hata oluştu.')
    } finally {
      setProcessingId(null)
    }
  }

  // ── Registration tab yükleme ──────────────────────────────────────────────

  async function loadReg() {
    setRegIsLoading(true)
    setRegError(null)
    try {
      const data = await getRegistrationRequests('PENDING')
      setRegRequests(data)
      updateRegCount(data.length)
    } catch (err) {
      setRegError(
        err instanceof Error ? err.message : 'Kayıt talepleri yüklenirken bir hata oluştu.'
      )
    } finally {
      setRegIsLoading(false)
    }
  }

  useEffect(() => {
    loadReg()
  }, [])

  async function handleRegApprove(id: number) {
    setRegProcessingId(id)
    setRegError(null)
    try {
      await approveRegistrationRequest(id)
      await loadReg()
    } catch (err) {
      setRegError(err instanceof Error ? err.message : 'Kayıt talebi onaylanırken bir hata oluştu.')
    } finally {
      setRegProcessingId(null)
    }
  }

  async function handleRegReject(id: number) {
    setRegProcessingId(id)
    setRegError(null)
    try {
      await rejectRegistrationRequest(id)
      await loadReg()
    } catch (err) {
      setRegError(err instanceof Error ? err.message : 'Kayıt talebi reddedilirken bir hata oluştu.')
    } finally {
      setRegProcessingId(null)
    }
  }

  // ── render ────────────────────────────────────────────────────────────────

  return (
    <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">

      {/* Tab bar */}
      <div className="flex border-b border-slate-200 dark:border-slate-700 px-6">
        <button
          onClick={() => setActiveTab('agent')}
          className={`py-4 mr-6 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'agent'
              ? 'border-violet-600 text-violet-700 dark:text-violet-400'
              : 'border-transparent text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200'
          }`}
        >
          Agent Talepleri ({requests.length})
        </button>
        <button
          onClick={() => setActiveTab('registration')}
          className={`py-4 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'registration'
              ? 'border-violet-600 text-violet-700 dark:text-violet-400'
              : 'border-transparent text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200'
          }`}
        >
          Kayıt Talepleri ({regRequests.length})
        </button>
      </div>

      {/* Agent Talepleri içeriği */}
      {activeTab === 'agent' ? (
        <div>
          {error && (
            <div className="mx-6 mt-4 px-4 py-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
              <p className="text-sm text-red-700 dark:text-red-400">{error}</p>
            </div>
          )}

          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-100 dark:border-slate-700">
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    Agent
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    Ticket No
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    Not
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    Tarih
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    İşlemler
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50 dark:divide-slate-700">
                {isLoading ? (
                  <>
                    <SkeletonRow cols={5} />
                    <SkeletonRow cols={5} />
                    <SkeletonRow cols={5} />
                  </>
                ) : requests.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-12 text-center text-sm text-slate-400">
                      Bekleyen atama isteği bulunamadı.
                    </td>
                  </tr>
                ) : (
                  requests.map((req) => {
                    const isProcessing = processingId === req.id
                    return (
                      <tr key={req.id} className="hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors">
                        <td className="px-4 py-3 text-slate-800 dark:text-slate-100 whitespace-nowrap">
                          {req.requestedByFullName}
                        </td>
                        <td className="px-4 py-3 font-mono text-xs text-slate-600 dark:text-slate-300 whitespace-nowrap">
                          {req.ticketNumber}
                        </td>
                        <td className="px-4 py-3 text-slate-600 dark:text-slate-300 max-w-xs truncate">
                          {req.note ?? '—'}
                        </td>
                        <td className="px-4 py-3 text-slate-500 dark:text-slate-400 whitespace-nowrap">
                          {formatDate(req.createdAt)}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap">
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => handleApprove(req.id)}
                              disabled={isProcessing}
                              className={`text-xs font-medium px-2.5 py-1 rounded-md border transition-colors ${
                                isProcessing
                                  ? 'opacity-50 cursor-not-allowed bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800 text-green-700 dark:text-green-400'
                                  : 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800 text-green-700 dark:text-green-400 hover:bg-green-100 dark:hover:bg-green-900/30'
                              }`}
                            >
                              Onayla
                            </button>
                            <button
                              onClick={() => handleReject(req.id)}
                              disabled={isProcessing}
                              className={`text-xs font-medium px-2.5 py-1 rounded-md border transition-colors ${
                                isProcessing
                                  ? 'opacity-50 cursor-not-allowed bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800 text-red-600 dark:text-red-400'
                                  : 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/30'
                              }`}
                            >
                              Reddet
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        /* Kayıt Talepleri içeriği */
        <div>
          {regError && (
            <div className="mx-6 mt-4 px-4 py-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
              <p className="text-sm text-red-700 dark:text-red-400">{regError}</p>
            </div>
          )}

          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-100 dark:border-slate-700">
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    Ad Soyad
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    Email
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    Not
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    Tarih
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    Durum
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                    İşlemler
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50 dark:divide-slate-700">
                {regIsLoading ? (
                  <>
                    <SkeletonRow cols={6} />
                    <SkeletonRow cols={6} />
                    <SkeletonRow cols={6} />
                  </>
                ) : regRequests.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-4 py-12 text-center text-sm text-slate-400">
                      Bekleyen kayıt talebi bulunamadı.
                    </td>
                  </tr>
                ) : (
                  regRequests.map((req) => {
                    const isProcessing = regProcessingId === req.id
                    return (
                      <tr key={req.id} className="hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors">
                        <td className="px-4 py-3 text-slate-800 dark:text-slate-100 whitespace-nowrap">
                          {req.firstName} {req.lastName}
                        </td>
                        <td className="px-4 py-3 text-slate-600 dark:text-slate-300 whitespace-nowrap">
                          {req.email}
                        </td>
                        <td className="px-4 py-3 text-slate-600 dark:text-slate-300 max-w-xs truncate">
                          {req.note ?? '—'}
                        </td>
                        <td className="px-4 py-3 text-slate-500 dark:text-slate-400 whitespace-nowrap">
                          {formatDate(req.createdAt)}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap">
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-violet-50 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 border border-violet-200 dark:border-violet-700">
                            Bekliyor
                          </span>
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap">
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => handleRegApprove(req.id)}
                              disabled={isProcessing}
                              className={`text-xs font-medium px-2.5 py-1 rounded-md border transition-colors ${
                                isProcessing
                                  ? 'opacity-50 cursor-not-allowed bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800 text-green-700 dark:text-green-400'
                                  : 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800 text-green-700 dark:text-green-400 hover:bg-green-100 dark:hover:bg-green-900/30'
                              }`}
                            >
                              Onayla
                            </button>
                            <button
                              onClick={() => handleRegReject(req.id)}
                              disabled={isProcessing}
                              className={`text-xs font-medium px-2.5 py-1 rounded-md border transition-colors ${
                                isProcessing
                                  ? 'opacity-50 cursor-not-allowed bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800 text-red-600 dark:text-red-400'
                                  : 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/30'
                              }`}
                            >
                              Reddet
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

export default RequestsPage
