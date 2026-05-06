import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import apiClient from '@/api/client'
import { invalidateAnalyticsQueries } from '@/lib/queryAnalytics'
import { useCanMutate } from '@/hooks/usePermissions'

export default function CertificateDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const canMutate = useCanMutate()

  const { data, isLoading } = useQuery({
    queryKey: ['certificate', id],
    enabled: Boolean(id),
    queryFn: async () => (await apiClient.get(`/certificates/${id}`)).data.data,
  })

  const deleteMutation = useMutation({
    mutationFn: async () => apiClient.delete(`/certificates/${id}`),
    onSuccess: async () => {
      toast.success('Certificate deleted.')
      await invalidateAnalyticsQueries(queryClient)
      await queryClient.invalidateQueries({ queryKey: ['certificates'] })
      navigate('/certificates')
    },
    onError: (e: unknown) => {
      const err = e as { response?: { data?: { message?: string } } }
      toast.error(err.response?.data?.message || 'Delete failed')
    },
  })

  if (isLoading || !data) {
    return <div className="page-content"><p className="page-subtitle">Loading certificate…</p></div>
  }

  return (
    <div className="page-content">
      <div className="page-header toolbar-row">
        <div>
          <h1 className="page-title">{data.commonName || '(unnamed)'}</h1>
          <p className="page-subtitle">{data.certificateType} · {data.assetName}</p>
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button className="btn btn-secondary" onClick={() => navigate('/certificates')}>Back</button>
          {canMutate && (
            <>
              <button className="btn btn-primary" onClick={() => navigate(`/certificates/${id}/edit`)}>Edit</button>
              <button
                className="btn btn-secondary"
                onClick={() => {
                  if (confirm(`Delete certificate "${data.commonName}"?`)) deleteMutation.mutate()
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
        <Detail label="Common Name" value={data.commonName ?? '-'} />
        <Detail label="Asset" value={data.assetName ?? '-'} />
        <Detail label="Type" value={data.certificateType ?? '-'} />
        <Detail label="Serial Number" value={data.serialNumber ?? '-'} />
        <Detail label="Issue Date" value={data.issueDate ?? '-'} />
        <Detail label="Expiry Date" value={data.expiryDate ?? '-'} />
        <Detail label="Days Remaining" value={data.daysRemaining != null ? `${data.daysRemaining}d` : '-'} />
        <Detail label="Status" value={data.status ?? '-'} />
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
