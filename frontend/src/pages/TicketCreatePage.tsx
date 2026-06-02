import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { createTicket } from '../api/ticket.api'
import type { Priority, Impact, Urgency } from '../types/ticket.types'

const CUSTOMER_PRIORITY_OPTIONS: { value: Priority; label: string }[] = [
  { value: 'HIGH', label: 'Yüksek' },
  { value: 'MEDIUM', label: 'Orta' },
  { value: 'LOW', label: 'Düşük' },
]

const ALL_PRIORITY_OPTIONS: { value: Priority; label: string }[] = [
  { value: 'BLOCKER', label: 'Blocker' },
  { value: 'CRITICAL', label: 'Kritik' },
  { value: 'HIGH', label: 'Yüksek' },
  { value: 'MEDIUM', label: 'Orta' },
  { value: 'LOW', label: 'Düşük' },
]

const IMPACT_OPTIONS: { value: Impact; label: string; description: string }[] = [
  { value: 'LOW', label: 'Düşük', description: 'Tek kullanıcı veya küçük bir grup etkileniyor' },
  { value: 'MEDIUM', label: 'Orta', description: 'Birden fazla kullanıcı veya departman etkileniyor' },
  { value: 'HIGH', label: 'Yüksek', description: 'Tüm kuruluş veya kritik iş akışı etkileniyor' },
]

const URGENCY_OPTIONS: { value: Urgency; label: string; description: string }[] = [
  { value: 'LOW', label: 'Düşük', description: 'Ertelenebilir, iş akışı devam ediyor' },
  { value: 'MEDIUM', label: 'Orta', description: 'Kısa sürede çözülmeli, geçici çözüm mevcut' },
  { value: 'HIGH', label: 'Yüksek', description: 'Acil müdahale gerekiyor, iş akışı durma noktasında' },
]

const TITLE_MAX = 255

