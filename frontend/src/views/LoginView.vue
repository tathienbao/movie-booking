<template>
  <div class="login-view">
    <div class="row justify-content-center">
      <div class="col-md-6 col-lg-4">
        <div class="card shadow">
          <div class="card-body p-4">
            <h2 class="text-center mb-4">
              <i class="bi bi-box-arrow-in-right"></i> Login
            </h2>

            <div v-if="authStore.error" class="alert alert-danger" role="alert">
              <i class="bi bi-exclamation-triangle"></i> {{ authStore.error }}
            </div>

            <form @submit.prevent="handleLogin">
              <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input
                  type="email"
                  class="form-control"
                  id="email"
                  v-model="formData.email"
                  required
                  placeholder="your@email.com"
                >
              </div>

              <div class="mb-3">
                <label for="password" class="form-label">Password</label>
                <input
                  type="password"
                  class="form-control"
                  id="password"
                  v-model="formData.password"
                  required
                  placeholder="Enter password"
                  minlength="8"
                >
              </div>

              <button
                type="submit"
                class="btn btn-primary w-100"
                :disabled="authStore.loading"
              >
                <span v-if="authStore.loading">
                  <span class="spinner-border spinner-border-sm me-2"></span>
                  Logging in...
                </span>
                <span v-else>
                  <i class="bi bi-box-arrow-in-right"></i> Login
                </span>
              </button>
            </form>

            <hr class="my-4">

            <p class="text-center text-muted mb-0">
              Don't have an account?
              <router-link to="/register">Register here</router-link>
            </p>

            <div class="mt-3 p-3 bg-light rounded">
              <small class="text-muted">
                <strong>Demo accounts:</strong><br>
                Admin: admin@example.com / admin123<br>
                Customer: customer@example.com / password123
              </small>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useAuthStore } from '../stores/authStore'
import { useRouter, useRoute } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const formData = reactive({
  email: '',
  password: ''
})

const handleLogin = async () => {
  authStore.clearError()

  try {
    await authStore.login(formData)
    // Redirect to previous page or home
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    // Error handled by store
    console.error('Login failed:', error)
  }
}
</script>

<style scoped>
.login-view {
  padding-top: 2rem;
}

.card {
  border: none;
  border-radius: 15px;
}

.btn-primary {
  border-radius: 25px;
  padding: 10px;
}
</style>
