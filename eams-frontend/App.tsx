import { Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'sonner'
import Sidebar from './src/components/layout/Sidebar'
import TopBar from './src/components/layout/TopBar'
import LoginPage from './src/pages/auth/LoginPage'
import DashboardPage from './src/pages/dashboard/DashboardPage'
import ProtectedRoute from './src/router/ProtectedRoute'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <div className="app-container">
              <Sidebar />
              <div className="main-content">
                <TopBar />
                <Routes>
                  <Route index element={<Navigate to="/dashboard" replace />} />
                  <Route path="dashboard" element={<DashboardPage />} />
                  {/* More routes can be added here */}
                </Routes>
              </div>
            </div>
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}

export default App