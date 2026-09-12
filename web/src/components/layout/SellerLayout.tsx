import React from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { Button, LedIndicator } from '../ui'
import { LayoutDashboard, PlusCircle, LogOut, Terminal, Shield } from 'lucide-react'

export const SellerLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path: string) => location.pathname === path

  return (
    <div className="min-h-screen bg-[#e0e5ec] text-[#2d3436] flex flex-col selection:bg-[#ff4757]/20 selection:text-[#ff4757]">
      {/* Heavy Industrial Chassis Header */}
      <header className="sticky top-0 z-40 bg-[#e0e5ec]/95 backdrop-blur-md border-b border-white/60 shadow-chassis-card">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-20">
            {/* Station Callout & Logo */}
            <div className="flex items-center gap-6">
              <Link to="/seller/dashboard" className="flex items-center gap-3 group">
                <div className="w-11 h-11 rounded-xl bg-[#e0e5ec] shadow-chassis-floating flex items-center justify-center border border-white/80 group-hover:scale-105 transition-mechanical">
                  <Terminal className="w-6 h-6 text-[#ff4757]" />
                </div>
                <div className="flex flex-col">
                  <div className="flex items-center gap-2">
                    <span className="text-lg font-black font-technical tracking-wider text-[#2d3436]">
                      ALGO<span className="text-[#ff4757]">ADDA</span>
                    </span>
                    <span className="text-[9px] font-technical px-1.5 py-0.5 rounded bg-[#2d3436] text-[#2ed573] font-bold">
                      v0.2.0
                    </span>
                  </div>
                  <span className="text-[10px] font-technical text-[#718096] uppercase tracking-wider">
                    SELLER TERMINAL // STATION_01
                  </span>
                </div>
              </Link>

              {/* Hardware Ventilation Louvers */}
              <div className="hidden lg:flex items-center gap-1.5 px-3 py-2 rounded-lg bg-[#d9e0ea] shadow-chassis-recessed">
                {[...Array(6)].map((_, i) => (
                  <div key={i} className="w-1 h-5 rounded-full bg-[#babecc]/60 shadow-[inset_1px_1px_1px_rgba(0,0,0,0.2)]" />
                ))}
              </div>
            </div>

            {/* Navigation Tabs */}
            <nav className="flex items-center gap-3">
              <Link to="/seller/dashboard">
                <Button
                  variant={isActive('/seller/dashboard') ? 'primary' : 'ghost'}
                  size="sm"
                  className="gap-2"
                >
                  <LayoutDashboard className="w-4 h-4" />
                  <span>DASHBOARD</span>
                </Button>
              </Link>

              <Link to="/seller/bots/new">
                <Button
                  variant={isActive('/seller/bots/new') ? 'primary' : 'secondary'}
                  size="sm"
                  className="gap-2"
                >
                  <PlusCircle className="w-4 h-4" />
                  <span>UPLOAD BOT</span>
                </Button>
              </Link>
            </nav>

            {/* User Terminal Status & Logout */}
            <div className="flex items-center gap-4">
              <div className="hidden sm:flex flex-col items-end leading-tight">
                <div className="flex items-center gap-1.5">
                  <Shield className="w-3.5 h-3.5 text-[#2ed573]" />
                  <span className="text-xs font-bold font-technical text-[#2d3436]">
                    {user?.email.split('@')[0]}
                  </span>
                </div>
                <span className="text-[9px] font-technical text-[#718096] uppercase">
                  ROLE: {user?.role}
                </span>
              </div>

              <div className="hidden md:block">
                <LedIndicator status="green" label="ONLINE" pulse />
              </div>

              <Button
                variant="ghost"
                size="sm"
                onClick={handleLogout}
                className="text-[#ff4757] hover:bg-[#ff4757]/10"
                title="Disconnect from station"
              >
                <LogOut className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Pane */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>

      {/* Industrial Chassis Sub-Footer */}
      <footer className="mt-auto border-t border-white/60 bg-[#d9e0ea]/50 py-4">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span className="text-[10px] font-technical text-[#718096]">
            ALGOADDA INDUSTRIAL ENGINE • WHITE-BOX COMPLIANCE PROTOCOL ENFORCED
          </span>
          <div className="flex items-center gap-4">
            <span className="text-[10px] font-technical text-[#718096]">
              AWS S3 VAULT: CONNECTED
            </span>
            <span className="w-1.5 h-1.5 rounded-full bg-[#2ed573] glow-led-green" />
          </div>
        </div>
      </footer>
    </div>
  )
}
