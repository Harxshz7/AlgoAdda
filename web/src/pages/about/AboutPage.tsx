import React from 'react'
import { Link } from 'react-router-dom'
import { Card, Button } from '../../components/ui'
import { ShieldCheck, FileCode, BarChart3, AlertCircle, ArrowRight, Lock, Sparkles } from 'lucide-react'

// White Box Verification Pipeline SVG Illustration
const PipelineIllustration: React.FC = () => {
  return (
    <svg
      viewBox="0 0 800 220"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className="w-full h-auto max-h-64"
    >
      {/* Connecting Flow Lines */}
      <path d="M 180 110 L 260 110" stroke="#DED8CF" strokeWidth="3" strokeDasharray="6 6" />
      <path d="M 380 110 L 460 110" stroke="#DED8CF" strokeWidth="3" strokeDasharray="6 6" />
      <path d="M 580 110 L 660 110" stroke="#5D7052" strokeWidth="3" strokeDasharray="6 6" />

      {/* Node 1: Disclosed Strategy Logic */}
      <g transform="translate(40, 40)">
        <rect width="140" height="140" rx="16" fill="#FDFCF8" stroke="#DED8CF" strokeWidth="2" />
        <rect x="20" y="20" width="36" height="36" rx="8" fill="#5D7052" opacity="0.12" />
        <path d="M 32 32 L 44 32 M 32 38 L 44 38 M 32 44 L 40 44" stroke="#5D7052" strokeWidth="2" strokeLinecap="round" />
        <text x="20" y="82" fill="#2C2C24" fontSize="13" fontWeight="700" fontFamily="Nunito, sans-serif">1. Disclosed Logic</text>
        <text x="20" y="100" fill="#78786C" fontSize="11" fontFamily="Nunito, sans-serif">Full source &amp; params</text>
        <text x="20" y="116" fill="#78786C" fontSize="11" fontFamily="Nunito, sans-serif">no black boxes</text>
      </g>

      {/* Node 2: VectorBT Engine */}
      <g transform="translate(240, 40)">
        <rect width="140" height="140" rx="16" fill="#FDFCF8" stroke="#DED8CF" strokeWidth="2" />
        <rect x="20" y="20" width="36" height="36" rx="8" fill="#2C2C24" opacity="0.1" />
        <path d="M 30 46 L 36 34 L 42 40 L 48 28" stroke="#2C2C24" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        <text x="20" y="82" fill="#2C2C24" fontSize="13" fontWeight="700" fontFamily="Nunito, sans-serif">2. VectorBT Backtest</text>
        <text x="20" y="100" fill="#78786C" fontSize="11" fontFamily="Nunito, sans-serif">Automated quantitative</text>
        <text x="20" y="116" fill="#78786C" fontSize="11" fontFamily="Nunito, sans-serif">drawdown calculation</text>
      </g>

      {/* Node 3: Compliance Gate */}
      <g transform="translate(440, 40)">
        <rect width="140" height="140" rx="16" fill="#FDFCF8" stroke="#C18C5D" strokeWidth="2" />
        <rect x="20" y="20" width="36" height="36" rx="8" fill="#C18C5D" opacity="0.15" />
        <path d="M 38 28 L 46 32 V 40 C 46 45 42 49 38 50 C 34 49 30 45 30 40 V 32 L 38 28 Z" stroke="#C18C5D" strokeWidth="2" fill="none" />
        <text x="20" y="82" fill="#2C2C24" fontSize="13" fontWeight="700" fontFamily="Nunito, sans-serif">3. Compliance Gate</text>
        <text x="20" y="100" fill="#78786C" fontSize="11" fontFamily="Nunito, sans-serif">White box rule check</text>
        <text x="20" y="116" fill="#78786C" fontSize="11" fontFamily="Nunito, sans-serif">no claims policy</text>
      </g>

      {/* Node 4: Verified Marketplace Listing */}
      <g transform="translate(640, 40)">
        <rect width="140" height="140" rx="16" fill="#5D7052" opacity="0.06" stroke="#5D7052" strokeWidth="2" />
        <rect x="20" y="20" width="36" height="36" rx="8" fill="#5D7052" />
        <path d="M 31 38 L 36 43 L 45 33" stroke="#FDFCF8" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
        <text x="20" y="82" fill="#2C2C24" fontSize="13" fontWeight="700" fontFamily="Nunito, sans-serif">4. Verified Listing</text>
        <text x="20" y="100" fill="#5D7052" fontSize="11" fontWeight="600" fontFamily="Nunito, sans-serif">SEBI-aligned store</text>
        <text x="20" y="116" fill="#78786C" fontSize="11" fontFamily="Nunito, sans-serif">one-click deployment</text>
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

      {/* 3. Why White Box Matters */}
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-1">
          <span className="text-xs font-semibold uppercase tracking-wider text-[#5D7052]">
            Core Principles
          </span>
          <h2 className="text-2xl font-heading font-bold text-[#2C2C24]">
            Why White Box Matters
          </h2>
          <p className="text-sm text-[#78786C]">
            Traditional bot marketplaces operate on black-box promises. AlgoAdda enforces four non-negotiable trust standards.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card className="p-6 flex flex-col gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
              <FileCode className="w-5 h-5" />
            </div>
            <h3 className="font-heading font-bold text-lg text-[#2C2C24]">
              Disclosed Strategy Logic
            </h3>
            <p className="text-sm text-[#78786C] leading-relaxed">
              Sellers disclose the underlying indicators, entry/exit rules, and risk management parameters. Buyers know exactly how the algorithm makes decisions before deploying capital.
            </p>
          </Card>

          <Card className="p-6 flex flex-col gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#2C2C24]/10 flex items-center justify-center text-[#2C2C24]">
              <BarChart3 className="w-5 h-5" />
            </div>
            <h3 className="font-heading font-bold text-lg text-[#2C2C24]">
              Verifiable VectorBT Metrics
            </h3>
            <p className="text-sm text-[#78786C] leading-relaxed">
              Backtests are generated using VectorBT quantitative engines over historical market data with fixed initial capital benchmarks — eliminating cherry-picked timeframe claims.
            </p>
          </Card>

          <Card className="p-6 flex flex-col gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#C18C5D]/15 flex items-center justify-center text-[#C18C5D]">
              <Lock className="w-5 h-5" />
            </div>
            <h3 className="font-heading font-bold text-lg text-[#2C2C24]">
              No Guaranteed Return Claims
            </h3>
            <p className="text-sm text-[#78786C] leading-relaxed">
              We strictly forbid fixed-return marketing or false profitability guarantees. Every listing highlights maximum historical drawdown and risk disclaimers.
            </p>
          </Card>
        </div>
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
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 p-8 rounded-2xl bg-[#2C2C24] text-white">
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