function TicketCreatePage() {
  const navigate = useNavigate()
  const { role, userId } = useAuth()

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [customerPriority, setCustomerPriority] = useState<Priority | ''>('')
  const [impact, setImpact] = useState<Impact | ''>('')
  const [urgency, setUrgency] = useState<Urgency | ''>('')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (userId === null) {
    return (
      <div className="max-w-2xl mx-auto">
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm px-6 py-12 text-center">
          <p className="text-sm text-red-600">
            Kullanıcı kimliği bulunamadı. Lütfen çıkış yapıp tekrar giriş yapın.
          </p>
        </div>
      </div>
    )
  }

  const priorityOptions = role === 'CUSTOMER' ? CUSTOMER_PRIORITY_OPTIONS : ALL_PRIORITY_OPTIONS

  function clearFieldError(field: string) {
    setErrors((prev) => {
      const next = { ...prev }
      delete next[field]
      return next
    })
  }

  function validate(): Record<string, string> {
    const errs: Record<string, string> = {}
    if (!title.trim()) {
      errs.title = 'Başlık zorunludur.'
    } else if (title.trim().length > TITLE_MAX) {
      errs.title = `Başlık en fazla ${TITLE_MAX} karakter olabilir.`
    }
    if (!description.trim()) errs.description = 'Açıklama zorunludur.'
    if (!customerPriority) errs.customerPriority = 'Öncelik seçiniz.'
    if (!impact) errs.impact = 'Etki seçiniz.'
    if (!urgency) errs.urgency = 'Aciliyet seçiniz.'
    return errs
  }

  async function handleSubmit() {
    const errs = validate()
    if (Object.keys(errs).length > 0) {
      setErrors(errs)
      return
    }

    if (userId === null) {
      setSubmitError('Kullanıcı kimliği bulunamadı. Lütfen çıkış yapıp tekrar giriş yapın.')
      return
    }

    setIsSubmitting(true)
    setSubmitError(null)
    try {
      await createTicket({
        createdById: userId,
        title: title.trim(),
        description: description.trim(),
        customerPriority: customerPriority as Priority,
        impact: impact as Impact,
        urgency: urgency as Urgency,
      })
      navigate('/tickets')
    } catch (err) {
      setSubmitError(
        err instanceof Error ? err.message : 'Ticket oluşturulurken bir hata oluştu.'
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto">
      <div className="mb-5 flex items-center gap-3">
        <button
          onClick={() => navigate('/tickets')}
          className="text-sm text-slate-400 dark:text-slate-500 hover:text-slate-700 dark:hover:text-slate-200 transition-colors"
        >
          ← Talepler
        </button>
        <span className="text-slate-200 dark:text-slate-600">/</span>
        <h2 className="text-sm font-semibold text-slate-800 dark:text-slate-100">Yeni Talep Oluştur</h2>
      </div>

      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
        <form
          noValidate
          onSubmit={(e) => {
            e.preventDefault()
            handleSubmit()
          }}
        >
          <div className="px-6 py-5 space-y-5">
            {/* Başlık */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                Başlık <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={title}
                disabled={isSubmitting}
                onChange={(e) => {
                  setTitle(e.target.value)
                  clearFieldError('title')
                }}
                placeholder="Sorunu kısaca özetleyin"
                className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-none focus:ring-2 focus:ring-violet-400 focus:border-transparent bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 disabled:bg-slate-50 dark:disabled:bg-slate-600 disabled:text-slate-400 ${
                  errors.title ? 'border-red-300 dark:border-red-600' : 'border-slate-200 dark:border-slate-600'
                }`}
              />
              <div className="flex justify-between mt-1">
                {errors.title ? (
                  <p className="text-xs text-red-600">{errors.title}</p>
                ) : (
                  <span />
                )}
                <span className={`text-xs ${title.length > TITLE_MAX ? 'text-red-500' : 'text-slate-400'}`}>
                  {title.length}/{TITLE_MAX}
                </span>
              </div>
            </div>

            {/* Açıklama */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                Açıklama <span className="text-red-500">*</span>
              </label>
              <textarea
                value={description}
                disabled={isSubmitting}
                onChange={(e) => {
                  setDescription(e.target.value)
                  clearFieldError('description')
                }}
                placeholder="Sorunu ayrıntılı açıklayın"
                rows={4}
                className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-none focus:ring-2 focus:ring-violet-400 focus:border-transparent bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 disabled:bg-slate-50 dark:disabled:bg-slate-600 disabled:text-slate-400 resize-none ${
                  errors.description ? 'border-red-300 dark:border-red-600' : 'border-slate-200 dark:border-slate-600'
                }`}
              />
              {errors.description && (
                <p className="mt-1 text-xs text-red-600">{errors.description}</p>
              )}
            </div>

            {/* Müşteri Önceliği */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                Öncelik <span className="text-red-500">*</span>
              </label>
              <select
                value={customerPriority}
                disabled={isSubmitting}
                onChange={(e) => {
                  setCustomerPriority(e.target.value as Priority)
                  clearFieldError('customerPriority')
                }}
                className={`w-full px-3 py-2 text-sm rounded-lg border bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-100 transition-colors focus:outline-none focus:ring-2 focus:ring-violet-400 focus:border-transparent disabled:bg-slate-50 dark:disabled:bg-slate-600 disabled:text-slate-400 ${
                  errors.customerPriority ? 'border-red-300 dark:border-red-600' : 'border-slate-200 dark:border-slate-600'
                }`}
              >
                <option value="">— Seçiniz —</option>
                {priorityOptions.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
              {errors.customerPriority && (
                <p className="mt-1 text-xs text-red-600">{errors.customerPriority}</p>
              )}
            </div>

            {/* Etki */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                Etki <span className="text-red-500">*</span>
              </label>
              <div className="grid grid-cols-3 gap-2">
                {IMPACT_OPTIONS.map((opt) => (
                  <button
                    key={opt.value}
                    type="button"
                    disabled={isSubmitting}
                    onClick={() => {
                      setImpact(opt.value)
                      clearFieldError('impact')
                    }}
                    className={`flex flex-col p-3 rounded-lg border text-left transition-colors disabled:opacity-50 ${
                      impact === opt.value
                        ? 'border-violet-400 dark:border-violet-500 bg-violet-50 dark:bg-violet-900/20'
                        : 'border-slate-200 dark:border-slate-600 hover:border-violet-200 dark:hover:border-violet-700 hover:bg-slate-50 dark:hover:bg-slate-700'
                    }`}
                  >
                    <span className="text-sm font-medium text-slate-800 dark:text-slate-100">{opt.label}</span>
                    <span className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 leading-relaxed">
                      {opt.description}
                    </span>
                  </button>
                ))}
              </div>
              {errors.impact && (
                <p className="mt-1 text-xs text-red-600">{errors.impact}</p>
              )}
            </div>

            {/* Aciliyet */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                Aciliyet <span className="text-red-500">*</span>
              </label>
              <div className="grid grid-cols-3 gap-2">
                {URGENCY_OPTIONS.map((opt) => (
                  <button
                    key={opt.value}
                    type="button"
                    disabled={isSubmitting}
                    onClick={() => {
                      setUrgency(opt.value)
                      clearFieldError('urgency')
                    }}
                    className={`flex flex-col p-3 rounded-lg border text-left transition-colors disabled:opacity-50 ${
                      urgency === opt.value
                        ? 'border-violet-400 dark:border-violet-500 bg-violet-50 dark:bg-violet-900/20'
                        : 'border-slate-200 dark:border-slate-600 hover:border-violet-200 dark:hover:border-violet-700 hover:bg-slate-50 dark:hover:bg-slate-700'
                    }`}
                  >
                    <span className="text-sm font-medium text-slate-800 dark:text-slate-100">{opt.label}</span>
                    <span className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 leading-relaxed">
                      {opt.description}
                    </span>
                  </button>
                ))}
              </div>
              {errors.urgency && (
                <p className="mt-1 text-xs text-red-600">{errors.urgency}</p>
              )}
            </div>
          </div>

          {submitError && (
            <div className="mx-6 mb-4 px-4 py-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
              <p className="text-sm text-red-700 dark:text-red-400">{submitError}</p>
            </div>
          )}

          <div className="px-6 py-4 border-t border-slate-100 dark:border-slate-700 flex justify-end gap-3">
            <button
              type="button"
              disabled={isSubmitting}
              onClick={() => navigate('/tickets')}
              className="px-4 py-2 text-sm font-medium text-slate-600 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg transition-colors disabled:opacity-50"
            >
              İptal
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-4 py-2 text-sm font-medium text-white bg-violet-600 hover:bg-violet-700 rounded-lg transition-colors disabled:opacity-60"
            >
              {isSubmitting ? 'Oluşturuluyor...' : 'Oluştur'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default TicketCreatePage
