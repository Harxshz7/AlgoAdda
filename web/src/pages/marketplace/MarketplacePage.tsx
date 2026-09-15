import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../lib/api'
import type { ListingSummary, PageResponse } from '../../lib/api'
import { Card, Button, Input, Select, OfficialBadge } from '../../components/ui'
import {
  Search,
  Filter,
  ShieldCheck,
  User,
  SlidersHorizontal,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
  AlertCircle,
} from 'lucide-react'

export const MarketplacePage: React.FC = () => {
  const [listingsPage, setListingsPage] = useState<PageResponse<ListingSummary> | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Filter & Query state
  const [q, setQ] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [strategyType, setStrategyType] = useState<string>('')
  const [minPrice, setMinPrice] = useState<string>('')
  const [maxPrice, setMaxPrice] = useState<string>('')
  const [sortBy, setSortBy] = useState<string>('newest')
  const [page, setPage] = useState<number>(0)

  const fetchListings = async () => {
    setIsLoading(true)
    setError(null)

    try {
      const data = await api.getListings({
        q: q || undefined,
        strategyType: strategyType || undefined,
        minPrice: minPrice ? parseFloat(minPrice) : undefined,
        maxPrice: maxPrice ? parseFloat(maxPrice) : undefined,
        sortBy: sortBy || undefined,
        page,
        size: 9,
      })
      setListingsPage(data)
    } catch (err: any) {
      setError(err.message || 'Failed to fetch marketplace listings.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchListings()
  }, [q, strategyType, minPrice, maxPrice, sortBy, page])

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setQ(searchInput.trim())
    setPage(0)
  }

  const handleClearFilters = () => {
    setQ('')
    setSearchInput('')
    setStrategyType('')
    setMinPrice('')
    setMaxPrice('')
    setSortBy('newest')
    setPage(0)
  }

  return (
    <div className="flex flex-col gap-8">
      {/* Hero Header */}
      <div className="flex flex-col gap-4 text-center sm:text-left max-w-3xl">
        <div className="inline-flex items-center gap-2 self-start px-3 py-1 rounded-full bg-[#5D7052]/10 text-[#5D7052] text-xs font-semibold">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>White-Box Strategy Store</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-heading font-extrabold text-[#2C2C24] tracking-tight">
          Verified Algorithmic Marketplace
        </h1>
        <p className="text-base text-[#78786C] font-body leading-relaxed">
          Browse backtested, compliance-verified trading algorithms with fully disclosed strategy logic.
          Evaluate transparent performance metrics before investing.
        </p>
      </div>

      {/* Search & Filter Bar */}
      <Card className="p-6">
        <form onSubmit={handleSearchSubmit} className="flex flex-col gap-5">
          {/* Main Search Row */}
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#78786C]" />
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="Search strategies by name, description, keywords..."
                className="w-full pl-10 pr-4 py-2.5 rounded-full bg-[#FDFCF8] border border-[#DED8CF] text-sm text-[#2C2C24] placeholder-[#78786C] focus:outline-none focus:border-[#5D7052] focus:ring-1 focus:ring-[#5D7052] transition-all"
              />
            </div>
            <Button type="submit" variant="primary" size="md" className="gap-2 shrink-0">
              <Search className="w-4 h-4" />
              <span>Search</span>
            </Button>
          </div>

          {/* Filter Controls Row */}
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 pt-4 border-t border-[#DED8CF]/40">
            {/* Strategy Type Filter */}
            <Select
              label="Strategy Type"
              value={strategyType}
              onChange={(e) => {
                setStrategyType(e.target.value)
                setPage(0)
              }}
              options={[
                { label: 'All Strategies', value: '' },
                { label: 'Momentum', value: 'MOMENTUM' },
                { label: 'Mean Reversion', value: 'MEAN_REVERSION' },
                { label: 'Breakout', value: 'BREAKOUT' },
                { label: 'Trend Following', value: 'TREND_FOLLOWING' },
                { label: 'Arbitrage', value: 'ARBITRAGE' },
              ]}
            />

            {/* Min Price */}
            <Input
              label="Min Price (₹)"
              type="number"
              placeholder="0"
              value={minPrice}
              onChange={(e) => {
                setMinPrice(e.target.value)
                setPage(0)
              }}
            />

            {/* Max Price */}
            <Input
              label="Max Price (₹)"
              type="number"
              placeholder="10000"
              value={maxPrice}
              onChange={(e) => {
                setMaxPrice(e.target.value)
                setPage(0)
              }}
            />

            {/* Sort By */}
            <Select
              label="Sort By"
              value={sortBy}
              onChange={(e) => {
                setSortBy(e.target.value)
                setPage(0)
              }}
              options={[
                { label: 'Newest First', value: 'newest' },
                { label: 'Price: Low to High', value: 'price_asc' },
                { label: 'Price: High to Low', value: 'price_desc' },
                { label: 'Highest Win Rate', value: 'winrate' },
              ]}
            />
          </div>

          {/* Active Filter Badges / Reset */}
          {(q || strategyType || minPrice || maxPrice || sortBy !== 'newest') && (
            <div className="flex items-center justify-between text-xs font-body text-[#78786C] pt-2">
              <span className="flex items-center gap-1">
                <SlidersHorizontal className="w-3.5 h-3.5" />
                Active Filters Applied
              </span>
              <button
                type="button"
                onClick={handleClearFilters}
                className="text-[#A85448] font-semibold hover:underline"
              >
                Reset Filters
              </button>
            </div>
          )}
        </form>
      </Card>

      {/* Error Alert */}
      {error && (
        <div className="p-4 rounded-2xl bg-[#A85448]/10 border border-[#A85448]/30 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <AlertCircle className="w-5 h-5 text-[#A85448]" />
            <span className="text-sm font-body text-[#A85448] font-medium">{error}</span>
          </div>
          <Button variant="ghost" size="sm" onClick={fetchListings} className="text-[#A85448]">
            Retry
          </Button>
        </div>
      )}

      {/* Loading Skeleton / Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((idx) => (
            <Card key={idx} className="p-6 flex flex-col justify-between gap-6 animate-pulse">
              <div className="flex flex-col gap-3">
                <div className="h-6 bg-[#DED8CF]/40 rounded-md w-3/4" />
                <div className="h-4 bg-[#DED8CF]/30 rounded-md w-1/2" />
                <div className="h-16 bg-[#DED8CF]/20 rounded-xl mt-2" />
              </div>
              <div className="h-10 bg-[#DED8CF]/40 rounded-full w-full" />
            </Card>
          ))}
        </div>
      ) : listingsPage && listingsPage.content.length > 0 ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {listingsPage.content.map((listing) => (
              <Card key={listing.listingId} interactive className="p-6 flex flex-col justify-between gap-6">
                <div className="flex flex-col gap-4">
                  {/* Card Header: Strategy Badge + Price */}
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-[#5D7052]/10 text-[#5D7052] tracking-wide">
                        {listing.strategyType || 'MOMENTUM'}
                      </span>
                      {(listing.official || listing.is_official) && <OfficialBadge />}
                    </div>
                    <div className="flex items-center gap-1 font-heading font-extrabold text-lg text-[#2C2C24]">
                      <span>₹{listing.price.toLocaleString()}</span>
                      <span className="text-[11px] font-normal text-[#78786C]">
                        / {listing.licenseType === 'TIMED' ? 'mo' : 'one-time'}
                      </span>
                    </div>
                  </div>

                  {/* Title & Seller */}
                  <div className="flex flex-col gap-1">
                    <h3 className="font-heading font-bold text-xl text-[#2C2C24] line-clamp-1">
                      {listing.name}
                    </h3>
                    <Link
                      to={`/sellers/${listing.sellerId}`}
                      className="inline-flex items-center gap-1 text-xs text-[#78786C] hover:text-[#5D7052] transition-colors"
                    >
                      <User className="w-3.5 h-3.5" />
                      <span>by <strong className="text-[#2C2C24]">{listing.sellerDisplayName}</strong></span>
                    </Link>
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
                        {listing.winRate !== null ? `${listing.winRate}%` : 'N/A'}
                      </span>
                    </div>

                    <div>
                      <span className="text-[10px] font-semibold text-[#78786C] uppercase block">Max DD</span>
                      <span className="text-sm font-bold text-[#A85448]">
                        {listing.maxDrawdown !== null ? `${listing.maxDrawdown}%` : 'N/A'}
                      </span>
                    </div>

                    <div>
                      <span className="text-[10px] font-semibold text-[#78786C] uppercase block">Sharpe</span>
                      <span className="text-sm font-bold text-[#2C2C24]">
                        {listing.sharpeRatio !== null ? listing.sharpeRatio : 'N/A'}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Action CTA */}
                <Link to={`/marketplace/${listing.listingId}`} className="w-full">
                  <Button variant="primary" size="md" className="w-full justify-center gap-2">
                    <span>Inspect Strategy Details</span>
                    <ChevronRight className="w-4 h-4" />
                  </Button>
                </Link>
              </Card>
            ))}
          </div>

          {/* Pagination Controls */}
          {listingsPage.totalPages > 1 && (
            <div className="flex items-center justify-between pt-6 border-t border-[#DED8CF]/50">
              <span className="text-sm font-body text-[#78786C]">
                Showing page <strong className="text-[#2C2C24]">{page + 1}</strong> of{' '}
                <strong className="text-[#2C2C24]">{listingsPage.totalPages}</strong> ({listingsPage.totalElements} total algorithms)
              </span>

              <div className="flex items-center gap-2">
                <Button
                  variant="ghost"
                  size="sm"
                  disabled={page === 0}
                  onClick={() => setPage((prev) => Math.max(0, prev - 1))}
                  className="gap-1"
                >
                  <ChevronLeft className="w-4 h-4" />
                  <span>Previous</span>
                </Button>

                <Button
                  variant="ghost"
                  size="sm"
                  disabled={page >= listingsPage.totalPages - 1}
                  onClick={() => setPage((prev) => prev + 1)}
                  className="gap-1"
                >
                  <span>Next</span>
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            </div>
          )}
        </>
      ) : (
        /* Empty State */
        <Card className="p-12 text-center flex flex-col items-center justify-center gap-4">
          <div className="w-12 h-12 rounded-full bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
            <Filter className="w-6 h-6" />
          </div>
          <h3 className="font-heading font-bold text-xl text-[#2C2C24]">
            No Published Listings Found
          </h3>
          <p className="text-sm font-body text-[#78786C] max-w-md">
            No active strategies match your criteria. Try loosening your search filters or resetting your query.
          </p>
          <Button variant="secondary" size="md" onClick={handleClearFilters} className="gap-2 mt-2">
            <RefreshCw className="w-4 h-4" />
            <span>Reset All Filters</span>
          </Button>
        </Card>
      )}
    </div>
  )
}
