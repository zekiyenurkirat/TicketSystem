import Sidebar from './Sidebar'
import Topbar from './Topbar'

type StatCardProps = {
  title: string
  accent: string
}

function StatCard({ title, accent }: StatCardProps) {
  return (
    <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
      <div className="flex items-center justify-between mb-3">
        <span className="text-sm font-medium text-slate-600">{title}</span>
        <span className={`w-2.5 h-2.5 rounded-full ${accent}`} />
      </div>
      <p className="text-3xl font-bold text-slate-800">—</p>
    </div>
  )
}

function AppLayout() {
  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar />

      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Topbar />

        <main className="flex-1 overflow-auto bg-slate-50 p-6">
          <div className="mb-6">
            <h2 className="text-xl font-semibold text-slate-800">Hoş geldiniz</h2>
            <p className="text-sm text-slate-500 mt-1">
              Güncel talep durumuna genel bakış
            </p>
          </div>

          <div className="grid grid-cols-3 gap-4 mb-6">
            <StatCard title="Açık Talepler" accent="bg-blue-500" />
            <StatCard title="Beklemede" accent="bg-amber-400" />
            <StatCard title="Çözülen" accent="bg-green-500" />
          </div>

          <div className="bg-white rounded-xl border border-slate-200 shadow-sm">
            <div className="px-6 py-4 border-b border-slate-200">
              <h3 className="text-sm font-semibold text-slate-700">Talepler</h3>
            </div>
            <div className="px-6 py-12 text-center">
              <p className="text-sm text-slate-400">Ticket tablosu burada olacak</p>
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}

export default AppLayout
