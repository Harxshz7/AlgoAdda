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
          className="text-xs font-bold uppercase tracking-wider text-[#4a5568] font-technical flex items-center justify-between"
        >
          <span>{label}</span>
          <span className="text-[10px] text-[#718096]">[SELECTOR_SLOT]</span>
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
            w-full min-h-[56px] rounded-lg appearance-none
            bg-[#d9e0ea] text-[#2d3436] font-technical text-sm
            shadow-chassis-recessed border-none outline-none
            transition-mechanical cursor-pointer
            focus:ring-2 focus:ring-[#ff4757]/70 focus:bg-[#d4dce7]
            disabled:opacity-50 disabled:cursor-not-allowed
            ${prefixIcon ? 'pl-12 pr-10' : 'pl-5 pr-10'}
            ${errorText ? 'ring-2 ring-[#ff4757]' : ''}
            ${className}
          `.trim()}
          {...props}
        >
          {options.map((opt) => (
            <option key={opt.value} value={opt.value} className="bg-[#e0e5ec] text-[#2d3436]">
              {opt.label}
            </option>
          ))}
        </select>

        <div className="absolute right-4 flex items-center pointer-events-none text-[#718096]">
          <ChevronDown className="w-4 h-4" />
        </div>
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

Select.displayName = 'Select'
