import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../lib/api'
import type { SellerDashboardBot } from '../../lib/api'
import { Button, Card, StatusBadge } from '../../components/ui'
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
      setError(err.message || 'Failed to load your bots.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchBots()
  }, [])

  // Maps bot listing status → organic StatusBadge props
  const getBotStatusBadge = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return <StatusBadge status="green" label="Published" />
      case 'PENDING_REVIEW':
        return <StatusBadge status="amber" label="In review" pulse />
      case 'REJECTED':
        return <StatusBadge status="orange" label="Rejected" />
      case 'DRAFT':
      default:
        return <StatusBadge status="neutral" label="Draft" />
    }
  }

  // Maps backtest status → inline organic pill
  const getBacktestBadge = (status: string | null) => {
    switch (status) {
      case 'COMPLETED':
        return <StatusBadge status="green" label="Backtest ready" />
      case 'FAILED':
        return <StatusBadge status="orange" label="Backtest failed" />
      case 'PENDING':
        return <StatusBadge status="amber" label="Processing" pulse />
      default:
        return <span className="text-xs font-body text-[#78786C]">No backtest yet</span>
    }
  }

  return (
    <div className="flex flex-col gap-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-[#DED8CF]/50">
        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-heading font-bold text-[#2C2C24]">
              Your strategies
            </h1>
            <span className="text-sm font-body px-2.5 py-1 rounded-full bg-[#E6DCCD] text-[#4A4A40] font-semibold">
              {bots.length} {bots.length === 1 ? 'bot' : 'bots'}
            </span>
          </div>
          <p className="text-sm font-body text-[#78786C]">
            Manage your algorithms, versions, and backtest results
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button variant="secondary" size="md" onClick={fetchBots} disabled={isLoading} icon={<RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />}>
            Refresh
          </Button>
          <Link to="/seller/bots/new">
            <Button variant="primary" size="md" icon={<PlusCircle className="w-4 h-4" />}>
              Upload bot
            </Button>
          </Link>
        </div>
      </div>

      {/* Error */}
      {error && (
        <div className="p-4 rounded-2xl bg-[#A85448]/8 border border-[#A85448]/25 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <AlertCircle className="w-4 h-4 text-[#A85448]" />
            <span className="text-sm font-body text-[#A85448]">{error}</span>
          </div>
          <Button variant="ghost" size="sm" onClick={fetchBots} className="text-[#A85448]">
            Retry
          </Button>
        </div>
      )}

      {/* Loading skeletons */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(3)].map((_, i) => (
            <Card key={i} className="p-6 flex flex-col gap-4 animate-pulse">
              <div className="h-5 w-1/2 bg-[#E6DCCD] rounded-full" />
              <div className="h-4 w-1/3 bg-[#E6DCCD] rounded-full" />
              <div className="h-20 bg-[#F0EBE5] rounded-2xl mt-2" />
            </Card>
          ))}
        </div>
      ) : bots.length === 0 ? (
        /* Empty state */
        <Card className="p-12 flex flex-col items-center justify-center text-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
            <BotIcon className="w-8 h-8" />
          </div>
          <div className="flex flex-col gap-2 max-w-sm">
            <h3 className="text-lg font-heading font-bold text-[#2C2C24]">
              No bots yet
            </h3>
            <p className="text-sm font-body text-[#78786C]">
              Upload your first quantitative trading algorithm to start backtesting and listing on AlgoAdda.
            </p>
          </div>
          <Link to="/seller/bots/new" className="mt-2">
            <Button variant="primary" size="lg" icon={<PlusCircle className="w-4 h-4" />}>
              Upload your first bot
            </Button>
          </Link>
        </Card>
      ) : (
        /* Bot grid */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {bots.map((bot) => (
            <Card key={bot.botId} interactive className="p-6 flex flex-col justify-between">
              <div className="flex flex-col gap-4">
                {/* Status row */}
                <div className="flex items-center justify-between gap-2">
                  <span className="text-xs font-semibold font-body px-2.5 py-1 rounded-full bg-[#E6DCCD] text-[#4A4A40]">
                    {bot.strategyType}
                  </span>
                  {getBotStatusBadge(bot.status)}
                </div>

                {/* Bot name & meta */}
                <div className="flex flex-col gap-0.5">
                  <h3 className="text-lg font-heading font-bold text-[#2C2C24] group-hover:text-[#5D7052] transition-colors">
                    {bot.name}
                  </h3>
                  <span className="text-xs font-body text-[#78786C]">
                    Created {new Date(bot.createdAt).toLocaleDateString()}
                  </span>
                </div>

                {/* Version & backtest data row */}
                <div className="p-3 rounded-2xl bg-[#F0EBE5] flex items-center justify-between">
                  <div className="flex flex-col">
                    <span className="text-[10px] font-semibold font-body text-[#78786C] uppercase tracking-wide">
                      Version
                    </span>
                    <span className="text-sm font-bold font-body text-[#2C2C24]">
                      v{bot.latestVersionNumber || '1.0.0'}
                    </span>
                  </div>
                  <div className="flex flex-col items-end">
                    <span className="text-[10px] font-semibold font-body text-[#78786C] uppercase tracking-wide mb-1">
                      Backtest
                    </span>
                    {getBacktestBadge(bot.backtestStatus)}
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div className="mt-5 pt-4 border-t border-[#DED8CF]/50 flex items-center gap-3">
                <Link to={`/seller/bots/${bot.botId}`} className="flex-1">
                  <Button variant="secondary" size="sm" className="w-full justify-between">
                    <span className="flex items-center gap-1.5">
                      <BarChart3 className="w-3.5 h-3.5" />
                      <span>View metrics</span>
                    </span>
                    <ArrowUpRight className="w-3.5 h-3.5" />
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
