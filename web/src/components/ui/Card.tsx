import React from 'react'

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  elevated?: boolean
  withScrews?: boolean
  withVents?: boolean
  interactive?: boolean
  children: React.ReactNode
}

export const Card: React.FC<CardProps> = ({
  elevated = false,
  withScrews = true,
  withVents = false,
  interactive = false,
  children,
  className = '',
  ...props
}) => {
  const baseElevation = elevated ? 'shadow-chassis-floating' : 'shadow-chassis-card'
  const hoverEffect = interactive
    ? 'hover:-translate-y-1 hover:shadow-chassis-floating transition-mechanical cursor-pointer'
    : ''

  return (
    <div
      className={`
        relative bg-[#e0e5ec] text-[#2d3436]
        rounded-2xl p-6 md:p-8
        border border-white/60
        ${baseElevation}
        ${hoverEffect}
        ${className}
      `.trim()}
      {...props}
    >
      {/* Precision Corner Screws */}
      {withScrews && (
        <>
          <div className="absolute top-3 left-3 screw-accent" aria-hidden="true" />
          <div className="absolute top-3 right-3 screw-accent" aria-hidden="true" />
          <div className="absolute bottom-3 left-3 screw-accent" aria-hidden="true" />
          <div className="absolute bottom-3 right-3 screw-accent" aria-hidden="true" />
        </>
      )}

      {/* Recessed Mechanical Vent Slots */}
      {withVents && (
        <div className="absolute top-4 right-10 flex gap-1.5" aria-hidden="true">
          <div className="h-5 w-1 rounded-full bg-[#d1d9e6] shadow-[inset_1px_1px_2px_rgba(0,0,0,0.25),inset_-1px_-1px_1px_rgba(255,255,255,0.8)]" />
          <div className="h-5 w-1 rounded-full bg-[#d1d9e6] shadow-[inset_1px_1px_2px_rgba(0,0,0,0.25),inset_-1px_-1px_1px_rgba(255,255,255,0.8)]" />
          <div className="h-5 w-1 rounded-full bg-[#d1d9e6] shadow-[inset_1px_1px_2px_rgba(0,0,0,0.25),inset_-1px_-1px_1px_rgba(255,255,255,0.8)]" />
        </div>
      )}

      {children}
    </div>
  )
}
