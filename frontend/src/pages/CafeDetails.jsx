import React, { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import axios from '../utils/api'
import CafeMap from '../components/CafeMap'
import ReviewForm from '../components/ReviewForm'
import ReviewList from '../components/ReviewList'
import ClaimCafeModal from '../components/ClaimCafeModal'
import useGooglePhoto from '../hooks/useGooglePhoto'
import { useAuth } from '../contexts/AuthContext'
import { 
  StarIcon, 
  MapPinIcon, 
  PhoneIcon, 
  WifiIcon,
  CurrencyDollarIcon,
  UserGroupIcon
} from '@heroicons/react/24/outline'
import { StarIcon as StarIconSolid } from '@heroicons/react/24/solid'

const CafeDetails = () => {
  const { id } = useParams()
  const { user } = useAuth()
  const [cafe, setCafe] = useState(null)
  const [reviews, setReviews] = useState([])
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('overview')
  const [showClaimModal, setShowClaimModal] = useState(false)
  const [canClaim, setCanClaim] = useState(false)
  
  // Get Google photo for the cafe banner (high resolution)
  const { photoUrl, loading: photoLoading, error: photoError } = useGooglePhoto(cafe, { 
    isBanner: true,
    maxWidth: 800,
    maxHeight: 400
  })

  useEffect(() => {
    const fetchCafeData = async () => {
      try {
        // Get all cafes and find the one we want (temporary workaround for 500 errors)
        const cafesRes = await axios.get('/api/cafes/public/search')
        const foundCafe = cafesRes.data.find(cafe => cafe.id === id)
        
        if (!foundCafe) {
          throw new Error('Cafe not found')
        }

        // Set cafe data
        setCafe(foundCafe)
        
        // Try to get reviews, but don't fail if they error
        try {
          const reviewsRes = await axios.get(`/api/reviews/public/cafe/${id}`)
          setReviews(reviewsRes.data || [])
        } catch (reviewError) {
          console.log('Reviews not available:', reviewError.message)
          setReviews([])
        }
        
        // Check if cafe can be claimed
        try {
          const claimRes = await axios.get(`/api/claims/can-claim/${id}`)
          setCanClaim(claimRes.data.canClaim)
        } catch (error) {
          console.log('Could not check claim status')
          // Default to true if check fails and cafe is not claimed
          setCanClaim(true)
        }
      } catch (error) {
        console.error('Error fetching cafe data:', error)
      } finally {
        setLoading(false)
      }
    }

    if (id) {
      fetchCafeData()
    }
  }, [id])

  const handleReviewSubmitted = (newReview) => {
    setReviews(prev => [newReview, ...prev])
    // Refresh only cafe data for updated ratings without triggering errors
    refreshCafeOnly()
  }

  const refreshCafeOnly = async () => {
    try {
      const cafesRes = await axios.get('/api/cafes/public/search')
      const foundCafe = cafesRes.data.find(cafe => cafe.id === id)
      if (foundCafe) {
        setCafe(foundCafe)
      }
    } catch (error) {
      // Silently fail to avoid error popups after successful review submission
      console.log('Could not refresh cafe ratings:', error.message)
    }
  }

  const handleClaimSubmitted = () => {
    // Refresh claim status
    setCanClaim(false)
    setShowClaimModal(false)
  }

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/3 mb-4"></div>
          <div className="h-64 bg-gray-200 rounded mb-6"></div>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2">
              <div className="h-48 bg-gray-200 rounded"></div>
            </div>
            <div className="h-48 bg-gray-200 rounded"></div>
          </div>
        </div>
      </div>
    )
  }

  if (!cafe) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <div className="text-6xl mb-4">☕</div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Cafe Not Found</h1>
          <p className="text-gray-600">The cafe you're looking for doesn't exist.</p>
        </div>
      </div>
    )
  }

  const renderStars = (rating) => {
    return (
      <div className="flex items-center">
        {[...Array(5)].map((_, i) => (
          i < Math.floor(rating) ? (
            <StarIconSolid key={i} className="h-5 w-5 text-yellow-400" />
          ) : (
            <StarIcon key={i} className="h-5 w-5 text-gray-300" />
          )
        ))}
        <span className="ml-2 text-sm text-gray-600">
          {rating.toFixed(1)} ({cafe.reviewsCount} reviews)
        </span>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between mb-2">
          <h1 className="text-3xl font-bold text-gray-900">{cafe.name}</h1>
          {canClaim && (
            <button
              onClick={() => setShowClaimModal(true)}
              className="btn-secondary text-sm px-4 py-2"
            >
              Claim This Business
            </button>
          )}
        </div>
        <div className="flex items-center space-x-4 text-gray-600 mb-4">
          <div className="flex items-center">
            <MapPinIcon className="h-5 w-5 mr-1" />
            <span>{cafe.address}, {cafe.city}, {cafe.state}</span>
          </div>
          {cafe.phone && (
            <div className="flex items-center">
              <PhoneIcon className="h-5 w-5 mr-1" />
              <span>{cafe.phone}</span>
            </div>
          )}
        </div>
        {renderStars(cafe.avgRating || 0)}
      </div>

      {/* Hero Image */}
      <div className="h-64 bg-gradient-to-br from-coffee-100 to-cream-200 rounded-lg mb-8 flex items-center justify-center relative overflow-hidden">
        {photoLoading ? (
          // Loading state
          <div className="flex flex-col items-center justify-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-coffee-600 mb-3"></div>
            <span className="text-lg text-coffee-600">Loading cafe photo...</span>
          </div>
        ) : photoUrl ? (
          // Show Google photo
          <>
            <img 
              src={photoUrl} 
              alt={`${cafe?.name || 'Cafe'} - interior/exterior photo`}
              className="w-full h-full object-cover"
              onError={(e) => {
                // Fallback to coffee emoji if image fails to load
                e.target.style.display = 'none'
                e.target.parentElement.querySelector('.fallback-emoji').style.display = 'flex'
              }}
            />
            {/* Photo attribution badge */}
            <div className="absolute bottom-4 right-4 bg-black bg-opacity-60 text-white text-sm px-3 py-2 rounded-md">
              📸 Google Places
            </div>
            {/* Gradient overlay for better text readability if needed */}
            <div className="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent"></div>
          </>
        ) : null}
        
        {/* Fallback coffee emoji (hidden by default, shown on image error or no photo) */}
        <div 
          className={`fallback-emoji absolute inset-0 flex flex-col items-center justify-center ${photoUrl && !photoError ? 'hidden' : 'flex'}`}
          style={{ display: photoUrl && !photoError ? 'none' : 'flex' }}
        >
          <span className="text-8xl mb-2">☕</span>
          <span className="text-lg text-coffee-600">No photo available</span>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200 mb-6">
        <nav className="-mb-px flex space-x-8">
          {[
            { id: 'overview', name: 'Overview' },
            { id: 'reviews', name: 'Reviews' },
            { id: 'menu', name: 'Menu' }
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === tab.id
                  ? 'border-coffee-500 text-coffee-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              {tab.name}
            </button>
          ))}
        </nav>
      </div>

      {/* Tab Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">
          {activeTab === 'overview' && (
            <div className="space-y-6">
              {/* Description */}
              {cafe.description && (
                <div>
                  <h3 className="text-lg font-semibold mb-2">About</h3>
                  <p className="text-gray-700">{cafe.description}</p>
                </div>
              )}

              {/* Amenities */}
              <div>
                <h3 className="text-lg font-semibold mb-4">Amenities</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                  {cafe.wifi && (
                    <div className="flex items-center">
                      <WifiIcon className="h-5 w-5 text-green-500 mr-2" />
                      <span>WiFi</span>
                    </div>
                  )}
                  {cafe.seating && (
                    <div className="flex items-center">
                      <UserGroupIcon className="h-5 w-5 text-blue-500 mr-2" />
                      <span>Seating</span>
                    </div>
                  )}
                  {cafe.workFriendly && (
                    <div className="flex items-center">
                      <span className="text-purple-500 mr-2">💻</span>
                      <span>Work-Friendly</span>
                    </div>
                  )}
                  {cafe.petFriendly && (
                    <div className="flex items-center">
                      <span className="text-orange-500 mr-2">🐕</span>
                      <span>Pet-Friendly</span>
                    </div>
                  )}
                  {cafe.priceRange && (
                    <div className="flex items-center">
                      <CurrencyDollarIcon className="h-5 w-5 text-green-500 mr-2" />
                      <span>{cafe.priceRange}</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Map */}
              <div>
                <h3 className="text-lg font-semibold mb-4">Location</h3>
                <CafeMap
                  cafes={[cafe]}
                  center={{ lat: cafe.latitude, lng: cafe.longitude }}
                  zoom={15}
                  className="w-full h-64 rounded-lg"
                />
              </div>
            </div>
          )}

          {activeTab === 'reviews' && (
            <div className="space-y-6">
              {user && (
                <ReviewForm cafeId={id} onReviewSubmitted={handleReviewSubmitted} />
              )}
              <ReviewList reviews={reviews} />
            </div>
          )}

          {activeTab === 'menu' && (
            <div>
              {cafe.menuItems && cafe.menuItems.length > 0 ? (
                <div className="space-y-4">
                  {cafe.menuItems.map((item, index) => (
                    <div key={index} className="border-b border-gray-200 pb-4">
                      <div className="flex justify-between items-start">
                        <div>
                          <h4 className="font-semibold">{item.name}</h4>
                          <p className="text-gray-600 text-sm">{item.description}</p>
                        </div>
                        <span className="font-semibold text-coffee-600">
                          ${item.price.toFixed(2)}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500">Menu information not available.</p>
              )}
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Hours */}
          {cafe.hours && (
            <div className="card">
              <h3 className="text-lg font-semibold mb-4">Hours</h3>
              <div className="space-y-1 text-sm">
                {Object.entries(cafe.hours).map(([day, hours]) => {
                  const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
                  return (
                    <div key={day} className="flex justify-between">
                      <span>{dayNames[day]}:</span>
                      <span>{hours}</span>
                    </div>
                  )
                })}
              </div>
            </div>
          )}

          {/* Contact */}
          <div className="card">
            <h3 className="text-lg font-semibold mb-4">Contact</h3>
            <div className="space-y-2 text-sm">
              {cafe.phone && (
                <div>
                  <span className="font-medium">Phone:</span> {cafe.phone}
                </div>
              )}
              {cafe.website && (
                <div>
                  <span className="font-medium">Website:</span>{' '}
                  <a 
                    href={cafe.website} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="text-coffee-600 hover:underline"
                  >
                    Visit
                  </a>
                </div>
              )}
              {cafe.socials && cafe.socials.length > 0 && (
                <div>
                  <span className="font-medium">Social:</span>
                  <div className="mt-1 space-y-1">
                    {cafe.socials.map((social, index) => (
                      <a
                        key={index}
                        href={social}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="block text-coffee-600 hover:underline text-sm"
                      >
                        {social.includes('instagram') ? 'Instagram' : 'Social Link'}
                      </a>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
      
      {/* Claim Modal */}
      {cafe && (
        <ClaimCafeModal
          cafe={cafe}
          isOpen={showClaimModal}
          onClose={() => setShowClaimModal(false)}
          onClaimSubmitted={handleClaimSubmitted}
        />
      )}
    </div>
  )
}

export default CafeDetails
