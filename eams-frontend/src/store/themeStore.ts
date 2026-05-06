import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type ThemeMode = 'dark' | 'light'

type S = { mode: ThemeMode; setMode: (m: ThemeMode) => void; toggle: () => void }

function apply(m: ThemeMode) {
  document.documentElement.setAttribute('data-theme', m)
}

export const useThemeStore = create<S>()(
  persist(
    (set, get) => ({
      mode: 'light',
      setMode: (mode) => {
        apply(mode)
        set({ mode })
      },
      toggle: () => {
        const next = get().mode === 'dark' ? 'light' : 'dark'
        apply(next)
        set({ mode: next })
      },
    }),
    {
      name: 'eams-theme',
      partialize: (s) => ({ mode: s.mode }),
      onRehydrateStorage: () => (st) => st && apply(st.mode),
    },
  ),
)
