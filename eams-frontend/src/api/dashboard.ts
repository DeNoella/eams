import apiClient from '@/api/client'

export interface DashboardExecutive {
  totalAssets: number
  totalUsers?: number
  assetCountByStatus: Record<string, number>
  assetCountByCriticality: Record<string, number>
  assetCountByCategory?: Record<string, number>
  certificatesExpiring30Days: number
  certificatesExpiring90Days: number
  certificatesExpiring180Days: number
  licencesAboveSeatThreshold: number
  overdueRevertsCount: number
  accessGrantsExpiringSoon: number
  openAiAnomaliesCount: number
}

export async function fetchExecutiveDashboard(): Promise<DashboardExecutive> {
  const response = await apiClient.get('/dashboard/executive')
  return response.data.data
}

export async function fetchAiInsights(): Promise<Record<string, unknown>> {
  const response = await apiClient.get('/dashboard/ai-insights')
  return response.data.data
}
