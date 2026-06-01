import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import type { UserRole } from '../types/auth.types'

type Props = {
  requiredRole?: UserRole
}

function ProtectedRoute({ requiredRole }: Props) {
  const { isAuthenticated, role, isLoading } = useAuth()

  if (isLoading) return null

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (requiredRole && role !== requiredRole) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}

export default ProtectedRoute
