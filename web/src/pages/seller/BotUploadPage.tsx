import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../../lib/api'
import { Button, Card, Input, Select, Textarea, FileDropzone, LedIndicator } from '../../components/ui'
import { UploadCloud, ArrowRight, AlertTriangle, ShieldAlert, CheckCircle2 } from 'lucide-react'

export const BotUploadPage: React.FC = () => {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [strategyType, setStrategyType] = useState('MOMENTUM')
  const [disclosedLogic, setDisclosedLogic] = useState('')
  const [symbol, setSymbol] = useState('^NSEI')
  const [fastPeriod, setFastPeriod] = useState('20')
  const [slowPeriod, setSlowPeriod] = useState('50')
  const [file, setFile] = useState<File | null>(null)

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [generalError, setGeneralError] = useState<string | null>(null)

  const navigate = useNavigate()

  const strategyOptions = [
    { value: 'MOMENTUM', label: 'MOMENTUM (Cross-sectional & Time Series)' },
    { value: 'TREND_FOLLOWING', label: 'TREND FOLLOWING (Breakout & Moving Average)' },
    { value: 'MEAN_REVERSION', label: 'MEAN REVERSION (Statistical Arbitrage & Pairs)' },
    { value: 'SCALPING', label: 'SCALPING / HIGH FREQUENCY MICROSTRUCTURE' },
    { value: 'MULTI_FACTOR', label: 'MULTI-FACTOR QUANT MODEL' },
  ]

  const validateForm = () => {
    const errs: Record<string, string> = {}

    if (!name.trim()) {
      errs.name = 'Bot identifier name is required.'
    }

    if (!disclosedLogic.trim()) {
      errs.disclosedLogic =
        "Disclosed logic is required for all listings under AlgoAdda's White Box compliance model."
    }

    if (!file) {
      errs.file = 'Please select a strategy code or configuration file.'
    }

    setErrors(errs)
    return Object.keys(errs).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setGeneralError(null)

    if (!validateForm()) {
      return
    }

    setIsSubmitting(true)
    try {
      const formData = new FormData()
      formData.append('name', name.trim())
      formData.append('description', description.trim())
      formData.append('strategyType', strategyType)
      formData.append('disclosedLogic', disclosedLogic.trim())

      const configObj = {
        symbol: symbol.trim() || '^NSEI',
        timeframe: '1d',
        strategy_name: strategyType,
        parameters: {
          fast_period: parseInt(fastPeriod) || 20,
          slow_period: parseInt(slowPeriod) || 50,
        },
      }
      formData.append('strategyConfig', JSON.stringify(configObj))

      if (file) {
        formData.append('file', file)
      }

      const botResponse = await api.uploadBot(formData)
      navigate(`/seller/bots/${botResponse.id}`, { replace: true })
    } catch (err: any) {
      setGeneralError(err.message || 'Failed to upload bot strategy. Please check parameters.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="max-w-4xl mx-auto flex flex-col gap-8">
      {/* Header */}
      <div className="flex flex-col gap-1 pb-6 border-b border-white/60">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-black font-technical tracking-wider text-[#2d3436]">
              UPLOAD STRATEGY // BOT INGESTION
            </h1>
            <span className="text-xs font-technical px-2 py-0.5 rounded bg-[#2d3436] text-[#ff4757] font-bold">
              PHASE 2
            </span>
          </div>
          <LedIndicator status="amber" label="SEBI GATE ACTIVE" pulse={false} />
        </div>
        <p className="text-xs font-technical text-[#718096] uppercase tracking-wider">
          REGISTER A NEW QUANTITATIVE BOT, ARCHIVE TO S3, AND TRIGGER VECTORBT SIMULATION
        </p>
      </div>

      {generalError && (
        <div className="p-4 rounded-xl bg-[#ff4757]/10 border border-[#ff4757]/30 shadow-chassis-recessed flex items-start gap-3">
          <AlertTriangle className="w-5 h-5 text-[#ff4757] shrink-0 mt-0.5" />
          <div className="flex flex-col">
            <span className="text-xs font-bold font-technical text-[#ff4757] uppercase">
              STRATEGY REJECTED
            </span>
            <span className="text-xs font-technical text-[#ff4757]/90 mt-0.5">
              {generalError}
            </span>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex flex-col gap-8">
        {/* Section 1: Core Strategy Specs */}
        <Card className="p-6 flex flex-col gap-6">
          <div className="flex items-center justify-between border-b border-black/5 pb-3">
            <span className="text-xs font-bold font-technical text-[#4a5568] uppercase tracking-wider">
              01 // BOT IDENTIFICATION
            </span>
            <span className="text-[10px] font-technical text-[#718096]">[SPEC_BLOCK]</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Input
              label="BOT NAME / IDENTIFIER"
              placeholder="e.g. Nifty SuperTrend Alpha"
              value={name}
              onChange={(e) => {
                setName(e.target.value)
                if (errors.name) setErrors({ ...errors, name: '' })
              }}
              errorText={errors.name}
              required
            />

            <Select
              label="STRATEGY CLASSIFICATION"
              options={strategyOptions}
              value={strategyType}
              onChange={(e) => setStrategyType(e.target.value)}
            />
          </div>

          <Textarea
            label="DESCRIPTION & OVERVIEW"
            placeholder="High-level description of strategy methodology, markets traded, and capital requirements..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            helperText="General overview for investors."
            rows={3}
          />
        </Card>

        {/* Section 2: White-Box Compliance Logic */}
        <Card className="p-6 flex flex-col gap-6 border-l-4 border-l-[#ff4757]">
          <div className="flex items-center justify-between border-b border-black/5 pb-3">
            <div className="flex items-center gap-2">
              <ShieldAlert className="w-4 h-4 text-[#ff4757]" />
              <span className="text-xs font-bold font-technical text-[#ff4757] uppercase tracking-wider">
                02 // MANDATORY WHITE-BOX LOGIC DISCLOSURE
              </span>
            </div>
            <span className="text-[10px] font-technical text-[#ff4757] font-bold">
              [SEBI REGULATION]
            </span>
          </div>

          <div className="p-3.5 rounded-xl bg-[#d9e0ea] shadow-chassis-recessed flex items-start gap-2.5">
            <span className="text-[11px] font-technical text-[#4a5568] leading-relaxed">
              <strong>Mandatory Disclosure:</strong> Outline entry/exit signals, indicators, timeframe, and risk parameters. Proprietary math or neural weights may remain private, but systemic execution mechanics must be disclosed to pass review.
            </span>
          </div>

          <Textarea
            label="DISCLOSED LOGIC & SYSTEM RULES"
            placeholder="e.g. Enters Long when 20 EMA > 50 EMA on 15m candle. Exit on 2.5 ATR trailing stop or 1:2 Risk-Reward target..."
            value={disclosedLogic}
            onChange={(e) => {
              setDisclosedLogic(e.target.value)
              if (errors.disclosedLogic) setErrors({ ...errors, disclosedLogic: '' })
            }}
            complianceBadge={true}
            errorText={errors.disclosedLogic}
            rows={5}
            required
          />
        </Card>

        {/* Section 3: File Vault & Backtest Specs */}
        <Card className="p-6 flex flex-col gap-6">
          <div className="flex items-center justify-between border-b border-black/5 pb-3">
            <span className="text-xs font-bold font-technical text-[#4a5568] uppercase tracking-wider">
              03 // ARTIFACT UPLOAD & INITIAL SIMULATION
            </span>
            <span className="text-[10px] font-technical text-[#718096]">[S3_INTEGRATION]</span>
          </div>

          <FileDropzone
            label="STRATEGY SOURCE CODE OR CONFIG"
            selectedFile={file}
            onFileSelect={(f) => {
              setFile(f)
              if (errors.file) setErrors({ ...errors, file: '' })
            }}
            errorText={errors.file}
          />

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
            <Input
              label="SIMULATION TICKER / SYMBOL"
              placeholder="^NSEI"
              value={symbol}
              onChange={(e) => setSymbol(e.target.value)}
              helperText="e.g. ^NSEI (Nifty 50), SPY, AAPL"
            />
            <Input
              label="FAST PERIOD / PARAM A"
              type="number"
              placeholder="20"
              value={fastPeriod}
              onChange={(e) => setFastPeriod(e.target.value)}
            />
            <Input
              label="SLOW PERIOD / PARAM B"
              type="number"
              placeholder="50"
              value={slowPeriod}
              onChange={(e) => setSlowPeriod(e.target.value)}
            />
          </div>
        </Card>

        {/* Submit Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 sm:p-6 rounded-2xl bg-[#e0e5ec] shadow-chassis-card border border-white/60">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-[#2ed573] shrink-0" />
            <span className="text-xs font-technical text-[#4a5568]">
              Ready for S3 encryption & Vectorbt backtest execution
            </span>
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            disabled={isSubmitting}
            className="gap-2 w-full sm:w-auto justify-center"
          >
            <UploadCloud className={`w-4 h-4 ${isSubmitting ? 'animate-bounce' : ''}`} />
            <span>{isSubmitting ? 'INGESTING & SIMULATING...' : 'PUBLISH STRATEGY (v1.0.0)'}</span>
            <ArrowRight className="w-4 h-4" />
          </Button>
        </div>
      </form>
    </div>
  )
}
