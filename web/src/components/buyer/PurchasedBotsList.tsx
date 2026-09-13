import React from 'react'
import { Link } from 'react-router-dom'
import { Card, Button } from '../ui'
import { ShoppingBag, ArrowRight, ShieldCheck, Sparkles } from 'lucide-react'

/* ============================================================================
 * PHASE 5 EXTENSION POINT:
 * In Phase 5 (Payments & Licensing), replace this empty state component with
 * real API integration:
 *
 *   const [licenses, setLicenses] = useState<License[]>([]);
 *   useEffect(() => {
 *     api.getBuyerLicenses().then(setLicenses);
 *   }, []);
 *
 * When licenses.length > 0, render the grid of purchased Bot cards with:
 * - Bot name, strategy type, license status, expiry date
 * - Strategy artifact download button / license key copy CTA
 * ============================================================================ */

export const PurchasedBotsList: React.FC = () => {
  // Phase 5 state placeholder: currently empty until Phase 5 licensing endpoints land
  const purchasedLicenses: any[] = []

  if (purchasedLicenses.length > 0) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Phase 5 License Cards will render here */}
      </div>
    )
  }

  return (
    <Card className="p-10 text-center flex flex-col items-center justify-center gap-5 border-dashed border-2 border-[#DED8CF]">
      <div className="w-14 h-14 rounded-full bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
        <ShoppingBag className="w-7 h-7" />
      </div>

      <div className="flex flex-col gap-2 max-w-md">
        <h3 className="font-heading font-bold text-xl text-[#2C2C24]">
          No Purchased Algorithms Yet
        </h3>
        <p className="text-sm font-body text-[#78786C] leading-relaxed">
          Explore backtested quantitative trading strategies with fully disclosed rules and verified backtest metrics on our white-box marketplace.
        </p>
      </div>

      <div className="flex items-center gap-3 pt-2">
        <Link to="/marketplace">
          <Button variant="primary" size="md" className="gap-2">
            <Sparkles className="w-4 h-4" />
            <span>Browse Marketplace</span>
            <ArrowRight className="w-4 h-4" />
          </Button>
        </Link>
      </div>

      <div className="pt-4 border-t border-[#DED8CF]/40 flex items-center gap-2 text-xs text-[#78786C]">
        <ShieldCheck className="w-4 h-4 text-[#5D7052]" />
        <span>All algorithm purchases include White-Box logic disclosure &amp; SEBI 2026 compliance verification</span>
      </div>
    </Card>
  )
}
