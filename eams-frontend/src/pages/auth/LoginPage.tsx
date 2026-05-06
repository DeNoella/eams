import { useEffect, useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Eye, EyeOff } from 'lucide-react'
import apiClient from '@/api/client'
import { useAuthStore } from '@/store/authStore'
import { parseJwtPayload } from '@/lib/jwt'

type AuthTab = 'signin' | 'signup'

interface SignInForm { email: string }
interface SignUpForm { firstName: string; lastName: string; email: string; password: string }

// ---------------- password rules + strength ----------------
const checks = (pw: string) => ({
  length: pw.length >= 8,
  letter: /[A-Za-z]/.test(pw),
  number: /\d/.test(pw),
  special: /[^A-Za-z0-9]/.test(pw),
})
const score = (pw: string) => { const c = checks(pw); return +c.length + +c.letter + +c.number + +c.special }
const labelOf = (s: number) => s <= 2 ? { l: 'Weak', c: '#B91C1C' } : s === 3 ? { l: 'Fair', c: '#B45309' } : { l: 'Strong', c: '#1F7A3D' }

const deriveName = (email: string) => {
  const s = email.split('@')[0] || 'User'
  return s.replace(/[._-]/g, ' ').split(' ').filter(Boolean)
    .map(p => p.charAt(0).toUpperCase() + p.slice(1).toLowerCase()).join(' ')
}

export default function LoginPage() {
  const [tab, setTab] = useState<AuthTab>('signin')
  const [prefillEmail, setPrefillEmail] = useState<string>('')

  const switchToSignUp = (email: string) => { setPrefillEmail(email); setTab('signup') }
  const switchToSignIn = (email: string) => { setPrefillEmail(email); setTab('signin') }

  return (
    <div className="login-container">
      <div className="login-card">
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <h1 className="login-title">NATIONAL BANK OF RWANDA<span> · BNR</span></h1>
          <p className="login-subtitle">
            {tab === 'signin'
              ? 'Sign in with secure magic link authentication'
              : 'Create your account to access the E-AMS platform'}
          </p>
        </div>

        <div className="tab-bar auth-tabs" role="tablist">
          <button type="button" role="tab" aria-selected={tab === 'signin'}
            className={`tab-btn ${tab === 'signin' ? 'active' : ''}`} onClick={() => setTab('signin')}>
            Sign In
          </button>
          <button type="button" role="tab" aria-selected={tab === 'signup'}
            className={`tab-btn ${tab === 'signup' ? 'active' : ''}`} onClick={() => setTab('signup')}>
            Sign Up
          </button>
          <div className="tab-spacer" />
        </div>

        {tab === 'signin'
          ? <SignInPanel prefill={prefillEmail} onNotRegistered={switchToSignUp} />
          : <SignUpPanel prefill={prefillEmail} onSuccess={switchToSignIn} />}
      </div>

      <p className="login-footer-note">
        © 2026 National Bank of Rwanda · E-AMS Platform · BNR-compliant infrastructure
      </p>
    </div>
  )
}

