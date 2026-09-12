import React from 'react'

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string
  helperText?: string
  errorText?: string
  prefixIcon?: React.ReactNode
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(({
  label,
  helperText,
  errorText,
  prefixIcon,
  className = '',
  disabled,
  id,
  ...props
}, ref) => {
  const generatedId = React.useId()
  const inputId = id || generatedId

  return (
    <div className="w-full flex flex-col gap-2">
      {label && (
        <label
          htmlFor={inputId}
          className="text-sm font-semibold text-[#4A4A40] font-body"
        >
          {label}
        </label>
      )}

      <div className="relative w-full flex items-center">
        {prefixIcon && (
          <div className="absolute left-4 flex items-center pointer-events-none text-[#718096]">
            {prefixIcon}
          </div>
        )}

        <input
          ref={ref}
          id={inputId}
          disabled={disabled}
          className={`
            w-full h-12 rounded-full
            bg-white/60 text-[#2C2C24] font-body text-sm
            border border-[#DED8CF]
            placeholder:text-[#78786C]/60
            transition-all duration-300
            focus:outline-none focus-visible:ring-2 focus-visible:ring-[#5D7052]/30 focus-visible:ring-offset-2 focus-visible:border-[#5D7052]
            disabled:opacity-50 disabled:cursor-not-allowed
            ${prefixIcon ? 'pl-11 pr-5' : 'px-5'}
            ${errorText ? 'border-[#A85448] ring-2 ring-[#A85448]/20' : ''}
            ${className}
          `.trim()}
          {...props}
        />
      </div>

      {errorText ? (
        <p className="text-xs font-body text-[#A85448] font-semibold flex items-center gap-1">
          <span className="w-1.5 h-1.5 rounded-full bg-[#A85448] inline-block" />
          {errorText}
        </p>
      ) : helperText ? (
        <p className="text-xs font-body text-[#78786C]">{helperText}</p>
      ) : null}
    </div>
  )
})

Input.displayName = 'Input'
