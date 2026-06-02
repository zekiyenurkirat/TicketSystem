import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useNotifications } from '../../hooks/useNotifications'
import type { NotificationSeverity } from '../../hooks/useNotifications'

const SEVERITY_ROW: Record<NotificationSeverity, string> = {
  critical: 'border-l-red-500 bg-red-50 dark:bg-red-900/20',
  warning:  'border-l-amber-400 bg-amber-50 dark:bg-amber-900/20',
  info:     'border-l-blue-400 bg-blue-50 dark:bg-blue-900/20',
}

const SEVERITY_TEXT: Record<NotificationSeverity, string> = {
  critical: 'text-red-700 dark:text-red-300',
  warning:  'text-amber-700 dark:text-amber-300',
  info:     'text-blue-700 dark:text-blue-300',
}

function BellIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
      <path d="M13.73 21a2 2 0 0 1-3.46 0" />
    </svg>
  )
}

function NotificationBell() {
  const { items, unseenCount, isLoading, markSeen, markAllSeen } = useNotifications()
  const [open, setOpen] = useState(false)

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((prev) => !prev)}
        className="relative flex items-center justify-center w-8 h-8 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
        aria-label="Bildirimler"
      >
        <BellIcon />
        {!isLoading && unseenCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 flex items-center justify-center min-w-[1rem] h-4 px-1 rounded-full bg-red-500 text-white text-[10px] font-bold leading-none">
            {unseenCount}
          </span>
        )}
      </button>

      {open && (
        <>
          <div
            className="fixed inset-0 z-40"
            onClick={() => setOpen(false)}
          />
          <div className="absolute right-0 top-10 z-50 w-72 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-lg overflow-hidden">

            {/* Başlık + tümünü okundu butonu */}
            <div className="px-4 py-3 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between">
              <h3 className="text-xs font-semibold text-slate-600 dark:text-slate-300 uppercase tracking-wide">
                Bildirimler
              </h3>
              {unseenCount > 0 && (
                <button
                  onClick={() => markAllSeen()}
                  className="text-[11px] text-violet-600 hover:text-violet-800 font-medium transition-colors"
                >
                  Tümünü okundu işaretle
                </button>
              )}
            </div>

            {/* İçerik */}
            {isLoading ? (
              <div className="px-4 py-3 space-y-2">
                {[1, 2].map((i) => (
                  <div key={i} className="h-10 bg-slate-100 dark:bg-slate-700 rounded animate-pulse" />
                ))}
              </div>
            ) : items.length === 0 ? (
              <div className="px-4 py-6 text-center">
                <p className="text-sm text-slate-400">Bildirim yok</p>
              </div>
            ) : (
              <ul className="py-1 max-h-80 overflow-y-auto">
                {items.map((item) => (
                  <li key={item.id}>
                    <Link
                      to={item.to}
                      onClick={() => {
                        if (!item.seen) markSeen(item.id)
                        setOpen(false)
                      }}
                      className={`flex w-full items-center gap-3 px-4 py-3 border-l-4 transition-opacity hover:opacity-80 ${
                        SEVERITY_ROW[item.severity]
                      } ${item.seen ? 'opacity-40' : ''}`}
                    >
                      <span
                        className={`flex-1 text-xs font-medium leading-snug pointer-events-none ${
                          SEVERITY_TEXT[item.severity]
                        }`}
                      >
                        {item.message}
                      </span>
                      <span className="text-slate-400 text-xs flex-shrink-0 pointer-events-none">
                        →
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
      )}
    </div>
  )
}

export default NotificationBell