// ---------------- Sign In (magic link) ----------------
function SignInPanel({ prefill, onNotRegistered }: { prefill: string; onNotRegistered: (email: string) => void }) {
  const [loading, setLoading] = useState(false)
  const [emailSent, setEmailSent] = useState<string | null>(null)
  const navigate = useNavigate()
  const { register, handleSubmit, setValue } = useForm<SignInForm>({ defaultValues: { email: prefill } })

  useEffect(() => { if (prefill) setValue('email', prefill) }, [prefill, setValue])

  // Listen for sign-in confirmation broadcast from the magic-link verify tab.
  // When the user clicks the magic link in their email, the verify tab will
  // broadcast the access token; THIS tab (the original one) then logs in
  // and navigates to the dashboard — no more "stuck on login" tab.
  useEffect(() => {
    if (typeof BroadcastChannel === 'undefined') return
    const ch = new BroadcastChannel('eams-magic-link')
    ch.onmessage = (e) => {
      const m = e.data
      if (m?.type === 'verified' && m.accessToken && m.email) {
        // Ack first so the verify tab knows we caught it and can stay on
        // its "you can close this" success state instead of self-redirecting.
        try { ch.postMessage({ type: 'ack' }) } catch { /* ignore */ }
        useAuthStore.getState().setTokens(m.accessToken, m.accessToken)
        const claims = parseJwtPayload<{ sub?: string; org?: string; roles?: string[] }>(m.accessToken)
        useAuthStore.getState().setUser({
          id: claims?.sub ?? 'session-user',
          email: m.email,
          fullName: deriveName(m.email),
          organisationId: claims?.org ?? '00000000-0000-0000-0000-000000000001',
          roles: Array.isArray(claims?.roles) ? claims.roles : [],
        })
        toast.success('Signed in successfully.')
        navigate('/dashboard', { replace: true })
      }
    }
    return () => ch.close()
  }, [navigate])

  const onSubmit = async (data: SignInForm) => {
    setLoading(true)
    const email = data.email.trim().toLowerCase()
    try {
      const res = await apiClient.post('/auth/magic-link/request', { email })
      if (res.data.success) {
        setEmailSent(email)
        toast.success('Magic link sent. Check your inbox.')
      }
    } catch (error: unknown) {
      const err = error as { response?: { status?: number; data?: { message?: string } } }
      const msg = err.response?.data?.message || ''
      // 404 (or "No account found" message) means the email is not registered yet.
      // Auto-switch to the Sign Up tab and prefill the email.
      if (err.response?.status === 404 || /no account|not.*found|please create/i.test(msg)) {
        toast.error('No account found. Please create an account first.')
        onNotRegistered(email)
      } else {
        toast.error(msg || 'Login failed')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="login-form">
      <div>
        <label className="login-label">Work Email</label>
        <input
          {...register('email', { required: true })}
          type="email"
          placeholder="yourworkemail@bnr.rw"
          className="login-input"
          autoFocus
        />
      </div>
      <button type="submit" disabled={loading} className="login-button">
        {loading ? 'Sending magic link...' : 'Send Magic Link'}
      </button>
      <p className="login-helper">Use your email and open the verification link from your inbox.</p>
      {emailSent && (
        <p className="login-helper" style={{ color: '#1F7A3D', fontWeight: 600 }}>
          Magic link sent to {emailSent}. Waiting for you to click it…
        </p>
      )}
    </form>
  )
}

// ---------------- Sign Up ----------------
function SignUpPanel({ prefill, onSuccess }: { prefill: string; onSuccess: (email: string) => void }) {
  const [loading, setLoading] = useState(false)
  const [showPw, setShowPw] = useState(false)
  const [emailError, setEmailError] = useState<string | null>(null)
  const { register, handleSubmit, watch, setValue, formState: { errors, isValid } } =
    useForm<SignUpForm>({ mode: 'onChange', defaultValues: { firstName: '', lastName: '', email: prefill, password: '' } })

  useEffect(() => { if (prefill) setValue('email', prefill, { shouldValidate: true }) }, [prefill, setValue])

  const password = watch('password') || ''
  const c = useMemo(() => checks(password), [password])
  const s = useMemo(() => score(password), [password])
  const meta = labelOf(s)
  const pwOk = c.length && c.letter && c.number && c.special
  const canSubmit = isValid && pwOk && !loading && !emailError

  const onSubmit = async (data: SignUpForm) => {
    setLoading(true); setEmailError(null)
    try {
      await apiClient.post('/auth/register', {
        firstName: data.firstName.trim(),
        lastName: data.lastName.trim(),
        email: data.email.trim().toLowerCase(),
        password: data.password,
      })
      toast.success('Account created. Please sign in with your magic link.')
      onSuccess(data.email.trim().toLowerCase())
    } catch (error: unknown) {
      const err = error as { response?: { status?: number; data?: { message?: string } } }
      if (err.response?.status === 409) {
        setEmailError('An account with this email already exists')
      } else {
        toast.error(err.response?.data?.message || 'Registration failed')
      }
    } finally { setLoading(false) }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="login-form" noValidate>
      <div>
        <label className="login-label">First Name</label>
        <input {...register('firstName', { required: 'First name is required', maxLength: 100 })}
          type="text" autoComplete="given-name" className="login-input" placeholder="Jane" autoFocus />
        {errors.firstName && <p className="auth-field-error">{errors.firstName.message}</p>}
      </div>
      <div>
        <label className="login-label">Last Name</label>
        <input {...register('lastName', { required: 'Last name is required', maxLength: 100 })}
          type="text" autoComplete="family-name" className="login-input" placeholder="Doe" />
        {errors.lastName && <p className="auth-field-error">{errors.lastName.message}</p>}
      </div>
      <div>
        <label className="login-label">Email</label>
        <input {...register('email', {
          required: 'Email is required',
          pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Enter a valid email' },
          onChange: () => setEmailError(null),
        })} type="email" autoComplete="email" className="login-input" placeholder="jane.doe@bnr.rw" />
        {errors.email && <p className="auth-field-error">{errors.email.message}</p>}
        {emailError && <p className="auth-field-error">{emailError}</p>}
      </div>
      <div>
        <label className="login-label">Password</label>
        <div className="auth-password-wrap">
          <input {...register('password', { required: 'Password is required' })}
            type={showPw ? 'text' : 'password'} autoComplete="new-password"
            className="login-input auth-password-input"
            placeholder="At least 8 chars, 1 letter, 1 number, 1 symbol" />
          <button type="button" onClick={() => setShowPw(v => !v)}
            className="auth-password-toggle"
            aria-label={showPw ? 'Hide password' : 'Show password'} tabIndex={-1}>
            {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>
        <div className="auth-strength" aria-live="polite">
          <div className="auth-strength-track">
            <div className="auth-strength-fill"
              style={{ width: `${(s / 4) * 100}%`, backgroundColor: password ? meta.c : 'transparent' }} />
          </div>
          <span className="auth-strength-label" style={{ color: password ? meta.c : 'var(--text-muted)' }}>
            {password ? meta.l : ' '}
          </span>
        </div>
        <ul className="auth-rules">
          <li className={c.length ? 'ok' : ''}>At least 8 characters</li>
          <li className={c.letter ? 'ok' : ''}>Contains a letter</li>
          <li className={c.number ? 'ok' : ''}>Contains a number</li>
          <li className={c.special ? 'ok' : ''}>Contains a special character</li>
        </ul>
      </div>
      <button type="submit" disabled={!canSubmit} className="login-button">
        {loading ? 'Creating account...' : 'Create Account'}
      </button>
      <p className="login-helper">After your account is created, sign in via the magic link sent to your inbox.</p>
    </form>
  )
}
