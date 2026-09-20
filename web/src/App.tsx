import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import { ProtectedRoute } from './components/auth/ProtectedRoute'
import { SellerLayout } from './components/layout/SellerLayout'
import { PublicLayout } from './components/layout/PublicLayout'
import { LoginPage } from './pages/auth/LoginPage'
import { RegisterPage } from './pages/auth/RegisterPage'
import { SellerOnboardPage } from './pages/seller/SellerOnboardPage'
import { SellerDashboardPage } from './pages/seller/SellerDashboardPage'
import { SellerAnalyticsPage } from './pages/seller/SellerAnalyticsPage'
import { BotUploadPage } from './pages/seller/BotUploadPage'
import { BotDetailPage } from './pages/seller/BotDetailPage'
import { MarketplacePage } from './pages/marketplace/MarketplacePage'
import { ListingDetailPage } from './pages/marketplace/ListingDetailPage'
import { StorePage } from './pages/store/StorePage'
import { StoreListingDetailPage } from './pages/store/StoreListingDetailPage'
import { PublicSellerProfilePage } from './pages/seller/PublicSellerProfilePage'
import { BuyerDashboardPage } from './pages/buyer/BuyerDashboardPage'
import { WatchlistPage } from './pages/buyer/WatchlistPage'
import { CartPage } from './pages/buyer/CartPage'
import { AboutPage } from './pages/about/AboutPage'
import { TermsPage } from './pages/legal/TermsPage'
import { RiskDisclosurePage } from './pages/legal/RiskDisclosurePage'
import { PrivacyPage } from './pages/legal/PrivacyPage'
import { AdminReportsPage } from './pages/admin/AdminReportsPage'

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <CartProvider>
        <BrowserRouter>
          <Routes>
            {/* Public Auth Routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Public About & Legal Routes */}
            <Route
              path="/about"
              element={
                <PublicLayout>
                  <AboutPage />
                </PublicLayout>
              }
            />
            <Route
              path="/terms"
              element={
                <PublicLayout>
                  <TermsPage />
                </PublicLayout>
              }
            />
            <Route
              path="/risk-disclosure"
              element={
                <PublicLayout>
                  <RiskDisclosurePage />
                </PublicLayout>
              }
            />
            <Route
              path="/privacy"
              element={
                <PublicLayout>
                  <PrivacyPage />
                </PublicLayout>
              }
            />

            {/* Public Official Store Routes */}
            <Route
              path="/store"
              element={
                <PublicLayout>
                  <StorePage />
                </PublicLayout>
              }
            />
            <Route
              path="/store/:listingId"
              element={
                <PublicLayout>
                  <StoreListingDetailPage />
                </PublicLayout>
              }
            />

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

            {/* Buyer Authenticated Routes */}
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

            <Route
              path="/buyer/watchlist"
              element={
                <ProtectedRoute requiredRole="BUYER">
                  <PublicLayout>
                    <WatchlistPage />
                  </PublicLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/cart"
              element={
                <ProtectedRoute requiredRole="BUYER">
                  <PublicLayout>
                    <CartPage />
                  </PublicLayout>
                </ProtectedRoute>
              }
            />

            {/* Admin Authenticated Routes */}
            <Route
              path="/admin/reports"
              element={
                <ProtectedRoute requiredRole="ADMIN">
                  <PublicLayout>
                    <AdminReportsPage />
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
              path="/seller/analytics"
              element={
                <ProtectedRoute requiredRole="SELLER">
                  <SellerLayout>
                    <SellerAnalyticsPage />
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
      </CartProvider>
    </AuthProvider>
  )
}

export default App
