import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../../context/CartContext'
import { useAuth } from '../../context/AuthContext'
import { api } from '../../lib/api'
import { Button, Card } from '../../components/ui'
import { ShoppingCart, Trash2, ShieldCheck, ArrowRight } from 'lucide-react'

export const CartPage: React.FC = () => {
  const { cart, removeFromCart, clearCart } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [isProcessing, setIsProcessing] = useState(false)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  const items = cart?.items || []
  const totalAmount = cart?.totalAmount || 0

  const handleCheckout = async () => {
    if (!user || user.role !== 'BUYER') {
      navigate('/login')
      return
    }

    if (items.length === 0) return

    try {
      setIsProcessing(true)
      setErrorMsg(null)

      const orderData = await api.createOrder()

      // Load Razorpay Checkout SDK
      const loadRazorpayScript = () => {
        return new Promise<boolean>((resolve) => {
          if ((window as any).Razorpay) {
            resolve(true)
            return
          }
          const script = document.createElement('script')
          script.src = 'https://checkout.razorpay.com/v1/checkout.js'
          script.onload = () => resolve(true)
          script.onerror = () => resolve(false)
          document.body.appendChild(script)
        })
      }

      const scriptLoaded = await loadRazorpayScript()
      if (!scriptLoaded) {
        alert('Failed to load Razorpay checkout SDK. Please check your network connection.')
        setIsProcessing(false)
        return
      }

      const options = {
        key: orderData.razorpayKeyId || 'rzp_test_dummyKeyId',
        amount: orderData.amountInPaise,
        currency: orderData.currency || 'INR',
        name: 'AlgoAdda',
        description: `Purchase ${items.length} quantitative algorithm(s)`,
        order_id: orderData.razorpayOrderId,
        handler: async function () {
          await clearCart()
          navigate('/buyer/dashboard')
        },
        prefill: {
          email: user.email,
        },
        theme: {
          color: '#5D7052',
        },
      }

      const rzp = new (window as any).Razorpay(options)
      rzp.on('payment.failed', function (response: any) {
        setErrorMsg(response.error?.description || 'Payment failed. Please try again.')
      })
      rzp.open()
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to initiate checkout.')
    } finally {
      setIsProcessing(false)
    }
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-[#DED8CF] pb-4">
        <div className="flex items-center gap-3">
          <ShoppingCart className="w-6 h-6 text-[#5D7052]" />
          <h1 className="font-heading font-bold text-2xl text-[#2C2C24]">Shopping Cart</h1>
          <span className="text-sm font-semibold text-[#78786C] bg-[#EFECE6] px-2.5 py-0.5 rounded-full">
            {items.length} {items.length === 1 ? 'item' : 'items'}
          </span>
        </div>
        {items.length > 0 && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => clearCart()}
            className="text-xs text-[#78786C] hover:text-[#A85448]"
          >
            Clear Cart
          </Button>
        )}
      </div>

      {errorMsg && (
        <div className="p-4 bg-[#FDF2F0] border border-[#E8C4C1] text-[#A85448] text-sm rounded-lg font-body">
          {errorMsg}
        </div>
      )}

      {items.length === 0 ? (
        <Card className="p-12 text-center space-y-4">
          <div className="w-12 h-12 bg-[#EFECE6] text-[#78786C] rounded-full flex items-center justify-center mx-auto">
            <ShoppingCart className="w-6 h-6" />
          </div>
          <p className="text-[#78786C] font-body text-base">Your cart is currently empty.</p>
          <div className="pt-2 flex justify-center gap-4">
            <Link to="/marketplace">
              <Button variant="primary" size="sm">
                Browse Marketplace
              </Button>
            </Link>
            <Link to="/store">
              <Button variant="secondary" size="sm">
                Browse AlgoAdda Store
              </Button>
            </Link>
          </div>
        </Card>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Cart Items List */}
          <div className="lg:col-span-2 space-y-3">
            {items.map((item) => (
              <Card key={item.id} className="p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 overflow-hidden min-w-0">
                <div className="space-y-1 min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2 min-w-0">
                    <h3 className="font-heading font-bold text-base text-[#2C2C24] truncate min-w-0">
                      {item.botName}
                    </h3>
                    {item.official && (
                      <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-[#C18C5D]/15 text-[#C18C5D] shrink-0">
                        Official
                      </span>
                    )}
                  </div>
                  <div className="text-xs text-[#78786C] font-body flex flex-wrap items-center gap-2">
                    <span>{item.strategyType}</span>
                    <span>•</span>
                    <span>v{item.versionNumber}</span>
                    <span>•</span>
                    <span>By {item.sellerName}</span>
                  </div>
                </div>

                <div className="flex items-center justify-between sm:justify-end w-full sm:w-auto gap-4 shrink-0 pt-2 sm:pt-0 border-t sm:border-t-0 border-[#DED8CF]/40">
                  <span className="font-heading font-bold text-lg text-[#2C2C24]">
                    ₹{item.price.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </span>
                  <button
                    onClick={() => removeFromCart(item.id)}
                    className="p-1.5 text-[#78786C] hover:text-[#A85448] hover:bg-[#FDF2F0] rounded-md transition-colors"
                    title="Remove item"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </Card>
            ))}
          </div>

          {/* Cart Summary Box */}
          <div className="space-y-4">
            <Card className="p-5 space-y-4 bg-[#FAF8F5]">
              <h2 className="font-heading font-bold text-lg text-[#2C2C24]">Order Summary</h2>

              <div className="space-y-2 border-t border-[#DED8CF]/60 pt-3 text-sm font-body">
                <div className="flex justify-between text-[#78786C]">
                  <span>Subtotal ({items.length} items)</span>
                  <span>₹{totalAmount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                </div>
                <div className="flex justify-between text-[#78786C]">
                  <span>Compliance Scanning</span>
                  <span className="text-[#5D7052] font-medium">Free / Verified</span>
                </div>
                <div className="flex justify-between font-bold text-base text-[#2C2C24] border-t border-[#DED8CF]/60 pt-3">
                  <span>Total Amount</span>
                  <span>₹{totalAmount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                </div>
              </div>

              <div className="text-[11px] text-[#78786C] font-body text-center leading-snug pt-1">
                Algorithmic trading carries risk. By continuing, you agree to our{' '}
                <Link to="/risk-disclosure" target="_blank" rel="noopener noreferrer" className="text-[#5D7052] font-semibold underline hover:text-[#4a5a41]">
                  Risk Disclosure
                </Link>
                .
              </div>

              <Button
                variant="primary"
                size="lg"
                className="w-full gap-2 justify-center"
                onClick={handleCheckout}
                disabled={isProcessing}
              >
                <span>{isProcessing ? 'Processing...' : 'Checkout Now'}</span>
                <ArrowRight className="w-4 h-4" />
              </Button>

              <div className="flex items-start gap-2 text-xs text-[#78786C] font-body pt-1">
                <ShieldCheck className="w-4 h-4 text-[#5D7052] shrink-0 mt-0.5" />
                <span>
                  Disclosed logic algorithms &amp; non-custodial strategy downloads under 2026 SEBI compliance framework.
                </span>
              </div>
            </Card>
          </div>
        </div>
      )}
    </div>
  )
}
