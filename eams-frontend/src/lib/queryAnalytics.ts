import type { QueryClient } from '@tanstack/react-query'

/** Invalidate dashboard and AI insight queries after registry-changing operations. */
export function invalidateAnalyticsQueries(queryClient: QueryClient) {
  return Promise.all([
    queryClient.invalidateQueries({ queryKey: ['dashboard-executive'] }),
    queryClient.invalidateQueries({ queryKey: ['dashboard-ai-insights'] }),
  ])
}
