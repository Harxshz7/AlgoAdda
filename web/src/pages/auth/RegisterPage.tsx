import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { Button, Card, Input } from '../../components/ui'
import { Mail, Lock, ArrowRight, AlertTriangle, ShieldCheck } from 'lucide-react'

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
      setError(err.message || 'Registration failed. Please check your details.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#FDFCF8] flex flex-col justify-center items-center p-4 relative overflow-hidden">
      {/* Ambient blob backgrounds */}
      <div
        className="absolute -top-32 -right-32 w-80 h-80 bg-[#5D7052]/8 blur-3xl pointer-events-none"
        style={{ borderRadius: '40% 60% 70% 30% / 60% 40% 30% 70%' }}
      />
      <div
        className="absolute -bottom-24 -left-24 w-72 h-72 bg-[#C18C5D]/8 blur-3xl pointer-events-none"
        style={{ borderRadius: '60% 40% 30% 70% / 40% 60% 70% 30%' }}
      />

      <div className="w-full max-w-md flex flex-col gap-6 relative z-10">
        {/* Branding */}
        <div className="flex flex-col items-center text-center gap-2">
          <div className="w-12 h-12 rounded-full bg-[#5D7052] flex items-center justify-center mb-1">
            <ShieldCheck className="w-5 h-5 text-white" />
          </div>
          <h1 className="text-3xl font-heading font-bold text-[#2C2C24]">
            Create your account
          </h1>
          <p className="text-sm font-body text-[#78786C]">
            Join AlgoAdda as a strategy seller
          </p>
        </div>

        {/* Registration Card */}
        <Card className="p-8">
          {error && (
            <div className="mb-6 p-4 rounded-2xl bg-[#A85448]/8 border border-[#A85448]/25 flex items-start gap-3">
              <AlertTriangle className="w-4 h-4 text-[#A85448] shrink-0 mt-0.5" />
              <span className="text-sm font-body text-[#A85448]">{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Input
              label="Email address"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              prefixIcon={<Mail className="w-4 h-4 text-[#78786C]" />}
              autoComplete="email"
              required
            />

            <Input
              label="Password (min. 6 characters)"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              prefixIcon={<Lock className="w-4 h-4 text-[#78786C]" />}
              autoComplete="new-password"
              required
            />

            <Input
              label="Confirm password"
              type="password"
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              prefixIcon={<Lock className="w-4 h-4 text-[#78786C]" />}
              autoComplete="new-password"
              required
            />

            <div className="p-3 rounded-2xl bg-[#5D7052]/8 border border-[#5D7052]/20 flex items-center gap-2.5">
              <ShieldCheck className="w-4 h-4 text-[#5D7052] shrink-0" />
              <span className="text-sm font-body text-[#4A4A40]">
                Registers you as a seller with full bot upload &amp; backtest access.
              </span>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={isSubmitting}
              className="w-full mt-2"
              icon={<ArrowRight className="w-4 h-4" />}
              iconPosition="right"
            >
              {isSubmitting ? 'Creating account…' : 'Create account'}
            </Button>
          </form>

          <div className="mt-6 pt-6 border-t border-[#DED8CF]/50 flex items-center justify-between text-sm font-body text-[#78786C]">
            <span>Already have an account?</span>
            <Link
              to="/login"
              className="text-[#5D7052] hover:text-[#4e6045] font-semibold flex items-center gap-1 transition-colors"
            >
              <span>Sign in</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </Card>
      </div>
    </div>
  )
}


