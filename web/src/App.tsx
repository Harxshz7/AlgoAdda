import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ProtectedRoute } from './components/auth/ProtectedRoute'
import { SellerLayout } from './components/layout/SellerLayout'
import { PublicLayout } from './components/layout/PublicLayout'
import { LoginPage } from './pages/auth/LoginPage'
import { RegisterPage } from './pages/auth/RegisterPage'
import { SellerOnboardPage } from './pages/seller/SellerOnboardPage'
import { SellerDashboardPage } from './pages/seller/SellerDashboardPage'
import { BotUploadPage } from './pages/seller/BotUploadPage'
import { BotDetailPage } from './pages/seller/BotDetailPage'
import { MarketplacePage } from './pages/marketplace/MarketplacePage'
import { ListingDetailPage } from './pages/marketplace/ListingDetailPage'
import { PublicSellerProfilePage } from './pages/seller/PublicSellerProfilePage'
import { BuyerDashboardPage } from './pages/buyer/BuyerDashboardPage'

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Auth Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Public Marketplace Routes */}
          <Route
            path="/marketplace"
            element={
              <PublicLayout>
                <MarketplacePage />
              </PublicLayout>
            }
          />
          <Route
            path="/marketplace/:listingId"
            element={
              <PublicLayout>
                <ListingDetailPage />
              </PublicLayout>
            }
          />
          <Route
            path="/sellers/:sellerId"
            element={
              <PublicLayout>
                <PublicSellerProfilePage />
              </PublicLayout>
            }
          />

          {/* Buyer Authenticated Route */}
          <Route
            path="/buyer/dashboard"
            element={
              <ProtectedRoute requiredRole="BUYER">
                <PublicLayout>
                  <BuyerDashboardPage />
                </PublicLayout>
              </ProtectedRoute>
            }
          />

          {/* Seller Onboarding Route */}
          <Route
            path="/seller/onboard"
            element={
              <ProtectedRoute requiredRole="SELLER">
                <SellerOnboardPage />
              </ProtectedRoute>
            }
          />

          {/* Seller Portal Authenticated Routes */}
          <Route
            path="/seller/dashboard"
            element={
              <ProtectedRoute requiredRole="SELLER">
                <SellerLayout>
                  <SellerDashboardPage />
                </SellerLayout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/seller/bots/new"
            element={
              <ProtectedRoute requiredRole="SELLER">
                <SellerLayout>
                  <BotUploadPage />
                </SellerLayout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/seller/bots/:botId"
            element={
              <ProtectedRoute requiredRole="SELLER">
                <SellerLayout>
                  <BotDetailPage />
                </SellerLayout>
              </ProtectedRoute>
            }
          />

          {/* Fallback Route */}
          <Route path="/" element={<Navigate to="/marketplace" replace />} />
          <Route path="*" element={<Navigate to="/marketplace" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
