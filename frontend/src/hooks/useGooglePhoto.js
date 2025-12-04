import { useState, useEffect, useRef } from 'react'

// Custom hook to fetch Google Places photos for cafes
const useGooglePhoto = (cafe, options = {}) => {
  const { 
    maxWidth = 400, 
    maxHeight = 300, 
    isBanner = false 
  } = options
  const [photoUrl, setPhotoUrl] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const abortControllerRef = useRef(null)

  useEffect(() => {
    if (!cafe) {
      setLoading(false)
      return
    }

    // Cancel previous request if any
    if (abortControllerRef.current) {
      abortControllerRef.current.abort()
    }
    abortControllerRef.current = new AbortController()

    const fetchPhoto = async () => {
      try {
        setLoading(true)
        setError(null)

        // Wait for Google Maps API to be available (with retry logic)
        let retries = 0
        const maxRetries = 10
        while (!window.google?.maps?.places && retries < maxRetries) {
          await new Promise(resolve => setTimeout(resolve, 500))
          retries++
        }

        if (!window.google?.maps?.places) {
          setError('Google Maps API not loaded')
          setLoading(false)
          return
        }

        // Create a service if it doesn't exist
        if (!window.placesService) {
          const dummyDiv = document.createElement('div')
          window.placesService = new window.google.maps.places.PlacesService(dummyDiv)
        }

        // Set a timeout to prevent hanging
        const timeoutId = setTimeout(() => {
          setLoading(false)
          setError('Timeout fetching photo')
        }, 8000)

        const query = `${cafe.name} ${cafe.address || ''} ${cafe.city || ''}`.trim()
        
        // Try modern findPlaceFromQuery first, fallback to textSearch
        let place = null
        let status = null

        // Try findPlaceFromQuery (modern API)
        if (window.placesService.findPlaceFromQuery) {
          try {
            const request = {
              query: query,
              fields: ['place_id', 'photos', 'name'],
              locationBias: {
                center: { lat: cafe.latitude, lng: cafe.longitude },
                radius: 100
              }
            }

            const response = await new Promise((resolve, reject) => {
              window.placesService.findPlaceFromQuery(request, (results, status) => {
                if (status === 'OK' || status === window.google.maps.places.PlacesServiceStatus.OK) {
                  resolve({ results, status })
                } else {
                  reject(status)
                }
              })
            })

            if (response.results && response.results.length > 0) {
              place = response.results[0]
              status = response.status
            }
          } catch (err) {
            // Fallback to textSearch if findPlaceFromQuery fails
            console.log('findPlaceFromQuery failed, trying textSearch:', err)
          }
        }

        // Fallback to textSearch if findPlaceFromQuery didn't work or isn't available
        if (!place && window.placesService.textSearch) {
          const request = {
            query: query,
            fields: ['place_id', 'photos', 'name'],
            locationBias: {
              center: { lat: cafe.latitude, lng: cafe.longitude },
              radius: 100
            }
          }

          await new Promise((resolve, reject) => {
            window.placesService.textSearch(request, (results, searchStatus) => {
              clearTimeout(timeoutId)
              
              if ((searchStatus === 'OK' || searchStatus === window.google.maps.places.PlacesServiceStatus.OK) && results && results.length > 0) {
                place = results[0]
                status = searchStatus
                resolve()
              } else {
                reject(searchStatus)
              }
            })
          })
        }

        clearTimeout(timeoutId)

        // If we have a place with photos, use them
        if (place && place.photos && place.photos.length > 0) {
          try {
            const photoSize = isBanner ? {
              maxWidth: 800,
              maxHeight: 400
            } : {
              maxWidth,
              maxHeight
            }
            
            const photo = place.photos[0].getUrl(photoSize)
            setPhotoUrl(photo)
          } catch (photoError) {
            console.log('Error getting photo URL:', photoError)
            setError('Failed to load photo')
          }
        } else if (place && (!place.photos || place.photos.length === 0)) {
          // Place found but no photos - try getDetails to get full photo data
          try {
            const detailsRequest = {
              placeId: place.place_id,
              fields: ['photos']
            }

            window.placesService.getDetails(detailsRequest, (detailsResult, detailsStatus) => {
              if ((detailsStatus === 'OK' || detailsStatus === window.google.maps.places.PlacesServiceStatus.OK) && 
                  detailsResult && detailsResult.photos && detailsResult.photos.length > 0) {
                try {
                  const photoSize = isBanner ? {
                    maxWidth: 800,
                    maxHeight: 400
                  } : {
                    maxWidth,
                    maxHeight
                  }
                  
                  const photo = detailsResult.photos[0].getUrl(photoSize)
                  setPhotoUrl(photo)
                } catch (photoError) {
                  console.log('Error getting photo URL from details:', photoError)
                  setError('Failed to load photo')
                }
              } else {
                setError('No photos available')
              }
              setLoading(false)
            })
            return // Don't set loading to false here, wait for getDetails callback
          } catch (detailsError) {
            console.log('Error getting place details:', detailsError)
            setError('No photos available')
          }
        } else {
          setError('Place not found')
        }

        setLoading(false)
      } catch (err) {
        console.error('Error fetching Google photo:', err)
        if (err.name !== 'AbortError') {
          setError(err.message || 'Failed to fetch photo')
        }
        setLoading(false)
      }
    }

    fetchPhoto()

    // Cleanup function
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort()
      }
    }
  }, [cafe, maxWidth, maxHeight, isBanner])

  return { photoUrl, loading, error }
}

export default useGooglePhoto
