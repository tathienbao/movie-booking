import apiClient from './axios'

export default {
  /**
   * Get all bookings for current user (requires auth)
   * @returns {Promise} Array of bookings
   */
  getAllBookings() {
    return apiClient.get('/bookings')
  },

  /**
   * Get booking by ID (requires auth)
   * @param {number} id - Booking ID
   * @returns {Promise} Booking object
   */
  getBookingById(id) {
    return apiClient.get(`/bookings/${id}`)
  },

  /**
   * Get all bookings for a specific movie (requires auth)
   * @param {number} movieId - Movie ID
   * @returns {Promise} Array of bookings
   */
  getBookingsByMovie(movieId) {
    return apiClient.get(`/bookings/movies/${movieId}`)
  },

  /**
   * Create new booking (requires auth)
   * @param {Object} bookingData - { movieId, numberOfSeats }
   * @returns {Promise} Created booking
   */
  createBooking(bookingData) {
    return apiClient.post('/bookings', bookingData)
  },

  /**
   * Cancel booking (requires auth)
   * @param {number} id - Booking ID
   * @returns {Promise}
   */
  cancelBooking(id) {
    return apiClient.delete(`/bookings/${id}`)
  }
}
