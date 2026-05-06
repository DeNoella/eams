import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import apiClient from '@/api/client'
import DataTable from '@/components/ui/DataTable'
import StatusBadge from '@/components/ui/StatusBadge'

interface UserApi {
  id: string
  employeeId: string
  fullName: string
  email: string
  phone?: string
  jobTitle?: string
  employmentStatus: string
  accessExpiryDate?: string
  mfaEnabled?: boolean
  roles?: string[]
}

export default function UserListPage() {
  const [page, setPage] = useState(0)

  const { data, isLoading } = useQuery({
    queryKey: ['users', page],
    queryFn: async () => (await apiClient.get(`/users?page=${page}&size=25`)).data.data,
  })

  const rows = (data?.content || []) as UserApi[]
  const totalPages = data?.totalPages || 1

  const columns = [
    { accessorKey: 'fullName', header: 'Name' },
    { accessorKey: 'email', header: 'Email' },
    { accessorKey: 'jobTitle', header: 'Job Title' },
    {
      accessorKey: 'roles',
      header: 'Roles',
      cell: ({ row }: { row: { original: UserApi } }) =>
        row.original.roles?.length ? row.original.roles.join(', ') : 'READ_ONLY_VIEWER',
    },
    {
      accessorKey: 'employmentStatus',
      header: 'Status',
      cell: ({ row }: { row: { original: UserApi } }) => <StatusBadge status={row.original.employmentStatus} />,
    },
    {
      accessorKey: 'mfaEnabled',
      header: 'MFA',
      cell: ({ row }: { row: { original: UserApi } }) => (row.original.mfaEnabled ? 'On' : 'Off'),
    },
    { accessorKey: 'accessExpiryDate', header: 'Access Expiry' },
  ]

  return (
    <div className="page-content">
      <div className="page-header">
        <h1 className="page-title">Users</h1>
        <p className="page-subtitle">FR-UM · People currently registered in E-AMS with their roles and access status.</p>
      </div>
      <div className="card">
        <DataTable columns={columns} data={rows} isLoading={isLoading} emptyMessage="No users yet." />
        <div className="pagination-row">
          <p className="page-subtitle">Page {page + 1} of {totalPages} · {data?.totalElements ?? 0} users</p>
          <div style={{ display: 'flex', gap: '8px' }}>
            <button className="btn btn-secondary" disabled={page === 0} onClick={() => setPage(Math.max(0, page - 1))}>Previous</button>
            <button className="btn btn-secondary" disabled={page >= totalPages - 1} onClick={() => setPage(Math.min(totalPages - 1, page + 1))}>Next</button>
          </div>
        </div>
      </div>
    </div>
  )
}
