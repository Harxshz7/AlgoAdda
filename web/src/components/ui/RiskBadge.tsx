import React from 'react'
import { Activity, ShieldAlert, ShieldCheck } from 'lucide-react'

export interface RiskBadgeProps {
  riskLabel?: 'CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE' | string | null
  className?: string
  size?: 'sm' | 'md'
}

export const RiskBadge: React.FC<RiskBadgeProps> = ({
  riskLabel,
  className = '',
  size = 'sm',
}) => {
  if (!riskLabel) return null

  const labelUpper = riskLabel.toUpperCase()

  let colorClasses = 'bg-[#C18C5D]/15 text-[#C18C5D] border-[#C18C5D]/30'
  let Icon = Activity

  if (labelUpper === 'CONSERVATIVE') {
    colorClasses = 'bg-[#5D7052]/15 text-[#5D7052] border-[#5D7052]/30'
    Icon = ShieldCheck
  } else if (labelUpper === 'MODERATE') {
    colorClasses = 'bg-[#C18C5D]/15 text-[#C18C5D] border-[#C18C5D]/30'
    Icon = Activity
  } else if (labelUpper === 'AGGRESSIVE') {
    colorClasses = 'bg-[#A85448]/15 text-[#A85448] border-[#A85448]/30'
    Icon = ShieldAlert
  }

  const sizeClasses =
    size === 'sm'
      ? 'px-2 py-0.5 text-[10px] gap-1'
      : 'px-2.5 py-1 text-xs gap-1.5'

  const iconSizes = size === 'sm' ? 'w-3 h-3' : 'w-3.5 h-3.5'

  return (
    <span
      className={`inline-flex items-center font-bold rounded-full border tracking-tight shrink-0 selection:bg-none ${colorClasses} ${sizeClasses} ${className}`}
      title={`Derived Risk Rating: ${labelUpper}`}
    >
      <Icon className={iconSizes} />
      <span>{labelUpper}</span>
    </span>
  )
}
