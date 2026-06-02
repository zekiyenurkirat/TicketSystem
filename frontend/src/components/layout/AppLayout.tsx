import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import Topbar from './Topbar'
import { PendingRequestCountsProvider } from '../../context/PendingRequestCountsContext'

function AppLayout() {
  return (
    <PendingRequestCountsProvider>
      <div className="h-screen flex overflow-hidden">
        <Sidebar />

        <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
          <Topbar />

          <main className="flex-1 overflow-auto bg-slate-50 dark:bg-slate-900 p-6">
            <Outlet />
          </main>
        </div>
      </div>
    </PendingRequestCountsProvider>
  )
}

export default AppLayout
