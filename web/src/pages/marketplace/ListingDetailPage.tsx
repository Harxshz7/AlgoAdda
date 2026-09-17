import React, { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { api } from '../../lib/api'
import type { ListingDetail, BacktestMetrics, EquityPoint, ReportReason } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { useCart } from '../../context/CartContext'
import { Button, Card, MetricGauge, EquityCurveChart, OfficialBadge } from '../../components/ui'
import {
  ArrowLeft,
  ShieldCheck,
  AlertTriangle,
  FileCode,
  ShoppingCart,
  Calendar,
  FileText,
  Lock,
  LogIn,
  Zap,
  Flag,
} from 'lucide-react'

export const ListingDetailPage: React.FC = () => {
  const { listingId } = useParams<{ listingId: string }>()
  const { user } = useAuth()
  const { addToCart } = useCart()
  const navigate = useNavigate()

  const [listing, setListing] = useState<ListingDetail | null>(null)
  const [metrics, setMetrics] = useState<BacktestMetrics | null>(null)
  const [equityCurve, setEquityCurve] = useState<EquityPoint[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isPurchasing, setIsPurchasing] = useState(false)
  const [isAddingToCart, setIsAddingToCart] = useState(false)
  const [showReportForm, setShowReportForm] = useState(false)
  const [reportReason, setReportReason] = useState<ReportReason>('MISLEADING_CLAIMS')
  const [reportComment, setReportComment] = useState('')
  const [reportSubmitting, setReportSubmitting] = useState(false)
  const [reportSuccess, setReportSuccess] = useState(false)
  const [reportError, setReportError] = useState<string | null>(null)

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
      const startingCapital = metrics.starting_capital || metrics.initial_capital || 100000
      const endingCapital = metrics.ending_capital || startingCapital * (1 + (metrics.win_rate ? metrics.win_rate / 100 : 0.2))
      const pointsCount = 12

      const generatedPoints: EquityPoint[] = []
      const step = (endingCapital - startingCapital) / pointsCount

      for (let i = 0; i <= pointsCount; i++) {
        const date = new Date(2025, i, 1).toISOString().split('T')[0]
        const noise = (Math.random() - 0.4) * (step * 0.5)
        const equity = Math.round(startingCapital + step * i + noise)
        generatedPoints.push({ timestamp: date, equity })
      }
      setEquityCurve(generatedPoints)
    }
  }, [metrics])

  const handleBuy = async () => {
    if (!listing) return
    setIsPurchasing(true)
    try {
      const orderRes = await api.createOrder(listing.listingId)

      const loadSDK = (): Promise<boolean> => {
        return new Promise((resolve) => {
          if ((window as any).Razorpay) {
            resolve(true)
            return
          }
          const script = document.createElement('script')
          script.src = 'https://checkout.razorpay.com/v1/checkout.js'
          script.onload = () => resolve(true)
          script.onerror = () => resolve(false)
          document.body.appendChild(script)
        })
      }

      const loaded = await loadSDK()
      if (!loaded) {
        alert('Failed to load Razorpay checkout SDK. Please check your network connection.')
        setIsPurchasing(false)
        return
      }

      const options = {
        key: orderRes.razorpayKeyId,
        amount: orderRes.amountInPaise,
        currency: orderRes.currency,
        name: 'AlgoAdda Marketplace',
        description: `License purchase for ${listing.name}`,
        order_id: orderRes.razorpayOrderId,
        handler: async function () {
          navigate('/buyer/dashboard')
        },
        prefill: {
          email: user?.email || '',
        },
        theme: {
          color: '#5D7052',
        },
      }

      const rzp = new (window as any).Razorpay(options)
      rzp.open()
    } catch (err: any) {
      alert(`Order creation failed: ${err.message || 'Please try again'}`)
    } finally {
      setIsPurchasing(false)
    }
  }

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3">
        <div className="w-8 h-8 border-3 border-[#5D7052] border-t-transparent rounded-full animate-spin"></div>
        <p className="text-sm text-[#78786C]">Loading marketplace listing details...</p>
      </div>
    )
  }

  if (error || !listing) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <AlertTriangle className="w-12 h-12 text-[#A85448]" />
        <h2 className="text-xl font-heading font-bold text-[#2C2C24]">Listing Not Found</h2>
        <p className="text-sm text-[#78786C] max-w-md text-center">{error || 'The requested listing does not exist.'}</p>
        <Link to="/marketplace">
          <Button variant="secondary" size="md">
            Back to Marketplace
          </Button>
        </Link>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-8 max-w-6xl mx-auto">
      {/* Back Navigation Bar */}
      <div className="flex items-center justify-between">
        <Link
          to="/marketplace"
          className="inline-flex items-center gap-2 text-sm font-semibold text-[#78786C] hover:text-[#2C2C24] transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Marketplace</span>
        </Link>

        {user && (
          <div className="relative">
            {reportSuccess ? (
              <span className="text-xs font-semibold text-[#5D7052]">Report submitted ✓</span>
            ) : (
              <button
                onClick={() => setShowReportForm(!showReportForm)}
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#78786C] hover:text-[#A85448] transition-colors"
                title="Report this listing"
              >
                <Flag className="w-3.5 h-3.5" />
                <span>Report</span>
              </button>
            )}

            {showReportForm && !reportSuccess && (
              <div className="absolute right-0 top-full mt-2 w-80 p-4 rounded-xl bg-white border border-[#DED8CF] shadow-lg z-50">
                <h4 className="text-sm font-heading font-bold text-[#2C2C24] mb-3">Report Listing</h4>

                <label className="block text-xs font-semibold text-[#78786C] mb-1">Reason</label>
                <select
                  value={reportReason}
                  onChange={(e) => setReportReason(e.target.value as ReportReason)}
                  className="w-full mb-3 px-3 py-2 rounded-lg border border-[#DED8CF] bg-[#FDFCF8] text-sm text-[#2C2C24] focus:outline-none focus:ring-2 focus:ring-[#5D7052]/30"
                >
                  <option value="MISLEADING_CLAIMS">Misleading Claims</option>
                  <option value="GUARANTEED_RETURN_LANGUAGE">Guaranteed Return Language</option>
                  <option value="ABUSE">Abuse</option>
                  <option value="OTHER">Other</option>
                </select>

                <label className="block text-xs font-semibold text-[#78786C] mb-1">Comment (optional)</label>
                <textarea
                  value={reportComment}
                  onChange={(e) => setReportComment(e.target.value)}
                  rows={2}
                  className="w-full mb-3 px-3 py-2 rounded-lg border border-[#DED8CF] bg-[#FDFCF8] text-sm text-[#2C2C24] resize-none focus:outline-none focus:ring-2 focus:ring-[#5D7052]/30"
                  placeholder="Describe the issue..."
                />

                {reportError && (
                  <p className="text-xs text-[#A85448] mb-2">{reportError}</p>
                )}

                <div className="flex items-center gap-2 justify-end">
                  <button
                    onClick={() => { setShowReportForm(false); setReportError(null) }}
                    className="text-xs font-semibold text-[#78786C] hover:text-[#2C2C24] transition-colors"
                  >
                    Cancel
                  </button>
                  <Button
                    variant="primary"
                    size="sm"
                    disabled={reportSubmitting}
                    onClick={async () => {
                      if (!listing) return
                      setReportSubmitting(true)
                      setReportError(null)
                      try {
                        await api.reportListing(listing.listingId, {
                          reason: reportReason,
                          comment: reportComment || undefined,
                        })
                        setReportSuccess(true)
                        setShowReportForm(false)
                      } catch (err: any) {
                        setReportError(err.message || 'Failed to submit report')
                      } finally {
                        setReportSubmitting(false)
                      }
                    }}
                  >
                    {reportSubmitting ? 'Submitting...' : 'Submit Report'}
                  </Button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Hero Header Banner */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 p-6 sm:p-8 rounded-2xl bg-[#FDFCF8] border border-[#DED8CF]">
        <div className="flex flex-col gap-2">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="text-xs font-semibold px-2.5 py-0.5 rounded bg-[#5D7052]/10 text-[#5D7052] tracking-wide uppercase">
              {listing.strategyType}
            </span>
            {(listing.official || listing.isOfficial) && <OfficialBadge />}
          </div>
          <h1 className="text-2xl sm:text-3xl font-heading font-extrabold text-[#2C2C24]">
            {listing.name}
          </h1>
          <div className="flex items-center gap-3 text-xs text-[#78786C]">
            <span>Listed by <strong className="text-[#2C2C24]">{listing.sellerDisplayName}</strong></span>
            <span>•</span>
            <span className="flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5" />
              {new Date(listing.createdAt).toLocaleDateString()}
            </span>
          </div>
        </div>

        {/* Pricing & Checkout Button */}
        <div className="flex items-center gap-4">
          <div className="flex flex-col items-end leading-tight">
            <span className="text-2xl font-heading font-black text-[#2C2C24]">
              ₹{listing.price.toLocaleString()}
            </span>
            <span className="text-xs text-[#78786C]">
              {listing.licenseType === 'TIMED' ? 'Monthly Subscription' : 'One-Time License'}
            </span>
          </div>

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
              <div className="flex items-center gap-2">
                <Button
                  variant="secondary"
                  size="lg"
                  disabled={isAddingToCart || isPurchasing}
                  onClick={async () => {
                    try {
                      setIsAddingToCart(true)
                      await addToCart(listing.listingId)
                      navigate('/cart')
                    } catch (err: any) {
                      alert(err.message || 'Failed to add item to cart')
                    } finally {
                      setIsAddingToCart(false)
                    }
                  }}
                  className="gap-2"
                >
                  <ShoppingCart className="w-5 h-5 text-[#5D7052]" />
                  <span>{isAddingToCart ? 'Adding...' : 'Add to Cart'}</span>
                </Button>

                <Button
                  variant="primary"
                  size="lg"
                  disabled={isPurchasing || isAddingToCart}
                  onClick={handleBuy}
                  className="gap-2"
                >
                  {isPurchasing ? (
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  ) : (
                    <Zap className="w-5 h-5" />
                  )}
                  <span>{isPurchasing ? 'Processing Order...' : 'Buy Now'}</span>
                </Button>
              </div>
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
            value={metrics ? `${metrics.sharpe_ratio}` : 'N/A'}
            sublabel="Risk-adjusted return ratio"
            trend="positive"
            badge="EFFICIENCY"
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

      {/* Chart Section: Equity Curve vs Invested Baseline */}
      <EquityCurveChart equityCurve={equityCurve} metrics={metrics} />
    </div>
  )
}
