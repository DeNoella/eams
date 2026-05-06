import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import apiClient from '@/api/client'
import { invalidateAnalyticsQueries } from '@/lib/queryAnalytics'
import { useCanMutate } from '@/hooks/usePermissions'

export default function LicenceDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const canMutate = useCanMutate()

  const { data, isLoading } = useQuery({
    queryKey: ['licence', id],
    enabled: Boolean(id),
    queryFn: async () => (await apiClient.get(`/licences/${id}`)).data.data,
  })

  const deleteMutation = useMutation({
    mutationFn: async () => apiClient.delete(`/licences/${id}`),
    onSuccess: async () => {
      toast.success('Licence deleted.')
      await invalidateAnalyticsQueries(queryClient)
      await queryClient.invalidateQueries({ queryKey: ['licences'] })
      navigate('/licences')
    },
    onError: (e: unknown) => {
      const err = e as { response?: { data?: { message?: string } } }
      toast.error(err.response?.data?.message || 'Delete failed')
    },
  })

  if (isLoading || !data) {
    return <div className="page-content"><p className="page-subtitle">Loading licence…</p></div>
  }

  return (
    <div className="page-content">
      <div className="page-header toolbar-row">
        <div>
          <h1 className="page-title">{data.licenceType} · {data.assetName}</h1>
          <p className="page-subtitle">Seats {data.usedSeats ?? 0}/{data.totalSeats ?? '∞'}</p>
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button className="btn btn-secondary" onClick={() => navigate('/licences')}>Back</button>
          {canMutate && (
            <>
              <button className="btn btn-primary" onClick={() => navigate(`/licences/${id}/edit`)}>Edit</button>
              <button
                className="btn btn-secondary"
                onClick={() => {
                  if (confirm(`Delete licence on "${data.assetName}"?`)) deleteMutation.mutate()
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
        <Detail label="Asset" value={data.assetName ?? '-'} />
        <Detail label="Licence Type" value={data.licenceType ?? '-'} />
        <Detail label="Total Seats" value={data.totalSeats != null ? String(data.totalSeats) : '-'} />
        <Detail label="Used Seats" value={data.usedSeats != null ? String(data.usedSeats) : '0'} />
        <Detail label="Expiry Date" value={data.expiryDate ?? '-'} />
        <Detail label="Days Remaining" value={data.daysRemaining != null && data.expiryDate ? `${data.daysRemaining}d` : '-'} />
        <Detail label="Status" value={data.status ?? '-'} />
        <Detail label="Key / Reference" value={data.keyOrReference ?? '-'} />
      </div>
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
