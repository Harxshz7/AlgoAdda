import React from 'react'

// StatusBadge replaces the old LedIndicator.
// Maps semantic status values to organic pill badges — no LED glow.
export interface StatusBadgeProps {
  status?: 'green' | 'orange' | 'amber' | 'neutral'
  label?: string
  sublabel?: string
  pulse?: boolean   // kept for API compat — drives a gentle opacity animation on pending states
  className?: string
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status = 'neutral',
  label = 'Active',
  sublabel,
  pulse = false,
  className = '',
}) => {
  const config = {
    green: {
      pill: 'status-published',
      dot: 'bg-[#5D7052]',
    },
    orange: {
      // orange = rejected/failed in organic palette → burnt sienna
      pill: 'status-rejected',
      dot: 'bg-[#A85448]',
    },
    amber: {
      // amber = pending/in-review → terracotta/sand
      pill: 'status-pending',
      dot: 'bg-[#C18C5D]',
    },
    neutral: {
      pill: 'status-draft',
      dot: 'bg-[#78786C]',
    },
  }[status]

  return (
    <div
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold font-body ${config.pill} ${className}`}
      role="status"
      aria-label={`${label}${sublabel ? ` — ${sublabel}` : ''}`}
    >
      <span
        className={`w-1.5 h-1.5 rounded-full shrink-0 ${config.dot} ${pulse ? 'animate-pulse' : ''}`}
      />
      <span>{label}</span>
      {sublabel && (
        <span className="opacity-70 text-[10px]">· {sublabel}</span>
      )}
    </div>
  )
}

// Backward-compatible alias — all existing imports of LedIndicator continue to work unchanged
export const LedIndicator = StatusBadge
export type LedIndicatorProps = StatusBadgeProps
