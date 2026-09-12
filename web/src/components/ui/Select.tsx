import React from 'react'
import { ChevronDown } from 'lucide-react'

export interface SelectOption {
  value: string
  label: string
}

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string
  helperText?: string
  errorText?: string
  options: SelectOption[]
  prefixIcon?: React.ReactNode
}

export const Select = React.forwardRef<HTMLSelectElement, SelectProps>(({
  label,
  helperText,
  errorText,
  options,
  prefixIcon,
  className = '',
  disabled,
  id,
  ...props
}, ref) => {
  const generatedId = React.useId()
  const selectId = id || generatedId

  return (
    <div className="w-full flex flex-col gap-2">
      {label && (
        <label
          htmlFor={selectId}
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

        <select
          ref={ref}
          id={selectId}
          disabled={disabled}
          className={`
            w-full h-12 rounded-full appearance-none
            bg-white/60 text-[#2C2C24] font-body text-sm
            border border-[#DED8CF]
            transition-all duration-300 cursor-pointer
            focus:outline-none focus-visible:ring-2 focus-visible:ring-[#5D7052]/30 focus-visible:ring-offset-2 focus-visible:border-[#5D7052]
            disabled:opacity-50 disabled:cursor-not-allowed
            ${prefixIcon ? 'pl-11 pr-10' : 'pl-5 pr-10'}
            ${errorText ? 'border-[#A85448] ring-2 ring-[#A85448]/20' : ''}
            ${className}
          `.trim()}
          {...props}
        >
          {options.map((opt) => (
            <option key={opt.value} value={opt.value} className="bg-white text-[#2C2C24]">
              {opt.label}
            </option>
          ))}
        </select>

        <div className="absolute right-4 flex items-center pointer-events-none text-[#78786C]">
          <ChevronDown className="w-4 h-4" />
        </div>
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

Select.displayName = 'Select'
