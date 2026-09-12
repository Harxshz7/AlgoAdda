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
          className="text-xs font-bold uppercase tracking-wider text-[#4a5568] font-technical flex items-center justify-between"
        >
          <span>{label}</span>
          <span className="text-[10px] text-[#718096]">[DATA_SLOT]</span>
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
            w-full min-h-[56px] rounded-lg
            bg-[#d9e0ea] text-[#2d3436] font-technical text-sm
            shadow-chassis-recessed border-none outline-none
            placeholder:text-[#718096]/60 placeholder:font-normal
            transition-mechanical
            focus:ring-2 focus:ring-[#ff4757]/70 focus:bg-[#d4dce7]
            disabled:opacity-50 disabled:cursor-not-allowed
            ${prefixIcon ? 'pl-12 pr-5' : 'px-5'}
            ${errorText ? 'ring-2 ring-[#ff4757]' : ''}
            ${className}
          `.trim()}
          {...props}
        />
      </div>

      {errorText ? (
        <p className="text-xs font-technical text-[#ff4757] font-semibold flex items-center gap-1">
          <span className="w-1.5 h-1.5 rounded-full bg-[#ff4757] inline-block" />
          {errorText}
        </p>
      ) : helperText ? (
        <p className="text-xs font-technical text-[#718096]">{helperText}</p>
      ) : null}
    </div>
  )
})

Input.displayName = 'Input'
