interface ExpiryCountdownProps {
  daysRemaining: number
}

export default function ExpiryCountdown({ daysRemaining }: ExpiryCountdownProps) {
  if (daysRemaining <= 0) {
    return <span className="text-status-critical font-bold animate-pulse-critical">EXPIRED</span>
  }
  
  if (daysRemaining <= 7) {
    return <span className="text-status-critical">{daysRemaining} days</span>
  }
  
  if (daysRemaining <= 30) {
    return <span className="text-status-warning">{daysRemaining} days</span>
  }
  
  if (daysRemaining <= 90) {
    return <span className="text-status-review">{daysRemaining} days</span>
  }
  
  if (daysRemaining <= 180) {
    return <span className="text-text-secondary">{daysRemaining} days</span>
  }
  
  return <span className="text-text-muted">{daysRemaining} days</span>
}