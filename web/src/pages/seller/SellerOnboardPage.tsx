import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../../lib/api'
import { Button, Card, Input, Textarea } from '../../components/ui'
import { UserCheck, ArrowRight, AlertTriangle, ShieldCheck, User } from 'lucide-react'

export const SellerOnboardPage: React.FC = () => {
  const [displayName, setDisplayName] = useState('')
  const [bio, setBio] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!displayName.trim()) {
      setError('Please provide your seller display name.')
      return
    }

    setIsSubmitting(true)
    try {
      await api.onboardSeller({
        displayName: displayName.trim(),
        bio: bio.trim(),
      })
      navigate('/seller/dashboard', { replace: true })
    } catch (err: any) {
      setError(err.message || 'Onboarding failed. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#FDFCF8] flex flex-col justify-center items-center p-4 relative overflow-hidden">
      {/* Ambient blobs */}
      <div
        className="absolute -top-32 -left-20 w-72 h-72 bg-[#5D7052]/8 blur-3xl pointer-events-none"
        style={{ borderRadius: '60% 40% 30% 70% / 60% 30% 70% 40%' }}
      />
      <div
        className="absolute -bottom-20 -right-20 w-64 h-64 bg-[#C18C5D]/8 blur-3xl pointer-events-none"
        style={{ borderRadius: '40% 60% 70% 30% / 60% 40% 30% 70%' }}
      />

      <div className="w-full max-w-xl flex flex-col gap-6 relative z-10">
        {/* Header */}
        <div className="flex flex-col items-center text-center gap-2">
          <div className="w-12 h-12 rounded-full bg-[#5D7052] flex items-center justify-center mb-1">
            <UserCheck className="w-5 h-5 text-white" />
          </div>
          <h1 className="text-3xl font-heading font-bold text-[#2C2C24]">
            Set up your seller profile
          </h1>
          <p className="text-sm font-body text-[#78786C]">
            Your public identity on AlgoAdda
          </p>
        </div>

        <Card className="p-6 sm:p-8">
          {error && (
            <div className="mb-6 p-4 rounded-2xl bg-[#A85448]/8 border border-[#A85448]/25 flex items-start gap-3">
              <AlertTriangle className="w-4 h-4 text-[#A85448] shrink-0 mt-0.5" />
              <span className="text-sm font-body text-[#A85448]">{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            <Input
              label="Display name"
              placeholder="e.g. Apex Quant Labs, Momentum Capital"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              prefixIcon={<User className="w-4 h-4 text-[#78786C]" />}
              helperText="This public name appears on all your strategy listings."
              required
            />

            <Textarea
              label="Bio & philosophy"
              placeholder="Describe your quantitative methodology, asset classes, and risk management approach…"
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              helperText="Optional — shown to prospective buyers."
              rows={4}
            />

            {/* Compliance info block */}
            <div className="p-4 rounded-2xl bg-[#5D7052]/8 border border-[#5D7052]/20 flex items-start gap-3">
              <ShieldCheck className="w-5 h-5 text-[#5D7052] shrink-0 mt-0.5" />
              <div className="flex flex-col gap-1">
                <span className="text-sm font-semibold font-body text-[#2C2C24]">
                  White-box compliance required
                </span>
                <span className="text-sm font-body text-[#78786C] leading-normal">
                  AlgoAdda enforces strategy logic disclosure for full transparency. No black-box or guaranteed-return strategies permitted.
                </span>
              </div>
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
              {isSubmitting ? 'Setting up your profile…' : 'Complete setup & go to dashboard'}
            </Button>
          </form>
        </Card>
      </div>
    </div>
  )
}
