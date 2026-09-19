import React, { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { Button, LogoIcon } from '../ui'
import { LayoutDashboard, PlusCircle, LogOut, BarChart2, Menu, X } from 'lucide-react'

export const SellerLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path: string) => location.pathname === path

  return (
    <div className="min-h-screen bg-[#FDFCF8] text-[#2C2C24] flex flex-col selection:bg-[#5D7052]/20 selection:text-[#5D7052]">

      {/* Organic Frosted-Glass Pill Nav */}
      <header className="sticky top-4 z-40 mx-4 sm:mx-6 lg:mx-8">
        <div className="max-w-7xl mx-auto">
          <div
            className="flex items-center justify-between h-16 px-4 sm:px-6
              bg-white/70 backdrop-blur-md
              border border-[#DED8CF]/50
              rounded-full
              shadow-[0_4px_20px_-2px_rgba(93,112,82,0.12)]"
          >
            {/* Logo */}
            <Link to="/seller/dashboard" className="flex items-center gap-3 group" onClick={() => setMobileMenuOpen(false)}>
              <LogoIcon className="w-9 h-9 group-hover:scale-105 transition-all duration-300" />
              <span className="font-heading font-bold text-lg text-[#2C2C24] tracking-tight">
                AlgoAdda
              </span>
            </Link>

            {/* Desktop Navigation */}
            <nav className="hidden md:flex items-center gap-2">
              <Link to="/seller/dashboard">
                <Button
                  variant={isActive('/seller/dashboard') ? 'primary' : 'ghost'}
                  size="sm"
                  className="gap-1.5"
                >
                  <LayoutDashboard className="w-4 h-4" />
                  <span>Dashboard</span>
                </Button>
              </Link>

              <Link to="/seller/analytics">
                <Button
                  variant={isActive('/seller/analytics') ? 'primary' : 'ghost'}
                  size="sm"
                  className="gap-1.5"
                >
                  <BarChart2 className="w-4 h-4" />
                  <span>Analytics</span>
                </Button>
              </Link>

              <Link to="/seller/bots/new">
                <Button
                  variant={isActive('/seller/bots/new') ? 'primary' : 'secondary'}
                  size="sm"
                  className="gap-1.5"
                >
                  <PlusCircle className="w-4 h-4" />
                  <span>Upload Bot</span>
                </Button>
              </Link>
            </nav>

            {/* Desktop User info & logout */}
            <div className="hidden md:flex items-center gap-3">
              <div className="flex flex-col items-end leading-tight">
                <span className="text-sm font-semibold text-[#2C2C24] font-body">
                  {user?.email.split('@')[0]}
                </span>
                <span className="text-xs text-[#78786C] font-body capitalize">
                  {user?.role?.toLowerCase()}
                </span>
              </div>

              <Button
                variant="ghost"
                size="sm"
                onClick={handleLogout}
                className="text-[#78786C] hover:text-[#A85448]"
                title="Sign out"
              >
                <LogOut className="w-4 h-4" />
              </Button>
            </div>

            {/* Mobile Menu Toggle Button */}
            <div className="flex md:hidden items-center">
              <button
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className="p-2 rounded-lg text-[#2C2C24] hover:bg-black/5 min-h-[44px] min-w-[44px] flex items-center justify-center"
                aria-label="Toggle Navigation Menu"
              >
                {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
              </button>
            </div>
          </div>

          {/* Mobile Dropdown Menu */}
          {mobileMenuOpen && (
            <div className="md:hidden mt-2 p-4 bg-white/95 backdrop-blur-md border border-[#DED8CF]/50 rounded-2xl shadow-lg flex flex-col gap-2">
              <Link to="/seller/dashboard" onClick={() => setMobileMenuOpen(false)}>
                <Button
                  variant={isActive('/seller/dashboard') ? 'primary' : 'ghost'}
                  size="sm"
                  className="w-full justify-start gap-2 h-11"
                >
                  <LayoutDashboard className="w-4 h-4" />
                  <span>Dashboard</span>
                </Button>
              </Link>

              <Link to="/seller/analytics" onClick={() => setMobileMenuOpen(false)}>
                <Button
                  variant={isActive('/seller/analytics') ? 'primary' : 'ghost'}
                  size="sm"
                  className="w-full justify-start gap-2 h-11"
                >
                  <BarChart2 className="w-4 h-4" />
                  <span>Analytics</span>
                </Button>
              </Link>

              <Link to="/seller/bots/new" onClick={() => setMobileMenuOpen(false)}>
                <Button
                  variant={isActive('/seller/bots/new') ? 'primary' : 'secondary'}
                  size="sm"
                  className="w-full justify-start gap-2 h-11"
                >
                  <PlusCircle className="w-4 h-4" />
                  <span>Upload Bot</span>
                </Button>
              </Link>

              <hr className="border-[#DED8CF]/40 my-1" />

              <div className="flex items-center justify-between pt-1 px-2">
                <div className="flex flex-col">
                  <span className="text-sm font-semibold text-[#2C2C24]">
                    {user?.email}
                  </span>
                  <span className="text-xs text-[#78786C] capitalize">
                    {user?.role?.toLowerCase()}
                  </span>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => {
                    setMobileMenuOpen(false)
                    handleLogout()
                  }}
                  className="text-[#78786C] hover:text-[#A85448] gap-1.5 h-11"
                >
                  <LogOut className="w-4 h-4" />
                  <span>Sign out</span>
                </Button>
              </div>
            </div>
          )}
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-10 mt-4">
        {children}
      </main>

      {/* Organic Footer */}
      <footer className="mt-auto border-t border-[#DED8CF]/50 py-5">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span className="text-xs font-body text-[#78786C]">
            AlgoAdda · White-box compliance enabled
          </span>
          <span className="text-xs font-body text-[#78786C]">
            Strategy files secured in AWS S3
          </span>
        </div>
      </footer>
    </div>
  )
}
