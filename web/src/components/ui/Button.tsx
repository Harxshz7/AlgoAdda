import React from 'react'

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  icon?: React.ReactNode
  iconPosition?: 'left' | 'right'
  children: React.ReactNode
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  icon,
  iconPosition = 'left',
  children,
  className = '',
  disabled,
  ...props
}) => {
  // Sizing definitions ensuring minimum 48px touch height on mobile/default
  const sizeStyles = {
    sm: 'h-10 px-4 text-xs tracking-wider gap-1.5 rounded-md',
    md: 'min-h-[48px] px-6 py-3 text-xs md:text-sm tracking-wider gap-2 rounded-lg',
    lg: 'min-h-[56px] px-8 py-4 text-sm md:text-base tracking-widest gap-2.5 rounded-xl',
  }[size]

  // Variant definitions
  let variantStyles = ''
  if (variant === 'primary') {
    variantStyles = `
      bg-[#ff4757] text-white font-bold uppercase
      shadow-accent-btn
      border border-white/20
      hover:brightness-105
      active:translate-y-[2px] active:shadow-accent-btn-pressed
      focus-visible:ring-2 focus-visible:ring-[#ff4757] focus-visible:ring-offset-2 focus-visible:ring-offset-[#e0e5ec]
    `
  } else if (variant === 'secondary') {
    variantStyles = `
      bg-[#e0e5ec] text-[#2d3436] font-bold uppercase
      shadow-chassis-card
      hover:text-[#ff4757] hover:shadow-chassis-floating
      active:translate-y-[2px] active:shadow-chassis-pressed
      focus-visible:ring-2 focus-visible:ring-[#ff4757] focus-visible:ring-offset-2 focus-visible:ring-offset-[#e0e5ec]
    `
  } else if (variant === 'ghost') {
    variantStyles = `
      bg-transparent text-[#4a5568] font-semibold uppercase
      hover:bg-[#d1d9e6]/50 hover:text-[#2d3436] hover:shadow-chassis-recessed
      active:translate-y-[1px] active:shadow-chassis-pressed
      focus-visible:ring-2 focus-visible:ring-[#4a5568] focus-visible:ring-offset-2
    `
  }

  const disabledStyles = disabled
    ? 'opacity-50 cursor-not-allowed pointer-events-none'
    : 'cursor-pointer'

  return (
    <button
      disabled={disabled}
      className={`
        inline-flex items-center justify-center
        font-technical select-none
        transition-mechanical outline-none
        ${sizeStyles}
        ${variantStyles}
        ${disabledStyles}
        ${className}
      `.trim()}
      {...props}
    >
      {icon && iconPosition === 'left' && <span className="shrink-0">{icon}</span>}
      <span className="leading-none">{children}</span>
      {icon && iconPosition === 'right' && <span className="shrink-0">{icon}</span>}
    </button>
  )
}
