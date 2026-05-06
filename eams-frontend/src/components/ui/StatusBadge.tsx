interface StatusBadgeProps {
  status: string
  size?: 'sm' | 'md'
}

const statusMap: Record<string, { label: string; bg: string; text: string }> = {
  ACTIVE: { label: 'Active', bg: 'rgba(34, 197, 94, 0.1)', text: '#22C55E' },
  INACTIVE: { label: 'Inactive', bg: 'rgba(107, 114, 128, 0.1)', text: '#6B7280' },
  IN_MAINTENANCE: { label: 'In Maintenance', bg: 'rgba(234, 179, 8, 0.1)', text: '#EAB308' },
  DECOMMISSIONED: { label: 'Decommissioned', bg: 'rgba(239, 68, 68, 0.1)', text: '#EF4444' },
  LOST: { label: 'Lost/Stolen', bg: 'rgba(239, 68, 68, 0.1)', text: '#EF4444' },
  VALID: { label: 'Valid', bg: 'rgba(34, 197, 94, 0.1)', text: '#22C55E' },
  REVIEW_SOON: { label: 'Review Soon', bg: 'rgba(234, 179, 8, 0.1)', text: '#EAB308' },
  WARNING: { label: 'Warning', bg: 'rgba(249, 115, 22, 0.1)', text: '#F97316' },
  CRITICAL: { label: 'Critical', bg: 'rgba(239, 68, 68, 0.1)', text: '#EF4444' },
  EXPIRED: { label: 'Expired', bg: 'rgba(239, 68, 68, 0.1)', text: '#EF4444' },
  APPROVED: { label: 'Approved', bg: 'rgba(34, 197, 94, 0.1)', text: '#22C55E' },
  PENDING: { label: 'Pending', bg: 'rgba(234, 179, 8, 0.1)', text: '#EAB308' },
  REJECTED: { label: 'Rejected', bg: 'rgba(239, 68, 68, 0.1)', text: '#EF4444' },
}

export default function StatusBadge({ status, size = 'sm' }: StatusBadgeProps) {
  const config = statusMap[status] || { label: status, bg: 'rgba(107, 114, 128, 0.1)', text: '#6B7280' }
  
  return (
    <span style={{
      backgroundColor: config.bg,
      color: config.text,
      padding: size === 'md' ? '4px 12px' : '2px 8px',
      borderRadius: '9999px',
      fontSize: size === 'md' ? '14px' : '12px',
      fontWeight: 500,
      display: 'inline-flex',
      alignItems: 'center',
    }}>
      {config.label}
    </span>
  )
}