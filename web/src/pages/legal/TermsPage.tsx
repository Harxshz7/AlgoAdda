import React from 'react'
import { Link } from 'react-router-dom'
import { FileText, Scale } from 'lucide-react'

export const TermsPage: React.FC = () => {
  return (
    <div className="flex flex-col gap-10 max-w-4xl mx-auto py-4">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <div className="inline-flex items-center gap-2 self-start px-3 py-1 rounded-full bg-[#5D7052]/10 text-[#5D7052] text-xs font-semibold border border-[#5D7052]/20">
          <FileText className="w-3.5 h-3.5" />
          <span>Legal &amp; Regulatory Framework</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-heading font-extrabold text-[#2C2C24] tracking-tight">
          Terms of Service
        </h1>
        <p className="text-sm sm:text-base text-[#78786C] font-body leading-relaxed">
          Last updated: September 2026. Please read these Terms of Service carefully before accessing or using the AlgoAdda platform.
        </p>
      </div>

      {/* Advisory Note */}
      <div className="p-4 sm:p-5 rounded-2xl bg-[#FAF8F5] border border-[#DED8CF] flex items-start gap-3.5">
        <Scale className="w-5 h-5 text-[#5D7052] shrink-0 mt-0.5" />
        <div className="text-xs sm:text-sm text-[#4A4A40] font-body leading-relaxed">
          <strong className="text-[#2C2C24] block mb-1">Standard Regulatory Notice</strong>
          AlgoAdda operates as a software marketplace facilitator connecting independent quantitative developers with traders. AlgoAdda is not a registered investment advisor, stockbroker, or portfolio manager under the Securities and Exchange Board of India (SEBI). All strategies listed adhere to SEBI&apos;s White Box disclosed logic framework.
        </div>
      </div>

      {/* Legal Prose Content */}
      <div className="flex flex-col gap-8 text-[#4A4A40] font-body text-sm sm:text-base leading-relaxed">

        {/* Section 1 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            1. Platform Role &amp; Nature of Service
          </h2>
          <p>
            AlgoAdda provides a non-custodial software marketplace where independent software authors (&quot;Sellers&quot;) can publish algorithmic trading source code and strategies, and users (&quot;Buyers&quot;) can license, evaluate, and download disclosed strategy logic.
          </p>
          <p>
            AlgoAdda does <strong>not</strong>:
          </p>
          <ul className="list-disc pl-6 space-y-1.5 text-[#78786C]">
            <li>Provide financial advice, investment recommendations, or personalized trading guidance.</li>
            <li>Execute orders or hold custody over client funds, securities, or trading capital.</li>
            <li>Guarantee any specific financial return, yield, or risk mitigation for any listed strategy.</li>
            <li>Operate as a registered Broker, Sub-broker, Research Analyst (RA), or Investment Adviser (IA).</li>
          </ul>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 2 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            2. Account Registration &amp; Responsibilities
          </h2>
          <p>
            Users must provide accurate, current, and complete information during registration. You are responsible for safeguarding your login credentials and for all activities that occur under your account. You agree to immediately notify AlgoAdda of any unauthorized use or security breach.
          </p>
          <p>
            AlgoAdda distinguishes between <strong>Buyer</strong> accounts (enabling strategy purchase, backtest evaluation, and download) and <strong>Seller</strong> accounts (enabling bot upload, backtesting pipeline execution, and store listing). Creating multiple accounts to circumvent moderation or compliance audits is strictly prohibited.
          </p>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 3 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            3. Seller Obligations &amp; White Box Compliance
          </h2>
          <p>
            Sellers on AlgoAdda are required to comply with the White Box verification standards:
          </p>
          <ul className="list-disc pl-6 space-y-1.5 text-[#78786C]">
            <li>
              <strong className="text-[#2C2C24]">Disclosed Strategy Logic:</strong> All uploaded strategies must include readable Python source code and transparent parameter definitions. Obfuscated, encrypted, or black-box binaries are prohibited.
            </li>
            <li>
              <strong className="text-[#2C2C24]">No Guaranteed Returns:</strong> Sellers must not make false, misleading, or unsubstantiated claims regarding profitability. Terms such as &quot;guaranteed returns&quot;, &quot;risk-free&quot;, &quot;100% profit&quot;, or &quot;no loss&quot; are automatically blocked by the platform&apos;s compliance gate.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Code Authenticity:</strong> Sellers warrant that uploaded strategies are original works or properly licensed, and do not infringe on any third-party intellectual property rights.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Accurate Backtesting:</strong> Sellers must not artificially manipulate or cherry-pick backtesting datasets to present distorted quantitative risk-adjusted metrics.
            </li>
          </ul>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 4 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            4. Buyer Responsibilities &amp; Self-Directed Trading
          </h2>
          <p>
            Buyers acknowledge that algorithmic trading involves substantial financial risk. By licensing an algorithm on AlgoAdda, you agree that:
          </p>
          <ul className="list-disc pl-6 space-y-1.5 text-[#78786C]">
            <li>You will evaluate the disclosed strategy logic, indicators, and historical drawdown metrics prior to deployment.</li>
            <li>Execution of any strategy takes place on your own broker terminal or API bridge at your sole discretion.</li>
            <li>You are solely responsible for setting position sizing, stop-loss triggers, and capital allocation limits.</li>
          </ul>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 5 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            5. Software License Terms
          </h2>
          <p>
            Purchasing a strategy grants the Buyer a non-transferable, non-exclusive license to use the specific version of the strategy (&quot;BotVersion&quot;) purchased. Unless explicitly stated in the listing:
          </p>
          <ul className="list-disc pl-6 space-y-1.5 text-[#78786C]">
            <li>Licenses apply to the specific version published and backtested at the time of purchase.</li>
            <li>Purchases do not grant automatic rights to future major architectural rewrites or separate strategy variants.</li>
            <li>Redistributing, sublicensing, or reselling source code files obtained from AlgoAdda is strictly prohibited.</li>
          </ul>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 6 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            6. Payments &amp; Refund Policy
          </h2>
          <p>
            Payments are securely processed via Razorpay. Because algorithms on AlgoAdda are digital software products accompanied by disclosed source logic and immediate download access upon purchase:
          </p>
          <p>
            There is no automatic or self-service refund mechanism. Refund requests are reviewed manually on a case-by-case basis by AlgoAdda administration (e.g., in instances of proven code defect or failure to deliver disclosed logic). To dispute a purchase, contact platform support with relevant order details.
          </p>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 7 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            7. Limitation of Liability
          </h2>
          <p>
            To the maximum extent permitted by applicable law, AlgoAdda, its directors, employees, and affiliates shall not be liable for any direct, indirect, incidental, special, consequential, or punitive damages, including without limitation trading losses, loss of profits, broker API outages, internet latency, data corruption, or market slippage arising from your use of the platform.
          </p>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 8 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            8. Modifications &amp; Governing Law
          </h2>
          <p>
            AlgoAdda reserves the right to modify or replace these Terms at any time. Continued use of the platform following the posting of any revisions constitutes acceptance of the updated terms. These Terms shall be governed by and construed in accordance with the laws of India.
          </p>
        </section>
      </div>

      {/* Navigation Links */}
      <div className="pt-6 border-t border-[#DED8CF] flex flex-wrap items-center justify-between gap-4 text-xs font-body text-[#78786C]">
        <span>Looking for other legal documents?</span>
        <div className="flex items-center gap-4">
          <Link to="/risk-disclosure" className="text-[#5D7052] font-semibold underline hover:text-[#4a5a41]">
            Risk Disclosure
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
