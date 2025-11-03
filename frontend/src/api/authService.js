import apiClient from './axios'

export default {
  /**
   * Register a new user
   * @param {Object} userData - { email, name, password }
   * @returns {Promise} Response with user data
   */
  register(userData) {
    return apiClient.post('/auth/register', userData)
  },

  /**
   * Login user
   * @param {Object} credentials - { email, password }
   * @returns {Promise} Response with { token, email, name, role }
   */
  login(credentials) {
    return apiClient.post('/auth/login', credentials)
  },

  /**
   * Logout user (client-side only, JWT is stateless)
   */
  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  },

  /**
   * Check if user is authenticated
   * @returns {boolean}
   */
  isAuthenticated() {
    return !!localStorage.getItem('token')
  },

  /**
   * Get current user from localStorage
   * @returns {Object|null}
   */
  getCurrentUser() {
    const userJson = localStorage.getItem('user')
    return userJson ? JSON.parse(userJson) : null
  }
}
