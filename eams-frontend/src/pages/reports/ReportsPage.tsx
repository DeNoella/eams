import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import apiClient from '@/api/client'
import { useAuthStore } from '@/store/authStore'

type ReportKey = 'asset-inventory' | 'licence-compliance' | 'security-posture'

const reportOptions: { key: ReportKey; label: string; description: string }[] = [
  { key: 'asset-inventory', label: 'Asset Inventory', description: 'Register snapshot grouped by status and category.' },
  { key: 'licence-compliance', label: 'Licence Compliance', description: 'Seat utilisation, over-allocation, upcoming renewals.' },
  { key: 'security-posture', label: 'Security Posture', description: 'Certificate expiry exposure and key KPIs.' },
]

export default function ReportsPage() {
  const [active, setActive] = useState<ReportKey>('asset-inventory')
  const orgId = useAuthStore((s) => s.user?.organisationId)

  const { data, isFetching } = useQuery({
    queryKey: ['reports', active, orgId],
    enabled: Boolean(orgId),
    queryFn: async () => {
      if (active === 'asset-inventory') {
        return (await apiClient.get('/reports/asset-inventory')).data.data
      }
      const endpoint = active === 'licence-compliance'
        ? '/reports/licence-compliance'
        : '/reports/security-posture'
      return (await apiClient.get(endpoint, { params: { organisationId: orgId } })).data.data
    },
  })

  return (
    <div className="page-content">
      <div className="page-header">
        <h1 className="page-title">Reports</h1>
        <p className="page-subtitle">FR-RP · Audit-ready snapshots drawn from your live E-AMS data.</p>
      </div>
      <div className="report-layout">
        <aside className="report-sidebar">
          {reportOptions.map((opt) => (
            <button
              key={opt.key}
              onClick={() => setActive(opt.key)}
              className={`report-option ${active === opt.key ? 'active' : ''}`}
            >
              <strong>{opt.label}</strong>
              <span>{opt.description}</span>
            </button>
          ))}
        </aside>
        <section className="card report-canvas">
          {isFetching && <p className="page-subtitle">Compiling report…</p>}
          {!isFetching && data && <ReportView payload={data} />}
        </section>
      </div>
    </div>
  )
}

function ReportView({ payload }: { payload: Record<string, unknown> }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {Object.entries(payload).map(([key, value]) => (
        <div key={key} className="report-row">
          <span className="detail-label">{key}</span>
          <pre className="detail-pre">{formatValue(value)}</pre>
        </div>
      ))}
    </div>
  )
}

function formatValue(value: unknown): string {
  if (value == null) return '-'
  if (typeof value === 'object') return JSON.stringify(value, null, 2)
  return String(value)
}
