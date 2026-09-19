import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { Card, Button } from '../../components/ui'
import { PurchasedBotsList } from '../../components/buyer/PurchasedBotsList'
import {
  ShoppingBag,
  Store,
  ShieldCheck,
  Zap,
  TrendingUp,
  Clock,
} from 'lucide-react'

export const BuyerDashboardPage: React.FC = () => {
  const { user } = useAuth()

  return (
    <div className="flex flex-col gap-8">
      {/* Welcome Banner Card */}
      <Card className="p-6 sm:p-8 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
        <div className="flex items-center gap-5">
          <div className="w-14 h-14 rounded-full bg-[#5D7052] flex items-center justify-center text-white text-xl font-heading font-bold shadow-md">
            {user?.email ? user.email.substring(0, 2).toUpperCase() : 'BY'}
          </div>

          <div className="flex flex-col gap-1">
            <div className="flex flex-wrap items-center gap-2 min-w-0">
              <h1 className="text-2xl font-heading font-extrabold text-[#2C2C24] break-words">
                Welcome, {user?.email.split('@')[0]}
              </h1>
              <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-0.5 rounded-full bg-[#5D7052]/10 text-[#5D7052]">
                <ShieldCheck className="w-3.5 h-3.5" />
                BUYER ACCOUNT
              </span>
            </div>

            <p className="text-sm font-body text-[#78786C]">
              Manage your licensed algorithmic trading strategies &amp; backtest disclosures.
            </p>
          </div>
        </div>

        <Link to="/marketplace">
          <Button variant="primary" size="md" className="gap-2 shrink-0">
            <Store className="w-4 h-4" />
            <span>Explore Marketplace</span>
          </Button>
        </Link>
      </Card>

      {/* Quick Summary Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="p-5 flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
            <ShoppingBag className="w-5 h-5" />
          </div>
          <div className="flex flex-col">
            <span className="text-xs font-semibold text-[#78786C] uppercase">Active Licenses</span>
            <span className="text-2xl font-heading font-extrabold text-[#2C2C24]">0</span>
          </div>
        </Card>

        <Card className="p-5 flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl bg-[#C18C5D]/10 flex items-center justify-center text-[#C18C5D]">
            <TrendingUp className="w-5 h-5" />
          </div>
          <div className="flex flex-col">
            <span className="text-xs font-semibold text-[#78786C] uppercase">Disclosed Strategies</span>
            <span className="text-2xl font-heading font-extrabold text-[#2C2C24]">0</span>
          </div>
        </Card>

        <Card className="p-5 flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
            <Clock className="w-5 h-5" />
          </div>
          <div className="flex flex-col">
            <span className="text-xs font-semibold text-[#78786C] uppercase">Status</span>
            <span className="text-sm font-bold text-[#5D7052] flex items-center gap-1">
              <Zap className="w-3.5 h-3.5 fill-[#5D7052]" />
              Ready for Phase 5 Checkout
            </span>
          </div>
        </Card>
      </div>

      {/* Purchased Bots Section */}
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <ShoppingBag className="w-5 h-5 text-[#5D7052]" />
            <h2 className="font-heading font-bold text-xl text-[#2C2C24]">
              My Purchased Algorithms
            </h2>
          </div>
        </div>

        {/* Component extension point for Phase 5 */}
        <PurchasedBotsList />
      </div>
    </div>
  )
}
