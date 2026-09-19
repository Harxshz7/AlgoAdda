import React, { useEffect, useState } from 'react'
import { api } from '../../lib/api'
import type { BotReviewsSummaryResponse } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { Card, Button, Textarea, StarRating } from './index'
import { MessageSquare, ShieldCheck, Trash2, Edit3, CheckCircle2, AlertCircle } from 'lucide-react'

export interface ReviewsSectionProps {
  botId: string
  className?: string
}

export const ReviewsSection: React.FC<ReviewsSectionProps> = ({ botId, className = '' }) => {
  const { user } = useAuth()
  const [summary, setSummary] = useState<BotReviewsSummaryResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Form state
  const [rating, setRating] = useState<number>(5)
  const [comment, setComment] = useState<string>('')
  const [isEditing, setIsEditing] = useState<boolean>(false)
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false)
  const [submitSuccess, setSubmitSuccess] = useState<boolean>(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const fetchReviews = async () => {
    if (!botId) return
    setIsLoading(true)
    setError(null)
    try {
      const data = await api.getBotReviews(botId)
      setSummary(data)
      if (data.userReview) {
        setRating(data.userReview.rating)
        setComment(data.userReview.comment || '')
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load reviews')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchReviews()
  }, [botId])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!rating || rating < 1 || rating > 5) {
      setSubmitError('Please select a star rating between 1 and 5.')
      return
    }

    setIsSubmitting(true)
    setSubmitError(null)
    setSubmitSuccess(false)

    try {
      if (summary?.userReview) {
        await api.updateBotReview(botId, { rating, comment: comment.trim() || undefined })
      } else {
        await api.createBotReview(botId, { rating, comment: comment.trim() || undefined })
      }
      setSubmitSuccess(true)
      setIsEditing(false)
      await fetchReviews()
    } catch (err: any) {
      setSubmitError(err.message || 'Failed to submit review')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDelete = async (reviewId: string) => {
    if (!window.confirm('Are you sure you want to delete this review?')) return
    try {
      await api.deleteBotReviewAdmin(reviewId)
      await fetchReviews()
    } catch (err: any) {
      alert(err.message || 'Failed to delete review')
    }
  }

  const isVerified = summary?.isVerifiedBuyer || summary?.verifiedBuyer
  const hasUserReviewed = !!summary?.userReview

  return (
    <Card className={`p-6 flex flex-col gap-6 ${className}`}>
      {/* Header with Average Rating & Count */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[#DED8CF]/40 pb-4">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-amber-500/10 flex items-center justify-center text-amber-600 dark:text-amber-400">
            <MessageSquare className="w-5 h-5" />
          </div>
          <div className="flex flex-col">
            <h3 className="font-heading font-bold text-lg text-[#2C2C24] dark:text-[#F3F1EC]">
              Verified Buyer Ratings & Reviews
            </h3>
            <span className="text-xs text-[#78786C] dark:text-[#A0A090]">
              Real feedback from traders with active licenses for this strategy
            </span>
          </div>
        </div>

        {summary && summary.reviewCount > 0 ? (
          <div className="flex items-center gap-2 bg-[#FDFCF8] dark:bg-[#1A1A14] px-3.5 py-1.5 rounded-xl border border-[#DED8CF]/60 self-start sm:self-auto">
            <StarRating rating={summary.averageRating} size="md" showCount={false} />
            <span className="text-xs text-[#78786C] dark:text-[#A0A090]">
              based on {summary.reviewCount} {summary.reviewCount === 1 ? 'review' : 'reviews'}
            </span>
          </div>
        ) : (
          <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-[#5D7052]/10 text-[#5D7052]">
            VERIFIED REVIEWS
          </span>
        )}
      </div>

      {/* Verified Buyer Review Form */}
      {user && user.role === 'BUYER' && isVerified && (
        <div className="p-4 rounded-xl bg-[#FDFCF8] dark:bg-[#1A1A14] border border-[#DED8CF]/80 flex flex-col gap-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4 text-[#5D7052]" />
              <span className="text-xs font-bold text-[#5D7052] uppercase tracking-wide">
                Verified Buyer Feedback
              </span>
            </div>
            {hasUserReviewed && !isEditing && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setIsEditing(true)}
                className="text-xs gap-1 py-1 h-auto"
              >
                <Edit3 className="w-3.5 h-3.5" />
                <span>Edit Your Review</span>
              </Button>
            )}
          </div>

          {submitSuccess && !isEditing && (
            <div className="flex items-center gap-2 text-xs text-emerald-700 dark:text-emerald-400 bg-emerald-500/10 p-2.5 rounded-lg border border-emerald-500/20">
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <span>Your review has been successfully published!</span>
            </div>
          )}

          {(!hasUserReviewed || isEditing) ? (
            <form onSubmit={handleSubmit} className="flex flex-col gap-3 pt-1">
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-[#2C2C24] dark:text-[#F3F1EC]">
                  Your Strategy Rating
                </label>
                <div className="flex items-center gap-2">
                  <StarRating
                    rating={rating}
                    size="lg"
                    interactive
                    onChange={(r) => setRating(r)}
                    showCount={false}
                    showValue={false}
                  />
                  <span className="text-xs font-bold text-amber-600 dark:text-amber-400 ml-1">
                    {rating} / 5 Stars
                  </span>
                </div>
              </div>

              <div className="flex flex-col gap-1">
                <label className="text-xs font-semibold text-[#2C2C24] dark:text-[#F3F1EC]">
                  Strategy Experience & Observations (Optional)
                </label>
                <Textarea
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Share details on performance, Sharpe ratio stability, drawdown experience..."
                  rows={3}
                  className="text-xs font-body"
                />
              </div>

              {submitError && (
                <div className="flex items-center gap-1.5 text-xs text-rose-600 bg-rose-500/10 p-2 rounded-lg">
                  <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                  <span>{submitError}</span>
                </div>
              )}

              <div className="flex items-center gap-2 pt-1">
                <Button
                  type="submit"
                  variant="primary"
                  size="sm"
                  disabled={isSubmitting}
                  className="gap-1.5"
                >
                  <span>{isSubmitting ? 'Saving...' : hasUserReviewed ? 'Update Review' : 'Submit Review'}</span>
                </Button>
                {isEditing && (
                  <Button
                    type="button"
                    variant="secondary"
                    size="sm"
                    onClick={() => {
                      setIsEditing(false)
                      if (summary?.userReview) {
                        setRating(summary.userReview.rating)
                        setComment(summary.userReview.comment || '')
                      }
                    }}
                  >
                    Cancel
                  </Button>
                )}
              </div>
            </form>
          ) : (
            <div className="flex flex-col gap-1.5 pt-1">
              <div className="flex items-center gap-2">
                <StarRating rating={summary?.userReview?.rating} size="sm" showCount={false} />
                <span className="text-xs text-[#78786C]">
                  Reviewed on {new Date(summary?.userReview?.createdAt || '').toLocaleDateString()}
                </span>
              </div>
              {summary?.userReview?.comment && (
                <p className="text-xs font-body text-[#2C2C24] dark:text-[#F3F1EC] leading-relaxed italic">
                  "{summary.userReview.comment}"
                </p>
              )}
            </div>
          )}
        </div>
      )}

      {/* Reviews List */}
      <div className="flex flex-col gap-3">
        {isLoading ? (
          <div className="py-8 text-center text-xs text-[#78786C] animate-pulse">
            Loading verified reviews...
          </div>
        ) : error ? (
          <div className="py-4 text-center text-xs text-rose-600">
            {error}
          </div>
        ) : summary && summary.reviews.length > 0 ? (
          <div className="flex flex-col divide-y divide-[#DED8CF]/30">
            {summary.reviews.map((rev) => (
              <div key={rev.id} className="py-3.5 first:pt-0 last:pb-0 flex flex-col gap-1.5">
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-[#2C2C24] dark:text-[#F3F1EC]">
                      {rev.buyerDisplayName}
                    </span>
                    <span className="inline-flex items-center gap-0.5 text-[10px] font-semibold text-emerald-700 dark:text-emerald-400 bg-emerald-500/10 px-1.5 py-0.5 rounded-full">
                      <ShieldCheck className="w-2.5 h-2.5" />
                      Verified Owner
                    </span>
                  </div>

                  <div className="flex items-center gap-3">
                    <span className="text-[11px] text-[#78786C]">
                      {new Date(rev.createdAt).toLocaleDateString()}
                    </span>
                    {user && user.role === 'ADMIN' && (
                      <button
                        onClick={() => handleDelete(rev.id)}
                        className="text-gray-400 hover:text-rose-600 p-1 rounded transition-colors"
                        title="Delete review (Admin Moderation)"
                        aria-label="Delete review"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <StarRating rating={rev.rating} size="xs" showCount={false} showValue={false} />
                </div>

                {rev.comment && (
                  <p className="text-xs font-body text-[#2C2C24]/90 dark:text-[#F3F1EC]/90 leading-relaxed pt-0.5">
                    {rev.comment}
                  </p>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="py-6 text-center flex flex-col items-center gap-1.5 text-[#78786C]">
            <span className="text-xs font-medium">No reviews yet for this bot.</span>
            <span className="text-[11px] text-[#78786C]/70">
              Verified buyers who own a valid license can leave the first review above.
            </span>
          </div>
        )}
      </div>
    </Card>
  )
}
