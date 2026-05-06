import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus, Pencil, Trash2, Eye, Download, AlertTriangle } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import apiClient from '@/api/client'
import DataTable from '@/components/ui/DataTable'
import StatusBadge from '@/components/ui/StatusBadge'
import { invalidateAnalyticsQueries } from '@/lib/queryAnalytics'
import { useCanMutate } from '@/hooks/usePermissions'

type ViewMode = 'all' | 'expiring-30' | 'expiring-90' | 'expiring-180' | 'expiring-custom'

interface LicenceApi {
  id: string
  assetName: string
  licenceType: string
  totalSeats?: number
  usedSeats?: number
  availableSeats?: number
  utilisationPct?: number
  expiryDate?: string
  status: string
  daysRemaining?: number
}

const VIEW_LABELS: Record<ViewMode, string> = {
  'all': 'All Licences',
  'expiring-30': 'Expiring ≤30 days',
  'expiring-90': 'Expiring ≤90 days',
  'expiring-180': 'Expiring ≤180 days',
  'expiring-custom': 'Custom window',
}

function expiringDays(mode: ViewMode, customDays: number): number | null {
  if (mode === 'expiring-30') return 30
  if (mode === 'expiring-90') return 90
  if (mode === 'expiring-180') return 180
  if (mode === 'expiring-custom') return Math.max(0, customDays)
  return null
}

export default function LicenceListPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const canMutate = useCanMutate()
  const [page, setPage] = useState(0)
  const [view, setView] = useState<ViewMode>('all')
  const [customDays, setCustomDays] = useState<number>(60)

  const { data, isLoading } = useQuery({
    queryKey: ['licences', view, page, customDays],
    queryFn: async () => {
      const days = expiringDays(view, customDays)
      const path = days != null
        ? `/licences/expiring?days=${days}&page=${page}&size=25`
        : `/licences?page=${page}&size=25`
      return (await apiClient.get(path)).data.data
    },
  })

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => apiClient.delete(`/licences/${id}`),
    onSuccess: async () => {
      toast.success('Licence deleted.')
      await invalidateAnalyticsQueries(queryClient)
      await queryClient.invalidateQueries({ queryKey: ['licences'] })
    },
    onError: (e: unknown) => {
      const err = e as { response?: { data?: { message?: string } } }
      toast.error(err.response?.data?.message || 'Delete failed')
    },
  })

  const rows = (data?.content || []) as LicenceApi[]
  const totalPages = data?.totalPages || 1

  const columns = [
    { accessorKey: 'licenceType', header: 'Type' },
    { accessorKey: 'assetName', header: 'Asset' },
    {
      accessorKey: 'seats',
      header: 'Seats',
      cell: ({ row }: { row: { original: LicenceApi } }) =>
        row.original.totalSeats ? `${row.original.usedSeats ?? 0}/${row.original.totalSeats}` : '-',
    },
    {
      accessorKey: 'utilisationPct',
      header: 'Utilisation',
      cell: ({ row }: { row: { original: LicenceApi } }) =>
        row.original.utilisationPct != null ? `${Number(row.original.utilisationPct).toFixed(0)}%` : '-',
    },
    { accessorKey: 'expiryDate', header: 'Expiry' },
    {
      accessorKey: 'daysRemaining',
      header: 'Days Left',
      cell: ({ row }: { row: { original: LicenceApi } }) => {
        const d = Number(row.original.daysRemaining ?? 0)
        if (!row.original.expiryDate) return '-'
        const cls = d <= 0 ? 'days-critical' : d <= 30 ? 'days-critical' : d <= 90 ? 'days-warning' : 'days-ok'
        return <span className={`days-pill ${cls}`}>{d}d</span>
      },
    },
    {
      accessorKey: 'status',
      header: 'Status',
      cell: ({ row }: { row: { original: LicenceApi } }) => <StatusBadge status={row.original.status} />,
    },
    {
      id: 'actions',
      header: 'Actions',
      cell: ({ row }: { row: { original: LicenceApi } }) => (
        <div style={{ display: 'flex', gap: '8px' }}>
          <button aria-label="View" className="icon-btn" onClick={() => navigate(`/licences/${row.original.id}`)}>
            <Eye size={16} />
          </button>
          {canMutate && (
            <>
              <button aria-label="Edit" className="icon-btn" onClick={() => navigate(`/licences/${row.original.id}/edit`)}>
                <Pencil size={16} />
              </button>
              <button
                aria-label="Delete"
                className="icon-btn icon-btn-danger"
                onClick={() => {
                  if (confirm(`Delete licence ${row.original.licenceType} on ${row.original.assetName}?`)) {
                    deleteMutation.mutate(row.original.id)
                  }
                }}
              >
                <Trash2 size={16} />
              </button>
            </>
          )}
        </div>
      ),
    },
  ]

  const onExport = async (format: 'xlsx' | 'csv') => {
    const params: Record<string, string> = { format }
    const days = expiringDays(view, customDays)
    if (days != null) params.expiringWithinDays = String(days)
    const r = await apiClient.get('/licences/export', { params, responseType: 'blob' })
    const blob = new Blob([r.data], { type: String(r.headers['content-type'] || 'application/octet-stream') })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `goshen-licences.${format}`
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  }

  const subtitle = view === 'all'
    ? 'FR-CL · Software licence register and seat governance.'
    : `FR-CL · ${VIEW_LABELS[view]} — dashboard already reflects these renewals.`

  return (
    <div className="page-content">
      <div className="page-header toolbar-row">
        <div>
          <h1 className="page-title">Licences</h1>
          <p className="page-subtitle">{subtitle}</p>
        </div>
        {canMutate && (
          <button className="btn btn-primary" onClick={() => navigate('/licences/new')}>
            <Plus size={16} /> Add Licence
          </button>
        )}
      </div>
      <div className="card">
        <div className="tab-bar">
          {(Object.keys(VIEW_LABELS) as ViewMode[]).map((k) => (
            <button
              key={k}
              className={`tab-btn ${view === k ? 'active' : ''}`}
              onClick={() => { setView(k); setPage(0) }}
            >
              {k.startsWith('expiring') && <AlertTriangle size={14} />} {VIEW_LABELS[k]}
            </button>
          ))}
          {view === 'expiring-custom' && (
            <label className="form-field" style={{ minWidth: '140px' }}>
              <span>Within (days)</span>
              <input
                type="number"
                min={0}
                value={customDays}
                onChange={(e) => { setCustomDays(Number(e.target.value) || 0); setPage(0) }}
              />
            </label>
          )}
          <div className="tab-spacer" />
          <button className="btn btn-secondary" onClick={() => onExport('xlsx')}>
            <Download size={16} /> Excel
          </button>
          <button className="btn btn-secondary" onClick={() => onExport('csv')}>
            <Download size={16} /> CSV
          </button>
        </div>

        <DataTable
          columns={columns}
          data={rows}
          isLoading={isLoading}
          emptyMessage={view === 'all' ? 'No licences tracked yet.' : 'No licences expiring in that window.'}
        />
        <div className="pagination-row">
          <p className="page-subtitle">Page {page + 1} of {totalPages} · {data?.totalElements ?? 0} entries</p>
          <div style={{ display: 'flex', gap: '8px' }}>
            <button className="btn btn-secondary" disabled={page === 0} onClick={() => setPage(Math.max(0, page - 1))}>Previous</button>
            <button className="btn btn-secondary" disabled={page >= totalPages - 1} onClick={() => setPage(Math.min(totalPages - 1, page + 1))}>Next</button>
          </div>
        </div>
      </div>
    </div>
  )
}
