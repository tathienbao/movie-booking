import { defineStore } from 'pinia'
import bookingService from '../api/bookingService'

export const useBookingStore = defineStore('booking', {
  state: () => ({
    bookings: [],
    currentBooking: null,
    loading: false,
    error: null
  }),

  getters: {
    totalBookings: (state) => state.bookings.length,

    getBookingById: (state) => (id) => {
      return state.bookings.find(booking => booking.id === id)
    },

    bookingsByMovie: (state) => (movieId) => {
      return state.bookings.filter(booking => booking.movieId === movieId)
    },

    totalAmount: (state) => {
      return state.bookings.reduce((sum, booking) => sum + booking.totalPrice, 0)
    }
  },

  actions: {
    async fetchBookings() {
      this.loading = true
      this.error = null

      try {
        const response = await bookingService.getAllBookings()
        this.bookings = response.data
      } catch (error) {
        this.error = error.response?.data?.error || 'Failed to fetch bookings'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchBookingById(id) {
      this.loading = true
      this.error = null

      try {
        const response = await bookingService.getBookingById(id)
        this.currentBooking = response.data
        return response.data
      } catch (error) {
        this.error = error.response?.data?.error || 'Failed to fetch booking'
        throw error
      } finally {
        this.loading = false
      }
    },

    async createBooking(bookingData) {
      this.loading = true
      this.error = null

      try {
        const response = await bookingService.createBooking(bookingData)
        this.bookings.push(response.data)
        return response.data
      } catch (error) {
        this.error = error.response?.data?.error || 'Failed to create booking'
        throw error
      } finally {
        this.loading = false
      }
    },

    async cancelBooking(id) {
      this.loading = true
      this.error = null

      try {
        await bookingService.cancelBooking(id)
        this.bookings = this.bookings.filter(b => b.id !== id)
        if (this.currentBooking?.id === id) {
          this.currentBooking = null
        }
      } catch (error) {
        this.error = error.response?.data?.error || 'Failed to cancel booking'
        throw error
      } finally {
        this.loading = false
      }
    },

    clearError() {
      this.error = null
    }
  }
})
