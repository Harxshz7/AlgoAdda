import React from 'react'

export interface LedIndicatorProps {
  status?: 'green' | 'orange' | 'amber' | 'neutral'
  label?: string
  sublabel?: string
  pulse?: boolean
  className?: string
}

export const LedIndicator: React.FC<LedIndicatorProps> = ({
  status = 'green',
  label = 'SYSTEM OPERATIONAL',
  sublabel,
  pulse = true,
  className = '',
}) => {
  const statusConfig = {
    green: {
      dot: 'bg-[#2ed573] glow-led-green',
      text: 'text-[#2b8a3e]',
    },
    orange: {
      dot: 'bg-[#ff4757] glow-led-orange',
      text: 'text-[#d63031]',
    },
    amber: {
      dot: 'bg-[#ffa502] glow-led-amber',
      text: 'text-[#d35400]',
    },
    neutral: {
      dot: 'bg-[#718096] shadow-[0_0_4px_rgba(0,0,0,0.2)]',
      text: 'text-[#4a5568]',
    },
  }[status]

  return (
    <div
      className={`inline-flex items-center gap-2.5 px-3 py-1.5 rounded-full bg-[#d1d9e6]/60 shadow-[inset_2px_2px_4px_#babecc,inset_-2px_-2px_4px_#ffffff] border border-white/50 ${className}`}
      role="status"
      aria-label={`${label} ${sublabel || ''}`}
    >
      <div className="relative flex items-center justify-center">
        <span
          className={`w-2.5 h-2.5 rounded-full transition-all duration-300 ${statusConfig.dot} ${
            pulse ? 'animate-pulse' : ''
          }`}
        />
      </div>

      <div className="flex flex-col leading-none">
        <span className="text-[11px] font-bold font-technical tracking-wider text-[#2d3436] uppercase">
          {label}
        </span>
        {sublabel && (
          <span className="text-[9px] font-technical tracking-tight text-[#718096] uppercase mt-0.5">
            {sublabel}
          </span>
        )}
      </div>
    </div>
  )
}
