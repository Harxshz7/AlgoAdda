import { useState, useEffect } from 'react'

interface HealthResponse {
  status?: string
  error?: string
}

export function App() {
  const [backendHealth, setBackendHealth] = useState<HealthResponse | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [apiUrl] = useState<string>(import.meta.env.VITE_API_URL || 'http://localhost:8080')

  const checkBackendHealth = async () => {
    setLoading(true)
    try {
      const response = await fetch(`${apiUrl}/api/health`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
        },
      })
      if (!response.ok) {
        throw new Error(`HTTP Error ${response.status}: ${response.statusText}`)
      }
      const data = await response.json()
      setBackendHealth(data)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Unable to connect to Core API'
      setBackendHealth({ error: message })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    checkBackendHealth()
  }, [])

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between selection:bg-indigo-500 selection:text-white">
      {/* Background glow effects */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none -z-10">
        <div className="absolute -top-40 -left-40 w-96 h-96 bg-indigo-600/20 rounded-full blur-[128px]"></div>
        <div className="absolute top-1/2 -right-40 w-96 h-96 bg-emerald-600/15 rounded-full blur-[128px]"></div>
      </div>

      {/* Navigation / Header */}
      <header className="border-b border-slate-800/80 backdrop-blur-md bg-slate-950/60 sticky top-0 z-10 px-6 py-4">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-indigo-500 to-emerald-400 flex items-center justify-center font-black text-slate-950 text-sm shadow-md shadow-indigo-500/20">
              AA
            </div>
            <span className="font-bold tracking-tight text-xl bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
              AlgoAdda
            </span>
          </div>
          <div className="flex items-center gap-2">
            <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              Phase 0 • Foundation
            </span>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-4xl mx-auto w-full px-6 py-16 flex flex-col items-center justify-center text-center">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-slate-900 border border-slate-800 text-xs text-slate-400 mb-6">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
          SEBI-Compliant White Box Trading Bot Marketplace
        </div>

        <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight text-white max-w-2xl mb-6 leading-tight">
          AlgoAdda — <span className="bg-gradient-to-r from-indigo-400 to-emerald-400 bg-clip-text text-transparent">coming soon</span>
        </h1>

        <p className="text-slate-400 text-base sm:text-lg max-w-xl mb-12">
          A marketplace for buying and selling automated trading bots with fully disclosed strategy logic and verified backtest methodology.
        </p>

        {/* Backend Connectivity Status Box */}
        <div className="w-full max-w-md bg-slate-900/80 border border-slate-800 rounded-2xl p-6 backdrop-blur shadow-xl text-left">
          <div className="flex items-center justify-between pb-4 border-b border-slate-800">
            <div className="flex items-center gap-2.5">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
                Core API Connection
              </span>
            </div>
            <button
              onClick={checkBackendHealth}
              disabled={loading}
              className="text-xs px-2.5 py-1 rounded-md bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium transition disabled:opacity-50"
            >
              {loading ? 'Checking...' : 'Refresh'}
            </button>
          </div>

          <div className="mt-4 space-y-3">
            <div className="flex items-center justify-between text-sm">
              <span className="text-slate-400">Target Endpoint:</span>
              <code className="text-xs bg-slate-950 px-2 py-1 rounded text-indigo-300 border border-slate-800">
                {apiUrl}/api/health
              </code>
            </div>

            <div className="flex items-center justify-between text-sm">
              <span className="text-slate-400">Status:</span>
              {loading ? (
                <span className="text-slate-400 flex items-center gap-1.5 text-xs">
                  <span className="w-2 h-2 rounded-full bg-amber-400 animate-ping"></span>
                  Checking...
                </span>
              ) : backendHealth?.status === 'ok' ? (
                <span className="inline-flex items-center gap-1.5 text-xs font-medium text-emerald-400 bg-emerald-950/60 px-2.5 py-1 rounded-full border border-emerald-800/50">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
                  Connected ({backendHealth.status})
                </span>
              ) : (
                <span className="inline-flex items-center gap-1.5 text-xs font-medium text-amber-400 bg-amber-950/60 px-2.5 py-1 rounded-full border border-amber-800/50">
                  <span className="w-1.5 h-1.5 rounded-full bg-amber-400"></span>
                  Offline / Waiting for DB
                </span>
              )}
            </div>

            {backendHealth?.error && (
              <div className="mt-3 p-3 bg-red-950/30 border border-red-900/50 rounded-lg text-xs text-red-300">
                {backendHealth.error}
              </div>
            )}
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800/60 px-6 py-6 text-center text-xs text-slate-500">
        <p>AlgoAdda • Phase 0 Local Scaffolding • Spring Boot + React + FastAPI</p>
      </footer>
    </div>
  )
}

export default App
