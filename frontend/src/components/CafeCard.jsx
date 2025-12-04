import React from 'react'
import { Link } from 'react-router-dom'
import { 
  StarIcon, 
  MapPinIcon, 
  WifiIcon,
  CurrencyDollarIcon
} from '@heroicons/react/24/outline'
import useGooglePhoto from '../hooks/useGooglePhoto'

const CafeCard = ({ cafe }) => {
  const { photoUrl, loading: photoLoading, error: photoError } = useGooglePhoto(cafe)

  return (
    <Link to={`/cafe/${cafe.id}`} className="cafe-card">
      {/* Cafe Image */}
      <div className="h-48 bg-gradient-to-br from-coffee-100 to-cream-200 flex items-center justify-center relative overflow-hidden">
        {photoLoading ? (
          // Loading state
          <div className="flex flex-col items-center justify-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-coffee-600 mb-2"></div>
            <span className="text-sm text-coffee-600">Loading photo...</span>
          </div>
        ) : photoUrl ? (
          // Show Google photo
          <img 
            src={photoUrl} 
            alt={`${cafe.name} - cafe photo`}
            className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
            onError={(e) => {
              // Fallback to coffee emoji if image fails to load
              e.target.style.display = 'none'
              e.target.nextElementSibling.style.display = 'flex'
            }}
          />
        ) : null}
        
        {/* Fallback coffee emoji (hidden by default, shown on image error or no photo) */}
        <div 
          className={`absolute inset-0 flex items-center justify-center ${photoUrl && !photoError ? 'hidden' : 'flex'}`}
          style={{ display: photoUrl && !photoError ? 'none' : 'flex' }}
        >
          <span className="text-6xl">☕</span>
        </div>
        
        {/* Photo attribution badge */}
        {photoUrl && (
          <div className="absolute bottom-2 right-2 bg-black bg-opacity-50 text-white text-xs px-2 py-1 rounded">
            📸 Google
          </div>
        )}
      </div>
      
      <div className="p-4">
        {/* Header */}
        <div className="flex justify-between items-start mb-2">
          <h3 className="text-lg font-semibold text-gray-900 truncate">{cafe.name}</h3>
          <div className="flex items-center">
            <StarIcon className="h-4 w-4 text-yellow-400 fill-current" />
            <span className="ml-1 text-sm font-medium text-gray-900">
              {cafe.avgRating ? cafe.avgRating.toFixed(1) : '0.0'}
            </span>
            <span className="ml-1 text-sm text-gray-500">
              ({cafe.reviewsCount || 0})
            </span>
          </div>
        </div>

        {/* Location */}
        <div className="flex items-center text-gray-600 mb-3">
          <MapPinIcon className="h-4 w-4 mr-1 flex-shrink-0" />
          <span className="text-sm truncate">{cafe.address}, {cafe.city}</span>
        </div>

        {/* Description */}
        {cafe.description && (
          <p className="text-gray-600 text-sm mb-3 line-clamp-2">
            {cafe.description}
          </p>
        )}

        {/* Amenities */}
        <div className="flex flex-wrap gap-2 mb-3">
          {cafe.wifi && (
            <span className="badge badge-blue">
              <WifiIcon className="h-3 w-3 mr-1" />
              WiFi
            </span>
          )}
          {cafe.priceRange && (
            <span className="badge badge-green">
              <CurrencyDollarIcon className="h-3 w-3 mr-1" />
              {cafe.priceRange}
            </span>
          )}
          {cafe.workFriendly && (
            <span className="badge badge-blue">Work-friendly</span>
          )}
          {cafe.petFriendly && (
            <span className="badge badge-yellow">Pet-friendly</span>
          )}
        </div>

        {/* Tags */}
        {cafe.tags && cafe.tags.length > 0 && (
          <div className="flex items-center">
            <span className="text-xs text-gray-500">
              {cafe.tags.slice(0, 2).join(', ')}
              {cafe.tags.length > 2 && '...'}
            </span>
          </div>
        )}
      </div>
    </Link>
  )
}

export default CafeCard
