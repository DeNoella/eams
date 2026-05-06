import { useAuthStore } from '@/store/authStore'

const WRITE_ROLES = new Set(['SYSTEM_ADMIN', 'ASSET_MANAGER'])
const READ_ONLY_ROLES = new Set(['READ_ONLY_VIEWER', 'AUDITOR'])

/**
 * Returns true when the current user is allowed to mutate registry data.
 * The backend enforces role-based access definitively; this hook drives UI visibility.
 *
 * Behaviour:
 *   - No authenticated user → false
 *   - JWT carries an explicit write role (SYSTEM_ADMIN / ASSET_MANAGER) → true
 *   - JWT carries only strict read-only roles (READ_ONLY_VIEWER / AUDITOR) → false
 *   - JWT has no roles claim at all → true (let the backend be the source of truth)
 */
export function useCanMutate(): boolean {
  const user = useAuthStore((s) => s.user)
  if (!user) return false
  const roles = user.roles ?? []
  if (roles.length === 0) return true
  if (roles.some((r) => WRITE_ROLES.has(r))) return true
  if (roles.every((r) => READ_ONLY_ROLES.has(r))) return false
  return true
}

export function useIsSystemAdmin(): boolean {
  const roles = useAuthStore((s) => s.user?.roles ?? [])
  if (roles.length === 0) return true
  return roles.includes('SYSTEM_ADMIN')
}
