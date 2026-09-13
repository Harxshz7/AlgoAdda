import React, { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { Button, Card, Input } from '../../components/ui'
import { Mail, Lock, ArrowRight, AlertTriangle } from 'lucide-react'

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!email.trim() || !password.trim()) {
      setError('Please enter both email and password.')
      return
    }

    setIsSubmitting(true)
    try {
      const loggedUser = await login({ email: email.trim(), password })
      const fromPath = location.state?.from?.pathname
      if (fromPath && fromPath !== '/seller/dashboard' && fromPath !== '/buyer/dashboard') {
        navigate(fromPath, { replace: true })
      } else if (loggedUser.role === 'BUYER') {
        navigate('/buyer/dashboard', { replace: true })
      } else {
        navigate('/seller/dashboard', { replace: true })
      }
    } catch (err: any) {
      setError(err.message || 'Invalid credentials. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#FDFCF8] flex flex-col justify-center items-center p-4 relative overflow-hidden">
      {/* Ambient blob backgrounds */}
      <div
        className="absolute -top-32 -left-32 w-80 h-80 bg-[#5D7052]/8 blur-3xl pointer-events-none"
        style={{ borderRadius: '60% 40% 30% 70% / 60% 30% 70% 40%' }}
      />
      <div
        className="absolute -bottom-24 -right-24 w-72 h-72 bg-[#C18C5D]/8 blur-3xl pointer-events-none"
        style={{ borderRadius: '40% 60% 70% 30% / 40% 70% 30% 60%' }}
      />

      <div className="w-full max-w-md flex flex-col gap-6 relative z-10">
        {/* Branding */}
        <div className="flex flex-col items-center text-center gap-2">
          <div className="w-12 h-12 rounded-full bg-[#5D7052] flex items-center justify-center mb-1">
            <Mail className="w-5 h-5 text-white" />
          </div>
          <h1 className="text-3xl font-heading font-bold text-[#2C2C24]">
            Welcome back
          </h1>
          <p className="text-sm font-body text-[#78786C]">
            Sign in to your AlgoAdda seller account
          </p>
        </div>

        {/* Login Card */}
        <Card className="p-8">
          {error && (
            <div className="mb-6 p-4 rounded-2xl bg-[#A85448]/8 border border-[#A85448]/25 flex items-start gap-3">
              <AlertTriangle className="w-4 h-4 text-[#A85448] shrink-0 mt-0.5" />
              <span className="text-sm font-body text-[#A85448]">{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
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
              label="Password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              prefixIcon={<Lock className="w-4 h-4 text-[#78786C]" />}
              autoComplete="current-password"
              required
            />

            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={isSubmitting}
              className="w-full mt-2"
              icon={<ArrowRight className="w-4 h-4" />}
              iconPosition="right"
            >
              {isSubmitting ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>

          <div className="mt-6 pt-6 border-t border-[#DED8CF]/50 flex items-center justify-between text-sm font-body text-[#78786C]">
            <span>New here?</span>
            <Link
              to="/register"
              className="text-[#5D7052] hover:text-[#4e6045] font-semibold flex items-center gap-1 transition-colors"
            >
              <span>Create an account</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </Card>

        <p className="text-center text-xs font-body text-[#78786C]">
          White-box compliance · All strategies disclosed at upload
        </p>
      </div>
    </div>
  )
}
