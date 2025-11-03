<template>
  <div class="movie-detail-view">
    <!-- Loading State -->
    <div v-if="movieStore.loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="movieStore.error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle"></i> {{ movieStore.error }}
    </div>

    <!-- Movie Details -->
    <div v-else-if="movie" class="row">
      <div class="col-lg-8">
        <div class="card shadow-sm">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <h2 class="card-title">{{ movie.title }}</h2>
              <span class="badge bg-primary fs-6">
                <i class="bi bi-tags"></i> {{ movie.genre }}
              </span>
            </div>

            <p class="card-text lead">{{ movie.description }}</p>

            <hr>

            <div class="row">
              <div class="col-md-6 mb-3">
                <strong><i class="bi bi-clock"></i> Duration:</strong>
                {{ movie.durationMinutes }} minutes
              </div>
              <div class="col-md-6 mb-3">
                <strong><i class="bi bi-currency-dollar"></i> Price:</strong>
                <span class="text-success fs-5">${{ movie.price.toFixed(2) }}</span>
              </div>
            </div>

            <!-- Admin Controls -->
            <div v-if="authStore.isAdmin" class="mt-4">
              <hr>
              <h5 class="mb-3">Admin Controls</h5>
              <div class="d-flex gap-2">
                <button class="btn btn-warning" data-bs-toggle="modal" data-bs-target="#editMovieModal">
                  <i class="bi bi-pencil"></i> Edit Movie
                </button>
                <button class="btn btn-danger" @click="handleDelete">
                  <i class="bi bi-trash"></i> Delete Movie
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-lg-4">
        <div class="card shadow-sm">
          <div class="card-body">
            <h5 class="card-title">Book This Movie</h5>

            <div v-if="!authStore.isAuthenticated" class="alert alert-info">
              <i class="bi bi-info-circle"></i> Please <router-link to="/login">login</router-link> to book tickets
            </div>

            <form v-else @submit.prevent="handleBooking">
              <div class="mb-3">
                <label class="form-label">Number of Seats</label>
                <input
                  v-model.number="numberOfSeats"
                  type="number"
                  class="form-control"
                  min="1"
                  max="10"
                  required
                >
              </div>

              <div class="mb-3">
                <strong>Total Price:</strong>
                <span class="text-success fs-4 ms-2">
                  ${{ (movie.price * numberOfSeats).toFixed(2) }}
                </span>
              </div>

              <button type="submit" class="btn btn-success w-100" :disabled="bookingStore.loading">
                <span v-if="bookingStore.loading">
                  <span class="spinner-border spinner-border-sm me-2"></span>
                  Booking...
                </span>
                <span v-else>
                  <i class="bi bi-ticket-perforated"></i> Book Now
                </span>
              </button>

              <div v-if="bookingSuccess" class="alert alert-success mt-3">
                <i class="bi bi-check-circle"></i> Booking successful!
                <router-link to="/bookings" class="alert-link">View bookings</router-link>
              </div>

              <div v-if="bookingStore.error" class="alert alert-danger mt-3">
                {{ bookingStore.error }}
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Movie Modal -->
    <div v-if="authStore.isAdmin && movie" class="modal fade" id="editMovieModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Edit Movie</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="handleUpdate">
              <div class="mb-3">
                <label class="form-label">Title</label>
                <input v-model="editForm.title" type="text" class="form-control" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Description</label>
                <textarea v-model="editForm.description" class="form-control" rows="3" required></textarea>
              </div>
              <div class="mb-3">
                <label class="form-label">Genre</label>
                <input v-model="editForm.genre" type="text" class="form-control" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Duration (minutes)</label>
                <input v-model.number="editForm.durationMinutes" type="number" class="form-control" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Price ($)</label>
                <input v-model.number="editForm.price" type="number" step="0.01" class="form-control" required>
              </div>
              <button type="submit" class="btn btn-warning w-100">
                <i class="bi bi-pencil"></i> Update Movie
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMovieStore } from '../stores/movieStore'
import { useBookingStore } from '../stores/bookingStore'
import { useAuthStore } from '../stores/authStore'

const route = useRoute()
const router = useRouter()
const movieStore = useMovieStore()
const bookingStore = useBookingStore()
const authStore = useAuthStore()

const numberOfSeats = ref(1)
const bookingSuccess = ref(false)

const movie = computed(() => movieStore.currentMovie)

const editForm = reactive({
  title: '',
  description: '',
  genre: '',
  durationMinutes: 0,
  price: 0
})

onMounted(async () => {
  const movieId = parseInt(route.params.id)
  await movieStore.fetchMovieById(movieId)

  if (movie.value) {
    Object.assign(editForm, {
      title: movie.value.title,
      description: movie.value.description,
      genre: movie.value.genre,
      durationMinutes: movie.value.durationMinutes,
      price: movie.value.price
    })
  }
})

const handleBooking = async () => {
  bookingSuccess.value = false
  bookingStore.clearError()

  try {
    await bookingStore.createBooking({
      movieId: movie.value.id,
      numberOfSeats: numberOfSeats.value
    })
    bookingSuccess.value = true
    numberOfSeats.value = 1
  } catch (error) {
    console.error('Booking failed:', error)
  }
}

const handleUpdate = async () => {
  try {
    await movieStore.updateMovie(movie.value.id, editForm)
    const modal = document.getElementById('editMovieModal')
    const bootstrapModal = bootstrap.Modal.getInstance(modal)
    bootstrapModal.hide()
  } catch (error) {
    console.error('Update failed:', error)
  }
}

const handleDelete = async () => {
  if (confirm(`Are you sure you want to delete "${movie.value.title}"?`)) {
    try {
      await movieStore.deleteMovie(movie.value.id)
      router.push('/movies')
    } catch (error) {
      console.error('Delete failed:', error)
    }
  }
}
</script>

<style scoped>
.card {
  border: none;
}
</style>
