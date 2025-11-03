<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
      <router-link to="/" class="navbar-brand">
        <i class="bi bi-film"></i> Movie Booking
      </router-link>

      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarNav"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto">
          <li class="nav-item">
            <router-link to="/movies" class="nav-link">
              <i class="bi bi-collection-play"></i> Movies
            </router-link>
          </li>
          <li v-if="authStore.isAuthenticated" class="nav-item">
            <router-link to="/bookings" class="nav-link">
              <i class="bi bi-ticket-perforated"></i> My Bookings
            </router-link>
          </li>
        </ul>

        <ul class="navbar-nav">
          <template v-if="authStore.isAuthenticated">
            <li class="nav-item dropdown">
              <a
                class="nav-link dropdown-toggle"
                href="#"
                id="navbarDropdown"
                role="button"
                data-bs-toggle="dropdown"
              >
                <i class="bi bi-person-circle"></i> {{ authStore.userName }}
                <span v-if="authStore.isAdmin" class="badge bg-warning text-dark ms-1">ADMIN</span>
              </a>
              <ul class="dropdown-menu dropdown-menu-end">
                <li>
                  <span class="dropdown-item-text">
                    <small class="text-muted">{{ authStore.userEmail }}</small>
                  </span>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                  <button @click="handleLogout" class="dropdown-item">
                    <i class="bi bi-box-arrow-right"></i> Logout
                  </button>
                </li>
              </ul>
            </li>
          </template>

          <template v-else>
            <li class="nav-item">
              <router-link to="/login" class="nav-link">
                <i class="bi bi-box-arrow-in-right"></i> Login
              </router-link>
            </li>
            <li class="nav-item">
              <router-link to="/register" class="nav-link">
                <i class="bi bi-person-plus"></i> Register
              </router-link>
            </li>
          </template>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '../stores/authStore'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar-brand {
  font-weight: bold;
  font-size: 1.5rem;
}

.nav-link {
  transition: color 0.3s;
}

.nav-link:hover {
  color: #ffc107 !important;
}

.router-link-active {
  color: #ffc107 !important;
}
</style>
