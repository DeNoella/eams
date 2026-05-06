import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import apiClient from '@/api/client'
import { invalidateAnalyticsQueries } from '@/lib/queryAnalytics'

interface AssetOption { id: string; name: string; assetIdDisplay: string }

interface CertForm {
  assetId: string
  certificateType: 'CA_ROOT' | 'CA_INTERMEDIATE' | 'SELF_SIGNED' | 'WILDCARD' | 'SAN' | 'SERVER' | 'CLIENT' | 'CODE_SIGNING'
  commonName: string
  serialNumber: string
  issueDate?: string
  expiryDate?: string
  status?: 'VALID' | 'REVIEW_SOON' | 'WARNING' | 'CRITICAL' | 'EXPIRED' | 'REVOKED'
}

export default function CertificateFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm<CertForm>({
    defaultValues: {
      certificateType: 'SERVER',
      commonName: '',
      serialNumber: '',
      status: 'VALID',
    },
  })

  const { data: assets } = useQuery({
    queryKey: ['assets-lookup'],
    queryFn: async () => (await apiClient.get('/assets?size=500')).data.data.content as AssetOption[],
  })

  const { data: existing } = useQuery({
    queryKey: ['certificate', id],
    enabled: isEdit,
    queryFn: async () => (await apiClient.get(`/certificates/${id}`)).data.data,
  })

  useEffect(() => {
    if (!existing) return
    reset({
      assetId: existing.assetId,
      certificateType: existing.certificateType,
      commonName: existing.commonName ?? '',
      serialNumber: existing.serialNumber ?? '',
      issueDate: existing.issueDate ?? '',
      expiryDate: existing.expiryDate ?? '',
      status: existing.status ?? 'VALID',
    })
  }, [existing, reset])

  const mutation = useMutation({
    mutationFn: async (v: CertForm) => {
      // The Certificate entity has several NOT NULL columns (algorithm,
      // issuingAuthority, subjectDn, environment, fingerprint). Supply safe
      // defaults so creates don't fail even though the UI no longer exposes
      // these fields — the service layer also applies blank-to-default.
      const payload = {
        assetId: v.assetId,
        certificateType: v.certificateType,
        commonName: v.commonName,
        serialNumber: v.serialNumber,
        issueDate: v.issueDate || null,
        expiryDate: v.expiryDate || null,
        status: v.status || 'VALID',
        issuingAuthority: 'Internal CA',
        subjectDn: `CN=${v.commonName || 'unspecified'}`,
        algorithm: 'RSA_2048',
        environment: 'PRODUCTION',
        fingerprintSha256: 'UNKNOWN',
      }
      if (isEdit) await apiClient.put(`/certificates/${id}`, payload)
      else await apiClient.post('/certificates', payload)
    },
    onSuccess: async () => {
      toast.success(isEdit ? 'Certificate updated.' : 'Certificate created.')
      await invalidateAnalyticsQueries(queryClient)
      await queryClient.invalidateQueries({ queryKey: ['certificates'] })
      navigate('/certificates')
    },
    onError: (e: unknown) => {
      const err = e as { response?: { data?: { message?: string } } }
      toast.error(err.response?.data?.message || 'Save failed')
    },
  })

  return (
    <div className="page-content">
      <div className="page-header">
        <h1 className="page-title">{isEdit ? 'Edit' : 'New'} Certificate</h1>
        <p className="page-subtitle">Core certificate details — dashboard refreshes on save.</p>
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
          <span>Type *</span>
          <select {...register('certificateType', { required: true })}>
            {['CA_ROOT','CA_INTERMEDIATE','SELF_SIGNED','WILDCARD','SAN','SERVER','CLIENT','CODE_SIGNING'].map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </label>
        <label className="form-field">
          <span>Common Name *</span>
          <input {...register('commonName', { required: true })} placeholder="api.goshenfinance.com" />
        </label>
        <label className="form-field">
          <span>Serial Number *</span>
          <input {...register('serialNumber', { required: true })} />
        </label>
        <label className="form-field">
          <span>Issue Date</span>
          <input type="date" {...register('issueDate')} />
        </label>
        <label className="form-field">
          <span>Expiry Date *</span>
          <input type="date" {...register('expiryDate', { required: true })} />
        </label>
        <label className="form-field">
          <span>Status</span>
          <select {...register('status')}>
            {['VALID','REVIEW_SOON','WARNING','CRITICAL','EXPIRED','REVOKED'].map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </label>
        <div className="form-actions">
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/certificates')}>Cancel</button>
          <button type="submit" disabled={isSubmitting || mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Certificate'}
          </button>
        </div>
      </form>
    </div>
  )
}
