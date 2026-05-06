import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import apiClient from '@/api/client'
import { invalidateAnalyticsQueries } from '@/lib/queryAnalytics'
import { AssetFormFields, toPayload, type AssetForm } from './AssetCreatePage'

interface CategoryLookup { id: string; name: string; code: string }
interface IdName { id: string; name: string; code?: string }

export default function AssetEditPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { register, handleSubmit, reset, watch, formState: { isSubmitting } } = useForm<AssetForm>({
    defaultValues: {
      criticality: 'MEDIUM',
      status: 'ACTIVE',
      lifecycleStage: 'IN_SERVICE',
    },
  })

  const { data: categories } = useQuery({
    queryKey: ['asset-categories'],
    queryFn: async () => (await apiClient.get('/asset-categories')).data.data as CategoryLookup[],
  })
  const { data: locations } = useQuery({
    queryKey: ['lookup-locations'],
    queryFn: async () => (await apiClient.get('/lookups/locations')).data.data as IdName[],
  })

  const { data: asset, isLoading } = useQuery({
    queryKey: ['asset', id],
    enabled: Boolean(id),
    queryFn: async () => (await apiClient.get(`/assets/${id}`)).data.data,
  })

  useEffect(() => {
    if (!asset) return
    reset({
      assetCategoryId: asset.assetCategoryId,
      name: asset.name,
      locationId: asset.locationId ?? '',
      yearOfInstallation: asset.yearOfInstallation != null ? String(asset.yearOfInstallation) : '',
      vendorName: asset.vendorName ?? '',
      versionNo: asset.versionNo ?? '',
      versionDate: asset.versionDate ?? '',
      numberOfLicences: asset.numberOfLicences != null ? String(asset.numberOfLicences) : '',
      hostingInstitution: asset.hostingInstitution ?? '',
      functionsOfSystem: asset.functionsOfSystem ?? '',
      staffInCharge: asset.staffInCharge ?? '',
      serverType: asset.serverType ?? '',
      operatingSystemName: asset.operatingSystemName ?? '',
      osVendorName: asset.osVendorName ?? '',
      osVersionNo: asset.osVersionNo ?? '',
      modelNumber: asset.modelNumber ?? '',
      equipmentLicenceType: asset.equipmentLicenceType ?? '',
      criticality: asset.criticality ?? 'MEDIUM',
      status: asset.status ?? 'ACTIVE',
      lifecycleStage: asset.lifecycleStage ?? 'IN_SERVICE',
    })
  }, [asset, reset])

  const selectedCategoryId = watch('assetCategoryId')
  const selectedCategory = (categories || []).find((c) => c.id === selectedCategoryId)
  const categoryCode = selectedCategory?.code?.toUpperCase() || asset?.categoryCode?.toUpperCase() || ''

  const mutation = useMutation({
    mutationFn: async (values: AssetForm) => apiClient.put(`/assets/${id}`, toPayload(values)),
    onSuccess: async () => {
      toast.success('Asset updated.')
      await invalidateAnalyticsQueries(queryClient)
      await queryClient.invalidateQueries({ queryKey: ['assets'] })
      await queryClient.invalidateQueries({ queryKey: ['asset', id] })
      navigate(`/assets/${id}`)
    },
    onError: (e: unknown) => {
      const err = e as { response?: { data?: { message?: string } } }
      toast.error(err.response?.data?.message || 'Update failed')
    },
  })

  if (isLoading || !asset) {
    return <div className="page-content"><p className="page-subtitle">Loading asset…</p></div>
  }

  return (
    <div className="page-content">
      <div className="page-header">
        <h1 className="page-title">Edit Asset</h1>
        <p className="page-subtitle">Update registry fields — dashboard metrics refresh automatically.</p>
      </div>
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="form-grid card">
        <AssetFormFields
          register={register}
          categories={categories || []}
          locations={locations || []}
          categoryCode={categoryCode}
          mode="edit"
          uniqueId={asset?.assetIdDisplay ?? null}
        />
        <div className="form-actions">
          <button type="button" onClick={() => navigate(`/assets/${id}`)} className="btn btn-secondary">Cancel</button>
          <button type="submit" disabled={isSubmitting || mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'Saving…' : 'Save Changes'}
          </button>
        </div>
      </form>
    </div>
  )
}
