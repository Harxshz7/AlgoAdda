import React, { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { Button, Card, Input, LedIndicator } from '../../components/ui'
import { Mail, Lock, Terminal, ArrowRight, AlertTriangle } from 'lucide-react'

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const from = location.state?.from?.pathname || '/seller/dashboard'

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!email.trim() || !password.trim()) {
      setError('Please enter both email and password.')
      return
    }

    setIsSubmitting(true)
    try {
      await login({ email: email.trim(), password })
      navigate(from, { replace: true })
    } catch (err: any) {
      setError(err.message || 'Invalid credentials. Please verify and try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#e0e5ec] flex flex-col justify-center items-center p-4 selection:bg-[#ff4757]/20 selection:text-[#ff4757]">
      <div className="w-full max-w-md flex flex-col gap-6">
        {/* Terminal Header Branding */}
        <div className="flex flex-col items-center text-center gap-2">
          <div className="w-14 h-14 rounded-2xl bg-[#e0e5ec] shadow-chassis-floating flex items-center justify-center border border-white/80">
            <Terminal className="w-8 h-8 text-[#ff4757]" />
          </div>
          <h1 className="text-2xl font-black font-technical tracking-wider text-[#2d3436]">
            ALGO<span className="text-[#ff4757]">ADDA</span> // ACCESS
          </h1>
          <p className="text-xs font-technical text-[#718096] uppercase tracking-wider">
            SELLER & QUANT TRADING TERMINAL
          </p>
        </div>

        {/* Login Chassis Card */}
        <Card className="p-8">
          <div className="flex items-center justify-between pb-4 mb-6 border-b border-black/5">
            <span className="text-xs font-bold font-technical text-[#4a5568] uppercase tracking-wider">
              OPERATOR AUTHENTICATION
            </span>
            <LedIndicator status="green" label="READY" pulse={false} />
          </div>

          {error && (
            <div className="mb-6 p-4 rounded-xl bg-[#ff4757]/10 border border-[#ff4757]/30 shadow-chassis-recessed flex items-start gap-3">
              <AlertTriangle className="w-5 h-5 text-[#ff4757] shrink-0 mt-0.5" />
              <div className="flex flex-col">
                <span className="text-xs font-bold font-technical text-[#ff4757] uppercase">
                  AUTHENTICATION REJECTED
                </span>
                <span className="text-xs font-technical text-[#ff4757]/90 mt-0.5">
                  {error}
                </span>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            <Input
              label="OPERATOR EMAIL"
              type="email"
              placeholder="quant@algoadda.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              prefixIcon={<Mail className="w-4 h-4" />}
              autoComplete="email"
              required
            />

            <Input
              label="SECURITY KEY / PASSWORD"
              type="password"
              placeholder="••••••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              prefixIcon={<Lock className="w-4 h-4" />}
              autoComplete="current-password"
              required
            />

            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={isSubmitting}
              className="w-full mt-2 gap-2"
            >
              <span>{isSubmitting ? 'VERIFYING CREDENTIALS...' : 'CONNECT TO STATION'}</span>
              <ArrowRight className="w-4 h-4" />
            </Button>
          </form>

          <div className="mt-6 pt-6 border-t border-black/5 flex items-center justify-between text-xs font-technical text-[#718096]">
            <span>NEW SELLER?</span>
            <Link
              to="/register"
              className="text-[#ff4757] hover:underline font-bold uppercase tracking-wider flex items-center gap-1"
            >
              <span>CREATE ACCOUNT</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </Card>

        <div className="flex items-center justify-center gap-2 text-[10px] font-technical text-[#718096]">
          <span>SEBI WHITE-BOX COMPLIANCE ENABLED</span>
          <span className="w-1.5 h-1.5 rounded-full bg-[#2ed573] glow-led-green" />
        </div>
      </div>
    </div>
  )
}
