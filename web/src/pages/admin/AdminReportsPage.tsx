import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../lib/api'
import type { ListingReportResponse, ResolveAction } from '../../lib/api'
import { Button, Card } from '../../components/ui'
import {
  ShieldAlert,
  AlertTriangle,
  CheckCircle2,
  XCircle,
  UserX,
  UserCheck,
  ExternalLink,
  RefreshCw,
  Search,
  Filter,
} from 'lucide-react'

export const AdminReportsPage: React.FC = () => {
  const [reports, setReports] = useState<ListingReportResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filterStatus, setFilterStatus] = useState<'ALL' | 'PENDING' | 'RESOLVED'>('PENDING')
  const [actionNotes, setActionNotes] = useState<{ [reportId: string]: string }>({})
  const [processingId, setProcessingId] = useState<string | null>(null)
  const [actionSuccess, setActionSuccess] = useState<string | null>(null)

  const fetchReports = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await api.getReportQueue()
      setReports(data)
    } catch (err: any) {
      setError(err.message || 'Failed to fetch report queue')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchReports()
  }, [])

  const handleResolve = async (reportId: string, action: ResolveAction) => {
    setProcessingId(reportId)
    setActionSuccess(null)
    try {
      const notes = actionNotes[reportId] || undefined
      await api.resolveReport(reportId, { action, notes })
      setActionSuccess(`Report successfully ${action === 'FORCE_DELIST' ? 'resolved with FORCE-DELIST' : 'dismissed'}.`)
      await fetchReports()
    } catch (err: any) {
      alert(`Action failed: ${err.message || 'Error resolving report'}`)
    } finally {
      setProcessingId(null)
    }
  }

  const handleSuspendSeller = async (sellerId: string, isSuspended: boolean) => {
    if (!confirm(`Are you sure you want to ${isSuspended ? 'unsuspend' : 'suspend'} this seller?`)) {
      return
    }
    setProcessingId(sellerId)
    setActionSuccess(null)
    try {
      if (isSuspended) {
        await api.unsuspendSeller(sellerId)
        setActionSuccess('Seller unsuspended successfully.')
      } else {
        await api.suspendSeller(sellerId)
        setActionSuccess('Seller suspended successfully. All their listings are now hidden.')
      }
      await fetchReports()
    } catch (err: any) {
      alert(`Seller action failed: ${err.message || 'Error updating seller suspension'}`)
    } finally {
      setProcessingId(null)
    }
  }

  const filteredReports = reports.filter((r) => {
    if (filterStatus === 'PENDING') return r.status === 'PENDING'
    if (filterStatus === 'RESOLVED') return r.status === 'RESOLVED'
    return true
  })

  const getReasonLabel = (reason: string) => {
    switch (reason) {
      case 'MISLEADING_CLAIMS':
        return 'Misleading Claims'
      case 'MALICIOUS_CODE':
        return 'Malicious / Suspicious Code'
      case 'COPYRIGHT_VIOLATION':
        return 'Copyright / IP Violation'
      case 'SPAM':
        return 'Spam or Junk'
      default:
        return reason
    }
  }

  return (
    <div className="flex flex-col gap-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-[#DED8CF]/50">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <ShieldAlert className="w-6 h-6 text-[#A85448]" />
            <h1 className="text-2xl font-heading font-extrabold text-[#2C2C24]">
              Trust &amp; Moderation Queue
            </h1>
          </div>
          <p className="text-sm font-body text-[#78786C]">
            Review flagged listings, issue force-delists, and manage seller suspensions.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="secondary"
            size="sm"
            onClick={fetchReports}
            disabled={isLoading}
            className="gap-1.5"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </Button>
        </div>
      </div>

      {actionSuccess && (
        <div className="p-4 rounded-xl bg-[#5D7052]/10 border border-[#5D7052]/30 flex items-center gap-3 text-sm text-[#5D7052]">
          <CheckCircle2 className="w-5 h-5 shrink-0" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {/* Filter tabs */}
      <div className="flex items-center gap-2 border-b border-[#DED8CF]">
        {(['PENDING', 'RESOLVED', 'ALL'] as const).map((status) => (
          <button
            key={status}
            onClick={() => setFilterStatus(status)}
            className={`px-4 py-2.5 text-xs font-semibold border-b-2 transition-colors -mb-px ${
              filterStatus === status
                ? 'border-[#5D7052] text-[#5D7052]'
                : 'border-transparent text-[#78786C] hover:text-[#2C2C24]'
            }`}
          >
            {status === 'PENDING' && `Pending (${reports.filter((r) => r.status === 'PENDING').length})`}
            {status === 'RESOLVED' && `Resolved (${reports.filter((r) => r.status === 'RESOLVED').length})`}
            {status === 'ALL' && `All (${reports.length})`}
          </button>
        ))}
      </div>

      {/* Report Table / List */}
      {isLoading ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] gap-3">
          <div className="w-8 h-8 border-3 border-[#5D7052] border-t-transparent rounded-full animate-spin"></div>
          <p className="text-sm text-[#78786C]">Loading moderation reports...</p>
        </div>
      ) : error ? (
        <div className="p-6 rounded-xl bg-[#A85448]/10 border border-[#A85448]/30 text-center text-sm text-[#A85448]">
          {error}
        </div>
      ) : filteredReports.length === 0 ? (
        <Card className="p-12 text-center">
          <CheckCircle2 className="w-12 h-12 text-[#5D7052] mx-auto mb-3" />
          <h3 className="font-heading font-bold text-lg text-[#2C2C24] mb-1">
            No Reports Found
          </h3>
          <p className="text-sm text-[#78786C]">
            {filterStatus === 'PENDING'
              ? 'There are no pending reports requiring action.'
              : 'No reports match the selected status filter.'}
          </p>
        </Card>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-[#DED8CF] bg-white shadow-sm">
          <table className="w-full text-left text-xs text-[#2C2C24]">
            <thead className="bg-[#FDFCF8] border-b border-[#DED8CF] text-[#78786C] font-semibold">
              <tr>
                <th className="p-4">Report Date</th>
                <th className="p-4">Listing / Bot</th>
                <th className="p-4">Seller</th>
                <th className="p-4">Reporter</th>
                <th className="p-4">Reason &amp; Comment</th>
                <th className="p-4">Status</th>
                <th className="p-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#DED8CF]/60">
              {filteredReports.map((report) => (
                <tr key={report.id} className="hover:bg-[#FDFCF8]/60 transition-colors">
                  <td className="p-4 whitespace-nowrap text-[#78786C]">
                    {new Date(report.createdAt).toLocaleString()}
                  </td>

                  <td className="p-4">
                    <div className="flex flex-col">
                      <Link
                        to={`/marketplace/${report.listingId}`}
                        className="font-bold text-[#2C2C24] hover:text-[#5D7052] inline-flex items-center gap-1"
                      >
                        <span>{report.listingName}</span>
                        <ExternalLink className="w-3 h-3 text-[#78786C]" />
                      </Link>
                      <span className="text-[11px] text-[#78786C]">ID: {report.listingId.substring(0, 8)}...</span>
                    </div>
                  </td>

                  <td className="p-4">
                    <div className="flex flex-col">
                      <span className="font-medium">{report.sellerDisplayName}</span>
                      <div className="flex items-center gap-1 mt-0.5">
                        <span className="text-[11px] text-[#78786C]">{report.sellerEmail}</span>
                        {report.sellerSuspended && (
                          <span className="px-1.5 py-0.2 text-[10px] font-bold rounded bg-[#A85448]/15 text-[#A85448]">
                            SUSPENDED
                          </span>
                        )}
                      </div>
                    </div>
                  </td>

                  <td className="p-4 text-[#78786C]">
                    {report.reporterEmail}
                  </td>

                  <td className="p-4 max-w-xs">
                    <span className="inline-block px-2 py-0.5 rounded text-[11px] font-semibold bg-[#C18C5D]/15 text-[#C18C5D] mb-1">
                      {getReasonLabel(report.reason)}
                    </span>
                    {report.comment && (
                      <p className="text-[#78786C] line-clamp-2 italic">"{report.comment}"</p>
                    )}
                  </td>

                  <td className="p-4 whitespace-nowrap">
                    {report.status === 'PENDING' ? (
                      <span className="px-2.5 py-1 rounded-full font-bold text-[10px] bg-[#C18C5D]/20 text-[#C18C5D] border border-[#C18C5D]/30">
                        PENDING
                      </span>
                    ) : (
                      <div className="flex flex-col">
                        <span className="px-2.5 py-1 rounded-full font-bold text-[10px] bg-[#5D7052]/20 text-[#5D7052] border border-[#5D7052]/30 w-fit mb-0.5">
                          RESOLVED ({report.actionTaken})
                        </span>
                        {report.adminNotes && (
                          <span className="text-[10px] text-[#78786C] italic">{report.adminNotes}</span>
                        )}
                      </div>
                    )}
                  </td>

                  <td className="p-4 text-right whitespace-nowrap">
                    {report.status === 'PENDING' ? (
                      <div className="flex flex-col items-end gap-2">
                        <input
                          type="text"
                          placeholder="Admin notes (optional)..."
                          value={actionNotes[report.id] || ''}
                          onChange={(e) =>
                            setActionNotes({ ...actionNotes, [report.id]: e.target.value })
                          }
                          className="px-2.5 py-1 text-[11px] border border-[#DED8CF] rounded-md bg-[#FDFCF8] text-[#2C2C24] w-48 focus:outline-none focus:border-[#5D7052]"
                        />
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => handleResolve(report.id, 'DISMISS')}
                            disabled={processingId === report.id}
                            className="px-2.5 py-1 text-[11px] font-semibold rounded-md border border-[#DED8CF] text-[#78786C] hover:bg-gray-100 disabled:opacity-50"
                            title="Dismiss report as invalid"
                          >
                            Dismiss
                          </button>

                          <button
                            onClick={() => handleResolve(report.id, 'FORCE_DELIST')}
                            disabled={processingId === report.id}
                            className="px-2.5 py-1 text-[11px] font-semibold rounded-md bg-[#A85448] text-white hover:bg-[#8A3F35] disabled:opacity-50 inline-flex items-center gap-1"
                            title="Unpublish this listing immediately"
                          >
                            <AlertTriangle className="w-3 h-3" />
                            <span>Force Delist</span>
                          </button>

                          <button
                            onClick={() => handleSuspendSeller(report.sellerId, report.sellerSuspended)}
                            disabled={processingId === report.sellerId}
                            className={`px-2.5 py-1 text-[11px] font-semibold rounded-md border disabled:opacity-50 inline-flex items-center gap-1 ${
                              report.sellerSuspended
                                ? 'border-[#5D7052] text-[#5D7052] hover:bg-[#5D7052]/10'
                                : 'border-[#A85448] text-[#A85448] hover:bg-[#A85448]/10'
                            }`}
                            title={report.sellerSuspended ? 'Unsuspend seller account' : 'Suspend seller account'}
                          >
                            {report.sellerSuspended ? (
                              <>
                                <UserCheck className="w-3 h-3" />
                                <span>Unsuspend</span>
                              </>
                            ) : (
                              <>
                                <UserX className="w-3 h-3" />
                                <span>Suspend Seller</span>
                              </>
                            )}
                          </button>
                        </div>
                      </div>
                    ) : (
                      <button
                        onClick={() => handleSuspendSeller(report.sellerId, report.sellerSuspended)}
                        disabled={processingId === report.sellerId}
                        className={`px-2.5 py-1 text-[11px] font-semibold rounded-md border disabled:opacity-50 inline-flex items-center gap-1 ${
                          report.sellerSuspended
                            ? 'border-[#5D7052] text-[#5D7052] hover:bg-[#5D7052]/10'
                            : 'border-[#78786C] text-[#78786C] hover:bg-gray-100'
                        }`}
                      >
                        {report.sellerSuspended ? 'Unsuspend Seller' : 'Suspend Seller'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

export default AdminReportsPage
