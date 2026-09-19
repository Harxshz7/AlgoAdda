import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { api } from '../../lib/api'
import type { PublicSellerProfile } from '../../lib/api'
import { Card, Button, RiskBadge } from '../../components/ui'
import {
  Calendar,
  Store,
  ChevronRight,
  ArrowLeft,
  AlertCircle,
  ShieldCheck,
  Package,
} from 'lucide-react'

export const PublicSellerProfilePage: React.FC = () => {
  const { sellerId } = useParams<{ sellerId: string }>()

  const [profile, setProfile] = useState<PublicSellerProfile | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchSeller = async () => {
      if (!sellerId) return
      setIsLoading(true)
      setError(null)

      try {
        const data = await api.getPublicSellerProfile(sellerId)
        setProfile(data)
      } catch (err: any) {
        setError(err.message || 'Failed to load seller profile.')
      } finally {
        setIsLoading(false)
      }
    }

    fetchSeller()
  }, [sellerId])

  if (isLoading) {
    return (
      <div className="flex flex-col gap-6 animate-pulse max-w-4xl mx-auto">
        <div className="h-40 bg-[#DED8CF]/30 rounded-2xl" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="h-56 bg-[#DED8CF]/30 rounded-2xl" />
          <div className="h-56 bg-[#DED8CF]/30 rounded-2xl" />
        </div>
      </div>
    )
  }

  if (error || !profile) {
    return (
      <div className="p-8 rounded-2xl bg-[#A85448]/10 border border-[#A85448]/30 flex flex-col items-center text-center gap-4 max-w-md mx-auto">
        <AlertCircle className="w-10 h-10 text-[#A85448]" />
        <h2 className="font-heading font-bold text-xl text-[#2C2C24]">Seller Not Found</h2>
        <p className="text-sm font-body text-[#78786C]">{error || 'The requested seller profile could not be retrieved.'}</p>
        <Link to="/marketplace">
          <Button variant="primary" size="md">Back to Marketplace</Button>
        </Link>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-8">
      {/* Top Header Navigation */}
      <div className="flex items-center gap-4 pb-4 border-b border-[#DED8CF]/50">
        <Link to="/marketplace">
          <Button variant="ghost" size="sm" className="gap-2 text-[#78786C]">
            <ArrowLeft className="w-4 h-4" />
            <span>MARKETPLACE</span>
          </Button>
        </Link>
        <div className="h-5 w-px bg-[#DED8CF]" />
        <span className="text-xs text-[#78786C] font-semibold uppercase tracking-wider">
          Creator Profile
        </span>
      </div>

      {/* Seller Header Banner Card */}
      <Card className="p-6 sm:p-8 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
        <div className="flex items-center gap-4 sm:gap-5">
          <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-full bg-[#5D7052] flex items-center justify-center text-white text-xl sm:text-2xl font-heading font-bold shadow-md shrink-0">
            {profile.displayName.substring(0, 2).toUpperCase()}
          </div>
          <div className="flex flex-col gap-1">
            <div className="flex flex-wrap items-center gap-2 min-w-0">
              <h1 className="text-xl sm:text-2xl font-heading font-extrabold text-[#2C2C24]">
                {profile.displayName}
              </h1>
              <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-0.5 rounded-full bg-[#5D7052]/10 text-[#5D7052]">
                <ShieldCheck className="w-3.5 h-3.5" />
                Verified Creator
              </span>
            </div>
            <p className="text-sm font-body text-[#78786C] max-w-lg">
              {profile.bio || 'Algorithm creator on AlgoAdda platform.'}
            </p>
            <span className="text-xs text-[#78786C] flex items-center gap-1 mt-1">
              <Calendar className="w-3.5 h-3.5" />
              Member since {new Date(profile.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })}
            </span>
          </div>
        </div>

        <div className="flex items-center gap-3 self-start sm:self-center">
          <div className="p-3.5 rounded-2xl bg-[#FDFCF8] border border-[#DED8CF] text-center min-w-[120px]">
            <span className="text-xs text-[#78786C] uppercase font-semibold block">Active Bots</span>
            <span className="text-2xl font-heading font-extrabold text-[#5D7052]">
              {profile.activeListings.length}
            </span>
          </div>
        </div>
      </Card>

      {/* Active Listings Section */}
      <div className="flex flex-col gap-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Store className="w-5 h-5 text-[#5D7052]" />
            <h2 className="font-heading font-bold text-xl text-[#2C2C24]">
              Active Marketplace Listings ({profile.activeListings.length})
            </h2>
          </div>
        </div>

        {profile.activeListings.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {profile.activeListings.map((listing) => (
              <Card key={listing.listingId} interactive className="p-6 flex flex-col justify-between gap-6">
                <div className="flex flex-col gap-4">
                  {/* Strategy Badge & Price */}
                  <div className="flex flex-wrap items-center justify-between gap-2 min-w-0">
                    <div className="flex flex-wrap items-center gap-1.5 min-w-0 max-w-full">
                      <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-[#5D7052]/10 text-[#5D7052] shrink-0">
                        {listing.strategyType}
                      </span>
                      {listing.riskLabel && <RiskBadge riskLabel={listing.riskLabel} />}
                    </div>
                    <span className="font-heading font-extrabold text-lg text-[#2C2C24] shrink-0 ml-auto">
                      ₹{listing.price.toLocaleString()}
                    </span>
                  </div>

                  {/* Title & Description */}
                  <div className="flex flex-col gap-1">
                    <h3 className="font-heading font-bold text-lg text-[#2C2C24] line-clamp-1">
                      {listing.name}
                    </h3>
                    <p className="text-sm font-body text-[#78786C] line-clamp-2 leading-relaxed">
                      {listing.description}
                    </p>
                  </div>

                  {/* Metrics Row */}
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

                <Link to={`/marketplace/${listing.listingId}`} className="w-full">
                  <Button variant="primary" size="md" className="w-full justify-center gap-2">
                    <span>Inspect Algorithm</span>
                    <ChevronRight className="w-4 h-4" />
                  </Button>
                </Link>
              </Card>
            ))}
          </div>
        ) : (
          <Card className="p-8 text-center flex flex-col items-center justify-center gap-3">
            <Package className="w-10 h-10 text-[#78786C]" />
            <h3 className="font-heading font-bold text-lg text-[#2C2C24]">No Active Listings</h3>
            <p className="text-sm font-body text-[#78786C]">This creator currently has no active published algorithms.</p>
          </Card>
        )}
      </div>
    </div>
  )
}
