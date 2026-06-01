import { useAuth } from '../context/AuthContext'
import CustomerDashboard from '../components/dashboard/CustomerDashboard'
import AgentDashboard from '../components/dashboard/AgentDashboard'
import ManagerDashboard from '../components/dashboard/ManagerDashboard'

function DashboardPage() {
  const { role, userId } = useAuth()

  if (role === 'CUSTOMER') return <CustomerDashboard userId={userId} />
  if (role === 'AGENT')    return <AgentDashboard userId={userId} />
  return <ManagerDashboard />
}

export default DashboardPage
