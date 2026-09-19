import React from 'react'
import { Link } from 'react-router-dom'
import { Card, Button } from '../../components/ui'
import { ShieldCheck, FileCode, BarChart3, AlertCircle, ArrowRight, Sparkles, Check, X } from 'lucide-react'

// White Box Verification Pipeline SVG Illustration
const PipelineIllustration: React.FC = () => {
  return (
    <svg
      viewBox="0 0 820 200"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className="w-full h-auto min-w-[680px]"
    >
      {/* Connecting Flow Lines */}
      <path d="M 190 105 L 230 105" stroke="#DED8CF" strokeWidth="3" strokeDasharray="6 6" />
      <path d="M 390 105 L 430 105" stroke="#DED8CF" strokeWidth="3" strokeDasharray="6 6" />
      <path d="M 590 105 L 630 105" stroke="#5D7052" strokeWidth="3" strokeDasharray="6 6" />

      {/* Node 1: Disclosed Strategy Logic */}
      <g transform="translate(30, 35)">
        <rect width="160" height="140" rx="16" fill="#FDFCF8" stroke="#DED8CF" strokeWidth="2" />
        <rect x="62" y="18" width="36" height="36" rx="8" fill="#5D7052" opacity="0.12" />
        <path d="M 74 30 L 86 30 M 74 36 L 86 36 M 74 42 L 82 42" stroke="#5D7052" strokeWidth="2" strokeLinecap="round" />
        <text x="80" y="80" fill="#2C2C24" fontSize="12" fontWeight="700" textAnchor="middle" fontFamily="Nunito, sans-serif">1. Disclosed Logic</text>
        <text x="80" y="98" fill="#78786C" fontSize="10.5" textAnchor="middle" fontFamily="Nunito, sans-serif">Full source &amp; params</text>
        <text x="80" y="114" fill="#78786C" fontSize="10.5" textAnchor="middle" fontFamily="Nunito, sans-serif">Zero black boxes</text>
      </g>

      {/* Node 2: VectorBT Engine */}
      <g transform="translate(230, 35)">
        <rect width="160" height="140" rx="16" fill="#FDFCF8" stroke="#DED8CF" strokeWidth="2" />
        <rect x="62" y="18" width="36" height="36" rx="8" fill="#2C2C24" opacity="0.1" />
        <path d="M 72 44 L 78 32 L 84 38 L 90 26" stroke="#2C2C24" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        <text x="80" y="80" fill="#2C2C24" fontSize="12" fontWeight="700" textAnchor="middle" fontFamily="Nunito, sans-serif">2. VectorBT Backtest</text>
        <text x="80" y="98" fill="#78786C" fontSize="10.5" textAnchor="middle" fontFamily="Nunito, sans-serif">Automated quant audit</text>
        <text x="80" y="114" fill="#78786C" fontSize="10.5" textAnchor="middle" fontFamily="Nunito, sans-serif">Verified drawdown</text>
      </g>

      {/* Node 3: Compliance Gate */}
      <g transform="translate(430, 35)">
        <rect width="160" height="140" rx="16" fill="#FDFCF8" stroke="#C18C5D" strokeWidth="2" />
        <rect x="62" y="18" width="36" height="36" rx="8" fill="#C18C5D" opacity="0.15" />
        <path d="M 80 26 L 88 30 V 38 C 88 43 84 47 80 48 C 76 47 72 43 72 38 V 30 L 80 26 Z" stroke="#C18C5D" strokeWidth="2" fill="none" />
        <text x="80" y="80" fill="#2C2C24" fontSize="12" fontWeight="700" textAnchor="middle" fontFamily="Nunito, sans-serif">3. Compliance Gate</text>
        <text x="80" y="98" fill="#78786C" fontSize="10.5" textAnchor="middle" fontFamily="Nunito, sans-serif">White-box audit check</text>
        <text x="80" y="114" fill="#78786C" fontSize="10.5" textAnchor="middle" fontFamily="Nunito, sans-serif">No claims policy</text>
      </g>

      {/* Node 4: Verified Marketplace Listing */}
      <g transform="translate(630, 35)">
        <rect width="160" height="140" rx="16" fill="#5D7052" opacity="0.06" stroke="#5D7052" strokeWidth="2" />
        <rect x="62" y="18" width="36" height="36" rx="8" fill="#5D7052" />
        <path d="M 73 36 L 78 41 L 87 31" stroke="#FDFCF8" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
        <text x="80" y="80" fill="#2C2C24" fontSize="12" fontWeight="700" textAnchor="middle" fontFamily="Nunito, sans-serif">4. Verified Listing</text>
        <text x="80" y="98" fill="#5D7052" fontSize="10.5" fontWeight="600" textAnchor="middle" fontFamily="Nunito, sans-serif">SEBI-aligned store</text>
        <text x="80" y="114" fill="#78786C" fontSize="10.5" textAnchor="middle" fontFamily="Nunito, sans-serif">1-click deployment</text>
      </g>
    </svg>
  )
}

