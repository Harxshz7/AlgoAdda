import React from 'react'

interface LogoIconProps {
  className?: string
  size?: number
}

export const LogoIcon: React.FC<LogoIconProps> = ({ className = 'w-9 h-9', size }) => {
  const style = size ? { width: `${size}px`, height: `${size}px` } : undefined

  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 32 32"
      fill="none"
      className={`shrink-0 ${className}`}
      style={style}
    >
      <rect width="32" height="32" rx="8" fill="#5D7052" />
      <line x1="16" y1="5" x2="16" y2="27" stroke="#C18C5D" strokeWidth="2" strokeLinecap="round" opacity={0.75} />
      <path d="M 7 23 L 16 8 L 25 23" stroke="#FDFCF8" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
      <rect x="11.5" y="15" width="9" height="4.5" rx="1.5" fill="#C18C5D" />
    </svg>
  )
}
