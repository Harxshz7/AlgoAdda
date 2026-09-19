import React from 'react'
import { Link } from 'react-router-dom'
import { AlertTriangle, ShieldAlert } from 'lucide-react'

export const RiskDisclosurePage: React.FC = () => {
  return (
    <div className="flex flex-col gap-10 max-w-4xl mx-auto py-4">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <div className="inline-flex items-center gap-2 self-start px-3 py-1 rounded-full bg-[#A85448]/10 text-[#A85448] text-xs font-semibold border border-[#A85448]/20">
          <AlertTriangle className="w-3.5 h-3.5" />
          <span>Mandatory Risk Warning</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-heading font-extrabold text-[#2C2C24] tracking-tight">
          Risk Disclosure Statement
        </h1>
        <p className="text-sm sm:text-base text-[#78786C] font-body leading-relaxed">
          Algorithmic trading and derivative transactions involve substantial risk of loss and are not suitable for every investor. Please read this disclosure carefully prior to purchasing, licensing, or executing any automated strategy.
        </p>
      </div>

      {/* Prominent High-Risk Warning Box */}
      <div className="p-5 sm:p-6 rounded-2xl bg-[#A85448]/8 border border-[#A85448]/25 flex items-start gap-4">
        <ShieldAlert className="w-6 h-6 text-[#A85448] shrink-0 mt-0.5" />
        <div className="text-xs sm:text-sm text-[#2C2C24] font-body leading-relaxed space-y-2">
          <strong className="text-[#A85448] block font-heading text-base font-bold">
            Important Notice: Capital at Risk
          </strong>
          <p>
            Trading equities, futures, options, and other financial instruments involves significant risk of monetary loss. You may sustain a total loss of initial funds and additional capital. Never trade with capital you cannot afford to lose.
          </p>
          <p className="text-[#78786C]">
            AlgoAdda does not provide guaranteed returns, portfolio management, or trade execution. You operate strategies entirely at your own discretion and risk.
          </p>
        </div>
      </div>

      {/* Legal Prose Content */}
      <div className="flex flex-col gap-8 text-[#4A4A40] font-body text-sm sm:text-base leading-relaxed">

        {/* Section 1 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            1. Hypothetical &amp; Backtested Performance Disclaimer
          </h2>
          <p>
            Backtested performance results presented on AlgoAdda (generated via automated quantitative pipelines such as VectorBT) are hypothetical in nature and are designed solely to demonstrate mathematical logic over historical historical price datasets.
          </p>
          <p className="text-[#78786C]">
            Hypothetical performance has inherent limitations. Unlike actual trading records, backtested strategies do not reflect the impact of real-time market liquidity, bid-ask spread expansion, exchange transaction fees, statutory taxes (such as STT/GST in India), or market impact costs. <strong>Past or backtested performance is no guarantee of future returns.</strong>
          </p>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 2 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            2. White Box Philosophy &amp; Disclosed Logic
          </h2>
          <p>
            AlgoAdda strictly enforces SEBI&apos;s White Box compliance principles. This means:
          </p>
          <ul className="list-disc pl-6 space-y-1.5 text-[#78786C]">
            <li>
              <strong className="text-[#2C2C24]">Full Transparency:</strong> Every strategy listed must provide readable source code and clearly disclosed mathematical parameters. There are no &quot;black box&quot; proprietary secrets.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Zero Assured Profits:</strong> AlgoAdda strictly prohibits and automatically filters any listing claiming &quot;guaranteed profit&quot;, &quot;fixed return&quot;, &quot;risk-free trading&quot;, or similar representations.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Buyer Audit Responsibility:</strong> Because all logic is disclosed, buyers have the opportunity and responsibility to inspect the underlying indicators, entry/exit rules, and historical maximum drawdowns before deploying code live.
            </li>
          </ul>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 3 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            3. Technical, Execution &amp; Infrastructure Risks
          </h2>
          <p>
            Algorithmic trading depends on high-availability technology infrastructure. Automated strategies are subject to operational failures that can result in unexpected losses, including:
          </p>
          <ul className="list-disc pl-6 space-y-1.5 text-[#78786C]">
            <li>
              <strong className="text-[#2C2C24]">Broker API Downtime:</strong> Third-party brokerage API rate limits, authentication timeouts, socket disconnections, or scheduled maintenance.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Latency &amp; Slippage:</strong> Market price movements between signal generation and order fill at the exchange, particularly during high-volatility sessions or news events.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Order Rejection:</strong> Broker risk management system (RMS) blocks, margin shortfall rejections, or circuit breaker freeze triggers.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Hardware &amp; Connectivity:</strong> Failure of local machines, cloud virtual private servers (VPS), or internet service providers during live market hours.
            </li>
          </ul>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 4 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            4. Market Regime Shifts &amp; Strategy Decay
          </h2>
          <p>
            Financial markets are dynamic and non-stationary. Trading models optimized for specific volatility regimes (e.g., strong bull trends) may underperform severely or experience unprecedented drawdown during sideways consolidation or high-frequency whipsaws. Quantitative strategies can and do suffer performance decay over time as market microstructure evolves.
          </p>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 5 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            5. No Investment Advice or Fiduciary Duty
          </h2>
          <p>
            AlgoAdda is an independent software marketplace facilitator and technology provider. AlgoAdda is not a SEBI-registered Investment Adviser (RIA), Research Analyst (RA), or Portfolio Management Service (PMS).
          </p>
          <p>
            No content, backtest score, review, or rating on AlgoAdda constitutes an endorsement or recommendation to buy, sell, or hold any security. You should consult a qualified, registered financial advisor before making any financial investment.
          </p>
        </section>
      </div>

      {/* Navigation Links */}
      <div className="pt-6 border-t border-[#DED8CF] flex flex-wrap items-center justify-between gap-4 text-xs font-body text-[#78786C]">
        <span>Looking for other legal documents?</span>
        <div className="flex items-center gap-4">
          <Link to="/terms" className="text-[#5D7052] font-semibold underline hover:text-[#4a5a41]">
            Terms of Service
          </Link>
          <span>•</span>
          <Link to="/privacy" className="text-[#5D7052] font-semibold underline hover:text-[#4a5a41]">
            Privacy Policy
          </Link>
          <span>•</span>
          <Link to="/about" className="text-[#5D7052] font-semibold underline hover:text-[#4a5a41]">
            About &amp; White Box
          </Link>
        </div>
      </div>
    </div>
  )
}
