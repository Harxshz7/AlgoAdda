import React, { useState } from 'react'
import { Star } from 'lucide-react'

export interface StarRatingProps {
  rating?: number | null
  count?: number | null
  size?: 'xs' | 'sm' | 'md' | 'lg'
  interactive?: boolean
  onChange?: (rating: number) => void
  showCount?: boolean
  showValue?: boolean
  className?: string
}

export const StarRating: React.FC<StarRatingProps> = ({
  rating = 0,
  count,
  size = 'sm',
  interactive = false,
  onChange,
  showCount = true,
  showValue = true,
  className = '',
}) => {
  const [hoverRating, setHoverRating] = useState<number | null>(null)
  const currentRating = interactive ? (hoverRating ?? rating ?? 0) : (rating ?? 0)

  const sizeMap = {
    xs: { star: 'w-3 h-3', text: 'text-[11px]', gap: 'gap-0.5' },
    sm: { star: 'w-3.5 h-3.5', text: 'text-xs', gap: 'gap-1' },
    md: { star: 'w-4 h-4', text: 'text-sm', gap: 'gap-1.5' },
    lg: { star: 'w-6 h-6', text: 'text-base', gap: 'gap-2' },
  }

  const { star: starSize, text: textSize, gap } = sizeMap[size]

  return (
    <div className={`inline-flex items-center ${gap} ${className}`}>
      <div className="flex items-center space-x-0.5">
        {[1, 2, 3, 4, 5].map((starValue) => {
          const isFilled = starValue <= Math.round(currentRating)
          const isInteractive = interactive && !!onChange

          return (
            <button
              key={starValue}
              type="button"
              disabled={!isInteractive}
              onClick={() => isInteractive && onChange?.(starValue)}
              onMouseEnter={() => isInteractive && setHoverRating(starValue)}
              onMouseLeave={() => isInteractive && setHoverRating(null)}
              className={`${
                isInteractive
                  ? 'cursor-pointer hover:scale-110 focus:outline-none transition-transform'
                  : 'cursor-default'
              } p-0.5`}
              aria-label={isInteractive ? `Rate ${starValue} stars` : undefined}
            >
              <Star
                className={`${starSize} ${
                  isFilled
                    ? 'fill-amber-400 text-amber-400'
                    : 'fill-transparent text-gray-400 dark:text-gray-600'
                } transition-colors`}
              />
            </button>
          )
        })}
      </div>

      {!interactive && rating != null && rating > 0 && showValue && (
        <span className={`font-semibold text-gray-800 dark:text-gray-200 ${textSize}`}>
          {rating.toFixed(1)}
        </span>
      )}

      {!interactive && count != null && count > 0 && showCount && (
        <span className={`text-gray-500 dark:text-gray-400 ${textSize}`}>
          ({count})
        </span>
      )}
    </div>
  )
}
