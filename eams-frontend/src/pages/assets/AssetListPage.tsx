import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus, Search, Download, Pencil, Trash2, Eye } from 'lucide-react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'
import apiClient from '@/api/client'
import DataTable from '@/components/ui/DataTable'
import StatusBadge from '@/components/ui/StatusBadge'
import { invalidateAnalyticsQueries } from '@/lib/queryAnalytics'
import { useCanMutate } from '@/hooks/usePermissions'

interface AssetApi {
  id: string
  assetIdDisplay: string
  name: string
  categoryName: string
  locationName: string
  status: string
  criticality: string
  assignedUserName: string
  lastAuditDate: string
}

interface AssetView {
  id: string
  assetIdDisplay: string
  name: string
  category: string
  location: string
  status: string
  criticality: string
  assignedTo: string
  lastAudited: string
}

export default function AssetListPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const canMutate = useCanMutate()
  const [params, setParams] = useSearchParams()
  const initialSearch = params.get('search') || ''
  const initialPage = Number(params.get('page') || '0')
  const [search, setSearch] = useState(initialSearch)
  const [page, setPage] = useState(initialPage)

  const { data, isLoading } = useQuery({
    queryKey: ['assets', search, page],
    queryFn: async () => {
      const q = new URLSearchParams({ page: String(page), size: '25' })
      if (search) q.append('search', search)
      const r = await apiClient.get(`/assets?${q}`)
      return r.data.data
    },
  })

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => apiClient.delete(`/assets/${id}`),
    onSuccess: async () => {
      toast.success('Asset deleted.')
      await invalidateAnalyticsQueries(queryClient)
      await queryClient.invalidateQueries({ queryKey: ['assets'] })
    },
    onError: (e: unknown) => {
      const err = e as { response?: { data?: { message?: string } } }
      toast.error(err.response?.data?.message || 'Delete failed')
    },
  })

  const assets: AssetView[] = ((data?.content || []) as AssetApi[]).map((item) => ({
    id: item.id,
    assetIdDisplay: item.assetIdDisplay,
    name: item.name,
    category: item.categoryName || '-',
    location: item.locationName || '-',
    status: item.status,
    criticality: item.criticality,
    assignedTo: item.assignedUserName || '-',
    lastAudited: item.lastAuditDate || '-',
  }))
  const totalPages = data?.totalPages || 1

  const columns = [
    { accessorKey: 'assetIdDisplay', header: 'Asset ID' },
    { accessorKey: 'name', header: 'Name' },
    { accessorKey: 'category', header: 'Category' },
    { accessorKey: 'location', header: 'Location' },
    {
      accessorKey: 'status',
      header: 'Status',
      cell: ({ row }: { row: { original: AssetView } }) => <StatusBadge status={row.original.status} />,
    },
    { accessorKey: 'criticality', header: 'Criticality' },
    { accessorKey: 'assignedTo', header: 'Assigned To' },
    { accessorKey: 'lastAudited', header: 'Last Audited' },
    {
      id: 'actions',
      header: 'Actions',
      cell: ({ row }: { row: { original: AssetView } }) => (
        <div style={{ display: 'flex', gap: '8px' }}>
          <button
            aria-label="View"
            className="icon-btn"
            onClick={() => navigate(`/assets/${row.original.id}`)}
          >
            <Eye size={16} />
          </button>
          {canMutate && (
            <>
              <button
                aria-label="Edit"
                className="icon-btn"
                onClick={() => navigate(`/assets/${row.original.id}/edit`)}
              >
                <Pencil size={16} />
              </button>
              <button
                aria-label="Delete"
                className="icon-btn icon-btn-danger"
                onClick={() => {
                  if (confirm(`Delete asset "${row.original.name}"?`)) {
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

  const syncUrlState = (nextSearch: string, nextPage: number) => {
    const next = new URLSearchParams()
    if (nextSearch) next.set('search', nextSearch)
    if (nextPage > 0) next.set('page', String(nextPage))
    setParams(next)
  }

  const onExport = async (format: 'xlsx' | 'csv') => {
    const response = await apiClient.get('/assets/export', {
      params: { format, search: search || undefined },
      responseType: 'blob',
    })
    const blob = new Blob([response.data], {
      type: String(response.headers['content-type'] || 'application/octet-stream'),
    })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `goshen-assets.${format}`
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="page-content">
      <div className="page-header toolbar-row">
        <div>
          <h1 className="page-title">Asset Register</h1>
          <p className="page-subtitle">Manage your organisation's IT assets</p>
        </div>
        {canMutate && (
          <button onClick={() => navigate('/assets/new')} className="btn btn-primary">
            <Plus size={16} /> Add Asset
          </button>
        )}
      </div>

      <div className="card">
        <div className="filter-bar">
          <div className="search-input-wrap">
            <Search size={16} />
            <input
              type="text"
              placeholder="Search assets…"
              value={search}
              onChange={(e) => {
                const v = e.target.value
                setSearch(v)
                setPage(0)
                syncUrlState(v, 0)
              }}
            />
          </div>
          <button className="btn btn-secondary" onClick={() => onExport('xlsx')}>
            <Download size={16} /> Excel
          </button>
          <button className="btn btn-secondary" onClick={() => onExport('csv')}>
            <Download size={16} /> CSV
          </button>
        </div>

        <DataTable
          columns={columns}
          data={assets}
          isLoading={isLoading}
          emptyMessage="No assets found. Add your first asset to get started."
        />

        <div className="pagination-row">
          <p className="page-subtitle">
            Showing {assets.length} of {data?.totalElements || 0} assets
          </p>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <button
              className="btn btn-secondary"
              onClick={() => {
                const n = Math.max(0, page - 1)
                setPage(n)
                syncUrlState(search, n)
              }}
              disabled={page === 0}
            >
              Previous
            </button>
            <span className="page-subtitle">
              Page {page + 1} of {totalPages}
            </span>
            <button
              className="btn btn-secondary"
              onClick={() => {
                const n = Math.min(Math.max(totalPages - 1, 0), page + 1)
                setPage(n)
                syncUrlState(search, n)
              }}
              disabled={page >= totalPages - 1}
            >
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
