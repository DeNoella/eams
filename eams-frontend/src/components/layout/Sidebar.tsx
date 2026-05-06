import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard, Package, Shield, Key, ArrowLeftRight,
  Landmark, Users, FileText, Bot, Settings, FileKey, ClipboardList,
  X,
} from 'lucide-react'
import { useIsSystemAdmin } from '@/hooks/usePermissions'

interface SidebarProps {
  open: boolean
  onClose: () => void
}

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
  { name: 'Assets', href: '/assets', icon: Package },
  { name: 'Certificates', href: '/certificates', icon: FileKey },
  { name: 'Licences', href: '/licences', icon: Shield },
  { name: 'Check-In/Out', href: '/transactions', icon: ArrowLeftRight },
  { name: 'Access Grants', href: '/access-grants', icon: Key },
  { name: 'Change Requests', href: '/change-requests', icon: ClipboardList },
  { name: 'Users', href: '/users', icon: Users },
  { name: 'Audit Log', href: '/audit', icon: FileText },
  { name: 'Reports', href: '/reports', icon: Landmark },
  { name: 'AI Analytics', href: '/ai', icon: Bot },
] as const

export default function Sidebar({ open, onClose }: SidebarProps) {
  const isAdmin = useIsSystemAdmin()
  const items = isAdmin
    ? [...navigation, { name: 'Admin', href: '/admin', icon: Settings }]
    : navigation

  return (
    <>
      <div className={`sidebar-scrim ${open ? 'open' : ''}`} onClick={onClose} />
      <aside className={`sidebar ${open ? 'open' : ''}`}>
        <div className="sidebar-logo">
          <span>National Bank of Rwanda · BNR</span>
          <button className="sidebar-close" onClick={onClose} aria-label="Close menu">
            <X size={18} />
          </button>
        </div>
        <nav className="sidebar-nav scrollbar-thin">
          {items.map((item) => (
            <NavLink
              key={item.name}
              to={item.href}
              onClick={onClose}
              className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
            >
              <item.icon />
              <span>{item.name}</span>
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  )
}
