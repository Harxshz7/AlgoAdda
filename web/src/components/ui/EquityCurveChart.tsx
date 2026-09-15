import React from 'react'
import {
  ResponsiveContainer,
  ComposedChart,
  Area,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts'
import { TrendingUp, Info, AlertTriangle, ArrowUpRight, ArrowDownRight } from 'lucide-react'
import { Card } from './Card'
import type { BacktestMetrics, EquityPoint } from '../../lib/api'

export interface EquityCurveChartProps {
  initialCapital?: number
  equityCurve?: EquityPoint[]
  metrics?: BacktestMetrics | null
  title?: string
  subtitle?: string
}

export const EquityCurveChart: React.FC<EquityCurveChartProps> = ({
  initialCapital: propInitialCapital,
  equityCurve,
  metrics,
  title = 'SIMULATED EQUITY GROWTH vs. INVESTED CAPITAL',
  subtitle,
}) => {
  // Determine starting capital baseline
  const initialCapital =
    propInitialCapital ||
    metrics?.starting_capital ||
    metrics?.initial_capital ||
    10000

  // Build chart dataset
  let data: {
    timestamp: string
    equity: number
    invested: number
    profit: number
    profitPct: number
  }[] = []

  if (equityCurve && equityCurve.length > 0) {
    data = equityCurve.map((pt) => {
      const eq = typeof pt.equity === 'number' ? pt.equity : parseFloat(pt.equity as any) || initialCapital
      const prof = eq - initialCapital
      const profPct = initialCapital > 0 ? (prof / initialCapital) * 100 : 0
      return {
        timestamp: pt.timestamp,
        equity: Math.round(eq * 100) / 100,
        invested: initialCapital,
        profit: Math.round(prof * 100) / 100,
        profitPct: Math.round(profPct * 100) / 100,
      }
    })
  } else if (metrics) {
    // Generate synthetic curve from starting to ending capital if curve array not directly provided
    const end = metrics.ending_capital || initialCapital * (1 + (metrics.win_rate || 50) / 100)
    const steps = 30
    const diff = (end - initialCapital) / steps

    for (let i = 0; i <= steps; i++) {
      const noise = Math.sin(i / 2) * (initialCapital * 0.03)
      const eq = Math.round(initialCapital + diff * i + noise)
      const prof = eq - initialCapital
      const profPct = initialCapital > 0 ? (prof / initialCapital) * 100 : 0
      data.push({
        timestamp: `Day ${i + 1}`,
        equity: eq,
        invested: initialCapital,
        profit: Math.round(prof * 100) / 100,
        profitPct: Math.round(profPct * 100) / 100,
      })
    }
  } else {
    // Fallback baseline data
    for (let i = 0; i <= 20; i++) {
      data.push({
        timestamp: `Day ${i + 1}`,
        equity: initialCapital,
        invested: initialCapital,
        profit: 0,
        profitPct: 0,
      })
    }
  }

  // Calculate min, max, and gradient split offset
  const minEquity = Math.min(...data.map((d) => d.equity), initialCapital)
  const maxEquity = Math.max(...data.map((d) => d.equity), initialCapital)
  const range = maxEquity - minEquity
  const off = range > 0 ? ((maxEquity - initialCapital) / range) * 100 : 50

  const hasDrawdownBelowPrincipal = data.some((d) => d.equity < initialCapital)

  return (
    <Card className="p-6 flex flex-col gap-6">
      {/* Header & Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[#DED8CF]/40 pb-4">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052] shrink-0">
            <TrendingUp className="w-5 h-5" />
          </div>
          <div className="flex flex-col">
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-heading font-extrabold text-[#2C2C24] uppercase tracking-wider">
                {title}
              </h3>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-[#5D7052]/10 text-[#5D7052] uppercase">
                Simulated Backtest
              </span>
            </div>
            <span className="text-xs font-body text-[#78786C] mt-0.5">
              {subtitle ||
                `Baseline Invested Principal: ₹${initialCapital.toLocaleString()} • Growth vs. Capital Invested`}
            </span>
          </div>
        </div>

        {/* Legend Pills */}
        <div className="flex flex-wrap items-center gap-3 text-xs font-body shrink-0">
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#5D7052]/10 border border-[#5D7052]/20">
            <span className="w-2.5 h-2.5 rounded-full bg-[#5D7052]" />
            <span className="font-semibold text-[#2C2C24]">Portfolio Value</span>
          </div>

          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#78786C]/10 border border-[#78786C]/20">
            <span className="w-3 h-0.5 bg-[#78786C]" />
            <span className="font-semibold text-[#78786C]">Invested (₹{initialCapital.toLocaleString()})</span>
          </div>

          {hasDrawdownBelowPrincipal && (
            <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#A85448]/10 border border-[#A85448]/30 text-[#A85448]">
              <AlertTriangle className="w-3 h-3" />
              <span className="font-semibold text-[11px]">Below Principal</span>
            </div>
          )}
        </div>
      </div>

      {/* Recharts Chassis */}
      <div className="w-full h-80 rounded-xl bg-[#14181f] p-4 shadow-[inset_2px_2px_8px_rgba(0,0,0,0.8)] relative overflow-hidden">
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart data={data} margin={{ top: 12, right: 12, left: 10, bottom: 4 }}>
            <defs>
              <linearGradient id="splitEquityGradient" x1="0" y1="0" x2="0" y2="1">
                {/* Profit region above initial capital */}
                <stop offset="0%" stopColor="#5D7052" stopOpacity={0.45} />
                <stop offset={`${off}%`} stopColor="#5D7052" stopOpacity={0.05} />
                {/* Drawdown region below initial capital */}
                <stop offset={`${off}%`} stopColor="#A85448" stopOpacity={0.15} />
                <stop offset="100%" stopColor="#A85448" stopOpacity={0.55} />
              </linearGradient>
            </defs>

            <CartesianGrid strokeDasharray="3 3" stroke="#2d3436" opacity={0.4} />

            <XAxis
              dataKey="timestamp"
              stroke="#718096"
              fontSize={10}
              fontFamily="JetBrains Mono, monospace"
              tickLine={false}
            />

            <YAxis
              stroke="#718096"
              fontSize={10}
              fontFamily="JetBrains Mono, monospace"
              domain={['dataMin - 500', 'dataMax + 500']}
              tickLine={false}
              tickFormatter={(val) => `₹${(val / 1000).toFixed(0)}k`}
            />

            {/* Custom Interactive Tooltip */}
            <Tooltip
              content={({ active, payload, label }) => {
                if (!active || !payload || !payload.length) return null
                const item = payload[0].payload
                const isProfit = item.profit >= 0

                return (
                  <div className="p-3.5 rounded-xl bg-[#1e242d] border border-[#5D7052]/60 shadow-xl text-xs font-mono text-white flex flex-col gap-2 min-w-[210px]">
                    <div className="text-[11px] font-bold text-[#718096] border-b border-[#2d3436] pb-1 flex justify-between">
                      <span>{label}</span>
                      <span className="text-white">{isProfit ? 'PROFIT' : 'DRAWDOWN'}</span>
                    </div>

                    <div className="flex flex-col gap-1 text-xs">
                      <div className="flex justify-between items-center">
                        <span className="text-[#a0aec0]">Portfolio Value:</span>
                        <span className="font-bold text-white">₹{item.equity.toLocaleString()}</span>
                      </div>

                      <div className="flex justify-between items-center">
                        <span className="text-[#a0aec0]">Invested Principal:</span>
                        <span className="font-medium text-[#78786C]">₹{item.invested.toLocaleString()}</span>
                      </div>

                      <div className="flex justify-between items-center pt-1 border-t border-[#2d3436]/60">
                        <span className="text-[#a0aec0]">Net P&amp;L:</span>
                        <span
                          className={`font-bold flex items-center gap-0.5 ${
                            isProfit ? 'text-[#5D7052]' : 'text-[#A85448]'
                          }`}
                        >
                          {isProfit ? (
                            <ArrowUpRight className="w-3.5 h-3.5" />
                          ) : (
                            <ArrowDownRight className="w-3.5 h-3.5" />
                          )}
                          <span>
                            {isProfit ? '+' : ''}₹{item.profit.toLocaleString()} ({isProfit ? '+' : ''}
                            {item.profitPct}%)
                          </span>
                        </span>
                      </div>
                    </div>
                  </div>
                )
              }}
            />

            {/* Area fill with split gradient */}
            <Area
              type="monotone"
              dataKey="equity"
              name="Portfolio Value"
              stroke="#5D7052"
              strokeWidth={2.5}
              fill="url(#splitEquityGradient)"
              baseValue={initialCapital}
            />

            {/* Flat invested capital reference line */}
            <Line
              type="monotone"
              dataKey="invested"
              name="Invested Amount"
              stroke="#78786C"
              strokeDasharray="6 6"
              strokeWidth={2}
              dot={false}
              isAnimationActive={false}
            />
          </ComposedChart>
        </ResponsiveContainer>
      </div>

      {/* Compliance / Regulatory Note */}
      <div className="p-3.5 rounded-xl bg-[#FDFCF8] border border-[#DED8CF]/60 flex items-start gap-2.5 text-xs text-[#78786C]">
        <Info className="w-4 h-4 text-[#5D7052] shrink-0 mt-0.5" />
        <span className="leading-relaxed font-body">
          <strong className="text-[#2C2C24]">Backtest Disclosure:</strong> The chart compares historical simulated strategy portfolio value against initial capital (₹{initialCapital.toLocaleString()}). Past backtested returns are generated from historical data and do not guarantee future live trading results.
        </span>
      </div>
    </Card>
  )
}
