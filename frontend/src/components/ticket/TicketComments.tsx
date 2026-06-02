import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import type { CommentResponse, CommentType } from '../../types/comment.types'
import { addComment, getCommentsForTicket } from '../../api/comment.api'

type TicketCommentsProps = {
  ticketId: number
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('tr-TR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function TicketComments({ ticketId }: TicketCommentsProps) {
  const { userId, role } = useAuth()

  const [comments, setComments] = useState<CommentResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [content, setContent] = useState('')
  const [commentType, setCommentType] = useState<CommentType>('EXTERNAL')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  const showInternalToggle = role === 'AGENT' || role === 'MANAGER'

  function refreshList() {
    if (userId === null) return
    getCommentsForTicket(ticketId, userId)
      .then(setComments)
      .catch(() => {})
  }

  useEffect(() => {
    if (userId === null) {
      setIsLoading(false)
      return
    }
    setIsLoading(true)
    setLoadError(null)
    getCommentsForTicket(ticketId, userId)
      .then(setComments)
      .catch((err) =>
        setLoadError(
          err instanceof Error ? err.message : 'Yorumlar yüklenirken bir hata oluştu.'
        )
      )
      .finally(() => setIsLoading(false))
  }, [])

  async function handleSubmit() {
    if (userId === null) {
      setFormError('Kullanıcı bilgisi alınamadı. Lütfen çıkış yapıp tekrar giriş yapın.')
      return
    }
    if (content.trim() === '') {
      setFormError('Yorum içeriği boş bırakılamaz.')
      return
    }
    setIsSubmitting(true)
    setFormError(null)
    try {
      await addComment({
        ticketId,
        authorId: userId,
        content: content.trim(),
        type: role === 'CUSTOMER' ? 'EXTERNAL' : commentType,
      })
      setContent('')
      setCommentType('EXTERNAL')
      refreshList()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Yorum eklenirken bir hata oluştu.')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (userId === null) {
    return (
      <div>
        <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wide mb-2">
          Yorumlar
        </h4>
        <p className="text-xs text-red-500">
          Yorumları görüntülemek için oturum bilgisi alınamadı.
        </p>
      </div>
    )
  }

  return (
    <div>
      <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wide mb-3">
        Yorumlar
      </h4>

      {isLoading ? (
        <p className="text-xs text-slate-400 mb-4">Yorumlar yükleniyor...</p>
      ) : loadError ? (
        <p className="text-xs text-red-500 mb-4">{loadError}</p>
      ) : comments.length === 0 ? (
        <p className="text-xs text-slate-400 mb-4">
          Bu talep için henüz yorum eklenmemiş.
        </p>
      ) : (
        <div className="space-y-2 mb-4">
          {comments.map((c) => (
            <div
              key={c.id}
              className={`rounded-lg px-3 py-2.5 ${
                c.type === 'INTERNAL'
                  ? 'bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800'
                  : 'bg-slate-50 dark:bg-slate-700/50 border border-slate-200 dark:border-slate-600'
              }`}
            >
              <div className="flex items-center justify-between gap-2 mb-1">
                <span className="text-xs font-medium text-slate-700 dark:text-slate-200">{c.authorFullName}</span>
                <div className="flex items-center gap-1.5 flex-shrink-0">
                  {c.type === 'INTERNAL' && (
                    <span className="text-xs font-medium text-amber-700 dark:text-amber-300 bg-amber-100 dark:bg-amber-900/30 px-1.5 py-0.5 rounded">
                      İç Not
                    </span>
                  )}
                  <span className="text-xs text-slate-400">{formatDateTime(c.createdAt)}</span>
                </div>
              </div>
              <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed whitespace-pre-wrap">
                {c.content}
              </p>
            </div>
          ))}
        </div>
      )}

      <div>
        {showInternalToggle && (
          <div className="flex gap-1.5 mb-2">
            <button
              type="button"
              disabled={isSubmitting}
              onClick={() => {
                setCommentType('EXTERNAL')
                setFormError(null)
              }}
              className={`px-2.5 py-1 text-xs font-medium rounded-md transition-colors ${
                commentType === 'EXTERNAL'
                  ? 'bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 border border-violet-300 dark:border-violet-700'
                  : 'bg-white dark:bg-slate-700 text-slate-500 dark:text-slate-400 border border-slate-200 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-600'
              }`}
            >
              Dış Yorum
            </button>
            <button
              type="button"
              disabled={isSubmitting}
              onClick={() => {
                setCommentType('INTERNAL')
                setFormError(null)
              }}
              className={`px-2.5 py-1 text-xs font-medium rounded-md transition-colors ${
                commentType === 'INTERNAL'
                  ? 'bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300 border border-amber-300 dark:border-amber-700'
                  : 'bg-white dark:bg-slate-700 text-slate-500 dark:text-slate-400 border border-slate-200 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-600'
              }`}
            >
              İç Not
            </button>
          </div>
        )}

        {showInternalToggle && commentType === 'INTERNAL' && (
          <p className="text-xs text-amber-600 dark:text-amber-400 mb-1.5">
            ⚠ Bu yorum müşteri tarafından görülmez.
          </p>
        )}

        <textarea
          value={content}
          disabled={isSubmitting}
          maxLength={2000}
          rows={3}
          placeholder={commentType === 'INTERNAL' ? 'İç not ekleyin...' : 'Yorum ekleyin...'}
          onChange={(e) => {
            setContent(e.target.value)
            setFormError(null)
          }}
          className={`w-full px-2 py-1.5 text-xs border rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-400 disabled:bg-slate-50 dark:disabled:bg-slate-600 disabled:text-slate-400 resize-none ${
            showInternalToggle && commentType === 'INTERNAL'
              ? 'border-amber-200 dark:border-amber-800 bg-amber-50 dark:bg-amber-900/20 text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500'
              : 'border-slate-200 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500'
          }`}
        />

        <div className="flex items-center justify-between mt-1.5">
          <span className="text-xs text-slate-400">{content.length}/2000</span>
          <button
            type="button"
            disabled={isSubmitting || content.trim() === ''}
            onClick={handleSubmit}
            className="px-3 py-1.5 text-xs font-medium text-white bg-violet-600 hover:bg-violet-700 rounded-lg transition-colors disabled:opacity-50"
          >
            {isSubmitting ? '...' : 'Gönder'}
          </button>
        </div>

        {formError && <p className="mt-1 text-xs text-red-600">{formError}</p>}
      </div>
    </div>
  )
}

export default TicketComments
