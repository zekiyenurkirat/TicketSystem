import { useEffect, useState } from 'react'
import {
  getAssignmentRequests,
  approveAssignmentRequest,
  rejectAssignmentRequest,
} from '../api/assignmentRequest.api'
import type { AssignmentRequestResponse } from '../types/assignmentRequest.types'

type ActiveTab = 'agent' | 'registration'

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('tr-TR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

function SkeletonRow() {
  return (
    <tr>
      {Array.from({ length: 5 }).map((_, i) => (
        <td key={i} className="px-4 py-3">
          <div className="h-4 bg-slate-100 rounded animate-pulse" />
        </td>
      ))}
    </tr>
  )
}

function RequestsPage() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('agent')
  const [requests, setRequests] = useState<AssignmentRequestResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)

  async function load() {
    setIsLoading(true)
    setError(null)
    try {
      const data = await getAssignmentRequests('PENDING')
      setRequests(data)
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
      setError(
        err instanceof Error ? err.message : 'İstek onaylanırken bir hata oluştu.'
      )
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
      setError(
        err instanceof Error ? err.message : 'İstek reddedilirken bir hata oluştu.'
      )
    } finally {
      setProcessingId(null)
    }
  }

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm">

      {/* Tab bar */}
      <div className="flex border-b border-slate-200 px-6">
        <button
          onClick={() => setActiveTab('agent')}
          className={`py-4 mr-6 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'agent'
              ? 'border-violet-600 text-violet-700'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          Agent Talepleri
        </button>
        <button
          onClick={() => setActiveTab('registration')}
          className={`py-4 flex items-center gap-2 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'registration'
              ? 'border-violet-600 text-violet-700'
              : 'border-transparent text-slate-400 hover:text-slate-500'
          }`}
        >
          Kayıt Talepleri
          <span className="text-[10px] font-medium bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded leading-none">
            Yakında
          </span>
        </button>
      </div>

      {/* Agent Talepleri içeriği */}
      {activeTab === 'agent' ? (
        <div>
          {error && (
            <div className="mx-6 mt-4 px-4 py-3 rounded-lg bg-red-50 border border-red-200">
              <p className="text-sm text-red-700">{error}</p>
            </div>
          )}

          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-100">
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">
                    Agent
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">
                    Ticket No
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">
                    Not
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">
                    Tarih
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">
                    İşlemler
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {isLoading ? (
                  <>
                    <SkeletonRow />
                    <SkeletonRow />
                    <SkeletonRow />
                  </>
                ) : requests.length === 0 ? (
                  <tr>
                    <td
                      colSpan={5}
                      className="px-4 py-12 text-center text-sm text-slate-400"
                    >
                      Bekleyen atama isteği bulunamadı.
                    </td>
                  </tr>
                ) : (
                  requests.map((req) => {
                    const isProcessing = processingId === req.id
                    return (
                      <tr
                        key={req.id}
                        className="hover:bg-slate-50 transition-colors"
                      >
                        <td className="px-4 py-3 text-slate-800 whitespace-nowrap">
                          {req.requestedByFullName}
                        </td>
                        <td className="px-4 py-3 font-mono text-xs text-slate-600 whitespace-nowrap">
                          {req.ticketNumber}
                        </td>
                        <td className="px-4 py-3 text-slate-600 max-w-xs truncate">
                          {req.note ?? '—'}
                        </td>
                        <td className="px-4 py-3 text-slate-500 whitespace-nowrap">
                          {formatDate(req.createdAt)}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap">
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => handleApprove(req.id)}
                              disabled={isProcessing}
                              className={`text-xs font-medium px-2.5 py-1 rounded-md border transition-colors ${
                                isProcessing
                                  ? 'opacity-50 cursor-not-allowed bg-green-50 border-green-200 text-green-700'
                                  : 'bg-green-50 border-green-200 text-green-700 hover:bg-green-100'
                              }`}
                            >
                              Onayla
                            </button>
                            <button
                              onClick={() => handleReject(req.id)}
                              disabled={isProcessing}
                              className={`text-xs font-medium px-2.5 py-1 rounded-md border transition-colors ${
                                isProcessing
                                  ? 'opacity-50 cursor-not-allowed bg-red-50 border-red-200 text-red-600'
                                  : 'bg-red-50 border-red-200 text-red-600 hover:bg-red-100'
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
        /* Kayıt Talepleri — yakında */
        <div className="px-4 py-16 text-center">
          <p className="text-sm text-slate-400">Bu bölüm yakında gelecek.</p>
        </div>
      )}
    </div>
  )
}

export default RequestsPage
