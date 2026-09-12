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
      <div className="flex items-center justify-between gap-3">
        {label && (
          <label
            htmlFor={textareaId}
            className="text-sm font-semibold text-[#4A4A40] font-body"
          >
            {label}
          </label>
        )}
        {complianceBadge && (
          <span className="inline-flex items-center gap-1 text-xs font-semibold font-body px-2.5 py-1 rounded-full bg-[#C18C5D]/12 text-[#7a5530] border border-[#C18C5D]/35 shrink-0">
            Mandatory · White-Box
          </span>
        )}
      </div>

      <textarea
        ref={ref}
        id={textareaId}
        disabled={disabled}
        className={`
          w-full min-h-[120px] rounded-2xl p-4
          bg-white/60 text-[#2C2C24] font-body text-sm leading-relaxed
          border border-[#DED8CF]
          placeholder:text-[#78786C]/60
          transition-all duration-300 resize-y
          focus:outline-none focus-visible:ring-2 focus-visible:ring-[#5D7052]/30 focus-visible:ring-offset-2 focus-visible:border-[#5D7052]
          disabled:opacity-50 disabled:cursor-not-allowed
          ${errorText ? 'border-[#A85448] ring-2 ring-[#A85448]/20' : ''}
          ${className}
        `.trim()}
        {...props}
      />

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

Textarea.displayName = 'Textarea'
