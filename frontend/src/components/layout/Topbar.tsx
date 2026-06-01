function Topbar() {
  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6 flex-shrink-0">
      <h1 className="text-base font-semibold text-slate-800">Dashboard</h1>

      <div className="flex items-center gap-3">
        <span className="text-sm text-slate-600">Kullanıcı Adı</span>
        <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-violet-100 text-violet-700">
          AGENT
        </span>
      </div>
    </header>
  )
}

export default Topbar
