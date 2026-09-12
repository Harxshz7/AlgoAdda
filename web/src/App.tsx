import { useState, useEffect } from 'react'
import {
  Activity,
  RefreshCw,
  Server,
  ShieldCheck,
  Cpu,
  Terminal,
  Layers,
  Radio
} from 'lucide-react'
import { Button, Card, Input, LedIndicator } from './components/ui'

interface HealthResponse {
  status?: string
  error?: string
  timestamp?: string
  latencyMs?: number
}

export function App() {
  const [apiUrl, setApiUrl] = useState<string>(
    import.meta.env.VITE_API_URL || 'http://localhost:8080'
  )
  const [backendHealth, setBackendHealth] = useState<HealthResponse | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [lastChecked, setLastChecked] = useState<string>('--:--:--')
  const [pingCount, setPingCount] = useState<number>(0)

  const checkBackendHealth = async (overrideUrl?: string) => {
    const targetUrl = overrideUrl || apiUrl
    setLoading(true)
    const startTime = performance.now()

    try {
      const response = await fetch(`${targetUrl}/api/health`, {
        method: 'GET',
        headers: {
          Accept: 'application/json',
        },
      })

      const endTime = performance.now()
      const latencyMs = Math.round(endTime - startTime)

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const data = await response.json()
      setBackendHealth({
        status: data.status,
        timestamp: new Date().toISOString(),
        latencyMs,
      })
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : 'Connection refused / Offline'
      setBackendHealth({
        error: message,
        timestamp: new Date().toISOString(),
      })
    } finally {
      setLoading(false)
      setLastChecked(new Date().toLocaleTimeString())
      setPingCount((prev) => prev + 1)
    }
  }

  useEffect(() => {
    checkBackendHealth()
  }, [])

  const isConnected = backendHealth?.status === 'ok'

  return (
    <div className="min-h-screen bg-[#e0e5ec] text-[#2d3436] font-sans relative selection:bg-[#ff4757] selection:text-white flex flex-col justify-between">
      {/* Background Micro Noise Texture & Subtle Blueprint Grid */}
      <div className="fixed inset-0 pointer-events-none texture-noise-overlay opacity-60 z-0" />
      <div className="fixed inset-0 pointer-events-none blueprint-grid -z-10" />

      {/* Top Left Lighting Hotspot Simulation */}
      <div className="fixed -top-32 -left-32 w-96 h-96 rounded-full bg-white/40 blur-3xl pointer-events-none -z-10" />

      {/* =========================================================================
          HEADER: Bolted Chassis Bar
          ========================================================================= */}
      <header className="relative z-10 border-b border-[#babecc]/50 bg-[#e0e5ec]/90 backdrop-blur-md px-6 py-4 shadow-[0_4px_12px_rgba(0,0,0,0.04)]">
        <div className="max-w-6xl mx-auto flex flex-wrap items-center justify-between gap-4">
          {/* Brand & Stamped Hardware Badge */}
          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-xl bg-[#e0e5ec] shadow-chassis-card flex items-center justify-center border border-white/80">
              <Cpu className="text-[#ff4757] w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold tracking-tight text-xl text-[#2d3436] embossed-light">
                  AlgoAdda
                </span>
                <span className="text-[10px] font-technical font-bold uppercase tracking-widest px-2 py-0.5 rounded bg-[#d1d9e6] text-[#4a5568] shadow-[inset_1px_1px_2px_#babecc,inset_-1px_-1px_2px_#ffffff]">
                  MOD.0
                </span>
              </div>
              <p className="text-[10px] font-technical tracking-wider text-[#718096] uppercase">
                SEBI White-Box Engine Console
              </p>
            </div>
          </div>

          {/* Real-time Telemetry LED & Hardware Clock */}
          <div className="flex items-center gap-4">
            <div className="hidden sm:flex items-center gap-2 font-technical text-xs text-[#4a5568] px-3 py-1.5 rounded-lg bg-[#e0e5ec] shadow-chassis-recessed">
              <Radio className="w-3.5 h-3.5 text-[#ff4757] animate-pulse" />
              <span>CLK: {lastChecked}</span>
            </div>

            <LedIndicator
              status={loading ? 'amber' : isConnected ? 'green' : 'orange'}
              label={loading ? 'SYNCHRONIZING' : isConnected ? 'SYSTEM OPERATIONAL' : 'CORE OFFLINE'}
              sublabel={isConnected ? `PING #${pingCount} • 8080/TCP` : 'STANDBY MODE'}
            />
          </div>
        </div>
      </header>

      {/* =========================================================================
          MAIN WORKSPACE
          ========================================================================= */}
      <main className="relative z-10 flex-1 max-w-5xl mx-auto w-full px-6 py-10 md:py-14 flex flex-col gap-10 justify-center">
        
        {/* Hero Section Callout */}
        <div className="text-center max-w-2xl mx-auto space-y-3">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#e0e5ec] shadow-chassis-card border border-white/70 text-xs font-technical font-bold text-[#4a5568]">
            <span className="w-2 h-2 rounded-full bg-[#ff4757] glow-led-orange" />
            <span>ARCHITECTURE SPECIFICATION // PHASE 0</span>
          </div>

          <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight text-[#2d3436] embossed-light">
            AlgoAdda —{' '}
            <span className="text-[#ff4757]">
              coming soon
            </span>
          </h1>

          <p className="text-sm sm:text-base text-[#4a5568] font-medium leading-relaxed">
            Marketplace for buying & selling automated trading bots built on a transparent White Box compliance model. Disclosed logic, verifiable vectorbt backtests, zero black-box claims.
          </p>
        </div>

        {/* Central Physical Control Unit / Device Console */}
        <Card withScrews={true} withVents={true} elevated={true} className="max-w-3xl mx-auto w-full">
          <div className="space-y-6">
            
            {/* Console Header Bar */}
            <div className="flex flex-wrap items-center justify-between gap-2 pb-4 border-b border-[#babecc]/60">
              <div className="flex items-center gap-2 font-technical text-xs font-bold text-[#2d3436] uppercase tracking-wider">
                <Terminal className="w-4 h-4 text-[#ff4757]" />
                <span>CORE-API INTERCONNECT BUS</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-[10px] font-technical uppercase text-[#718096]">
                  PORT: 8080 // ADDR: 127.0.0.1
                </span>
              </div>
            </div>

            {/* CRT Screen Module (Level -1 Screen Well) */}
            <div className="rounded-xl bg-[#11161d] p-5 shadow-[inset_4px_4px_10px_rgba(0,0,0,0.6),inset_-2px_-2px_4px_rgba(255,255,255,0.08)] border border-[#2d3436] relative overflow-hidden">
              <div className="absolute inset-0 crt-screen-overlay pointer-events-none opacity-90" />
              
              <div className="relative z-10 font-technical text-xs space-y-3">
                <div className="flex items-center justify-between text-[#2ed573] border-b border-[#2d3436] pb-2">
                  <span className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-[#2ed573] glow-led-green animate-pulse" />
                    STATUS: {isConnected ? 'ONLINE [200 OK]' : loading ? 'PROBING...' : 'DISCONNECTED'}
                  </span>
                  <span className="text-[#718096]">
                    {backendHealth?.latencyMs ? `${backendHealth.latencyMs}ms RTT` : 'STANDBY'}
                  </span>
                </div>

                <div className="text-slate-300 font-mono text-[11px] leading-relaxed space-y-1">
                  <p className="text-[#ff4757] font-bold">&gt; GET /api/health</p>
                  {loading ? (
                    <p className="text-amber-400 animate-pulse">&gt; Transmitting probe packets to Core API daemon...</p>
                  ) : isConnected ? (
                    <div className="text-[#2ed573] bg-[#0c1015] p-3 rounded border border-emerald-900/50">
                      <code>{JSON.stringify(backendHealth, null, 2)}</code>
                    </div>
                  ) : (
                    <div className="text-[#ff4757] bg-[#1a0f12] p-3 rounded border border-red-900/50">
                      <code>{`[ERR] ${backendHealth?.error || 'Unable to establish socket connection'}`}</code>
                      <p className="text-[10px] text-slate-400 mt-1">
                        Ensure core-api is running: <span className="text-white">mvn spring-boot:run</span>
                      </p>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Mechanical Controls Row */}
            <div className="space-y-4 pt-2">
              <div className="flex flex-col sm:flex-row gap-4 items-end">
                <div className="flex-1 w-full">
                  <Input
                    label="Core API Host URL"
                    value={apiUrl}
                    onChange={(e) => setApiUrl(e.target.value)}
                    prefixIcon={<Server className="w-4 h-4" />}
                    placeholder="http://localhost:8080"
                  />
                </div>

                <div className="flex gap-2 w-full sm:w-auto">
                  <Button
                    variant="primary"
                    size="md"
                    onClick={() => checkBackendHealth(apiUrl)}
                    disabled={loading}
                    icon={<RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />}
                    className="w-full sm:w-auto"
                  >
                    {loading ? 'Pinging...' : 'Ping Core API'}
                  </Button>

                  <Button
                    variant="secondary"
                    size="md"
                    onClick={() => setApiUrl('http://localhost:8080')}
                    className="shrink-0"
                    title="Reset to default local URL"
                  >
                    Reset
                  </Button>
                </div>
              </div>
            </div>

          </div>
        </Card>

        {/* Industrial Specification Blocks */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-3xl mx-auto w-full">
          <Card withScrews={false} className="p-5 flex items-start gap-4">
            <div className="w-10 h-10 rounded-lg bg-[#e0e5ec] shadow-chassis-card flex items-center justify-center shrink-0 border border-white/80">
              <ShieldCheck className="w-5 h-5 text-[#ff4757]" />
            </div>
            <div>
              <h2 className="text-xs font-bold uppercase font-technical text-[#2d3436]">
                SEBI Compliance Gate
              </h2>
              <p className="text-xs text-[#4a5568] mt-1 leading-normal">
                Strict White Box validation. Guaranteed-return claims automatically blocked.
              </p>
            </div>
          </Card>

          <Card withScrews={false} className="p-5 flex items-start gap-4">
            <div className="w-10 h-10 rounded-lg bg-[#e0e5ec] shadow-chassis-card flex items-center justify-center shrink-0 border border-white/80">
              <Activity className="w-5 h-5 text-[#ff4757]" />
            </div>
            <div>
              <h2 className="text-xs font-bold uppercase font-technical text-[#2d3436]">
                vectorbt Engine
              </h2>
              <p className="text-xs text-[#4a5568] mt-1 leading-normal">
                Independent Python microservice evaluating Sharpe, drawdown, and win rates.
              </p>
            </div>
          </Card>

          <Card withScrews={false} className="p-5 flex items-start gap-4">
            <div className="w-10 h-10 rounded-lg bg-[#e0e5ec] shadow-chassis-card flex items-center justify-center shrink-0 border border-white/80">
              <Layers className="w-5 h-5 text-[#ff4757]" />
            </div>
            <div>
              <h2 className="text-xs font-bold uppercase font-technical text-[#2d3436]">
                Versioned Licenses
              </h2>
              <p className="text-xs text-[#4a5568] mt-1 leading-normal">
                Immutable strategy versioning run directly against buyer broker credentials.
              </p>
            </div>
          </Card>
        </div>

      </main>

      {/* =========================================================================
          FOOTER: Stamped Chassis Metadata
          ========================================================================= */}
      <footer className="relative z-10 border-t border-[#babecc]/50 px-6 py-6 text-center text-xs font-technical text-[#718096]">
        <div className="max-w-6xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>ALGOADDA CHASSIS // SPEC 2026.0</span>
          <span>ESTABLISHED NATIVE STACK: SPRING BOOT 3 • FASTAPI • REACT 18</span>
        </div>
      </footer>
    </div>
  )
}

export default App
