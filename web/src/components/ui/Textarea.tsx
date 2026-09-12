import React from 'react'

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string
  helperText?: string
  errorText?: string
  complianceBadge?: boolean
}

export const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(({
  label,
  helperText,
  errorText,
  complianceBadge = false,
  className = '',
  disabled,
  id,
  ...props
}, ref) => {
  const generatedId = React.useId()
  const textareaId = id || generatedId

  return (
    <div className="w-full flex flex-col gap-2">
      <div className="flex items-center justify-between">
        {label && (
          <label
            htmlFor={textareaId}
            className="text-xs font-bold uppercase tracking-wider text-[#4a5568] font-technical flex items-center gap-2"
          >
            <span>{label}</span>
          </label>
        )}
        {complianceBadge ? (
          <span className="text-[10px] font-technical px-2 py-0.5 rounded bg-[#ff4757]/10 text-[#ff4757] font-semibold border border-[#ff4757]/30 tracking-tight">
            [MANDATORY WHITE-BOX COMPLIANCE]
          </span>
        ) : (
          <span className="text-[10px] text-[#718096] font-technical">[LOGIC_BUFFER]</span>
        )}
      </div>

      <textarea
        ref={ref}
        id={textareaId}
        disabled={disabled}
        className={`
          w-full min-h-[120px] rounded-lg p-4
          bg-[#d9e0ea] text-[#2d3436] font-technical text-sm leading-relaxed
          shadow-chassis-recessed border-none outline-none resize-y
          placeholder:text-[#718096]/60 placeholder:font-normal
          transition-mechanical
          focus:ring-2 focus:ring-[#ff4757]/70 focus:bg-[#d4dce7]
          disabled:opacity-50 disabled:cursor-not-allowed
          ${errorText ? 'ring-2 ring-[#ff4757]' : ''}
          ${className}
        `.trim()}
        {...props}
      />

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

Textarea.displayName = 'Textarea'
