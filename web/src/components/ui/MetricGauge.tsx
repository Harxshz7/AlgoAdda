import React from 'react'

export interface MetricGaugeProps {
  label: string
  value: string | number
  unit?: string
  sublabel?: string
  trend?: 'positive' | 'negative' | 'neutral'
  badge?: string
  className?: string
}

export const MetricGauge: React.FC<MetricGaugeProps> = ({
  label,
  value,
  unit,
  sublabel,
  trend = 'neutral',
  badge,
  className = '',
}) => {
  const trendColor = {
    positive: 'text-[#2ed573]',
    negative: 'text-[#ff4757]',
    neutral: 'text-[#2d3436]',
  }[trend]

  return (
    <div
      className={`
        p-4 rounded-xl bg-[#e0e5ec] shadow-chassis-card border border-white/60
        flex flex-col justify-between relative overflow-hidden group
        ${className}
      `}
    >
      <div className="flex items-center justify-between gap-2 mb-2">
        <span className="text-[10px] font-bold font-technical text-[#4a5568] uppercase tracking-wider">
          {label}
        </span>
        {badge && (
          <span className="text-[9px] font-technical px-1.5 py-0.5 rounded bg-[#d1d9e6] text-[#2d3436] font-semibold border border-white/40">
            {badge}
          </span>
        )}
      </div>

      <div className="p-3 rounded-lg bg-[#14181f] text-white shadow-[inset_2px_2px_6px_rgba(0,0,0,0.8)] flex items-baseline justify-between">
        <div className="flex items-baseline gap-1">
          <span className={`text-2xl font-black font-technical tracking-tight ${trendColor}`}>
            {value}
          </span>
          {unit && (
            <span className="text-xs font-technical text-[#718096] uppercase font-bold">
              {unit}
            </span>
          )}
        </div>
        <div className="w-2 h-2 rounded-full bg-[#2ed573] glow-led-green opacity-80" />
      </div>

      {sublabel && (
        <span className="text-[10px] font-technical text-[#718096] mt-2 tracking-tight">
          {sublabel}
        </span>
      )}
    </div>
  )
}
