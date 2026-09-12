import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { Button, Card, Input, LedIndicator } from '../../components/ui'
import { Mail, Lock, Terminal, ArrowRight, AlertTriangle, ShieldCheck } from 'lucide-react'

export const RegisterPage: React.FC = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const { register } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!email.trim() || !password.trim()) {
      setError('Please fill in all required fields.')
      return
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters.')
      return
    }

    setIsSubmitting(true)
    try {
      await register({
        email: email.trim(),
        password,
        role: 'SELLER',
      })
      navigate('/seller/onboard', { replace: true })
    } catch (err: any) {
      setError(err.message || 'Registration failed. Please check your details and try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#e0e5ec] flex flex-col justify-center items-center p-4 selection:bg-[#ff4757]/20 selection:text-[#ff4757]">
      <div className="w-full max-w-md flex flex-col gap-6">
        {/* Header */}
        <div className="flex flex-col items-center text-center gap-2">
          <div className="w-14 h-14 rounded-2xl bg-[#e0e5ec] shadow-chassis-floating flex items-center justify-center border border-white/80">
            <Terminal className="w-8 h-8 text-[#ff4757]" />
          </div>
          <h1 className="text-2xl font-black font-technical tracking-wider text-[#2d3436]">
            ALGO<span className="text-[#ff4757]">ADDA</span> // ONBOARDING
          </h1>
          <p className="text-xs font-technical text-[#718096] uppercase tracking-wider">
            REGISTER AS A QUANT SELLER
          </p>
        </div>

        {/* Registration Card */}
        <Card className="p-8">
          <div className="flex items-center justify-between pb-4 mb-6 border-b border-black/5">
            <span className="text-xs font-bold font-technical text-[#4a5568] uppercase tracking-wider">
              NEW OPERATOR ENROLLMENT
            </span>
            <LedIndicator status="amber" label="SELLER TIER" pulse={false} />
          </div>

          {error && (
            <div className="mb-6 p-4 rounded-xl bg-[#ff4757]/10 border border-[#ff4757]/30 shadow-chassis-recessed flex items-start gap-3">
              <AlertTriangle className="w-5 h-5 text-[#ff4757] shrink-0 mt-0.5" />
              <div className="flex flex-col">
                <span className="text-xs font-bold font-technical text-[#ff4757] uppercase">
                  REGISTRATION REJECTED
                </span>
                <span className="text-xs font-technical text-[#ff4757]/90 mt-0.5">
                  {error}
                </span>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Input
              label="OPERATOR EMAIL"
              type="email"
              placeholder="trader@algoadda.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              prefixIcon={<Mail className="w-4 h-4" />}
              autoComplete="email"
              required
            />

            <Input
              label="PASSWORD (MIN 6 CHARS)"
              type="password"
              placeholder="••••••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              prefixIcon={<Lock className="w-4 h-4" />}
              autoComplete="new-password"
              required
            />

            <Input
              label="CONFIRM PASSWORD"
              type="password"
              placeholder="••••••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              prefixIcon={<Lock className="w-4 h-4" />}
              autoComplete="new-password"
              required
            />

            <div className="p-3 rounded-lg bg-[#d9e0ea] shadow-chassis-recessed flex items-center gap-2.5">
              <ShieldCheck className="w-4 h-4 text-[#2ed573] shrink-0" />
              <span className="text-[11px] font-technical text-[#4a5568]">
                Enrolls in SELLER tier with full bot upload & backtest access.
              </span>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={isSubmitting}
              className="w-full mt-2 gap-2"
            >
              <span>{isSubmitting ? 'ENROLLING OPERATOR...' : 'INITIALIZE ACCOUNT'}</span>
              <ArrowRight className="w-4 h-4" />
            </Button>
          </form>

          <div className="mt-6 pt-6 border-t border-black/5 flex items-center justify-between text-xs font-technical text-[#718096]">
            <span>ALREADY REGISTERED?</span>
            <Link
              to="/login"
              className="text-[#ff4757] hover:underline font-bold uppercase tracking-wider flex items-center gap-1"
            >
              <span>SIGN IN</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </Card>
      </div>
    </div>
  )
}
