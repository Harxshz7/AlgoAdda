import React from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { Button, LogoIcon } from '../ui'
import { Store, LayoutDashboard, LogIn, UserPlus, LogOut, ShoppingBag, Sparkles, Info } from 'lucide-react'

export const PublicLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path: string) => location.pathname === path || (path !== '/' && location.pathname.startsWith(path))

  return (
    <div className="min-h-screen bg-[#FDFCF8] text-[#2C2C24] flex flex-col selection:bg-[#5D7052]/20 selection:text-[#5D7052]">

      {/* Organic Frosted-Glass Pill Nav Header */}
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
            <Link to="/marketplace" className="flex items-center gap-3 group">
              <LogoIcon className="w-9 h-9 group-hover:scale-105 transition-all duration-300" />
              <span className="font-heading font-bold text-lg text-[#2C2C24] tracking-tight">
                AlgoAdda
              </span>
            </Link>

            {/* Navigation */}
            <nav className="flex items-center gap-2">
              <Link to="/store">
                <Button
                  variant={isActive('/store') ? 'primary' : 'ghost'}
                  size="sm"
                  className="gap-1.5"
                >
                  <Sparkles className="w-4 h-4 text-[#C18C5D]" />
                  <span>Store</span>
                </Button>
              </Link>

              <Link to="/marketplace">
                <Button
                  variant={isActive('/marketplace') ? 'primary' : 'ghost'}
                  size="sm"
                  className="gap-1.5"
                >
                  <Store className="w-4 h-4" />
                  <span>Marketplace</span>
                </Button>
              </Link>

              <Link to="/about">
                <Button
                  variant={isActive('/about') ? 'primary' : 'ghost'}
                  size="sm"
                  className="gap-1.5"
                >
                  <Info className="w-4 h-4" />
                  <span>About</span>
                </Button>
              </Link>

              {user?.role === 'SELLER' && (
                <Link to="/seller/dashboard">
                  <Button
                    variant={isActive('/seller') ? 'primary' : 'ghost'}
                    size="sm"
                    className="gap-1.5"
                  >
                    <LayoutDashboard className="w-4 h-4" />
                    <span>Seller Portal</span>
                  </Button>
                </Link>
              )}

              {user?.role === 'BUYER' && (
                <Link to="/buyer/dashboard">
                  <Button
                    variant={isActive('/buyer') ? 'primary' : 'ghost'}
                    size="sm"
                    className="gap-1.5"
                  >
                    <ShoppingBag className="w-4 h-4" />
                    <span>My Purchased Bots</span>
                  </Button>
                </Link>
              )}
            </nav>

            {/* User Auth Info / Actions */}
            <div className="flex items-center gap-2 sm:gap-3">
              {user ? (
                <>
                  <div className="hidden sm:flex flex-col items-end leading-tight">
                    <span className="text-sm font-semibold text-[#2C2C24] font-body">
                      {user.email.split('@')[0]}
                    </span>
                    <span className="text-xs text-[#78786C] font-body capitalize">
                      {user.role?.toLowerCase()}
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
                </>
              ) : (
                <>
                  <Link to="/login">
                    <Button variant="ghost" size="sm" className="gap-1.5">
                      <LogIn className="w-4 h-4" />
                      <span>Log In</span>
                    </Button>
                  </Link>

                  <Link to="/register">
                    <Button variant="primary" size="sm" className="gap-1.5">
                      <UserPlus className="w-4 h-4" />
                      <span>Register</span>
                    </Button>
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-10 mt-4">
        {children}
      </main>

      {/* Organic Footer */}
      <footer className="mt-auto border-t border-[#DED8CF]/50 py-5">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-2">
          <div className="flex items-center gap-3 text-xs font-body text-[#78786C]">
            <span>AlgoAdda · Transparent White-Box Strategy Marketplace</span>
            <span>·</span>
            <Link to="/about" className="hover:text-[#5D7052] underline font-semibold transition-colors">
              About &amp; Compliance
            </Link>
          </div>
          <span className="text-xs font-body text-[#78786C]">
            Verified Backtests &amp; Disclosed Strategy Logic
          </span>
        </div>
      </footer>
    </div>
  )
}
