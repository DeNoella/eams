import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import apiClient from '@/api/client'
import { invalidateAnalyticsQueries } from '@/lib/queryAnalytics'

interface AssetOption { id: string; name: string; assetIdDisplay: string }

interface LicenceForm {
  assetId: string
  licenceType: 'PERPETUAL' | 'SUBSCRIPTION' | 'VOLUME' | 'PER_SEAT' | 'PER_CORE' | 'TRIAL' | 'OEM' | 'OPEN_SOURCE'
  totalSeats?: number
  usedSeats?: number
  expiryDate?: string
  status?: 'VALID' | 'REVIEW_SOON' | 'WARNING' | 'CRITICAL' | 'EXPIRED'
  keyOrReference?: string
}

export default function LicenceFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm<LicenceForm>({
    defaultValues: {
      licenceType: 'SUBSCRIPTION',
      status: 'VALID',
      usedSeats: 0,
    },
  })

  const { data: assets } = useQuery({
    queryKey: ['assets-lookup'],
    queryFn: async () => (await apiClient.get('/assets?size=500')).data.data.content as AssetOption[],
  })

  const { data: existing } = useQuery({
    queryKey: ['licence', id],
    enabled: isEdit,
    queryFn: async () => (await apiClient.get(`/licences/${id}`)).data.data,
  })

  useEffect(() => {
    if (!existing) return
    reset({
      assetId: existing.assetId,
      licenceType: existing.licenceType,
      totalSeats: existing.totalSeats ?? undefined,
      usedSeats: existing.usedSeats ?? 0,
      expiryDate: existing.expiryDate ?? '',
      status: existing.status ?? 'VALID',
      keyOrReference: existing.keyOrReference ?? '',
    })
  }, [existing, reset])

  const mutation = useMutation({
    mutationFn: async (v: LicenceForm) => {
      // Keep default numeric values the backend treats as safe fallbacks so we
      // don't overwrite existing records with nulls when editing.
      const payload = {
        assetId: v.assetId,
        licenceType: v.licenceType,
        totalSeats: v.totalSeats ? Number(v.totalSeats) : null,
        usedSeats: v.usedSeats != null && String(v.usedSeats).trim() !== '' ? Number(v.usedSeats) : 0,
        expiryDate: v.expiryDate || null,
        status: v.status || 'VALID',
        keyOrReference: v.keyOrReference || null,
      }
      if (isEdit) await apiClient.put(`/licences/${id}`, payload)
      else await apiClient.post('/licences', payload)
    },
    onSuccess: async () => {
      toast.success(isEdit ? 'Licence updated.' : 'Licence created.')
      await invalidateAnalyticsQueries(queryClient)
      await queryClient.invalidateQueries({ queryKey: ['licences'] })
      navigate('/licences')
    },
    onError: (e: unknown) => {
      const err = e as { response?: { data?: { message?: string } } }
      toast.error(err.response?.data?.message || 'Save failed')
    },
  })

  return (
    <div className="page-content">
      <div className="page-header">
        <h1 className="page-title">{isEdit ? 'Edit' : 'New'} Licence</h1>
        <p className="page-subtitle">Licence terms, seat allocation and renewal timeline.</p>
      </div>
      <form className="form-grid card" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <label className="form-field">
          <span>Asset *</span>
          <select {...register('assetId', { required: true })}>
            <option value="">Select asset</option>
            {(assets || []).map((a) => <option key={a.id} value={a.id}>{a.name} ({a.assetIdDisplay})</option>)}
          </select>
        </label>
        <label className="form-field">
          <span>Licence Type *</span>
          <select {...register('licenceType', { required: true })}>
            {['PERPETUAL','SUBSCRIPTION','VOLUME','PER_SEAT','PER_CORE','TRIAL','OEM','OPEN_SOURCE'].map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </label>
        <label className="form-field">
          <span>Total Seats</span>
          <input type="number" {...register('totalSeats')} />
        </label>
        <label className="form-field">
          <span>Used Seats</span>
          <input type="number" {...register('usedSeats')} />
        </label>
        <label className="form-field">
          <span>Expiry Date</span>
          <input type="date" {...register('expiryDate')} />
        </label>
        <label className="form-field">
          <span>Status</span>
          <select {...register('status')}>
            {['VALID','REVIEW_SOON','WARNING','CRITICAL','EXPIRED'].map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </label>
        <label className="form-field form-full">
          <span>Key / Reference</span>
          <input {...register('keyOrReference')} />
        </label>
        <div className="form-actions">
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/licences')}>Cancel</button>
          <button type="submit" disabled={isSubmitting || mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Licence'}
          </button>
        </div>
      </form>
    </div>
  )
}
