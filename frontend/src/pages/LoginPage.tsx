import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import type { LoginRequest } from '../types/auth.types'
import { createRegistrationRequest } from '../api/registrationRequest.api'
import type { CreateRegistrationRequestRequest } from '../types/registrationRequest.types'

// ─── Logo ────────────────────────────────────────────────────────────────────

function Logo() {
  return (
    <div className="text-center mb-8">
      <div className="inline-flex items-center justify-center w-11 h-11 bg-violet-600 rounded-xl mb-4">
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="white"
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
          <rect x="9" y="3" width="6" height="4" rx="2" />
        </svg>
      </div>
      <h1 className="text-2xl font-semibold text-slate-800 dark:text-slate-100">TicketSystem</h1>
      <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">IT Servis Yönetim Sistemi</p>
    </div>
  )
}

// ─── Shared field classes ─────────────────────────────────────────────────────

function fieldCls(hasError: boolean) {
  return `w-full px-3.5 py-2.5 rounded-lg border text-sm text-slate-800 dark:text-slate-100 bg-white dark:bg-slate-700 outline-none transition-colors placeholder:text-slate-400 dark:placeholder:text-slate-500 ${
    hasError
      ? 'border-red-300 focus:border-red-400 dark:border-red-600'
      : 'border-slate-200 focus:border-violet-400 dark:border-slate-600 dark:focus:border-violet-500'
  }`
}

// ─── LoginPage ────────────────────────────────────────────────────────────────

type View = 'login' | 'register'

interface RegFields {
  firstName: string
  lastName: string
  email: string
  password: string
  passwordConfirm: string
  note: string
}

interface RegErrors {
  firstName: string
  lastName: string
  email: string
  password: string
  passwordConfirm: string
}

const EMPTY_REG_FIELDS: RegFields = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  passwordConfirm: '',
  note: '',
}

const EMPTY_REG_ERRORS: RegErrors = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  passwordConfirm: '',
}

