import React from 'react'
import { Link } from 'react-router-dom'
import { Lock, ShieldCheck } from 'lucide-react'

export const PrivacyPage: React.FC = () => {
  return (
    <div className="flex flex-col gap-10 max-w-4xl mx-auto py-4">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <div className="inline-flex items-center gap-2 self-start px-3 py-1 rounded-full bg-[#5D7052]/10 text-[#5D7052] text-xs font-semibold border border-[#5D7052]/20">
          <Lock className="w-3.5 h-3.5" />
          <span>Data Protection &amp; Security</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-heading font-extrabold text-[#2C2C24] tracking-tight">
          Privacy Policy
        </h1>
        <p className="text-sm sm:text-base text-[#78786C] font-body leading-relaxed">
          Last updated: September 2026. This Privacy Policy describes how AlgoAdda collects, uses, and safeguards your personal information and uploaded algorithmic strategy assets.
        </p>
      </div>

      {/* Advisory Note */}
      <div className="p-4 sm:p-5 rounded-2xl bg-[#FAF8F5] border border-[#DED8CF] flex items-start gap-3.5">
        <ShieldCheck className="w-5 h-5 text-[#5D7052] shrink-0 mt-0.5" />
        <div className="text-xs sm:text-sm text-[#4A4A40] font-body leading-relaxed">
          <strong className="text-[#2C2C24] block mb-1">Our Privacy Commitment</strong>
          AlgoAdda respects developer intellectual property and user privacy. We collect only the data necessary to provide backtesting verification, marketplace transactions, and compliance under regulatory standards.
        </div>
      </div>

      {/* Legal Prose Content */}
      <div className="flex flex-col gap-8 text-[#4A4A40] font-body text-sm sm:text-base leading-relaxed">

        {/* Section 1 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            1. Information We Collect
          </h2>
          <p>
            When you register, create listings, backtest algorithms, or purchase software licenses on AlgoAdda, we collect the following categories of information:
          </p>
          <ul className="list-disc pl-6 space-y-1.5 text-[#78786C]">
            <li>
              <strong className="text-[#2C2C24]">Account &amp; Identity Data:</strong> Email address, hashed password, account role (Buyer, Seller, or Admin), and optional seller profile details (display name, bio, social links).
            </li>
            <li>
              <strong className="text-[#2C2C24]">Strategy Code &amp; Parameter Files:</strong> Python strategy source code, entry/exit parameters, dependencies, and configuration files uploaded by Sellers.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Backtest &amp; Execution Metrics:</strong> Automated performance stats (Sharpe ratio, max drawdown, win rate, total return) computed during vectorbt verification runs.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Transaction &amp; Order Records:</strong> Order IDs, licensed BotVersion identifiers, license keys, and payment confirmation metadata.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Usage &amp; Diagnostic Data:</strong> IP addresses, browser agent headers, access timestamps, and error logs for platform security and rate-limiting enforcement.
            </li>
          </ul>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 2 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            2. How We Use Collected Data
          </h2>
          <p>
            We utilize collected data strictly for stated operational and compliance purposes:
          </p>
          <ul className="list-disc pl-6 space-y-1.5 text-[#78786C]">
            <li>To operate the algorithmic trading marketplace, facilitate license delivery, and manage user accounts.</li>
            <li>To execute the automated 4-stage White Box verification pipeline (scanning for prohibited black-box constructs, compliance blocklist screening, and VectorBT backtest execution).</li>
            <li>To generate public marketplace cards and verified metric summaries for listed strategies.</li>
            <li>To prevent fraud, enforce endpoint rate limits, and investigate policy violations or reported listings.</li>
          </ul>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 3 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            3. Strategy Files &amp; Disclosed Logic Framework
          </h2>
          <p>
            Under SEBI&apos;s White Box compliance framework, transparency is fundamental to the AlgoAdda platform:
          </p>
          <ul className="list-disc pl-6 space-y-1.5 text-[#78786C]">
            <li>Uploaded strategy files are stored securely in cloud storage (AWS S3) and processed exclusively by our automated verification pipeline.</li>
            <li>When a seller publishes a listing, the strategy&apos;s disclosed logic, mathematical parameters, and verified performance metrics are displayed to prospective buyers in accordance with the White Box model.</li>
            <li>Source code downloads are granted exclusively to verified buyers upon successful completion of a license transaction.</li>
          </ul>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 4 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            4. Third-Party Service Providers
          </h2>
          <p>
            AlgoAdda works with trusted third-party technology providers to power critical marketplace operations:
          </p>
          <ul className="list-disc pl-6 space-y-1.5 text-[#78786C]">
            <li>
              <strong className="text-[#2C2C24]">Payment Processing (Razorpay):</strong> All financial transactions are processed directly by Razorpay Software Private Limited. AlgoAdda does not collect, store, or have access to your credit card numbers, UPI PINs, or bank account credentials.
            </li>
            <li>
              <strong className="text-[#2C2C24]">Cloud Storage &amp; Infrastructure (Amazon Web Services):</strong> Strategy files, database backups, and backtesting worker clusters are hosted on secure AWS cloud infrastructure.
            </li>
          </ul>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 5 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            5. Data Retention &amp; User Rights
          </h2>
          <p>
            We retain account information and transaction histories for as long as your account remains active or as required by Indian regulatory and taxation recordkeeping requirements.
          </p>
          <p>
            You have the right to request access to your personal data, request correction of inaccurate profile information, or request account closure and data deletion by contacting platform administration, subject to statutory retention obligations for financial transactions.
          </p>
        </section>

        <hr className="border-[#DED8CF]/60" />

        {/* Section 6 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-xl sm:text-2xl font-heading font-bold text-[#2C2C24] tracking-tight">
            6. Security Measures
          </h2>
          <p>
            We employ industry-standard administrative, physical, and technical safeguards to protect your data, including TLS encryption in transit, encrypted storage at rest in AWS S3, and strict role-based access controls for internal systems.
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
          <Link to="/risk-disclosure" className="text-[#5D7052] font-semibold underline hover:text-[#4a5a41]">
            Risk Disclosure
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
