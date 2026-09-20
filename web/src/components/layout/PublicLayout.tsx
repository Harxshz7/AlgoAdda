import React, { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useCart } from '../../context/CartContext'
import { Button, LogoIcon } from '../ui'
import { Store, LayoutDashboard, LogIn, UserPlus, LogOut, ShoppingBag, Sparkles, Info, ShoppingCart, ShieldAlert, Menu, X, Bookmark } from 'lucide-react'

export const PublicLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout } = useAuth()
  const { cartCount } = useCart()
  const location = useLocation()
  const navigate = useNavigate()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

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
            <Link to="/marketplace" className="flex items-center gap-3 group" onClick={() => setMobileMenuOpen(false)}>
              <LogoIcon className="w-9 h-9 group-hover:scale-105 transition-all duration-300" />
              <span className="font-heading font-bold text-lg text-[#2C2C24] tracking-tight">
                AlgoAdda
              </span>
            </Link>

            {/* Desktop Navigation */}
            <nav className="hidden md:flex items-center gap-2">
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
                <>
                  <Link to="/buyer/dashboard">
                    <Button
                      variant={isActive('/buyer/dashboard') ? 'primary' : 'ghost'}
                      size="sm"
                      className="gap-1.5"
                    >
                      <ShoppingBag className="w-4 h-4" />
                      <span>My Purchased Bots</span>
                    </Button>
                  </Link>

                  <Link to="/buyer/watchlist">
                    <Button
                      variant={isActive('/buyer/watchlist') ? 'primary' : 'ghost'}
                      size="sm"
                      className="gap-1.5"
                    >
                      <Bookmark className="w-4 h-4" />
                      <span>Watchlist</span>
                    </Button>
                  </Link>

                  <Link to="/cart">
                    <Button
                      variant={isActive('/cart') ? 'primary' : 'ghost'}
                      size="sm"
                      className="gap-1.5 relative"
                      title="Shopping Cart"
                    >
                      <ShoppingCart className="w-4 h-4" />
                      <span>Cart</span>
                      {cartCount > 0 && (
                        <span className="ml-1 bg-[#5D7052] text-white text-[10px] font-bold px-1.5 py-0.2 rounded-full">
                          {cartCount}
                        </span>
                      )}
                    </Button>
                  </Link>
                </>
              )}

              {user?.role === 'ADMIN' && (
                <Link to="/admin/reports">
                  <Button
                    variant={isActive('/admin') ? 'primary' : 'ghost'}
                    size="sm"
                    className="gap-1.5"
                  >
                    <ShieldAlert className="w-4 h-4 text-[#A85448]" />
                    <span>Admin Moderation</span>
                  </Button>
                </Link>
              )}
            </nav>

            {/* Desktop User Auth Info / Actions */}
            <div className="hidden md:flex items-center gap-2 sm:gap-3">
              {user ? (
                <>
                  <div className="flex flex-col items-end leading-tight">
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

            {/* Mobile Hamburger Toggle & Cart Icon */}
            <div className="flex md:hidden items-center gap-2">
              {user?.role === 'BUYER' && (
                <Link to="/cart" onClick={() => setMobileMenuOpen(false)}>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="p-2 relative"
                    title="Shopping Cart"
                  >
                    <ShoppingCart className="w-5 h-5" />
                    {cartCount > 0 && (
                      <span className="absolute -top-1 -right-1 bg-[#5D7052] text-white text-[10px] font-bold min-w-4 h-4 flex items-center justify-center px-1 rounded-full">
                        {cartCount}
                      </span>
                    )}
                  </Button>
                </Link>
              )}
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
              <Link to="/store" onClick={() => setMobileMenuOpen(false)}>
                <Button
                  variant={isActive('/store') ? 'primary' : 'ghost'}
                  size="sm"
                  className="w-full justify-start gap-2 h-11"
                >
                  <Sparkles className="w-4 h-4 text-[#C18C5D]" />
                  <span>Store</span>
                </Button>
              </Link>

              <Link to="/marketplace" onClick={() => setMobileMenuOpen(false)}>
                <Button
                  variant={isActive('/marketplace') ? 'primary' : 'ghost'}
                  size="sm"
                  className="w-full justify-start gap-2 h-11"
                >
                  <Store className="w-4 h-4" />
                  <span>Marketplace</span>
                </Button>
              </Link>

              <Link to="/about" onClick={() => setMobileMenuOpen(false)}>
                <Button
                  variant={isActive('/about') ? 'primary' : 'ghost'}
                  size="sm"
                  className="w-full justify-start gap-2 h-11"
                >
                  <Info className="w-4 h-4" />
                  <span>About</span>
                </Button>
              </Link>

              {user?.role === 'SELLER' && (
                <Link to="/seller/dashboard" onClick={() => setMobileMenuOpen(false)}>
                  <Button
                    variant={isActive('/seller') ? 'primary' : 'ghost'}
                    size="sm"
                    className="w-full justify-start gap-2 h-11"
                  >
                    <LayoutDashboard className="w-4 h-4" />
                    <span>Seller Portal</span>
                  </Button>
                </Link>
              )}

              {user?.role === 'BUYER' && (
                <>
                  <Link to="/buyer/dashboard" onClick={() => setMobileMenuOpen(false)}>
                    <Button
                      variant={isActive('/buyer/dashboard') ? 'primary' : 'ghost'}
                      size="sm"
                      className="w-full justify-start gap-2 h-11"
                    >
                      <ShoppingBag className="w-4 h-4" />
                      <span>My Purchased Bots</span>
                    </Button>
                  </Link>

                  <Link to="/buyer/watchlist" onClick={() => setMobileMenuOpen(false)}>
                    <Button
                      variant={isActive('/buyer/watchlist') ? 'primary' : 'ghost'}
                      size="sm"
                      className="w-full justify-start gap-2 h-11"
                    >
                      <Bookmark className="w-4 h-4" />
                      <span>Watchlist</span>
                    </Button>
                  </Link>
                </>
              )}

              {user?.role === 'ADMIN' && (
                <Link to="/admin/reports" onClick={() => setMobileMenuOpen(false)}>
                  <Button
                    variant={isActive('/admin') ? 'primary' : 'ghost'}
                    size="sm"
                    className="w-full justify-start gap-2 h-11"
                  >
                    <ShieldAlert className="w-4 h-4 text-[#A85448]" />
                    <span>Admin Moderation</span>
                  </Button>
                </Link>
              )}

              <hr className="border-[#DED8CF]/40 my-1" />

              {user ? (
                <div className="flex items-center justify-between pt-1 px-2">
                  <div className="flex flex-col">
                    <span className="text-sm font-semibold text-[#2C2C24]">
                      {user.email}
                    </span>
                    <span className="text-xs text-[#78786C] capitalize">
                      {user.role?.toLowerCase()}
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
              ) : (
                <div className="flex flex-col gap-2 pt-1">
                  <Link to="/login" onClick={() => setMobileMenuOpen(false)}>
                    <Button variant="ghost" size="sm" className="w-full justify-center gap-1.5 h-11">
                      <LogIn className="w-4 h-4" />
                      <span>Log In</span>
                    </Button>
                  </Link>

                  <Link to="/register" onClick={() => setMobileMenuOpen(false)}>
                    <Button variant="primary" size="sm" className="w-full justify-center gap-1.5 h-11">
                      <UserPlus className="w-4 h-4" />
                      <span>Register</span>
                    </Button>
                  </Link>
                </div>
              )}
            </div>
          )}
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-10 mt-4">
        {children}
      </main>

      {/* Organic Footer */}
      <footer className="mt-auto border-t border-[#DED8CF]/50 py-6">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex flex-wrap items-center justify-center md:justify-start gap-x-3 gap-y-1.5 text-xs font-body text-[#78786C]">
            <span>AlgoAdda · White-Box Strategy Marketplace</span>
            <span>·</span>
            <Link to="/about" className="hover:text-[#5D7052] underline font-semibold transition-colors">
              About
            </Link>
            <span>·</span>
            <Link to="/terms" className="hover:text-[#5D7052] underline font-semibold transition-colors">
              Terms of Service
            </Link>
            <span>·</span>
            <Link to="/risk-disclosure" className="hover:text-[#5D7052] underline font-semibold transition-colors">
              Risk Disclosure
            </Link>
            <span>·</span>
            <Link to="/privacy" className="hover:text-[#5D7052] underline font-semibold transition-colors">
              Privacy Policy
            </Link>
          </div>
          <span className="text-xs font-body text-[#78786C] text-center md:text-right">
            Disclosed Strategy Logic &amp; Verified VectorBT Backtests
          </span>
        </div>
      </footer>
    </div>
  )
}
