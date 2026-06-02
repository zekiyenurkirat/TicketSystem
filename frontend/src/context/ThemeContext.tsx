import { createContext, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'

const THEME_KEY = 'ts-theme'

export type Theme = 'light' | 'dark'

type ThemeContextValue = {
  theme: Theme
  toggleTheme: () => void
}

const ThemeContext = createContext<ThemeContextValue | null>(null)

export function ThemeProvider({ children }: { children: ReactNode }) {
  const stored = localStorage.getItem(THEME_KEY)
  const [theme, setTheme] = useState<Theme>(stored === 'dark' ? 'dark' : 'light')

  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark')
  }, [theme])

  function toggleTheme() {
    const next: Theme = theme === 'light' ? 'dark' : 'light'
    setTheme(next)
    localStorage.setItem(THEME_KEY, next)
  }

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTheme(): ThemeContextValue {
  const ctx = useContext(ThemeContext)
  if (!ctx) throw new Error('useTheme bir ThemeProvider içinde kullanılmalıdır.')
  return ctx
}
