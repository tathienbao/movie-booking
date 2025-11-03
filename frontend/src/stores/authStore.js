import { defineStore } from 'pinia'
import authService from '../api/authService'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: authService.getCurrentUser(),
    token: localStorage.getItem('token'),
    isAuthenticated: authService.isAuthenticated(),
    loading: false,
    error: null
  }),

  getters: {
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isCustomer: (state) => state.user?.role === 'CUSTOMER',
    userName: (state) => state.user?.name || '',
    userEmail: (state) => state.user?.email || ''
  },

  actions: {
    async login(credentials) {
      this.loading = true
      this.error = null

      try {
        const response = await authService.login(credentials)
        const { token, email, name, role } = response.data

        // Store token and user in localStorage
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify({ email, name, role }))

        // Update state
        this.token = token
        this.user = { email, name, role }
        this.isAuthenticated = true

        return response
      } catch (error) {
        this.error = error.response?.data?.error || 'Login failed'
        throw error
      } finally {
        this.loading = false
      }
    },

    async register(userData) {
      this.loading = true
      this.error = null

      try {
        const response = await authService.register(userData)
        // After successful registration, automatically log in
        return await this.login({
          email: userData.email,
          password: userData.password
        })
      } catch (error) {
        this.error = error.response?.data?.error || 'Registration failed'
        throw error
      } finally {
        this.loading = false
      }
    },

    logout() {
      authService.logout()
      this.token = null
      this.user = null
      this.isAuthenticated = false
      this.error = null
    },

    clearError() {
      this.error = null
    }
  }
})
