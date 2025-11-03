import apiClient from './axios'

export default {
  /**
   * Get all movies (public endpoint)
   * @returns {Promise} Array of movies
   */
  getAllMovies() {
    return apiClient.get('/movies')
  },

  /**
   * Get movie by ID (public endpoint)
   * @param {number} id - Movie ID
   * @returns {Promise} Movie object
   */
  getMovieById(id) {
    return apiClient.get(`/movies/${id}`)
  },

  /**
   * Create new movie (ADMIN only)
   * @param {Object} movieData - { title, description, genre, durationMinutes, price }
   * @returns {Promise} Created movie
   */
  createMovie(movieData) {
    return apiClient.post('/movies', movieData)
  },

  /**
   * Update existing movie (ADMIN only)
   * @param {number} id - Movie ID
   * @param {Object} movieData - Updated movie data
   * @returns {Promise} Updated movie
   */
  updateMovie(id, movieData) {
    return apiClient.put(`/movies/${id}`, movieData)
  },

  /**
   * Delete movie (ADMIN only)
   * @param {number} id - Movie ID
   * @returns {Promise}
   */
  deleteMovie(id) {
    return apiClient.delete(`/movies/${id}`)
  }
}
