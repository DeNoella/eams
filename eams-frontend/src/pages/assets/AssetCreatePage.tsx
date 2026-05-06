import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import apiClient from '@/api/client'
import { invalidateAnalyticsQueries } from '@/lib/queryAnalytics'

interface CategoryLookup { id: string; name: string; code: string }
interface IdName { id: string; name: string; code?: string }

interface AssetForm {
  assetCategoryId: string
  name: string
  locationId?: string
  yearOfInstallation?: string
  vendorName?: string
  versionNo?: string
  versionDate?: string
  numberOfLicences?: string
  hostingInstitution?: string
  functionsOfSystem?: string
  staffInCharge?: string
  serverType?: string
  operatingSystemName?: string
  osVendorName?: string
  osVersionNo?: string
  modelNumber?: string
  equipmentLicenceType?: string
  criticality?: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'
  status?: 'ACTIVE' | 'INACTIVE' | 'DECOMMISSIONED' | 'IN_MAINTENANCE' | 'IN_STOCK' | 'LOST' | 'STOLEN'
  lifecycleStage?: 'IN_PROCUREMENT' | 'IN_STOCK' | 'IN_SERVICE' | 'IN_MAINTENANCE' | 'DECOMMISSIONED'
}

export default function AssetCreatePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { register, handleSubmit, watch, formState: { isSubmitting } } = useForm<AssetForm>({
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

  const selectedCategoryId = watch('assetCategoryId')
  const selectedCategory = (categories || []).find((c) => c.id === selectedCategoryId)
  const categoryCode = selectedCategory?.code?.toUpperCase() || ''

  const onSubmit = async (values: AssetForm): Promise<void> => {
    try {
      const response = await apiClient.post('/assets', toPayload(values))
      toast.success('Asset created successfully.')
      await invalidateAnalyticsQueries(queryClient)
      await queryClient.invalidateQueries({ queryKey: ['assets'] })
      const createdId = response.data?.data?.id
      navigate(createdId ? `/assets/${createdId}` : '/assets')
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } }
      toast.error(err.response?.data?.message || 'Failed to create asset')
    }
  }

  return (
    <div className="page-content">
      <div className="page-header">
        <h1 className="page-title">Create Asset</h1>
        <p className="page-subtitle">Register a new NATIONAL BANK OF RWANDA IT asset — dashboard refreshes on save.</p>
      </div>
      <form className="form-grid card" onSubmit={handleSubmit(onSubmit)}>
        <AssetFormFields
          register={register}
          categories={categories || []}
          locations={locations || []}
          categoryCode={categoryCode}
          mode="create"
          uniqueId={null}
        />
        <div className="form-actions">
          <button type="button" onClick={() => navigate('/assets')} className="btn btn-secondary">Cancel</button>
          <button type="submit" disabled={isSubmitting} className="btn btn-primary">
            {isSubmitting ? 'Saving…' : 'Create Asset'}
          </button>
        </div>
      </form>
    </div>
  )
}

export function toPayload(values: AssetForm) {
  // Classification fields (criticality/status/lifecycleStage) come from the
  // Asset Classification section of the form and fall back to sensible
  // defaults if somehow unset. currencyCode is forced to USD because the
  // underlying column is CHAR(3) and no UI control exists for it.
  return {
    assetCategoryId: values.assetCategoryId,
    name: values.name,
    locationId: values.locationId || null,
    criticality: values.criticality || 'MEDIUM',
    status: values.status || 'ACTIVE',
    lifecycleStage: values.lifecycleStage || 'IN_SERVICE',
    currencyCode: 'USD',
    yearOfInstallation: values.yearOfInstallation ? Number(values.yearOfInstallation) : null,
    vendorName: values.vendorName || null,
    versionNo: values.versionNo || null,
    versionDate: values.versionDate || null,
    numberOfLicences: values.numberOfLicences ? Number(values.numberOfLicences) : null,
    hostingInstitution: values.hostingInstitution || null,
    functionsOfSystem: values.functionsOfSystem || null,
    staffInCharge: values.staffInCharge || null,
    serverType: values.serverType || null,
    operatingSystemName: values.operatingSystemName || null,
    osVendorName: values.osVendorName || null,
    osVersionNo: values.osVersionNo || null,
    modelNumber: values.modelNumber || null,
    equipmentLicenceType: values.equipmentLicenceType || null,
  }
}

export type { AssetForm }

export type AssetFormKind = 'APPLICATION' | 'DATABASE' | 'SERVER' | 'EQUIPMENT'

export function resolveFormKind(categoryCode: string): AssetFormKind {
  const code = categoryCode.toUpperCase()
  if (code === 'APP') return 'APPLICATION'
  if (code === 'DB') return 'DATABASE'
  if (code === 'SRV') return 'SERVER'
  return 'EQUIPMENT'
}

