import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../lib/api'
import type { SellerDashboardBot } from '../../lib/api'
import { Button, Card, LedIndicator } from '../../components/ui'
import { PlusCircle, Bot as BotIcon, ArrowUpRight, BarChart3, AlertCircle, RefreshCw } from 'lucide-react'

export const SellerDashboardPage: React.FC = () => {
  const [bots, setBots] = useState<SellerDashboardBot[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchBots = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await api.getSellerBots()
      setBots(data)
    } catch (err: any) {
      setError(err.message || 'Failed to load seller bots.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchBots()
  }, [])

  const getStatusLed = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return <LedIndicator status="green" label="PUBLISHED" pulse={false} />
      case 'PENDING_REVIEW':
        return <LedIndicator status="amber" label="IN REVIEW" pulse />
      case 'REJECTED':
        return <LedIndicator status="orange" label="REJECTED" pulse={false} />
      case 'DRAFT':
      default:
        return <LedIndicator status="neutral" label="DRAFT" pulse={false} />
    }
  }

  const getBacktestLed = (status: string | null) => {
    switch (status) {
      case 'COMPLETED':
        return <span className="text-[10px] font-bold font-technical text-[#2ed573] flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-[#2ed573] glow-led-green" /> BACKTEST READY</span>
      case 'FAILED':
        return <span className="text-[10px] font-bold font-technical text-[#ff4757] flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-[#ff4757] glow-led-orange" /> BACKTEST FAILED</span>
      case 'PENDING':
        return <span className="text-[10px] font-bold font-technical text-[#ffa502] flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-[#ffa502] animate-pulse" /> PROCESSING</span>
      default:
        return <span className="text-[10px] font-technical text-[#718096]">NO BACKTEST</span>
    }
  }

  return (
    <div className="flex flex-col gap-8">
      {/* Top Banner & Control Deck */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-white/60">
        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-black font-technical tracking-wider text-[#2d3436]">
              QUANT BOT PORTFOLIO
            </h1>
            <span className="text-xs font-technical px-2 py-0.5 rounded-full bg-[#d1d9e6] font-bold text-[#4a5568]">
              {bots.length} {bots.length === 1 ? 'ALGO' : 'ALGOS'}
            </span>
          </div>
          <p className="text-xs font-technical text-[#718096] uppercase tracking-wider">
            MANAGE DEPLOYED STRATEGIES, VERSIONS, AND VECTORBT BACKTESTS
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button variant="secondary" size="md" onClick={fetchBots} disabled={isLoading} className="gap-2">
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            <span>REFRESH</span>
          </Button>

          <Link to="/seller/bots/new">
            <Button variant="primary" size="md" className="gap-2">
              <PlusCircle className="w-4 h-4" />
              <span>UPLOAD NEW BOT</span>
            </Button>
          </Link>
        </div>
      </div>

      {/* Error Callout */}
      {error && (
        <div className="p-4 rounded-xl bg-[#ff4757]/10 border border-[#ff4757]/30 shadow-chassis-recessed flex items-center justify-between">
          <div className="flex items-center gap-3">
            <AlertCircle className="w-5 h-5 text-[#ff4757]" />
            <span className="text-xs font-technical text-[#ff4757] font-semibold">{error}</span>
          </div>
          <Button variant="ghost" size="sm" onClick={fetchBots} className="text-[#ff4757]">
            RETRY
          </Button>
        </div>
      )}

      {/* Loading Skeleton */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(3)].map((_, i) => (
            <Card key={i} className="p-6 flex flex-col gap-4 animate-pulse">
              <div className="h-6 w-1/2 bg-[#d1d9e6] rounded" />
              <div className="h-4 w-1/3 bg-[#d1d9e6] rounded" />
              <div className="h-24 bg-[#d1d9e6] rounded-xl mt-2" />
            </Card>
          ))}
        </div>
      ) : bots.length === 0 ? (
        /* Empty State */
        <Card className="p-12 flex flex-col items-center justify-center text-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-[#e0e5ec] shadow-chassis-floating flex items-center justify-center border border-white/80 text-[#718096]">
            <BotIcon className="w-8 h-8" />
          </div>
          <div className="flex flex-col gap-1 max-w-sm">
            <h3 className="text-base font-bold font-technical text-[#2d3436] uppercase tracking-wider">
              NO BOTS DEPLOYED YET
            </h3>
            <p className="text-xs font-technical text-[#718096]">
              Upload your first quantitative trading algorithm to begin backtesting and listing on AlgoAdda.
            </p>
          </div>
          <Link to="/seller/bots/new" className="mt-2">
            <Button variant="primary" size="lg" className="gap-2">
              <PlusCircle className="w-4 h-4" />
              <span>UPLOAD YOUR FIRST BOT</span>
            </Button>
          </Link>
        </Card>
      ) : (
        /* Bot Grid */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {bots.map((bot) => (
            <Card key={bot.botId} className="p-6 flex flex-col justify-between group hover:scale-[1.01] transition-mechanical">
              <div className="flex flex-col gap-4">
                {/* Status Header */}
                <div className="flex items-center justify-between gap-2">
                  <span className="text-[10px] font-bold font-technical px-2.5 py-1 rounded-md bg-[#d9e0ea] text-[#4a5568] shadow-chassis-sharp uppercase tracking-wider">
                    {bot.strategyType}
                  </span>
                  {getStatusLed(bot.status)}
                </div>

                {/* Bot Details */}
                <div className="flex flex-col gap-1">
                  <h3 className="text-lg font-black font-technical text-[#2d3436] group-hover:text-[#ff4757] transition-colors tracking-wide">
                    {bot.name}
                  </h3>
                  <span className="text-[10px] font-technical text-[#718096]">
                    ID: {bot.botId.slice(0, 8)}... • CREATED {new Date(bot.createdAt).toLocaleDateString()}
                  </span>
                </div>

                {/* Backtest Status Data Slot */}
                <div className="p-3.5 rounded-xl bg-[#d9e0ea] shadow-chassis-recessed flex items-center justify-between">
                  <div className="flex flex-col">
                    <span className="text-[9px] font-bold font-technical text-[#718096] uppercase">
                      LATEST VERSION
                    </span>
                    <span className="text-xs font-black font-technical text-[#2d3436]">
                      v{bot.latestVersionNumber || '1.0.0'}
                    </span>
                  </div>
                  <div className="flex flex-col items-end">
                    <span className="text-[9px] font-bold font-technical text-[#718096] uppercase">
                      ENGINE STATE
                    </span>
                    {getBacktestLed(bot.backtestStatus)}
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="mt-6 pt-4 border-t border-black/5 flex items-center gap-3">
                <Link to={`/seller/bots/${bot.botId}`} className="flex-1">
                  <Button variant="secondary" size="sm" className="w-full justify-between">
                    <span className="flex items-center gap-1.5">
                      <BarChart3 className="w-3.5 h-3.5 text-[#ff4757]" />
                      <span>VIEW METRICS</span>
                    </span>
                    <ArrowUpRight className="w-3.5 h-3.5 text-[#718096]" />
                  </Button>
                </Link>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
