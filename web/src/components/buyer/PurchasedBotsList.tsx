import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Card, Button } from '../ui'
import { ShoppingBag, ArrowRight, ShieldCheck, Sparkles, Download, CheckCircle2, AlertTriangle, FileCode } from 'lucide-react'
import { api, BuyerLicense } from '../../lib/api'

export const PurchasedBotsList: React.FC = () => {
  const [licenses, setLicenses] = useState<BuyerLicense[]>([])
  const [loading, setLoading] = useState<boolean>(true)
  const [downloadingId, setDownloadingId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const fetchLicenses = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await api.getBuyerLicenses()
      setLicenses(data)
    } catch (err: any) {
      console.error('Failed to fetch licenses:', err)
      setError(err.message || 'Failed to load purchased algorithms')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchLicenses()
  }, [])

  const handleDownload = async (licenseId: string, botName: string) => {
    try {
      setDownloadingId(licenseId)
      const response = await api.downloadLicense(licenseId)
      if (response.downloadUrl) {
        const link = document.createElement('a')
        link.href = response.downloadUrl
        link.download = `${botName.replace(/\s+/g, '_')}_source.py`
        link.target = '_blank'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      }
    } catch (err: any) {
      alert(`Download failed: ${err.message || 'Access denied or link expired'}`)
    } finally {
      setDownloadingId(null)
    }
  }

  if (loading) {
    return (
      <Card className="p-8 text-center flex flex-col items-center justify-center gap-3">
        <div className="w-6 h-6 border-2 border-[#5D7052] border-t-transparent rounded-full animate-spin"></div>
        <p className="text-sm text-[#78786C]">Loading your purchased algorithms...</p>
      </Card>
    )
  }

  if (error) {
    return (
      <Card className="p-6 text-center flex flex-col items-center justify-center gap-3 border border-red-200 bg-red-50/50">
        <AlertTriangle className="w-6 h-6 text-red-600" />
        <p className="text-sm text-red-700">{error}</p>
        <Button variant="secondary" size="sm" onClick={fetchLicenses}>
          Retry
        </Button>
      </Card>
    )
  }

  if (licenses.length > 0) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {licenses.map((license) => (
          <Card key={license.licenseId} className="p-6 flex flex-col justify-between gap-5 border-t-4 border-t-[#5D7052]">
            <div className="flex flex-col gap-3">
              <div className="flex items-start justify-between gap-2">
                <div className="flex flex-col gap-1">
                  <div className="flex items-center gap-2">
                    <h4 className="font-heading font-bold text-lg text-[#2C2C24]">
                      {license.botName}
                    </h4>
                    <span className="text-[11px] font-semibold bg-[#5D7052]/10 text-[#5D7052] px-2 py-0.5 rounded">
                      v{license.versionNumber}
                    </span>
                  </div>
                  <span className="text-xs text-[#78786C]">
                    By {license.sellerName} {license.isOfficial ? '• Official Store' : ''}
                  </span>
                </div>

                {license.revoked ? (
                  <span className="text-xs font-semibold text-red-600 bg-red-100 px-2.5 py-1 rounded-full shrink-0">
                    Revoked
                  </span>
                ) : license.isActive ? (
                  <span className="text-xs font-semibold text-[#5D7052] bg-[#5D7052]/10 px-2.5 py-1 rounded-full shrink-0 flex items-center gap-1">
                    <CheckCircle2 className="w-3 h-3" />
                    Active
                  </span>
                ) : (
                  <span className="text-xs font-semibold text-amber-600 bg-amber-100 px-2.5 py-1 rounded-full shrink-0">
                    Expired
                  </span>
                )}
              </div>

              <div className="flex flex-col gap-1 text-xs text-[#78786C] pt-2 border-t border-[#DED8CF]/40">
                <div className="flex justify-between">
                  <span>Issued Date:</span>
                  <span className="font-medium text-[#2C2C24]">
                    {new Date(license.issuedAt).toLocaleDateString()}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span>License Term:</span>
                  <span className="font-medium text-[#2C2C24]">
                    {license.isPerpetual ? 'Perpetual (One-Time)' : license.expiresAt ? new Date(license.expiresAt).toLocaleDateString() : 'Active'}
                  </span>
                </div>
              </div>
            </div>

            <Button
              variant="primary"
              size="sm"
              className="w-full gap-2"
              disabled={!license.isActive || downloadingId === license.licenseId}
              onClick={() => handleDownload(license.licenseId, license.botName)}
            >
              {downloadingId === license.licenseId ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <Download className="w-4 h-4" />
              )}
              <span>{downloadingId === license.licenseId ? 'Preparing Presigned S3 Link...' : 'Download Strategy Source'}</span>
            </Button>
          </Card>
        ))}
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