function LoginPage() {
  const { login, isAuthenticated } = useAuth()

  // ── view toggle ────────────────────────────────────────────────────────────
  const [view, setView] = useState<View>('login')

  // ── login state ────────────────────────────────────────────────────────────
  const [loginEmail, setLoginEmail] = useState('')
  const [loginPassword, setLoginPassword] = useState('')
  const [loginEmailError, setLoginEmailError] = useState('')
  const [loginPasswordError, setLoginPasswordError] = useState('')
  const [loginServerError, setLoginServerError] = useState('')
  const [loginSubmitting, setLoginSubmitting] = useState(false)

  // ── register state ─────────────────────────────────────────────────────────
  const [reg, setReg] = useState<RegFields>(EMPTY_REG_FIELDS)
  const [regErrors, setRegErrors] = useState<RegErrors>(EMPTY_REG_ERRORS)
  const [regServerError, setRegServerError] = useState('')
  const [regSubmitting, setRegSubmitting] = useState(false)
  const [regSuccess, setRegSuccess] = useState(false)

  if (isAuthenticated) return <Navigate to="/dashboard" replace />

  // ── view switch ────────────────────────────────────────────────────────────

  function switchToRegister() {
    setReg(EMPTY_REG_FIELDS)
    setRegErrors(EMPTY_REG_ERRORS)
    setRegServerError('')
    setRegSuccess(false)
    setView('register')
  }

  function switchToLogin() {
    setLoginEmail('')
    setLoginPassword('')
    setLoginEmailError('')
    setLoginPasswordError('')
    setLoginServerError('')
    setView('login')
  }

  // ── login handlers ─────────────────────────────────────────────────────────

  function validateLogin(): boolean {
    let valid = true
    setLoginEmailError('')
    setLoginPasswordError('')

    if (!loginEmail.trim()) {
      setLoginEmailError('Email adresi zorunludur.')
      valid = false
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(loginEmail)) {
      setLoginEmailError('Geçerli bir email adresi girin.')
      valid = false
    }

    if (!loginPassword) {
      setLoginPasswordError('Parola zorunludur.')
      valid = false
    }

    return valid
  }

  async function handleLoginSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setLoginServerError('')
    if (!validateLogin()) return

    setLoginSubmitting(true)
    try {
      const request: LoginRequest = { email: loginEmail.trim(), password: loginPassword }
      await login(request)
    } catch (err) {
      setLoginServerError(err instanceof Error ? err.message : 'Giriş sırasında bir hata oluştu.')
    } finally {
      setLoginSubmitting(false)
    }
  }

  // ── register handlers ──────────────────────────────────────────────────────

  function setRegField(field: keyof RegFields, value: string) {
    setReg((prev) => ({ ...prev, [field]: value }))
  }

  function validateReg(): boolean {
    const errors: RegErrors = { ...EMPTY_REG_ERRORS }
    let valid = true

    if (!reg.firstName.trim()) {
      errors.firstName = 'Ad zorunludur.'
      valid = false
    }
    if (!reg.lastName.trim()) {
      errors.lastName = 'Soyad zorunludur.'
      valid = false
    }
    if (!reg.email.trim()) {
      errors.email = 'Email adresi zorunludur.'
      valid = false
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(reg.email)) {
      errors.email = 'Geçerli bir email adresi girin.'
      valid = false
    }
    if (!reg.password) {
      errors.password = 'Parola zorunludur.'
      valid = false
    } else if (reg.password.length < 8) {
      errors.password = 'Parola en az 8 karakter olmalıdır.'
      valid = false
    }
    if (!reg.passwordConfirm) {
      errors.passwordConfirm = 'Parola tekrarı zorunludur.'
      valid = false
    } else if (reg.password !== reg.passwordConfirm) {
      errors.passwordConfirm = 'Parolalar eşleşmiyor.'
      valid = false
    }

    setRegErrors(errors)
    return valid
  }

  async function handleRegSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setRegServerError('')
    if (!validateReg()) return

    setRegSubmitting(true)
    try {
      const payload: CreateRegistrationRequestRequest = {
        firstName: reg.firstName.trim(),
        lastName: reg.lastName.trim(),
        email: reg.email.trim(),
        password: reg.password,
        ...(reg.note.trim() ? { note: reg.note.trim() } : {}),
      }
      await createRegistrationRequest(payload)
      setRegSuccess(true)
    } catch (err) {
      setRegServerError(
        err instanceof Error ? err.message : 'Kayıt talebi oluşturulurken bir hata oluştu.'
      )
    } finally {
      setRegSubmitting(false)
    }
  }

  // ── render ─────────────────────────────────────────────────────────────────

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 flex items-center justify-center p-4">
      <div className="w-full max-w-sm">
        <Logo />

        {view === 'login' ? (
          <>
            <div className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm p-8">
              <form onSubmit={handleLoginSubmit} noValidate>

                <div className="mb-4">
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                    Email
                  </label>
                  <input
                    type="email"
                    value={loginEmail}
                    onChange={(e) => setLoginEmail(e.target.value)}
                    placeholder="ornek@sirket.com"
                    autoComplete="email"
                    className={fieldCls(!!loginEmailError)}
                  />
                  {loginEmailError && (
                    <p className="mt-1.5 text-xs text-red-500">{loginEmailError}</p>
                  )}
                </div>

                <div className="mb-6">
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                    Parola
                  </label>
                  <input
                    type="password"
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                    placeholder="••••••••"
                    autoComplete="current-password"
                    className={fieldCls(!!loginPasswordError)}
                  />
                  {loginPasswordError && (
                    <p className="mt-1.5 text-xs text-red-500">{loginPasswordError}</p>
                  )}
                </div>

                {loginServerError && (
                  <div className="mb-5 px-4 py-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                    <p className="text-sm text-red-600 dark:text-red-400">{loginServerError}</p>
                  </div>
                )}

                <button
                  type="submit"
                  disabled={loginSubmitting}
                  className="w-full py-2.5 bg-violet-600 text-white text-sm font-medium rounded-lg hover:bg-violet-700 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {loginSubmitting ? 'Giriş yapılıyor...' : 'Giriş Yap'}
                </button>

              </form>
            </div>

            <p className="text-center text-sm text-slate-500 dark:text-slate-400 mt-5">
              Hesabın yok mu?{' '}
              <button
                type="button"
                onClick={switchToRegister}
                className="text-violet-600 font-medium hover:text-violet-700 transition-colors"
              >
                Kayıt talebi oluştur
              </button>
            </p>
          </>
        ) : (
          <>
            <div className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm p-8">

              {regSuccess ? (
                <div className="py-2">
                  <div className="px-4 py-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg mb-6">
                    <p className="text-sm text-green-700 dark:text-green-400 font-medium">
                      Kayıt talebiniz alındı.
                    </p>
                    <p className="text-sm text-green-600 dark:text-green-500 mt-1">
                      MANAGER onayı sonrası giriş yapabilirsiniz.
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={switchToLogin}
                    className="w-full py-2.5 bg-violet-600 text-white text-sm font-medium rounded-lg hover:bg-violet-700 transition-colors"
                  >
                    Giriş yap
                  </button>
                </div>
              ) : (
                <form onSubmit={handleRegSubmit} noValidate>

                  <div className="grid grid-cols-2 gap-3 mb-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                        Ad
                      </label>
                      <input
                        type="text"
                        value={reg.firstName}
                        onChange={(e) => setRegField('firstName', e.target.value)}
                        placeholder="Ahmet"
                        autoComplete="given-name"
                        className={fieldCls(!!regErrors.firstName)}
                      />
                      {regErrors.firstName && (
                        <p className="mt-1.5 text-xs text-red-500">{regErrors.firstName}</p>
                      )}
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                        Soyad
                      </label>
                      <input
                        type="text"
                        value={reg.lastName}
                        onChange={(e) => setRegField('lastName', e.target.value)}
                        placeholder="Yılmaz"
                        autoComplete="family-name"
                        className={fieldCls(!!regErrors.lastName)}
                      />
                      {regErrors.lastName && (
                        <p className="mt-1.5 text-xs text-red-500">{regErrors.lastName}</p>
                      )}
                    </div>
                  </div>

                  <div className="mb-4">
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                      Email
                    </label>
                    <input
                      type="email"
                      value={reg.email}
                      onChange={(e) => setRegField('email', e.target.value)}
                      placeholder="ornek@sirket.com"
                      autoComplete="email"
                      className={fieldCls(!!regErrors.email)}
                    />
                    {regErrors.email && (
                      <p className="mt-1.5 text-xs text-red-500">{regErrors.email}</p>
                    )}
                  </div>

                  <div className="mb-4">
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                      Parola
                    </label>
                    <input
                      type="password"
                      value={reg.password}
                      onChange={(e) => setRegField('password', e.target.value)}
                      placeholder="••••••••"
                      autoComplete="new-password"
                      className={fieldCls(!!regErrors.password)}
                    />
                    {regErrors.password && (
                      <p className="mt-1.5 text-xs text-red-500">{regErrors.password}</p>
                    )}
                  </div>

                  <div className="mb-4">
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                      Parola tekrar
                    </label>
                    <input
                      type="password"
                      value={reg.passwordConfirm}
                      onChange={(e) => setRegField('passwordConfirm', e.target.value)}
                      placeholder="••••••••"
                      autoComplete="new-password"
                      className={fieldCls(!!regErrors.passwordConfirm)}
                    />
                    {regErrors.passwordConfirm && (
                      <p className="mt-1.5 text-xs text-red-500">{regErrors.passwordConfirm}</p>
                    )}
                  </div>

                  <div className="mb-6">
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-1.5">
                      Not{' '}
                      <span className="text-slate-400 dark:text-slate-500 font-normal">(opsiyonel)</span>
                    </label>
                    <textarea
                      value={reg.note}
                      onChange={(e) => setRegField('note', e.target.value)}
                      placeholder="Kendinizi kısaca tanıtın..."
                      rows={3}
                      className="w-full px-3.5 py-2.5 rounded-lg border border-slate-200 dark:border-slate-600 focus:border-violet-400 dark:focus:border-violet-500 text-sm text-slate-800 dark:text-slate-100 bg-white dark:bg-slate-700 outline-none transition-colors placeholder:text-slate-400 dark:placeholder:text-slate-500 resize-none"
                    />
                  </div>

                  {regServerError && (
                    <div className="mb-5 px-4 py-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                      <p className="text-sm text-red-600 dark:text-red-400">{regServerError}</p>
                    </div>
                  )}

                  <button
                    type="submit"
                    disabled={regSubmitting}
                    className="w-full py-2.5 bg-violet-600 text-white text-sm font-medium rounded-lg hover:bg-violet-700 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
                  >
                    {regSubmitting ? 'Gönderiliyor...' : 'Kayıt Talebi Oluştur'}
                  </button>

                </form>
              )}
            </div>

            <p className="text-center text-sm text-slate-500 dark:text-slate-400 mt-5">
              Zaten hesabın var mı?{' '}
              <button
                type="button"
                onClick={switchToLogin}
                className="text-violet-600 font-medium hover:text-violet-700 transition-colors"
              >
                Giriş yap
              </button>
            </p>
          </>
        )}

      </div>
    </div>
  )
}

export default LoginPage
