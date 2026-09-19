import React, { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { Card, Button } from '../../components/ui'
import { BarChart2, Eye, ShoppingBag, DollarSign, Award } from 'lucide-react'
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts'

interface BotAnalytics {
  botId: string
  botName: string
  strategyType: string
  status: string
  totalViews: number
  totalPurchases: number
  conversionRate: number | null
  grossRevenue: number
  refundedAmount: number
  netRevenue: number
  averageRating: number | null
}

interface AnalyticsSummary {
  totalViews: number
  totalPurchases: number
  totalGrossRevenue: number
  totalRefundedAmount: number
  totalNetRevenue: number
  mostPopularBotId: string | null
  mostPopularBotName: string | null
  mostPopularBotPurchases: number
}

interface AnalyticsData {
  summary: AnalyticsSummary
  bots: BotAnalytics[]
}

export const SellerAnalyticsPage: React.FC = () => {
  const { token } = useAuth()
  const [data, setData] = useState<AnalyticsData | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchAnalytics()
  }, [token])

  const fetchAnalytics = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await fetch('/api/sellers/me/analytics', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })
      if (!res.ok) {
        throw new Error('Failed to load analytics data')
      }
      const json = await res.json()
      setData(json)
    } catch (err: any) {
      setError(err.message || 'An error occurred')
    } finally {
      setLoading(false)
    }
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2
    }).format(amount)
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#5D7052]"></div>
      </div>
    )
  }

  if (error || !data) {
    return (
      <div className="p-6 bg-[#A85448]/10 text-[#A85448] rounded-xl border border-[#A85448]/20">
        <p className="font-semibold">Error</p>
        <p className="text-sm">{error || 'Failed to load analytics'}</p>
        <Button size="sm" className="mt-4" onClick={fetchAnalytics}>
          Retry
        </Button>
      </div>
    )
  }

  const chartData = data.bots.map((b) => ({
    name: b.botName.length > 15 ? b.botName.substring(0, 15) + '...' : b.botName,
    NetRevenue: b.netRevenue,
    GrossRevenue: b.grossRevenue,
    Purchases: b.totalPurchases
  }))

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-heading font-bold text-[#2C2C24]">Seller Analytics</h1>
        <p className="text-[#78786C] text-sm mt-1">
          Monitor your bot views, sales performance, conversion rates, and revenue.
        </p>
      </div>

      {/* Top Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <Card className="p-5 bg-white border border-[#DED8CF]/60 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#78786C] uppercase tracking-wider">
              Net Revenue
            </span>
            <div className="p-2 bg-[#5D7052]/10 rounded-lg text-[#5D7052]">
              <DollarSign className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl font-bold font-heading text-[#2C2C24]">
              {formatCurrency(data.summary.totalNetRevenue)}
            </span>
            <div className="flex items-center gap-2 mt-1 text-xs text-[#78786C]">
              <span>Gross: {formatCurrency(data.summary.totalGrossRevenue)}</span>
              {data.summary.totalRefundedAmount > 0 && (
                <span className="text-[#A85448]">
                  (-{formatCurrency(data.summary.totalRefundedAmount)})
                </span>
              )}
            </div>
          </div>
        </Card>

        <Card className="p-5 bg-white border border-[#DED8CF]/60 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#78786C] uppercase tracking-wider">
              Total Views
            </span>
            <div className="p-2 bg-[#5D7052]/10 rounded-lg text-[#5D7052]">
              <Eye className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl font-bold font-heading text-[#2C2C24]">
              {data.summary.totalViews.toLocaleString()}
            </span>
            <p className="text-xs text-[#78786C] mt-1">Across all published listing pages</p>
          </div>
        </Card>

        <Card className="p-5 bg-white border border-[#DED8CF]/60 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#78786C] uppercase tracking-wider">
              Total Purchases
            </span>
            <div className="p-2 bg-[#5D7052]/10 rounded-lg text-[#5D7052]">
              <ShoppingBag className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl font-bold font-heading text-[#2C2C24]">
              {data.summary.totalPurchases.toLocaleString()}
            </span>
            <p className="text-xs text-[#78786C] mt-1">Paid licenses issued</p>
          </div>
        </Card>

        <Card className="p-5 bg-white border border-[#DED8CF]/60 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#78786C] uppercase tracking-wider">
              Most Popular Bot
            </span>
            <div className="p-2 bg-[#5D7052]/10 rounded-lg text-[#5D7052]">
              <Award className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-lg font-bold font-heading text-[#2C2C24] truncate block">
              {data.summary.mostPopularBotName || 'N/A'}
            </span>
            <p className="text-xs text-[#78786C] mt-1">
              {data.summary.mostPopularBotPurchases > 0
                ? `${data.summary.mostPopularBotPurchases} purchases`
                : 'No sales yet'}
            </p>
          </div>
        </Card>
      </div>

      {/* Revenue Chart */}
      {data.bots.length > 0 && (
        <Card className="p-6 bg-white border border-[#DED8CF]/60 shadow-sm">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-lg font-bold font-heading text-[#2C2C24]">Net Revenue by Bot</h2>
              <p className="text-xs text-[#78786C]">Comparison of net earnings across your bots</p>
            </div>
            <BarChart2 className="w-5 h-5 text-[#5D7052]" />
          </div>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#EAE5DF" />
                <XAxis dataKey="name" stroke="#78786C" fontSize={12} />
                <YAxis stroke="#78786C" fontSize={12} />
                <Tooltip
                  formatter={(val: number | undefined) => [formatCurrency(val || 0), 'Net Revenue']}
                  contentStyle={{ backgroundColor: '#FFF', borderColor: '#DED8CF', borderRadius: '8px' }}
                />
                <Bar dataKey="NetRevenue" fill="#5D7052" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      )}

      {/* Per Bot Table */}
      <Card className="p-6 bg-white border border-[#DED8CF]/60 shadow-sm overflow-hidden">
        <h2 className="text-lg font-bold font-heading text-[#2C2C24] mb-4">Bot Performance Breakdown</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-[#DED8CF]/60 text-xs font-semibold text-[#78786C] uppercase tracking-wider">
                <th className="py-3 px-4">Bot Name</th>
                <th className="py-3 px-4">Strategy</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4 text-right">Views</th>
                <th className="py-3 px-4 text-right">Purchases</th>
                <th className="py-3 px-4 text-right">Conv. Rate</th>
                <th className="py-3 px-4 text-right">Net Revenue</th>
                <th className="py-3 px-4 text-right">Avg Rating</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#DED8CF]/40 text-sm">
              {data.bots.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-8 text-center text-[#78786C]">
                    No bots found. Upload a bot to start tracking analytics!
                  </td>
                </tr>
              ) : (
                data.bots.map((bot) => (
                  <tr key={bot.botId} className="hover:bg-[#FDFCF8]">
                    <td className="py-3 px-4 font-semibold text-[#2C2C24]">
                      {bot.botName}
                    </td>
                    <td className="py-3 px-4 text-[#78786C]">
                      <span className="px-2 py-0.5 text-xs font-medium rounded-full bg-[#5D7052]/10 text-[#5D7052]">
                        {bot.strategyType}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-[#78786C]">
                      <span className="text-xs uppercase font-medium">
                        {bot.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right font-mono text-[#2C2C24]">
                      {bot.totalViews}
                    </td>
                    <td className="py-3 px-4 text-right font-mono text-[#2C2C24]">
                      {bot.totalPurchases}
                    </td>
                    <td className="py-3 px-4 text-right font-mono">
                      {bot.conversionRate !== null ? (
                        <span className="text-[#5D7052] font-semibold">
                          {(bot.conversionRate * 100).toFixed(1)}%
                        </span>
                      ) : (
                        <span className="text-[#78786C]">N/A</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-right font-semibold text-[#2C2C24]">
                      {formatCurrency(bot.netRevenue)}
                    </td>
                    <td className="py-3 px-4 text-right text-[#78786C]">
                      {bot.averageRating !== null ? bot.averageRating.toFixed(1) : 'N/A'}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  )
}
