import React from 'react'

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  elevated?: boolean
  interactive?: boolean
  children: React.ReactNode
}

export const Card: React.FC<CardProps> = ({
  elevated = false,
  interactive = false,
  children,
  className = '',
  ...props
}) => {
  const shadowClass = elevated
    ? 'shadow-[0_10px_40px_-10px_rgba(193,140,93,0.2)]'
    : 'shadow-[0_4px_20px_-2px_rgba(93,112,82,0.15)]'

  const hoverEffect = interactive
    ? 'hover:-translate-y-1 hover:shadow-[0_20px_40px_-10px_rgba(93,112,82,0.15)] transition-all duration-300 cursor-pointer'
    : ''

  return (
    <div
      className={`
        relative bg-[#FEFEFA] text-[#2C2C24]
        rounded-[2rem] p-6 md:p-8
        border border-[#DED8CF]/50
        ${shadowClass}
        ${hoverEffect}
        ${className}
      `.trim()}
      {...props}
    >
      {children}
    </div>
  )
}
