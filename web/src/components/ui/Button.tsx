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
  // Sizing — h-12 default meets 48px minimum touch target
  const sizeStyles = {
    sm: 'h-11 sm:h-10 px-4 sm:px-5 text-sm gap-1.5',
    md: 'h-12 px-8 text-sm gap-2',
    lg: 'h-14 px-10 text-base gap-2.5',
  }[size]

  let variantStyles = ''
  if (variant === 'primary') {
    variantStyles = `
      bg-[#5D7052] text-[#F3F4F1] font-bold
      shadow-[0_4px_20px_-2px_rgba(93,112,82,0.15)]
      hover:bg-[#4e6045] hover:shadow-[0_6px_24px_-4px_rgba(93,112,82,0.25)]
      focus-visible:ring-2 focus-visible:ring-[#5D7052] focus-visible:ring-offset-2 focus-visible:ring-offset-[#FDFCF8]
    `
  } else if (variant === 'secondary') {
    variantStyles = `
      bg-transparent text-[#C18C5D] font-bold
      border-2 border-[#C18C5D]
      hover:bg-[#C18C5D]/10
      focus-visible:ring-2 focus-visible:ring-[#C18C5D] focus-visible:ring-offset-2 focus-visible:ring-offset-[#FDFCF8]
    `
  } else if (variant === 'ghost') {
    variantStyles = `
      bg-transparent text-[#5D7052] font-semibold
      hover:bg-[#5D7052]/10
      focus-visible:ring-2 focus-visible:ring-[#5D7052] focus-visible:ring-offset-2
    `
  }

  const disabledStyles = disabled
    ? 'opacity-50 cursor-not-allowed pointer-events-none'
    : 'cursor-pointer hover:scale-105 active:scale-95'

  return (
    <button
      disabled={disabled}
      className={`
        inline-flex items-center justify-center
        font-body rounded-full select-none
        transition-all duration-300 outline-none
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
