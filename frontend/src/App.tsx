import { AuthProvider, useAuth } from './context/AuthContext'
import AppLayout from './components/layout/AppLayout'
import LoginPage from './pages/LoginPage'

function AppContent() {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) return null

  return isAuthenticated ? <AppLayout /> : <LoginPage />
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  )
}

export default App
