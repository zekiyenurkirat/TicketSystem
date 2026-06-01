import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import AppLayout from '../components/layout/AppLayout'
import LoginPage from '../pages/LoginPage'
import DashboardPage from '../pages/DashboardPage'
import TicketListPage from '../pages/TicketListPage'
import TicketCreatePage from '../pages/TicketCreatePage'
import UserManagementPage from '../pages/UserManagementPage'
import ProtectedRoute from './ProtectedRoute'

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/tickets" element={<TicketListPage />} />
            <Route path="/tickets/create" element={<TicketCreatePage />} />
            <Route element={<ProtectedRoute requiredRole="MANAGER" />}>
              <Route path="/users" element={<UserManagementPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default AppRouter
