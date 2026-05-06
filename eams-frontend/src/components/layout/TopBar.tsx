import { Search, Bell, User, Menu, Moon, Sun, LogOut } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import { useThemeStore } from '@/store/themeStore'

interface TopBarProps {
  onToggleSidebar: () => void
}

export default function TopBar({ onToggleSidebar }: TopBarProps) {
  const navigate = useNavigate()
  const user = useAuthStore((state) => state.user)
  const logout = useAuthStore((state) => state.logout)
  const themeMode = useThemeStore((s) => s.mode)
  const toggleTheme = useThemeStore((s) => s.toggle)
  const [query, setQuery] = useState('')

  return (
    <header className="topbar">
      <button className="topbar-menu" onClick={onToggleSidebar} aria-label="Toggle navigation">
        <Menu size={20} />
      </button>
      <div className="topbar-search">
        <Search />
        <input
          type="text"
          placeholder="Global search (assets by name/id/vendor)…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              const next = query.trim()
              navigate(next ? `/assets?search=${encodeURIComponent(next)}` : '/assets')
            }
          }}
        />
      </div>

      <div className="topbar-right">
        <button
          className="topbar-icon"
          title={themeMode === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
          onClick={toggleTheme}
        >
          {themeMode === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
        </button>
        <button className="topbar-icon" title="Notifications">
          <Bell size={18} />
          <span className="badge" />
        </button>
        <div className="user-section">
          <div className="user-info">
            <p className="user-name">{user?.fullName || 'User'}</p>
            <p className="user-email">{user?.email || 'user@example.com'}</p>
          </div>
          <button className="user-avatar" title={user?.fullName || 'Profile'}>
            <User size={16} />
          </button>
          <button
            className="topbar-icon"
            title="Logout"
            onClick={() => {
              logout()
              navigate('/login')
            }}
          >
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </header>
  )
}
