import React from 'react'
import { ShieldCheck } from 'lucide-react'

export interface OfficialBadgeProps {
  className?: string
  size?: 'sm' | 'md'
}

export const OfficialBadge: React.FC<OfficialBadgeProps> = ({
  className = '',
  size = 'sm',
}) => {
  const sizeClasses =
    size === 'sm'
      ? 'px-2 py-0.5 text-[10px] gap-1'
      : 'px-2.5 py-1 text-xs gap-1.5'

  const iconSizes = size === 'sm' ? 'w-3 h-3' : 'w-3.5 h-3.5'

  return (
    <span
      className={`inline-flex items-center font-bold rounded-full bg-[#C18C5D]/15 text-[#C18C5D] border border-[#C18C5D]/30 tracking-tight shrink-0 selection:bg-none ${sizeClasses} ${className}`}
      title="Verified Official Strategy created directly by AlgoAdda In-House Quant Labs"
    >
      <ShieldCheck className={`${iconSizes} text-[#C18C5D]`} />
      <span>Official</span>
    </span>
  )
}
