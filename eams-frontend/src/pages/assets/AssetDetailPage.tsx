import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import apiClient from '@/api/client'
import { invalidateAnalyticsQueries } from '@/lib/queryAnalytics'
import { useCanMutate } from '@/hooks/usePermissions'
import { resolveFormKind } from './AssetCreatePage'

export default function AssetDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const canMutate = useCanMutate()

  const { data, isLoading } = useQuery({
    queryKey: ['asset', id],
    enabled: Boolean(id),
    queryFn: async () => (await apiClient.get(`/assets/${id}`)).data.data,
  })

  const deleteMutation = useMutation({
    mutationFn: async () => apiClient.delete(`/assets/${id}`),
    onSuccess: async () => {
      toast.success('Asset deleted.')
      await invalidateAnalyticsQueries(queryClient)
      await queryClient.invalidateQueries({ queryKey: ['assets'] })
      navigate('/assets')
    },
    onError: (e: unknown) => {
      const err = e as { response?: { data?: { message?: string } } }
      toast.error(err.response?.data?.message || 'Delete failed')
    },
  })

  if (isLoading || !data) {
    return <div className="page-content"><p className="page-subtitle">Loading asset…</p></div>
  }

  const kind = resolveFormKind(String(data.categoryCode || ''))
  const nameLabel =
    kind === 'APPLICATION' ? 'Application Name'
      : kind === 'DATABASE' ? 'Database Name'
      : kind === 'SERVER' ? 'Server Name'
      : 'Equipment Name'

  return (
    <div className="page-content">
      <div className="page-header toolbar-row">
        <div>
          <h1 className="page-title">{data.name}</h1>
          <p className="page-subtitle">{data.assetIdDisplay} · {data.categoryName}</p>
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button className="btn btn-secondary" onClick={() => navigate('/assets')}>Back</button>
          {canMutate && (
            <>
              <button className="btn btn-primary" onClick={() => navigate(`/assets/${id}/edit`)}>Edit</button>
              <button
                className="btn btn-secondary"
                onClick={() => {
                  if (confirm(`Delete asset "${data.name}"?`)) deleteMutation.mutate()
                }}
                disabled={deleteMutation.isPending}
              >
                {deleteMutation.isPending ? 'Deleting…' : 'Delete'}
              </button>
            </>
          )}
        </div>
      </div>

      <div className="detail-grid">
        <Detail label={nameLabel} value={data.name ?? '-'} />
        <Detail label="Unique ID" value={data.assetIdDisplay ?? '-'} />
        <Detail label="Category" value={data.categoryName ?? '-'} />

        {kind === 'SERVER' && <Detail label="Server Type" value={data.serverType ?? '-'} />}

        {(kind === 'APPLICATION' || kind === 'DATABASE' || kind === 'SERVER') && (
          <>
            <Detail label="Year of Installation" value={data.yearOfInstallation != null ? String(data.yearOfInstallation) : '-'} />
            <Detail label="Vendor Name" value={data.vendorName ?? '-'} />
            <Detail label="Version No" value={data.versionNo ?? '-'} />
            <Detail label="Version Date" value={data.versionDate ?? '-'} />
            <Detail label="Number of Licences" value={data.numberOfLicences != null ? String(data.numberOfLicences) : '-'} />
          </>
        )}

        {kind === 'SERVER' && (
          <>
            <Detail label="Operating System" value={data.operatingSystemName ?? '-'} />
            <Detail label="OS Vendor" value={data.osVendorName ?? '-'} />
            <Detail label="OS Version No" value={data.osVersionNo ?? '-'} />
          </>
        )}

        {kind === 'EQUIPMENT' && (
          <>
            <Detail label="Model Number" value={data.modelNumber ?? '-'} />
            <Detail label="Provider Name" value={data.vendorName ?? '-'} />
            <Detail label="Licence Type" value={data.equipmentLicenceType ?? '-'} />
          </>
        )}

        <Detail label="Hosting Location" value={data.locationName ?? '-'} />
        <Detail label="Hosting Institution" value={data.hostingInstitution ?? '-'} />
        <Detail label="Staff in Charge" value={data.staffInCharge ?? '-'} />

        <Detail label="Criticality" value={String(data.criticality ?? '-')} />
        <Detail label="Status" value={String(data.status ?? '-')} />
        <Detail label="Lifecycle Stage" value={String(data.lifecycleStage ?? '-')} />
      </div>

      {data.functionsOfSystem && (
        <div className="card" style={{ marginTop: '16px' }}>
          <h3 className="section-title">Functions of the System</h3>
          <p>{data.functionsOfSystem}</p>
        </div>
      )}
    </div>
  )
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div className="card detail-card">
      <span className="detail-label">{label}</span>
      <span className="detail-value">{value}</span>
    </div>
  )
}
