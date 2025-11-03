import { defineStore } from 'pinia'
import movieService from '../api/movieService'

export const useMovieStore = defineStore('movie', {
  state: () => ({
    movies: [],
    currentMovie: null,
    loading: false,
    error: null
  }),

  getters: {
    getMovieById: (state) => (id) => {
      return state.movies.find(movie => movie.id === id)
    },

    moviesByGenre: (state) => (genre) => {
      return state.movies.filter(movie => movie.genre === genre)
    },

    genres: (state) => {
      return [...new Set(state.movies.map(movie => movie.genre))]
    }
  },

  actions: {
    async fetchMovies() {
      this.loading = true
      this.error = null

      try {
        const response = await movieService.getAllMovies()
        this.movies = response.data
      } catch (error) {
        this.error = error.response?.data?.error || 'Failed to fetch movies'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchMovieById(id) {
      this.loading = true
      this.error = null

      try {
        const response = await movieService.getMovieById(id)
        this.currentMovie = response.data
        return response.data
      } catch (error) {
        this.error = error.response?.data?.error || 'Failed to fetch movie'
        throw error
      } finally {
        this.loading = false
      }
    },

    async createMovie(movieData) {
      this.loading = true
      this.error = null

      try {
        const response = await movieService.createMovie(movieData)
        this.movies.push(response.data)
        return response.data
      } catch (error) {
        this.error = error.response?.data?.error || 'Failed to create movie'
        throw error
      } finally {
        this.loading = false
      }
    },

    async updateMovie(id, movieData) {
      this.loading = true
      this.error = null

      try {
        const response = await movieService.updateMovie(id, movieData)
        const index = this.movies.findIndex(m => m.id === id)
        if (index !== -1) {
          this.movies[index] = response.data
        }
        if (this.currentMovie?.id === id) {
          this.currentMovie = response.data
        }
        return response.data
      } catch (error) {
        this.error = error.response?.data?.error || 'Failed to update movie'
        throw error
      } finally {
        this.loading = false
      }
    },

    async deleteMovie(id) {
      this.loading = true
      this.error = null

      try {
        await movieService.deleteMovie(id)
        this.movies = this.movies.filter(m => m.id !== id)
        if (this.currentMovie?.id === id) {
          this.currentMovie = null
        }
      } catch (error) {
        this.error = error.response?.data?.error || 'Failed to delete movie'
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
