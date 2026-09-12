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
    positive: 'text-[#5D7052]',
    negative: 'text-[#A85448]',
    neutral:  'text-[#2C2C24]',
  }[trend]

  return (
    <div
      className={`
        p-4 rounded-2xl bg-[#FEFEFA]
        border border-[#DED8CF]/50
        shadow-[0_4px_20px_-2px_rgba(93,112,82,0.12)]
        flex flex-col gap-2
        ${className}
      `}
    >
      {/* Label row */}
      <div className="flex items-center justify-between gap-2">
        <span className="text-xs font-semibold font-body text-[#78786C] uppercase tracking-wide">
          {label}
        </span>
        {badge && (
          <span className="text-[10px] font-body px-1.5 py-0.5 rounded-full bg-[#E6DCCD] text-[#4A4A40] font-semibold">
            {badge}
          </span>
        )}
      </div>

      {/* Value display */}
      <div className="flex items-baseline gap-1">
        <span className={`text-2xl font-heading font-bold leading-none ${trendColor}`}>
          {value}
        </span>
        {unit && (
          <span className="text-xs font-body text-[#78786C] font-semibold uppercase">
            {unit}
          </span>
        )}
      </div>

      {sublabel && (
        <span className="text-[11px] font-body text-[#78786C] leading-tight">
          {sublabel}
        </span>
      )}
    </div>
  )
}
