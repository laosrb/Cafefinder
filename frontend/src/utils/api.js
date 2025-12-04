import axios from 'axios'

// Configure axios defaults - use environment variable or fallback to proxy
const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? '' : 'http://localhost:8080')
axios.defaults.baseURL = API_URL
axios.defaults.headers.common['Content-Type'] = 'application/json'

// Add response interceptor to handle errors globally
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      
      // Try to refresh token
      try {
        const response = await axios.post('/api/auth/refresh')
        const { token } = response.data
        
        // Update stored token
        localStorage.setItem('token', token)
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
        
        // Retry original request
        originalRequest.headers['Authorization'] = `Bearer ${token}`
        return axios(originalRequest)
      } catch (refreshError) {
        // Refresh failed, redirect to login
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        delete axios.defaults.headers.common['Authorization']
        window.location.href = '/login'
      }
    }
    
    return Promise.reject(error)
  }
)

export default axios
