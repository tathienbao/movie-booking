<template>
  <div class="bookings-view">
    <h2 class="mb-4">
      <i class="bi bi-ticket-perforated"></i> My Bookings
    </h2>

    <!-- Loading State -->
    <div v-if="bookingStore.loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2">Loading bookings...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="bookingStore.error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle"></i> {{ bookingStore.error }}
    </div>

    <!-- Bookings List -->
    <div v-else>
      <div v-if="bookingStore.bookings.length === 0" class="alert alert-info text-center">
        <i class="bi bi-info-circle"></i> You don't have any bookings yet.
        <router-link to="/movies" class="alert-link">Browse movies</router-link>
      </div>

      <div v-else>
        <div class="mb-3">
          <strong>Total Bookings:</strong> {{ bookingStore.totalBookings }}
          <span class="ms-3">
            <strong>Total Amount:</strong>
            <span class="text-success">${{ bookingStore.totalAmount.toFixed(2) }}</span>
          </span>
        </div>

        <div class="row">
          <div
            v-for="booking in bookingStore.bookings"
            :key="booking.id"
            class="col-md-6 col-lg-4 mb-3"
          >
            <div class="card booking-card">
              <div class="card-body">
                <h5 class="card-title">
                  {{ booking.movieTitle || `Movie #${booking.movieId}` }}
                </h5>

                <div class="mb-2">
                  <i class="bi bi-calendar"></i>
                  <small class="text-muted">
                    {{ formatDate(booking.createdAt) }}
                  </small>
                </div>

                <hr>

                <div class="d-flex justify-content-between mb-2">
                  <span><i class="bi bi-people"></i> Seats:</span>
                  <strong>{{ booking.numberOfSeats }}</strong>
                </div>

                <div class="d-flex justify-content-between mb-3">
                  <span><i class="bi bi-currency-dollar"></i> Total:</span>
                  <strong class="text-success">${{ booking.totalPrice.toFixed(2) }}</strong>
                </div>

                <button
                  @click="handleCancel(booking.id)"
                  class="btn btn-sm btn-outline-danger w-100"
                  :disabled="bookingStore.loading"
                >
                  <i class="bi bi-x-circle"></i> Cancel Booking
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useBookingStore } from '../stores/bookingStore'

const bookingStore = useBookingStore()

onMounted(() => {
  bookingStore.fetchBookings()
})

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleCancel = async (bookingId) => {
  if (confirm('Are you sure you want to cancel this booking?')) {
    try {
      await bookingStore.cancelBooking(bookingId)
    } catch (error) {
      console.error('Cancel failed:', error)
    }
  }
}
</script>

<style scoped>
.booking-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.booking-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}
</style>
