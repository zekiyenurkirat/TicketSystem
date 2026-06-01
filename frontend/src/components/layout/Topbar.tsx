import { useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import type { UserRole } from '../../types/auth.types'

const roleBadgeClass: Record<UserRole, string> = {
  MANAGER: 'bg-purple-100 text-purple-700',
  AGENT: 'bg-violet-100 text-violet-700',
  CUSTOMER: 'bg-green-100 text-green-700',
}

const pageTitles: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/tickets': 'Talepler',
  '/users': 'Kullanıcı Yönetimi',
}

function Topbar() {
  const { email, role, logout } = useAuth()
  const { pathname } = useLocation()

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
