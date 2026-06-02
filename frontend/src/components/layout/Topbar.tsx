import { useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useTheme } from '../../context/ThemeContext'
import type { UserRole } from '../../types/auth.types'
import NotificationBell from '../notifications/NotificationBell'

function MoonIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
    </svg>
  )
}

function SunIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="12" cy="12" r="5" />
      <line x1="12" y1="1" x2="12" y2="3" />
      <line x1="12" y1="21" x2="12" y2="23" />
      <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
      <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
      <line x1="1" y1="12" x2="3" y2="12" />
      <line x1="21" y1="12" x2="23" y2="12" />
      <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
      <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
    </svg>
  )
}

const roleBadgeClass: Record<UserRole, string> = {
  MANAGER: 'bg-purple-100 text-purple-700',
  AGENT: 'bg-violet-100 text-violet-700',
  CUSTOMER: 'bg-green-100 text-green-700',
}

const pageTitles: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/tickets': 'Talepler',
  '/tickets/create': 'Yeni Talep Oluştur',
  '/users': 'Kullanıcı Yönetimi',
  '/workload': 'Agent İş Yükü',
  '/requests': 'İstekler',
}

function Topbar() {
  const { email, role, logout } = useAuth()
  const { pathname } = useLocation()
  const { theme, toggleTheme } = useTheme()

  const pageTitle = pageTitles[pathname] ?? 'Dashboard'

  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6 flex-shrink-0">
      <h1 className="text-base font-semibold text-slate-800">{pageTitle}</h1>

      <div className="flex items-center gap-3">
        {email && (
          <span className="text-sm text-slate-600">{email}</span>
        )}
        {role && (
          <span className={`text-xs font-medium px-2.5 py-1 rounded-full ${roleBadgeClass[role]}`}>
            {role}
          </span>
        )}
        <div className="w-px h-4 bg-slate-200" />
        <NotificationBell />
        <div className="w-px h-4 bg-slate-200" />
        <button
          onClick={toggleTheme}
          aria-label="Tema değiştir"
          className="text-slate-400 hover:text-slate-700 transition-colors"
        >
          {theme === 'dark' ? <SunIcon /> : <MoonIcon />}
        </button>
        <div className="w-px h-4 bg-slate-200" />
        <button
          onClick={logout}
          className="text-sm text-slate-400 hover:text-slate-700 transition-colors"
        >
          Çıkış
        </button>
      </div>
    </header>
  )
}

export default Topbar
