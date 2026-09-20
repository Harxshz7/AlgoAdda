import React, { useState } from 'react'
import { Bookmark } from 'lucide-react'
import { api } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { useNavigate } from 'react-router-dom'

export interface FavoriteButtonProps {
  botId: string
  isFavorited?: boolean
  onToggle?: (newFavoritedState: boolean) => void
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

export const FavoriteButton: React.FC<FavoriteButtonProps> = ({
  botId,
  isFavorited = false,
  onToggle,
  size = 'md',
  className = '',
}) => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [favorited, setFavorited] = useState<boolean>(isFavorited)
  const [isLoading, setIsLoading] = useState<boolean>(false)

  // Sync with prop if it changes
  React.useEffect(() => {
    setFavorited(!!isFavorited)
  }, [isFavorited])

  const handleToggle = async (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()

    if (!user) {
      navigate('/login')
      return
    }

    if (user.role !== 'BUYER') {
      return
    }

    if (isLoading) return

    const nextState = !favorited
    setFavorited(nextState)
    setIsLoading(true)

    try {
      if (nextState) {
        await api.favoriteBot(botId)
      } else {
        await api.unfavoriteBot(botId)
      }
      onToggle?.(nextState)
    } catch (err) {
      // Revert on failure
      setFavorited(!nextState)
      console.error('Failed to toggle favorite:', err)
    } finally {
      setIsLoading(false)
    }
  }

  const iconSizes = {
    sm: 'w-3.5 h-3.5',
    md: 'w-4 h-4',
    lg: 'w-5 h-5',
  }

  const buttonSizes = {
    sm: 'w-7 h-7 p-1',
    md: 'w-8 h-8 p-1.5',
    lg: 'w-9 h-9 p-2',
  }

  return (
    <button
      type="button"
      onClick={handleToggle}
      disabled={isLoading || (user && user.role !== 'BUYER')}
      className={`shrink-0 rounded-xl flex items-center justify-center transition-all ${
        favorited
          ? 'bg-[#5D7052]/15 text-[#5D7052] border border-[#5D7052]/30 hover:bg-[#5D7052]/25'
          : 'bg-[#F5F2EB]/80 dark:bg-[#2C2C24]/80 text-[#78786C] border border-[#DED8CF]/60 hover:text-[#5D7052] hover:bg-[#5D7052]/10 hover:border-[#5D7052]/30'
      } ${buttonSizes[size]} ${className}`}
      title={
        !user
          ? 'Log in to save to watchlist'
          : user.role !== 'BUYER'
          ? 'Watchlist available for buyers'
          : favorited
          ? 'Remove from watchlist'
          : 'Save to watchlist'
      }
      aria-label={favorited ? 'Remove from watchlist' : 'Save to watchlist'}
    >
      <Bookmark
        className={`${iconSizes[size]} transition-transform active:scale-90 ${
          favorited ? 'fill-[#5D7052]' : 'fill-transparent'
        }`}
      />
    </button>
  )
}
