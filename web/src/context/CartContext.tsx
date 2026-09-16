import React, { createContext, useContext, useState, useEffect } from 'react'
import { api } from '../lib/api'
import type { Cart } from '../lib/api'
import { useAuth } from './AuthContext'

interface CartContextType {
  cart: Cart | null
  cartCount: number
  loading: boolean
  addToCart: (listingId: string) => Promise<void>
  removeFromCart: (cartItemId: string) => Promise<void>
  clearCart: () => Promise<void>
  refreshCart: () => Promise<void>
}

const CartContext = createContext<CartContextType | undefined>(undefined)

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, token } = useAuth()
  const [cart, setCart] = useState<Cart | null>(null)
  const [loading, setLoading] = useState(false)

  const refreshCart = async () => {
    if (!token || user?.role !== 'BUYER') {
      setCart(null)
      return
    }
    try {
      setLoading(true)
      const data = await api.getCart()
      setCart(data)
    } catch (err) {
      console.error('Failed to fetch cart:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    refreshCart()
  }, [user, token])

  const addToCart = async (listingId: string) => {
    if (!token || user?.role !== 'BUYER') {
      throw new Error('Please log in as a buyer to add items to your cart')
    }
    const updatedCart = await api.addToCart(listingId)
    setCart(updatedCart)
  }

  const removeFromCart = async (cartItemId: string) => {
    if (!token) return
    const updatedCart = await api.removeFromCart(cartItemId)
    setCart(updatedCart)
  }

  const clearCart = async () => {
    if (!token) return
    const updatedCart = await api.clearCart()
    setCart(updatedCart)
  }

  return (
    <CartContext.Provider
      value={{
        cart,
        cartCount: cart ? cart.itemCount : 0,
        loading,
        addToCart,
        removeFromCart,
        clearCart,
        refreshCart,
      }}
    >
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => {
  const context = useContext(CartContext)
  if (!context) {
    throw new Error('useCart must be used within a CartProvider')
  }
  return context
}
