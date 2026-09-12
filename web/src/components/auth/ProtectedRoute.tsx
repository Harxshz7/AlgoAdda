import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { LedIndicator } from '../ui'

interface ProtectedRouteProps {
  children: React.ReactNode
  requiredRole?: 'SELLER' | 'BUYER' | 'ADMIN'
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredRole = 'SELLER',
}) => {
  const { isAuthenticated, isLoading, user } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#e0e5ec]">
        <div className="flex flex-col items-center gap-4">
          <LedIndicator status="amber" label="INITIALIZING STATION..." pulse />
          <span className="text-xs font-technical text-[#718096]">AUTHENTICATING CREDENTIALS</span>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (requiredRole && user?.role !== requiredRole && user?.role !== 'ADMIN') {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}
