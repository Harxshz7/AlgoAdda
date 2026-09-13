import React, { createContext, useContext, useState, useEffect } from 'react'
import { api } from '../lib/api'
import type { User } from '../lib/api'

interface AuthContextType {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (credentials: { email: string; password: string }) => Promise<User>
  register: (data: { email: string; password: string; role: 'BUYER' | 'SELLER' }) => Promise<User>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const savedToken = localStorage.getItem('algoadda_access_token')
    const savedUser = localStorage.getItem('algoadda_user')

    if (savedToken && savedUser) {
      try {
        setToken(savedToken)
        setUser(JSON.parse(savedUser))
      } catch (e) {
        console.error('Failed to parse saved auth state', e)
        localStorage.removeItem('algoadda_access_token')
        localStorage.removeItem('algoadda_user')
      }
    }
    setIsLoading(false)
  }, [])

  const login = async (credentials: { email: string; password: string }) => {
    const response = await api.login(credentials)
    api.setToken(response.accessToken)
    localStorage.setItem('algoadda_user', JSON.stringify(response.user))
    setToken(response.accessToken)
    setUser(response.user)
    return response.user
  }

  const register = async (data: { email: string; password: string; role: 'BUYER' | 'SELLER' }) => {
    const response = await api.register(data)
    api.setToken(response.accessToken)
    localStorage.setItem('algoadda_user', JSON.stringify(response.user))
    setToken(response.accessToken)
    setUser(response.user)
    return response.user
  }

  const logout = () => {
    api.clearToken()
    setUser(null)
    setToken(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