export function AssetFormFields({
  register,
  categories,
  locations,
  categoryCode,
  mode,
  uniqueId,
}: {
  register: ReturnType<typeof useForm<AssetForm>>['register']
  categories: IdName[]
  locations: IdName[]
  categoryCode: string
  mode: 'create' | 'edit'
  uniqueId: string | null
}) {
  const kind = resolveFormKind(categoryCode)
  const nameLabel =
    kind === 'APPLICATION' ? 'Application Name *'
      : kind === 'DATABASE' ? 'Database Name *'
      : kind === 'SERVER' ? 'Server Name *'
      : 'Equipment Name *'

  const showCommonSoftware = kind === 'APPLICATION' || kind === 'DATABASE' || kind === 'SERVER'

  return (
    <>
      <label className="form-field">
        <span>Asset Category *</span>
        <select {...register('assetCategoryId', { required: true })}>
          <option value="">Select category</option>
          {categories.map((c) => <option key={c.id} value={c.id}>{c.name}{c.code ? ` (${c.code})` : ''}</option>)}
        </select>
      </label>

      <label className="form-field">
        <span>{nameLabel}</span>
        <input {...register('name', { required: true })} placeholder="e.g. Core Banking System" />
      </label>

      {mode === 'edit' && uniqueId && (
        <label className="form-field">
          <span>Unique ID</span>
          <input value={uniqueId} readOnly disabled />
        </label>
      )}
      {mode === 'create' && (
        <label className="form-field">
          <span>Unique ID</span>
          <input value="Auto-generated on save" readOnly disabled />
        </label>
      )}

      {kind === 'SERVER' && (
        <label className="form-field">
          <span>Server Type</span>
          <select {...register('serverType')}>
            <option value="">—</option>
            <option value="VM">VM</option>
            <option value="HOSTER">Hoster</option>
          </select>
        </label>
      )}

      {kind === 'SERVER' && (
        <label className="form-field">
          <span>Operating System Name</span>
          <input {...register('operatingSystemName')} placeholder="Ubuntu / Windows Server" />
        </label>
      )}

      {showCommonSoftware && (
        <label className="form-field">
          <span>Year of Installation</span>
          <input type="number" {...register('yearOfInstallation')} placeholder="2024" />
        </label>
      )}

      {(kind === 'APPLICATION' || kind === 'DATABASE') && (
        <label className="form-field">
          <span>Vendor Name</span>
          <input {...register('vendorName')} />
        </label>
      )}

      {kind === 'SERVER' && (
        <>
          <label className="form-field">
            <span>OS Vendor Name</span>
            <input {...register('osVendorName')} />
          </label>
          <label className="form-field">
            <span>OS Version No</span>
            <input {...register('osVersionNo')} />
          </label>
        </>
      )}

      {(kind === 'APPLICATION' || kind === 'DATABASE') && (
        <label className="form-field">
          <span>Version No</span>
          <input {...register('versionNo')} placeholder="e.g. 12.4.1" />
        </label>
      )}

      {showCommonSoftware && (
        <>
          <label className="form-field">
            <span>Version Date</span>
            <input type="date" {...register('versionDate')} />
          </label>
          <label className="form-field">
            <span>Number of Licences</span>
            <input type="number" {...register('numberOfLicences')} />
          </label>
        </>
      )}

      {kind === 'EQUIPMENT' && (
        <>
          <label className="form-field">
            <span>Model Number</span>
            <input {...register('modelNumber')} />
          </label>
          <label className="form-field">
            <span>Provider Name</span>
            <input {...register('vendorName')} />
          </label>
          <label className="form-field">
            <span>Licence Type</span>
            <input {...register('equipmentLicenceType')} placeholder="e.g. PERPETUAL / SUBSCRIPTION" />
          </label>
        </>
      )}

      <label className="form-field">
        <span>Hosting Location</span>
        <select {...register('locationId')}>
          <option value="">—</option>
          {locations.map((l) => <option key={l.id} value={l.id}>{l.name}{l.code ? ` (${l.code})` : ''}</option>)}
        </select>
      </label>

      <label className="form-field">
        <span>Hosting Institution</span>
        <input {...register('hostingInstitution')} placeholder="e.g. Internal, AWS, Azure" />
      </label>

      <label className="form-field form-full">
        <span>Functions of the System</span>
        <textarea rows={2} {...register('functionsOfSystem')} />
      </label>

      <label className="form-field">
        <span>Staff in Charge</span>
        <input {...register('staffInCharge')} placeholder="Full name or team" />
      </label>

      <div className="form-full" style={{ borderTop: '1px solid var(--border)', marginTop: '8px', paddingTop: '12px' }}>
        <h4 className="section-title" style={{ marginBottom: 0 }}>Asset Classification</h4>
        <p className="page-subtitle" style={{ margin: 0 }}>
          Feeds the dashboard KPIs (Critical Assets, Asset Status donut, Lifecycle).
        </p>
      </div>

      <label className="form-field">
        <span>Criticality</span>
        <select {...register('criticality')}>
          <option value="CRITICAL">Critical</option>
          <option value="HIGH">High</option>
          <option value="MEDIUM">Medium</option>
          <option value="LOW">Low</option>
        </select>
      </label>

      <label className="form-field">
        <span>Status</span>
        <select {...register('status')}>
          <option value="ACTIVE">Active</option>
          <option value="IN_MAINTENANCE">In Maintenance</option>
          <option value="IN_STOCK">In Stock</option>
          <option value="INACTIVE">Inactive</option>
          <option value="DECOMMISSIONED">Decommissioned</option>
          <option value="LOST">Lost</option>
          <option value="STOLEN">Stolen</option>
        </select>
      </label>

      <label className="form-field">
        <span>Lifecycle Stage</span>
        <select {...register('lifecycleStage')}>
          <option value="IN_PROCUREMENT">In Procurement</option>
          <option value="IN_STOCK">In Stock</option>
          <option value="IN_SERVICE">In Service</option>
          <option value="IN_MAINTENANCE">In Maintenance</option>
          <option value="DECOMMISSIONED">Decommissioned</option>
        </select>
      </label>
    </>
  )
}
