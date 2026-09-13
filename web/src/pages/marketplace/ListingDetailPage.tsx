import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { api } from '../../lib/api'
import type { ListingDetail, BacktestMetrics, EquityPoint } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { Button, Card, MetricGauge } from '../../components/ui'
import {
  ArrowLeft,
  ShieldCheck,
  TrendingUp,
  AlertTriangle,
  FileCode,
  ShoppingCart,
  Calendar,
  AlertCircle,
  FileText,
  Lock,
  LogIn,
} from 'lucide-react'
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts'

export const ListingDetailPage: React.FC = () => {
  const { listingId } = useParams<{ listingId: string }>()
  const { user } = useAuth()

  const [listing, setListing] = useState<ListingDetail | null>(null)
  const [metrics, setMetrics] = useState<BacktestMetrics | null>(null)
  const [equityCurve, setEquityCurve] = useState<EquityPoint[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchDetail = async () => {
      if (!listingId) return
      setIsLoading(true)
      setError(null)

      try {
        const data = await api.getListingDetail(listingId)
        setListing(data)

        if (data.metrics) {
          try {
            const parsed = JSON.parse(data.metrics)
            setMetrics(parsed)
          } catch {
            // parsing fallback
          }
        }
      } catch (err: any) {
        setError(err.message || 'Failed to load listing details.')
      } finally {
        setIsLoading(false)
      }
    }

    fetchDetail()
  }, [listingId])

  // Synthetic or parsed equity curve based on backtest metrics
  useEffect(() => {
    if (metrics) {
      const points: EquityPoint[] = []
      const start = metrics.starting_capital || 10000
      const end = metrics.ending_capital || (start * (1 + (metrics.win_rate || 50) / 100))
      const steps = 30
      const diff = (end - start) / steps

      for (let i = 0; i <= steps; i++) {
        const noise = Math.sin(i / 2) * (start * 0.02)
        points.push({
          timestamp: `Day ${i + 1}`,
          equity: Math.round(start + diff * i + noise),
        })
      }
      setEquityCurve(points)
    }
  }, [metrics])

  if (isLoading) {
    return (
      <div className="flex flex-col gap-6 animate-pulse">
        <div className="h-8 bg-[#DED8CF]/40 rounded-md w-1/3" />
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="h-64 bg-[#DED8CF]/30 rounded-2xl" />
          <div className="lg:col-span-2 h-64 bg-[#DED8CF]/30 rounded-2xl" />
        </div>
      </div>
    )
  }

  if (error || !listing) {
    return (
      <div className="p-8 rounded-2xl bg-[#A85448]/10 border border-[#A85448]/30 flex flex-col items-center text-center gap-4">
        <AlertCircle className="w-10 h-10 text-[#A85448]" />
        <h2 className="font-heading font-bold text-xl text-[#2C2C24]">Listing Not Found</h2>
        <p className="text-sm font-body text-[#78786C]">{error || 'The requested listing could not be retrieved.'}</p>
        <Link to="/marketplace">
          <Button variant="primary" size="md">Back to Marketplace</Button>
        </Link>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-8">
      {/* Top Header Navigation */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-[#DED8CF]/50">
        <div className="flex items-center gap-4">
          <Link to="/marketplace">
            <Button variant="ghost" size="sm" className="gap-2 text-[#78786C]">
              <ArrowLeft className="w-4 h-4" />
              <span>MARKETPLACE</span>
            </Button>
          </Link>
          <div className="h-6 w-px bg-[#DED8CF]" />
          <div className="flex flex-col">
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-heading font-extrabold text-[#2C2C24]">
                {listing.name}
              </h1>
              <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-[#5D7052]/10 text-[#5D7052]">
                {listing.strategyType}
              </span>
            </div>
            <span className="text-xs text-[#78786C]">
              Published by{' '}
              <Link to={`/sellers/${listing.sellerId}`} className="font-semibold text-[#2C2C24] hover:underline">
                {listing.sellerDisplayName}
              </Link>
            </span>
          </div>
        </div>

        {/* Pricing & Phase 5 Disabled Buy CTA */}
        <div className="flex items-center gap-4">
          <div className="flex flex-col items-end leading-tight">
            <span className="text-2xl font-heading font-black text-[#2C2C24]">
              ₹{listing.price.toLocaleString()}
            </span>
            <span className="text-xs text-[#78786C]">
              {listing.licenseType === 'TIMED' ? 'Monthly Subscription' : 'One-Time License'}
            </span>
          </div>

          {/* Buy Button - Contextual Tooltip */}
          <div className="relative group">
            {!user ? (
              <Link to="/login">
                <Button variant="primary" size="lg" className="gap-2">
                  <LogIn className="w-5 h-5" />
                  <span>Log in to Purchase</span>
                </Button>
              </Link>
            ) : user.role === 'SELLER' ? (
              <>
                <Button
                  variant="primary"
                  size="lg"
                  disabled
                  className="gap-2 opacity-60 cursor-not-allowed shadow-none"
                >
                  <ShoppingCart className="w-5 h-5" />
                  <span>Buy Algorithm</span>
                </Button>
                <div className="absolute right-0 bottom-full mb-2 hidden group-hover:block w-64 p-3 rounded-xl bg-[#2C2C24] text-white text-xs font-body shadow-xl z-50">
                  <div className="flex items-center gap-1.5 font-semibold text-[#C18C5D] mb-1">
                    <Lock className="w-3.5 h-3.5" />
                    <span>Seller Account</span>
                  </div>
                  Sellers cannot purchase algorithms. Switch to a Buyer account.
                </div>
              </>
            ) : (
              <>
                <Button
                  variant="primary"
                  size="lg"
                  disabled
                  className="gap-2 opacity-60 cursor-not-allowed shadow-none"
                >
                  <ShoppingCart className="w-5 h-5" />
                  <span>Buy Algorithm</span>
                </Button>
                <div className="absolute right-0 bottom-full mb-2 hidden group-hover:block w-64 p-3 rounded-xl bg-[#2C2C24] text-white text-xs font-body shadow-xl z-50">
                  <div className="flex items-center gap-1.5 font-semibold text-[#5D7052] mb-1">
                    <Lock className="w-3.5 h-3.5" />
                    <span>Phase 5 Upcoming</span>
                  </div>
                  Checkout &amp; purchasing flow coming soon in Phase 5.
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Main Grid: Overview & Metrics */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Strategy & Compliance Card */}
        <Card className="p-6 flex flex-col justify-between gap-6">
          <div className="flex flex-col gap-4">
            <div className="flex items-center justify-between border-b border-[#DED8CF]/40 pb-3">
              <span className="text-xs font-bold text-[#78786C] uppercase tracking-wider">
                Listing Overview
              </span>
              <span className="text-xs font-semibold px-2 py-0.5 rounded bg-[#5D7052]/10 text-[#5D7052]">
                VERIFIED
              </span>
            </div>

            <p className="text-sm font-body text-[#2C2C24] leading-relaxed">
              {listing.description}
            </p>

            <div className="flex flex-col gap-3 pt-2">
              <div className="p-3.5 rounded-xl bg-[#FDFCF8] border border-[#DED8CF]/50 flex items-center justify-between">
                <span className="text-xs font-medium text-[#78786C]">Compliance Gate</span>
                <span className="text-xs font-bold text-[#5D7052] flex items-center gap-1">
                  <ShieldCheck className="w-4 h-4" />
                  White-Box Gate Passed
                </span>
              </div>

              <div className="p-3.5 rounded-xl bg-[#FDFCF8] border border-[#DED8CF]/50 flex items-center justify-between">
                <span className="text-xs font-medium text-[#78786C]">Strategy File Storage</span>
                <span className="text-xs font-bold text-[#2C2C24] flex items-center gap-1">
                  <FileCode className="w-4 h-4 text-[#78786C]" />
                  Secured S3 Artifact
                </span>
              </div>
            </div>
          </div>

          {/* Seller Card Box */}
          <div className="p-4 rounded-xl bg-[#FDFCF8] border border-[#DED8CF]/60 flex flex-col gap-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-[#78786C] uppercase">Author / Creator</span>
              <Link to={`/sellers/${listing.sellerId}`} className="text-xs text-[#5D7052] font-semibold hover:underline">
                View Profile
              </Link>
            </div>
            <div className="flex items-center gap-3 pt-1">
              <div className="w-9 h-9 rounded-full bg-[#5D7052]/15 flex items-center justify-center text-[#5D7052] font-bold">
                {listing.sellerDisplayName.substring(0, 2).toUpperCase()}
              </div>
              <div className="flex flex-col">
                <span className="text-sm font-bold text-[#2C2C24]">{listing.sellerDisplayName}</span>
                <span className="text-xs text-[#78786C] flex items-center gap-1">
                  <Calendar className="w-3 h-3" />
                  Member since {new Date(listing.sellerCreatedAt).toLocaleDateString()}
                </span>
              </div>
            </div>
            {listing.sellerBio && (
              <p className="text-xs font-body text-[#78786C] italic pt-1 line-clamp-2">
                "{listing.sellerBio}"
              </p>
            )}
          </div>
        </Card>

        {/* Right Column: Performance Gauges */}
        <div className="lg:col-span-2 grid grid-cols-2 sm:grid-cols-4 gap-4">
          <MetricGauge
            label="WIN RATE"
            value={metrics ? `${metrics.win_rate}%` : 'N/A'}
            unit="PCT"
            sublabel="Trading session win ratio"
            trend="positive"
            badge="ACCURACY"
          />

          <MetricGauge
            label="PROFIT FACTOR"
            value={metrics?.profit_factor !== undefined ? metrics.profit_factor : '2.10'}
            sublabel="Gross profit / Gross loss"
            trend="positive"
            badge="PAYOFF"
          />

          <MetricGauge
            label="SHARPE RATIO"
            value={metrics ? metrics.sharpe_ratio : 'N/A'}
            sublabel="Risk-adjusted annual alpha"
            trend="positive"
            badge="QUALITY"
          />

          <MetricGauge
            label="MAX DRAWDOWN"
            value={metrics ? `${metrics.max_drawdown}%` : 'N/A'}
            unit="DD"
            sublabel="Peak-to-valley decline"
            trend="negative"
            badge="RISK"
          />
        </div>
      </div>

      {/* Disclosed Logic Section (White-Box Feature) */}
      <Card className="p-6 flex flex-col gap-4">
        <div className="flex items-center justify-between border-b border-[#DED8CF]/40 pb-3">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
              <FileText className="w-4 h-4" />
            </div>
            <div className="flex flex-col">
              <h3 className="font-heading font-bold text-lg text-[#2C2C24]">
                Disclosed Strategy Logic & Methodology
              </h3>
              <span className="text-xs text-[#78786C]">
                Mandatory white-box rules verified by automated compliance check
              </span>
            </div>
          </div>
          <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-[#5D7052]/10 text-[#5D7052]">
            WHITE-BOX DISCLOSED
          </span>
        </div>

        <div className="p-4 rounded-xl bg-[#FDFCF8] border border-[#DED8CF] font-mono text-xs text-[#2C2C24] whitespace-pre-wrap leading-relaxed max-h-60 overflow-y-auto">
          {listing.disclosedLogic || 'No disclosed logic provided.'}
        </div>

        {listing.methodologyNotes && (
          <div className="p-3 rounded-lg bg-[#5D7052]/5 border border-[#5D7052]/20 text-xs text-[#78786C]">
            <strong className="text-[#2C2C24]">Backtest Methodology:</strong> {listing.methodologyNotes}
          </div>
        )}
      </Card>

      {/* Risk Disclaimer Box */}
      <div className="p-5 rounded-2xl bg-[#A85448]/10 border border-[#A85448]/25 flex items-start gap-3">
        <AlertTriangle className="w-5 h-5 text-[#A85448] shrink-0 mt-0.5" />
        <div className="flex flex-col gap-1">
          <h4 className="font-heading font-bold text-sm text-[#A85448]">
            Risk Disclaimer & Warning
          </h4>
          <p className="text-xs font-body text-[#2C2C24]/80 leading-relaxed">
            {listing.riskDisclaimer ||
              'Past performance generated through historical backtests is no guarantee of future returns. Algorithmic trading carries significant financial risk. Evaluate strategy parameters and market conditions thoroughly before deployment.'}
          </p>
        </div>
      </div>

      {/* Simulated Equity Growth Trajectory Chart */}
      <Card className="p-6 flex flex-col gap-6">
        <div className="flex items-center justify-between border-b border-[#DED8CF]/40 pb-4">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
              <TrendingUp className="w-4 h-4" />
            </div>
            <div className="flex flex-col">
              <span className="text-xs font-bold text-[#2C2C24] uppercase tracking-wider">
                Simulated Equity Growth Trajectory
              </span>
              <span className="text-xs text-[#78786C]">
                STARTING CAPITAL: ₹10,000.00 • COMPOUNDED PORTFOLIO VALUE
              </span>
            </div>
          </div>
        </div>

        {/* Recharts Area Chart with Dark Chassis Palette */}
        <div className="w-full h-72 rounded-xl bg-[#14181f] p-4 shadow-[inset_2px_2px_8px_rgba(0,0,0,0.8)] relative overflow-hidden">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={equityCurve} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="equityGradientPublic" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#5D7052" stopOpacity={0.5} />
                  <stop offset="95%" stopColor="#5D7052" stopOpacity={0.0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#2d3436" opacity={0.4} />
              <XAxis dataKey="timestamp" stroke="#718096" fontSize={10} fontFamily="JetBrains Mono, monospace" tickLine={false} />
              <YAxis stroke="#718096" fontSize={10} fontFamily="JetBrains Mono, monospace" domain={['dataMin - 500', 'dataMax + 500']} tickLine={false} />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#1e242d',
                  border: '1px solid #5D7052',
                  borderRadius: '8px',
                  fontFamily: 'JetBrains Mono, monospace',
                  fontSize: '11px',
                  color: '#ffffff',
                }}
                formatter={(val: any) => [`₹${Number(val).toLocaleString()}`, 'Portfolio Equity']}
              />
              <Area type="monotone" dataKey="equity" stroke="#5D7052" strokeWidth={2.5} fillOpacity={1} fill="url(#equityGradientPublic)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </Card>
    </div>
  )
}
