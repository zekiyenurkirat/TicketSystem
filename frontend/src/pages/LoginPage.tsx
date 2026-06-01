import { useState } from 'react'
import type { FormEvent } from 'react'
import { useAuth } from '../context/AuthContext'
import type { LoginRequest } from '../types/auth.types'

function LoginPage() {
  const { login } = useAuth()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [emailError, setEmailError] = useState('')
  const [passwordError, setPasswordError] = useState('')
  const [serverError, setServerError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  function validate(): boolean {
    let valid = true
    setEmailError('')
    setPasswordError('')

    if (!email.trim()) {
      setEmailError('Email adresi zorunludur.')
      valid = false
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setEmailError('Geçerli bir email adresi girin.')
      valid = false
    }

    if (!password) {
      setPasswordError('Parola zorunludur.')
      valid = false
    }

    return valid
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setServerError('')

    if (!validate()) return

    setIsSubmitting(true)
    try {
      const request: LoginRequest = { email: email.trim(), password }
      await login(request)
    } catch (err) {
      setServerError(err instanceof Error ? err.message : 'Giriş sırasında bir hata oluştu.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="w-full max-w-sm">

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
          <h1 className="text-2xl font-semibold text-slate-800">TicketSystem</h1>
          <p className="text-sm text-slate-500 mt-1">IT Servis Yönetim Sistemi</p>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-8">
          <form onSubmit={handleSubmit} noValidate>

            <div className="mb-4">
              <label className="block text-sm font-medium text-slate-700 mb-1.5">
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="ornek@sirket.com"
                autoComplete="email"
                className={`w-full px-3.5 py-2.5 rounded-lg border text-sm text-slate-800 outline-none transition-colors placeholder:text-slate-400 ${
                  emailError
                    ? 'border-red-300 focus:border-red-400'
                    : 'border-slate-200 focus:border-violet-400'
                }`}
              />
              {emailError && (
                <p className="mt-1.5 text-xs text-red-500">{emailError}</p>
              )}
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium text-slate-700 mb-1.5">
                Parola
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                autoComplete="current-password"
                className={`w-full px-3.5 py-2.5 rounded-lg border text-sm text-slate-800 outline-none transition-colors placeholder:text-slate-400 ${
                  passwordError
                    ? 'border-red-300 focus:border-red-400'
                    : 'border-slate-200 focus:border-violet-400'
                }`}
              />
              {passwordError && (
                <p className="mt-1.5 text-xs text-red-500">{passwordError}</p>
              )}
            </div>

            {serverError && (
              <div className="mb-5 px-4 py-3 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-600">{serverError}</p>
              </div>
            )}

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-2.5 bg-violet-600 text-white text-sm font-medium rounded-lg hover:bg-violet-700 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {isSubmitting ? 'Giriş yapılıyor...' : 'Giriş Yap'}
            </button>

          </form>
        </div>

      </div>
    </div>
  )
}

export default LoginPage
