<template>
  <div class="home">
    <div class="jumbotron bg-white p-5 rounded shadow-sm">
      <h1 class="display-4">
        <i class="bi bi-film text-primary"></i> Welcome to Movie Booking
      </h1>
      <p class="lead">Your one-stop solution for booking movie tickets online</p>
      <hr class="my-4">
      <p>Browse our collection of movies and book your seats instantly!</p>
      <div class="d-flex gap-2">
        <router-link to="/movies" class="btn btn-primary btn-lg">
          <i class="bi bi-collection-play"></i> Browse Movies
        </router-link>
        <router-link v-if="!authStore.isAuthenticated" to="/register" class="btn btn-outline-secondary btn-lg">
          <i class="bi bi-person-plus"></i> Sign Up
        </router-link>
      </div>
    </div>

    <!-- Features Section -->
    <div class="row mt-5">
      <div class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body text-center">
            <i class="bi bi-search fs-1 text-primary"></i>
            <h5 class="card-title mt-3">Browse Movies</h5>
            <p class="card-text">Explore our wide collection of latest and classic movies</p>
          </div>
        </div>
      </div>

      <div class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body text-center">
            <i class="bi bi-ticket-perforated fs-1 text-success"></i>
            <h5 class="card-title mt-3">Easy Booking</h5>
            <p class="card-text">Book your tickets in just a few clicks</p>
          </div>
        </div>
      </div>

      <div class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body text-center">
            <i class="bi bi-shield-check fs-1 text-warning"></i>
            <h5 class="card-title mt-3">Secure Payment</h5>
            <p class="card-text">Safe and secure booking process</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()
</script>

<style scoped>
.jumbotron {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.jumbotron h1 {
  font-weight: bold;
}

.card {
  transition: transform 0.3s, box-shadow 0.3s;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}
</style>
