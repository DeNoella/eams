import { useEffect, useRef, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import apiClient from '@/api/client'
import { parseJwtPayload } from '@/lib/jwt'
import { useAuthStore } from '@/store/authStore'

interface VerifyResponse { accessToken: string }

function deriveNameFromEmail(email: string): string {
  const source = email.split('@')[0] || 'User'
  return source
    .replace(/[._-]/g, ' ')
    .split(' ')
    .filter(Boolean)
    .map((p) => p.charAt(0).toUpperCase() + p.slice(1).toLowerCase())
    .join(' ')
}

type State = 'verifying' | 'broadcast-success' | 'self-success' | 'error'

export default function MagicLinkVerifyPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const hasStartedRef = useRef(false)
  const [state, setState] = useState<State>('verifying')
  const [errMsg, setErrMsg] = useState<string>('')

  useEffect(() => {
    if (hasStartedRef.current) return
    hasStartedRef.current = true

    const verify = async () => {
      const params = new URLSearchParams(location.search)
      const token = params.get('token')
      const email = params.get('email')

      if (!token || !email) {
        toast.error('Invalid magic link.')
        navigate('/login', { replace: true })
        return
      }

      try {
        const response = await apiClient.get('/auth/magic-link/verify', { params: { token, email } })
        const payload = response.data?.data as VerifyResponse | undefined
        const accessToken = payload?.accessToken
        if (!accessToken) throw new Error('Missing access token')

        // Always store locally too so this tab is logged in if the user keeps it.
        useAuthStore.getState().setTokens(accessToken, accessToken)
        const claims = parseJwtPayload<{ sub?: string; org?: string; roles?: string[] }>(accessToken)
        useAuthStore.getState().setUser({
          id: claims?.sub ?? 'session-user',
          email,
          fullName: deriveNameFromEmail(email),
          organisationId: claims?.org ?? '00000000-0000-0000-0000-000000000001',
          roles: Array.isArray(claims?.roles) ? claims.roles : [],
        })

        // Broadcast so the original Sign-In tab can pick it up and become the dashboard.
        let acked = false
        if (typeof BroadcastChannel !== 'undefined') {
          const ch = new BroadcastChannel('eams-magic-link')
          ch.onmessage = (e) => {
            if (e.data?.type === 'ack') acked = true
          }
          ch.postMessage({ type: 'verified', accessToken, email })
          // Give the listener a moment to handle, then close the channel.
          window.setTimeout(() => ch.close(), 800)
        }

        // Wait briefly to see if another tab acked. If so, this tab stays on
        // the success page and the original tab takes over the dashboard.
        // If no ack arrives (no original tab open), navigate this tab to /dashboard.
        window.setTimeout(() => {
          if (acked) {
            setState('broadcast-success')
            // Best-effort close (only works for tabs opened via window.open).
            try { window.close() } catch { /* ignore */ }
          } else {
            setState('self-success')
            navigate('/dashboard', { replace: true })
          }
        }, 600)
      } catch (error: unknown) {
        const err = error as { response?: { data?: { message?: string } } }
        const msg = err.response?.data?.message || 'Magic link verification failed.'
        setErrMsg(msg)
        setState('error')
        toast.error(msg)
      }
    }

    void verify()
  }, [location.search, navigate])

  return (
    <div className="login-container">
      <div className="login-card">
        {state === 'verifying' && (
          <>
            <h1 className="login-title">Verifying Magic Link...</h1>
            <p className="login-subtitle">
              Please wait while we sign you in to NATIONAL BANK OF RWANDA E-AMS.
            </p>
          </>
        )}
        {state === 'broadcast-success' && (
          <>
            <h1 className="login-title">Sign-in complete</h1>
            <p className="login-subtitle">
              You&apos;re now signed in on the original window. You can safely close this tab.
            </p>
            <button
              type="button"
              className="login-button"
              style={{ marginTop: 18 }}
              onClick={() => navigate('/dashboard', { replace: true })}
            >
              Open dashboard here instead
            </button>
          </>
        )}
        {state === 'self-success' && (
          <>
            <h1 className="login-title">Signed in</h1>
            <p className="login-subtitle">Redirecting you to the dashboard…</p>
          </>
        )}
        {state === 'error' && (
          <>
            <h1 className="login-title">Verification failed</h1>
            <p className="login-subtitle">{errMsg}</p>
            <button
              type="button"
              className="login-button"
              style={{ marginTop: 18 }}
              onClick={() => navigate('/login', { replace: true })}
            >
              Back to sign in
            </button>
          </>
        )}
      </div>
      <p className="login-footer-note">
        © 2026 National Bank of Rwanda · E-AMS Platform · BNR-compliant infrastructure
      </p>
    </div>
  )
}
