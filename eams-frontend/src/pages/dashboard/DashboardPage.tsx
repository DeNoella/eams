import { Package, AlertTriangle, FileKey, Shield, ClipboardList, Users } from 'lucide-react'
import type { LucideProps } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import StatusDonutChart from '@/components/charts/StatusDonutChart'
import ExpiryTimelineChart from '@/components/charts/ExpiryTimelineChart'
import { fetchAiInsights, fetchExecutiveDashboard } from '@/api/dashboard'

// BNR-aligned status / category palette: earth tones + restrained semantic colors
const statusColors: Record<string, string> = {
  ACTIVE: '#1F7A3D',
  IN_MAINTENANCE: '#7A6B1A',
  INACTIVE: '#8B7B5C',
  DECOMMISSIONED: '#B91C1C',
}

const categoryPalette = [
  '#6B2D0E', // primary brown
  '#7A6B1A', // olive/gold
  '#8B4513', // medium brown
  '#1F7A3D', // success green
  '#B45309', // warning amber
  '#1E4FA0', // info blue
  '#5B3A2A', // deep wood
  '#9C7A1F', // muted gold
]

export default function DashboardPage() {
  const { data: executive, isLoading } = useQuery({
    queryKey: ['dashboard-executive'],
    queryFn: fetchExecutiveDashboard,
    refetchInterval: 60_000,
    refetchOnWindowFocus: true,
  })

  const { data: aiInsights } = useQuery({
    queryKey: ['dashboard-ai-insights'],
    queryFn: fetchAiInsights,
    refetchInterval: 60_000,
  })

  const stats = {
    totalAssets: executive?.totalAssets ?? 0,
    totalUsers: (executive as unknown as { totalUsers?: number })?.totalUsers ?? 0,
    criticalAssets: executive?.assetCountByCriticality?.CRITICAL ?? 0,
    certsExpiring: executive?.certificatesExpiring30Days ?? 0,
    licencesOverused: executive?.licencesAboveSeatThreshold ?? 0,
    openActions: (executive?.overdueRevertsCount ?? 0) + (executive?.accessGrantsExpiringSoon ?? 0),
  }

  const statusChart = Object.entries(executive?.assetCountByStatus ?? {}).map(([key, value]) => ({
    name: key.replaceAll('_', ' '),
    value: value as number,
    color: statusColors[key] || '#6B2D0E',
  }))

  const categoryChart = Object.entries(
    (executive as unknown as { assetCountByCategory?: Record<string, number> })?.assetCountByCategory ?? {},
  ).map(([name, value], index) => ({
    name,
    value: value as number,
    color: categoryPalette[index % categoryPalette.length],
  }))

  const expiryTimeline = [
    { month: '30d', certs: executive?.certificatesExpiring30Days ?? 0, licences: executive?.licencesAboveSeatThreshold ?? 0 },
    { month: '90d', certs: executive?.certificatesExpiring90Days ?? 0, licences: executive?.accessGrantsExpiringSoon ?? 0 },
    { month: '180d', certs: executive?.certificatesExpiring180Days ?? 0, licences: executive?.overdueRevertsCount ?? 0 },
  ]

  return (
    <div className="page-content">
      <div className="page-header">
        <h1 className="page-title">National Bank of Rwanda Dashboard</h1>
        <p className="page-subtitle">Executive overview · auto-refreshes on registry changes</p>
      </div>

      <div className="kpi-grid">
        <KpiCard title="Total Assets" value={stats.totalAssets} icon={Package} subtitle="live" />
        <KpiCard title="Critical Assets" value={stats.criticalAssets} icon={AlertTriangle} alert />
        <KpiCard title="Certs Expiring" value={stats.certsExpiring} icon={FileKey} subtitle="≤30 days" />
        <KpiCard title="Licence Alerts" value={stats.licencesOverused} icon={Shield} subtitle="seat threshold" />
        <KpiCard title="Users" value={stats.totalUsers} icon={Users} subtitle="registered" />
      </div>

      <div className="grid-3">
        <div className="card">
          <h3 className="section-title">Asset Status</h3>
          <div className="chart-container">
            <StatusDonutChart data={statusChart} />
          </div>
        </div>
        <div className="card grid-span-2">
          <h3 className="section-title">Expiry Timeline</h3>
          <div className="chart-container">
            <ExpiryTimelineChart data={expiryTimeline} />
          </div>
        </div>
      </div>

      <div className="grid-3">
        <div className="card">
          <h3 className="section-title">Assets by Category</h3>
          <div className="chart-container">
            {categoryChart.length > 0 ? (
              <StatusDonutChart data={categoryChart} />
            ) : (
              <p className="page-subtitle">No categories yet.</p>
            )}
          </div>
        </div>
        <div className="card">
          <h3 className="section-title">Open Actions</h3>
          <p className="page-subtitle">Overdue reverts: <strong>{executive?.overdueRevertsCount ?? 0}</strong></p>
          <p className="page-subtitle">Access grants expiring ≤7 days: <strong>{executive?.accessGrantsExpiringSoon ?? 0}</strong></p>
          <p className="page-subtitle">Total open: <strong>{stats.openActions}</strong></p>
        </div>
        <div className="card">
          <h3 className="section-title">
            AI Intelligence <ClipboardList size={14} style={{ verticalAlign: 'middle', marginLeft: 4 }} />
          </h3>
          <p className="page-subtitle">
            {isLoading ? 'Loading insights…' : String(aiInsights?.note || 'Connected to live E-AMS data.')}
          </p>
        </div>
      </div>
    </div>
  )
}

function KpiCard({ title, value, icon: Icon, subtitle, alert }: {
  title: string
  value: number
  icon: React.ComponentType<LucideProps>
  subtitle?: string
  alert?: boolean
}) {
  return (
    <div className={`kpi-card ${alert ? 'alert' : ''}`}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div>
          <p className="kpi-title">{title}</p>
          <p className="kpi-value">{value.toLocaleString()}</p>
          {subtitle && <p className="kpi-subtitle">{subtitle}</p>}
        </div>
        <Icon className="kpi-icon" />
      </div>
    </div>
  )
}