export const AboutPage: React.FC = () => {
  return (
    <div className="flex flex-col gap-12 max-w-5xl mx-auto">
      {/* 1. Header & What AlgoAdda Is */}
      <div className="flex flex-col gap-4 text-center sm:text-left max-w-3xl">
        <div className="inline-flex items-center gap-2 self-start px-3 py-1 rounded-full bg-[#5D7052]/10 text-[#5D7052] text-xs font-semibold border border-[#5D7052]/20">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>White Box Compliance Infrastructure</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-heading font-extrabold text-[#2C2C24] tracking-tight">
          About AlgoAdda
        </h1>
        <p className="text-base sm:text-lg text-[#78786C] font-body leading-relaxed">
          AlgoAdda is an algorithmic trading software marketplace built on transparency and SEBI-aligned White Box compliance. We enable quantitative developers to publish verified trading strategies while providing buyers with fully disclosed logic and reproducible backtest metrics.
        </p>
      </div>

      {/* 2. How White Box Verification Works (4 Native Cards + Responsive Diagram) */}
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-1">
          <span className="text-xs font-semibold uppercase tracking-wider text-[#C18C5D]">
            Verification Process
          </span>
          <h2 className="text-2xl font-heading font-bold text-[#2C2C24]">
            How White Box Verification Works
          </h2>
          <p className="text-sm text-[#78786C]">
            Every strategy undergoes an automated 4-stage compliance &amp; quantitative verification lifecycle before listing.
          </p>
        </div>

        {/* 4 Step Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card className="p-5 flex flex-col justify-between gap-4 border-t-4 border-t-[#5D7052]">
            <div className="flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="w-7 h-7 rounded-full bg-[#5D7052]/10 text-[#5D7052] font-bold text-xs flex items-center justify-center">1</span>
                <FileCode className="w-4 h-4 text-[#5D7052]" />
              </div>
              <h3 className="font-heading font-bold text-base text-[#2C2C24]">1. Disclosed Logic</h3>
              <p className="text-xs font-body text-[#78786C] leading-relaxed">
                Sellers upload complete source code, indicators, and risk parameters. No hidden black boxes.
              </p>
            </div>
            <span className="text-[11px] font-semibold text-[#5D7052] bg-[#5D7052]/10 px-2 py-0.5 rounded self-start">
              Source Disclosed
            </span>
          </Card>

          <Card className="p-5 flex flex-col justify-between gap-4 border-t-4 border-t-[#2C2C24]">
            <div className="flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="w-7 h-7 rounded-full bg-[#2C2C24]/10 text-[#2C2C24] font-bold text-xs flex items-center justify-center">2</span>
                <BarChart3 className="w-4 h-4 text-[#2C2C24]" />
              </div>
              <h3 className="font-heading font-bold text-base text-[#2C2C24]">2. VectorBT Backtest</h3>
              <p className="text-xs font-body text-[#78786C] leading-relaxed">
                Automated quantitative execution calculates win rate, max drawdown, and equity trajectory.
              </p>
            </div>
            <span className="text-[11px] font-semibold text-[#2C2C24] bg-[#2C2C24]/10 px-2 py-0.5 rounded self-start">
              Quantitative Audit
            </span>
          </Card>

          <Card className="p-5 flex flex-col justify-between gap-4 border-t-4 border-t-[#C18C5D]">
            <div className="flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="w-7 h-7 rounded-full bg-[#C18C5D]/15 text-[#C18C5D] font-bold text-xs flex items-center justify-center">3</span>
                <ShieldCheck className="w-4 h-4 text-[#C18C5D]" />
              </div>
              <h3 className="font-heading font-bold text-base text-[#2C2C24]">3. Compliance Gate</h3>
              <p className="text-xs font-body text-[#78786C] leading-relaxed">
                White box checklist audit to enforce risk disclosures and prohibit guaranteed claims.
              </p>
            </div>
            <span className="text-[11px] font-semibold text-[#C18C5D] bg-[#C18C5D]/15 px-2 py-0.5 rounded self-start">
              SEBI Alignment
            </span>
          </Card>

          <Card className="p-5 flex flex-col justify-between gap-4 border-t-4 border-t-[#5D7052]">
            <div className="flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="w-7 h-7 rounded-full bg-[#5D7052] text-white font-bold text-xs flex items-center justify-center">4</span>
                <Sparkles className="w-4 h-4 text-[#5D7052]" />
              </div>
              <h3 className="font-heading font-bold text-base text-[#2C2C24]">4. Verified Listing</h3>
              <p className="text-xs font-body text-[#78786C] leading-relaxed">
                Published on official store or marketplace with transparent metrics and one-click licensing.
              </p>
            </div>
            <span className="text-[11px] font-semibold text-[#5D7052] bg-[#5D7052]/10 px-2 py-0.5 rounded self-start">
              Live &amp; Verified
            </span>
          </Card>
        </div>

        {/* Scalable SVG Flow Diagram Card */}
        <Card className="p-6 flex flex-col gap-3 overflow-hidden">
          <span className="text-xs font-semibold text-[#78786C] uppercase tracking-wider">
            Verification Pipeline Diagram
          </span>
          <div className="w-full overflow-x-auto p-2 bg-[#FDFCF8] rounded-xl border border-[#DED8CF]/50">
            <PipelineIllustration />
          </div>
        </Card>
      </div>

      {/* 3. White Box vs. Black Box Comparison */}
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-1">
          <span className="text-xs font-semibold uppercase tracking-wider text-[#5D7052]">
            Model Comparison
          </span>
          <h2 className="text-2xl font-heading font-bold text-[#2C2C24]">
            White Box vs. Black Box Strategies
          </h2>
          <p className="text-sm text-[#78786C]">
            Understanding the structural and regulatory distinctions between disclosed algorithms and black-box strategies.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* White Box Card */}
          <Card className="p-6 flex flex-col justify-between gap-6 border-t-4 border-t-[#5D7052]">
            <div className="flex flex-col gap-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
                    <Check className="w-4 h-4" />
                  </div>
                  <h3 className="font-heading font-bold text-lg text-[#2C2C24]">
                    White Box Strategy
                  </h3>
                </div>
                <span className="text-xs font-semibold text-[#5D7052] bg-[#5D7052]/10 px-2.5 py-1 rounded-full border border-[#5D7052]/20">
                  Listed on AlgoAdda
                </span>
              </div>

              <div className="flex flex-col gap-3.5 text-sm font-body text-[#78786C]">
                <div className="flex flex-col gap-1">
                  <span className="font-semibold text-[#2C2C24]">Strategy Logic</span>
                  <p className="leading-relaxed">
                    Strategy logic is disclosed and visible to the buyer before purchase. Buyers can inspect entry/exit rules, technical indicators used, and risk parameters.
                  </p>
                </div>
                <div className="flex flex-col gap-1">
                  <span className="font-semibold text-[#2C2C24]">Verification</span>
                  <p className="leading-relaxed">
                    Verifiable and independently backtested using standardized quantitative engines over historical market data.
                  </p>
                </div>
                <div className="flex flex-col gap-1">
                  <span className="font-semibold text-[#2C2C24]">Regulatory &amp; Scope</span>
                  <p className="leading-relaxed">
                    Operates as transparent software tools under standard licensing. This is what AlgoAdda lists across all marketplace tiers.
                  </p>
                </div>
              </div>
            </div>
          </Card>

          {/* Black Box Card */}
          <Card className="p-6 flex flex-col justify-between gap-6 border-t-4 border-t-[#78786C]">
            <div className="flex flex-col gap-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-[#2C2C24]/10 flex items-center justify-center text-[#78786C]">
                    <X className="w-4 h-4" />
                  </div>
                  <h3 className="font-heading font-bold text-lg text-[#2C2C24]">
                    Black Box Strategy
                  </h3>
                </div>
                <span className="text-xs font-semibold text-[#78786C] bg-[#2C2C24]/5 px-2.5 py-1 rounded-full border border-[#2C2C24]/10">
                  Not Offered on AlgoAdda
                </span>
              </div>

              <div className="flex flex-col gap-3.5 text-sm font-body text-[#78786C]">
                <div className="flex flex-col gap-1">
                  <span className="font-semibold text-[#2C2C24]">Strategy Logic</span>
                  <p className="leading-relaxed">
                    Strategy logic is hidden. The buyer trusts claimed performance without seeing internal trading rules or parameters.
                  </p>
                </div>
                <div className="flex flex-col gap-1">
                  <span className="font-semibold text-[#2C2C24]">Verification</span>
                  <p className="leading-relaxed">
                    Unverifiable internal logic; relies on seller-reported track records without independent rule inspection.
                  </p>
                </div>
                <div className="flex flex-col gap-1">
                  <span className="font-semibold text-[#2C2C24]">Regulatory &amp; Scope</span>
                  <p className="leading-relaxed">
                    Higher regulatory burden (SEBI Research Analyst registration required for providers due to advisory nature). Not offered on AlgoAdda in this phase.
                  </p>
                </div>
              </div>
            </div>
          </Card>
        </div>

        <p className="text-sm font-body text-[#78786C] italic">
          AlgoAdda only lists White Box strategies for this reason — trust through disclosure, not trust through branding.
        </p>
      </div>

      {/* 5. Platform Regulatory Disclaimer */}
      <div className="p-6 rounded-2xl bg-[#5D7052]/5 border border-[#5D7052]/20 flex flex-col sm:flex-row items-start gap-4">
        <AlertCircle className="w-6 h-6 text-[#5D7052] shrink-0 mt-0.5" />
        <div className="flex flex-col gap-1 text-xs font-body text-[#78786C] leading-relaxed">
          <strong className="text-sm font-semibold text-[#2C2C24]">Regulatory &amp; Platform Disclaimer</strong>
          <p>
            AlgoAdda is a software technology platform connecting quantitative strategy creators with independent market participants. AlgoAdda is not a SEBI-registered Investment Advisor (IA) or Portfolio Manager (PMS) and does not provide financial advice, managed accounts, or guaranteed returns. Past performance demonstrated in historical backtests is no guarantee of future market results. Trading financial instruments carries inherent risk.
          </p>
        </div>
      </div>

      {/* CTA Row */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 p-6 sm:p-8 rounded-2xl bg-[#2C2C24] text-white">
        <div className="flex flex-col gap-1">
          <h3 className="font-heading font-bold text-xl text-white">
            Explore Verified Algorithms
          </h3>
          <p className="text-xs text-[#DED8CF] font-body">
            Browse transparent marketplace strategies or inspect official AlgoAdda flagship bots.
          </p>
        </div>

        <div className="flex items-center gap-3 w-full sm:w-auto">
          <Link to="/store" className="flex-1 sm:flex-initial">
            <Button variant="primary" size="md" className="w-full gap-2">
              <Sparkles className="w-4 h-4 text-[#C18C5D]" />
              <span>Official Store</span>
            </Button>
          </Link>
          <Link to="/marketplace" className="flex-1 sm:flex-initial">
            <Button variant="secondary" size="md" className="w-full gap-2">
              <span>Marketplace</span>
              <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
        </div>
      </div>
    </div>
  )
}

export default AboutPage
