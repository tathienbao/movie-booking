<template>
  <div class="movies-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2><i class="bi bi-collection-play"></i> Movies</h2>
      <button
        v-if="authStore.isAdmin"
        class="btn btn-success"
        data-bs-toggle="modal"
        data-bs-target="#addMovieModal"
      >
        <i class="bi bi-plus-circle"></i> Add Movie
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="movieStore.loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2">Loading movies...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="movieStore.error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle"></i> {{ movieStore.error }}
    </div>

    <!-- Movies Grid -->
    <div v-else class="row">
      <div
        v-for="movie in movieStore.movies"
        :key="movie.id"
        class="col-md-6 col-lg-4 mb-4"
      >
        <div class="card h-100 movie-card">
          <div class="card-body">
            <h5 class="card-title">{{ movie.title }}</h5>
            <p class="card-text text-muted">
              <i class="bi bi-tags"></i> {{ movie.genre }}
            </p>
            <p class="card-text">{{ movie.description }}</p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge bg-primary">
                <i class="bi bi-clock"></i> {{ movie.durationMinutes }} min
              </span>
              <span class="text-success fw-bold">
                ${{ movie.price.toFixed(2) }}
              </span>
            </div>
          </div>
          <div class="card-footer bg-transparent">
            <router-link
              :to="`/movies/${movie.id}`"
              class="btn btn-primary btn-sm w-100"
            >
              <i class="bi bi-info-circle"></i> View Details
            </router-link>
          </div>
        </div>
      </div>

      <div v-if="movieStore.movies.length === 0" class="col-12">
        <div class="alert alert-info text-center">
          <i class="bi bi-info-circle"></i> No movies available
        </div>
      </div>
    </div>

    <!-- Add Movie Modal (Admin only) -->
    <div v-if="authStore.isAdmin" class="modal fade" id="addMovieModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Add New Movie</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="handleAddMovie">
              <div class="mb-3">
                <label class="form-label">Title</label>
                <input v-model="newMovie.title" type="text" class="form-control" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Description</label>
                <textarea v-model="newMovie.description" class="form-control" rows="3" required></textarea>
              </div>
              <div class="mb-3">
                <label class="form-label">Genre</label>
                <input v-model="newMovie.genre" type="text" class="form-control" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Duration (minutes)</label>
                <input v-model.number="newMovie.durationMinutes" type="number" class="form-control" required min="1">
              </div>
              <div class="mb-3">
                <label class="form-label">Price ($)</label>
                <input v-model.number="newMovie.price" type="number" step="0.01" class="form-control" required min="0">
              </div>
              <button type="submit" class="btn btn-success w-100" :disabled="movieStore.loading">
                <span v-if="movieStore.loading">
                  <span class="spinner-border spinner-border-sm me-2"></span>
                  Creating...
                </span>
                <span v-else>Create Movie</span>
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { Modal } from 'bootstrap'
import { useMovieStore } from '../stores/movieStore'
import { useAuthStore } from '../stores/authStore'

const movieStore = useMovieStore()
const authStore = useAuthStore()

const newMovie = reactive({
  title: '',
  description: '',
  genre: '',
  durationMinutes: 0,
  price: 0
})

onMounted(() => {
  movieStore.fetchMovies()
})

const handleAddMovie = async () => {
  try {
    await movieStore.createMovie(newMovie)
    // Close modal
    const modal = document.getElementById('addMovieModal')
    const bootstrapModal = Modal.getInstance(modal)
    if (bootstrapModal) {
      bootstrapModal.hide()
    }
    // Reset form
    Object.assign(newMovie, {
      title: '',
      description: '',
      genre: '',
      durationMinutes: 0,
      price: 0
    })
  } catch (error) {
    console.error('Failed to create movie:', error)
  }
}
</script>

<style scoped>
.movie-card {
  transition: transform 0.3s, box-shadow 0.3s;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.movie-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.card-title {
  color: #333;
  font-weight: bold;
}
</style>
