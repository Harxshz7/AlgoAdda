import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../lib/api'
import type { ListingSummary } from '../../lib/api'
import { Card, Button, OfficialBadge, RiskBadge, StarRating, FavoriteButton } from '../../components/ui'
import { Bookmark, User, ChevronRight, Store, ArrowLeft } from 'lucide-react'

export const WatchlistPage: React.FC = () => {
  const [favorites, setFavorites] = useState<ListingSummary[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchFavorites = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await api.getBuyerFavorites()
      setFavorites(data)
    } catch (err: any) {
      setError(err.message || 'Failed to load watchlist')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchFavorites()
  }, [])

  const handleFavoriteToggle = (botId: string, isFavorited: boolean) => {
    if (!isFavorited) {
      // Remove from list
      setFavorites((prev) => prev.filter((item) => item.botId !== botId))
    }
  }

  return (
    <div className="flex flex-col gap-8 max-w-7xl mx-auto">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-6 sm:p-8 rounded-2xl bg-[#FDFCF8] border border-[#DED8CF]">
        <div className="flex flex-col gap-1.5">
          <div className="flex items-center gap-2">
            <Link
              to="/buyer/dashboard"
              className="inline-flex items-center gap-1 text-xs font-semibold text-[#78786C] hover:text-[#2C2C24] transition-colors"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>Buyer Dashboard</span>
            </Link>
          </div>
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
              <Bookmark className="w-5 h-5 fill-[#5D7052]" />
            </div>
            <div>
              <h1 className="text-2xl sm:text-3xl font-heading font-extrabold text-[#2C2C24]">
                My Saved Strategies
              </h1>
              <p className="text-xs sm:text-sm font-body text-[#78786C]">
                Track and monitor interesting algorithmic trading bots from the marketplace.
              </p>
            </div>
          </div>
        </div>

        <Link to="/marketplace">
          <Button variant="primary" size="md" className="gap-2 shrink-0">
            <Store className="w-4 h-4" />
            <span>Browse Marketplace</span>
          </Button>
        </Link>
      </div>

      {/* Grid Content / Skeletons / Empty State */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((n) => (
            <Card key={n} className="p-6 flex flex-col justify-between gap-6 animate-pulse">
              <div className="flex justify-between items-center">
                <div className="h-6 bg-[#DED8CF]/40 rounded-full w-24" />
                <div className="h-6 bg-[#DED8CF]/40 rounded-md w-20" />
              </div>
              <div className="flex flex-col gap-3">
                <div className="h-6 bg-[#DED8CF]/40 rounded-md w-3/4" />
                <div className="h-4 bg-[#DED8CF]/30 rounded-md w-1/2" />
                <div className="h-16 bg-[#DED8CF]/20 rounded-xl mt-2" />
              </div>
              <div className="h-10 bg-[#DED8CF]/40 rounded-full w-full" />
            </Card>
          ))}
        </div>
      ) : error ? (
        <Card className="p-8 text-center text-sm text-[#A85448]">
          {error}
        </Card>
      ) : favorites.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {favorites.map((listing) => (
            <Card key={listing.botId} interactive className="p-6 flex flex-col justify-between gap-6">
              <div className="flex flex-col gap-4">
                {/* Card Header: Strategy Badge + Price + Watchlist Bookmark */}
                <div className="flex flex-wrap items-center justify-between gap-2 min-w-0">
                  <div className="flex flex-wrap items-center gap-1.5 min-w-0 max-w-full">
                    <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-[#5D7052]/10 text-[#5D7052] tracking-wide shrink-0">
                      {listing.strategyType || 'MOMENTUM'}
                    </span>
                    {(listing.official || listing.isOfficial) && <OfficialBadge />}
                    {listing.riskLabel && <RiskBadge riskLabel={listing.riskLabel} />}
                  </div>

                  <div className="flex items-center gap-2 shrink-0 ml-auto">
                    {listing.price !== null && listing.price !== undefined ? (
                      <div className="flex items-center gap-1 font-heading font-extrabold text-lg text-[#2C2C24]">
                        <span>₹{listing.price.toLocaleString()}</span>
                        <span className="text-[11px] font-normal text-[#78786C]">
                          / {listing.licenseType === 'TIMED' ? 'mo' : 'one-time'}
                        </span>
                      </div>
                    ) : null}
                    <FavoriteButton
                      botId={listing.botId}
                      isFavorited={true}
                      onToggle={(fav) => handleFavoriteToggle(listing.botId, fav)}
                      size="sm"
                    />
                  </div>
                </div>

                {/* Title & Seller */}
                <div className="flex flex-col gap-1">
                  <h3 className="font-heading font-bold text-xl text-[#2C2C24] line-clamp-1">
                    {listing.name}
                  </h3>
                  <div className="flex items-center justify-between gap-2 pt-0.5">
                    {listing.sellerId ? (
                      <Link
                        to={`/sellers/${listing.sellerId}`}
                        className="inline-flex items-center gap-1 text-xs text-[#78786C] hover:text-[#5D7052] transition-colors truncate"
                      >
                        <User className="w-3.5 h-3.5 shrink-0" />
                        <span className="truncate">by <strong className="text-[#2C2C24]">{listing.sellerDisplayName}</strong></span>
                      </Link>
                    ) : (
                      <span className="text-xs text-[#78786C]">by {listing.sellerDisplayName}</span>
                    )}

                    {listing.averageRating ? (
                      <StarRating rating={listing.averageRating} count={listing.reviewCount} size="xs" />
                    ) : (
                      <span className="text-[11px] text-[#78786C]/70 shrink-0">No reviews yet</span>
                    )}
                  </div>
                </div>

                {/* Description */}
                <p className="text-sm font-body text-[#78786C] line-clamp-2 leading-relaxed">
                  {listing.description}
                </p>

                {/* Backtest Metrics Box */}
                <div className="grid grid-cols-3 gap-2 p-3 rounded-2xl bg-[#FDFCF8] border border-[#DED8CF]/50 text-center">
                  <div>
                    <span className="text-[10px] font-semibold text-[#78786C] uppercase block">Win Rate</span>
                    <span className="text-sm font-bold text-[#5D7052]">
                      {listing.winRate !== null && listing.winRate !== undefined ? `${listing.winRate}%` : 'N/A'}
                    </span>
                  </div>

                  <div>
                    <span className="text-[10px] font-semibold text-[#78786C] uppercase block">Max DD</span>
                    <span className="text-sm font-bold text-[#A85448]">
                      {listing.maxDrawdown !== null && listing.maxDrawdown !== undefined ? `${listing.maxDrawdown}%` : 'N/A'}
                    </span>
                  </div>

                  <div>
                    <span className="text-[10px] font-semibold text-[#78786C] uppercase block">Sharpe</span>
                    <span className="text-sm font-bold text-[#2C2C24]">
                      {listing.sharpeRatio !== null && listing.sharpeRatio !== undefined ? listing.sharpeRatio : 'N/A'}
                    </span>
                  </div>
                </div>
              </div>

              {/* Action CTA */}
              {listing.listingId ? (
                <Link to={`/marketplace/${listing.listingId}`} className="w-full">
                  <Button variant="primary" size="md" className="w-full justify-center gap-2">
                    <span>Inspect Strategy Details</span>
                    <ChevronRight className="w-4 h-4" />
                  </Button>
                </Link>
              ) : (
                <Link to="/marketplace" className="w-full">
                  <Button variant="secondary" size="md" className="w-full justify-center gap-2">
                    <span>Find in Marketplace</span>
                    <ChevronRight className="w-4 h-4" />
                  </Button>
                </Link>
              )}
            </Card>
          ))}
        </div>
      ) : (
        /* Empty State */
        <Card className="p-12 text-center flex flex-col items-center justify-center gap-4 max-w-lg mx-auto">
          <div className="w-14 h-14 rounded-2xl bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
            <Bookmark className="w-7 h-7" />
          </div>
          <div className="flex flex-col gap-1">
            <h3 className="font-heading font-bold text-xl text-[#2C2C24]">
              No saved strategies yet
            </h3>
            <p className="text-sm text-[#78786C] font-body leading-relaxed">
              Explore the marketplace to discover verified quantitative trading algorithms and save them to your watchlist.
            </p>
          </div>
          <Link to="/marketplace" className="mt-2">
            <Button variant="primary" size="md" className="gap-2">
              <Store className="w-4 h-4" />
              <span>Browse Marketplace</span>
            </Button>
          </Link>
        </Card>
      )}
    </div>
  )
}
