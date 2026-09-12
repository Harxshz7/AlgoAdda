import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../../lib/api'
import { Button, Card, Input, Textarea, LedIndicator } from '../../components/ui'
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
      setError('Please provide your seller / studio display name.')
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
    <div className="min-h-screen bg-[#e0e5ec] flex flex-col justify-center items-center p-4 selection:bg-[#ff4757]/20 selection:text-[#ff4757]">
      <div className="w-full max-w-xl flex flex-col gap-6">
        {/* Header */}
        <div className="flex flex-col items-center text-center gap-2">
          <div className="w-14 h-14 rounded-2xl bg-[#e0e5ec] shadow-chassis-floating flex items-center justify-center border border-white/80">
            <UserCheck className="w-8 h-8 text-[#ff4757]" />
          </div>
          <h1 className="text-2xl font-black font-technical tracking-wider text-[#2d3436]">
            SELLER PROFILE // SETUP
          </h1>
          <p className="text-xs font-technical text-[#718096] uppercase tracking-wider">
            ESTABLISH YOUR QUANT TRADING DESK IDENTIFIER
          </p>
        </div>

        <Card className="p-8">
          <div className="flex items-center justify-between pb-4 mb-6 border-b border-black/5">
            <span className="text-xs font-bold font-technical text-[#4a5568] uppercase tracking-wider">
              QUANT STUDIO METADATA
            </span>
            <LedIndicator status="amber" label="KYC STUBBED" pulse={false} />
          </div>

          {error && (
            <div className="mb-6 p-4 rounded-xl bg-[#ff4757]/10 border border-[#ff4757]/30 shadow-chassis-recessed flex items-start gap-3">
              <AlertTriangle className="w-5 h-5 text-[#ff4757] shrink-0 mt-0.5" />
              <div className="flex flex-col">
                <span className="text-xs font-bold font-technical text-[#ff4757] uppercase">
                  ONBOARDING ERROR
                </span>
                <span className="text-xs font-technical text-[#ff4757]/90 mt-0.5">
                  {error}
                </span>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            <Input
              label="STUDIO / TRADER DISPLAY NAME"
              placeholder="e.g. Apex Quant Labs, Momentum Capital"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              prefixIcon={<User className="w-4 h-4" />}
              helperText="This public name will appear on all your strategy listings."
              required
            />

            <Textarea
              label="QUANT PROFILE BIO & PHILOSOPHY"
              placeholder="Describe your quantitative methodology, asset classes, and risk management framework..."
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              helperText="Optional trader bio shown to prospective bot subscribers."
              rows={4}
            />

            <div className="p-4 rounded-xl bg-[#d9e0ea] shadow-chassis-recessed flex items-start gap-3 border border-white/50">
              <ShieldCheck className="w-5 h-5 text-[#2ed573] shrink-0 mt-0.5" />
              <div className="flex flex-col gap-1">
                <span className="text-xs font-bold font-technical text-[#2d3436]">
                  WHITE-BOX COMPLIANCE READY
                </span>
                <span className="text-[11px] font-technical text-[#4a5568] leading-normal">
                  AlgoAdda enforces algorithmic logic disclosure for full transparency. No "black-box" or guaranteed return promises permitted.
                </span>
              </div>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={isSubmitting}
              className="w-full mt-2 gap-2"
            >
              <span>{isSubmitting ? 'CONFIGURING DESK...' : 'COMPLETE ONBOARDING & ENTER DASHBOARD'}</span>
              <ArrowRight className="w-4 h-4" />
            </Button>
          </form>
        </Card>
      </div>
    </div>
  )
}
