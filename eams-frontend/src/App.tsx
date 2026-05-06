import { useEffect, useState } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'sonner'
import Sidebar from './components/layout/Sidebar'
import TopBar from './components/layout/TopBar'
import LoginPage from './pages/auth/LoginPage'
import MagicLinkVerifyPage from './pages/auth/MagicLinkVerifyPage'
import DashboardPage from './pages/dashboard/DashboardPage'
import AssetListPage from './pages/assets/AssetListPage'
import AssetCreatePage from './pages/assets/AssetCreatePage'
import AssetEditPage from './pages/assets/AssetEditPage'
import AssetDetailPage from './pages/assets/AssetDetailPage'
import CertificateListPage from './pages/certificates/CertificateListPage'
import CertificateFormPage from './pages/certificates/CertificateFormPage'
import CertificateDetailPage from './pages/certificates/CertificateDetailPage'
import LicenceListPage from './pages/licences/LicenceListPage'
import LicenceFormPage from './pages/licences/LicenceFormPage'
import LicenceDetailPage from './pages/licences/LicenceDetailPage'
import UserListPage from './pages/users/UserListPage'
import ReportsPage from './pages/reports/ReportsPage'
import AiAnalyticsPage from './pages/ai/AiAnalyticsPage'
import PlaceholderPage from './pages/PlaceholderPage'
import ProtectedRoute from './router/ProtectedRoute'
import { useThemeStore } from './store/themeStore'

function App() {
  const themeMode = useThemeStore((s) => s.mode)
  const [sidebarOpen, setSidebarOpen] = useState(false)

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', themeMode)
  }, [themeMode])

  return (
    <>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/auth/verify" element={<MagicLinkVerifyPage />} />

        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <div className="app-container">
                <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
                <div className="main-content">
                  <TopBar onToggleSidebar={() => setSidebarOpen((v) => !v)} />
                  <Routes>
                    <Route index element={<Navigate to="/dashboard" replace />} />
                    <Route path="dashboard" element={<DashboardPage />} />
                    <Route path="assets" element={<AssetListPage />} />
                    <Route path="assets/new" element={<AssetCreatePage />} />
                    <Route path="assets/:id" element={<AssetDetailPage />} />
                    <Route path="assets/:id/edit" element={<AssetEditPage />} />
                    <Route path="certificates" element={<CertificateListPage />} />
                    <Route path="certificates/new" element={<CertificateFormPage />} />
                    <Route path="certificates/:id" element={<CertificateDetailPage />} />
                    <Route path="certificates/:id/edit" element={<CertificateFormPage />} />
                    <Route path="licences" element={<LicenceListPage />} />
                    <Route path="licences/new" element={<LicenceFormPage />} />
                    <Route path="licences/:id" element={<LicenceDetailPage />} />
                    <Route path="licences/:id/edit" element={<LicenceFormPage />} />
                    <Route path="users" element={<UserListPage />} />
                    <Route path="reports" element={<ReportsPage />} />
                    <Route path="ai" element={<AiAnalyticsPage />} />
                    <Route path="transactions" element={<PlaceholderPage title="Check-In/Out" />} />
                    <Route path="access-grants" element={<PlaceholderPage title="Access Grants" />} />
                    <Route path="change-requests" element={<PlaceholderPage title="Change Requests" />} />
                    <Route path="audit" element={<PlaceholderPage title="Audit Log" />} />
                    <Route path="admin" element={<PlaceholderPage title="Admin Console" />} />
                  </Routes>
                </div>
              </div>
            </ProtectedRoute>
          }
        />
      </Routes>
      <Toaster position="top-right" theme={themeMode === 'dark' ? 'dark' : 'light'} />
    </>
  )
}

export default App
