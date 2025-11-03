<template>
  <div class="register-view">
    <div class="row justify-content-center">
      <div class="col-md-6 col-lg-5">
        <div class="card shadow">
          <div class="card-body p-4">
            <h2 class="text-center mb-4">
              <i class="bi bi-person-plus"></i> Register
            </h2>

            <div v-if="authStore.error" class="alert alert-danger" role="alert">
              <i class="bi bi-exclamation-triangle"></i> {{ authStore.error }}
            </div>

            <form @submit.prevent="handleRegister">
              <div class="mb-3">
                <label for="name" class="form-label">Full Name</label>
                <input
                  type="text"
                  class="form-control"
                  id="name"
                  v-model="formData.name"
                  required
                  placeholder="John Doe"
                >
              </div>

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
                  placeholder="Min 8 chars, letter + number"
                  minlength="8"
                >
                <small class="form-text text-muted">
                  Must be at least 8 characters with letters and numbers
                </small>
              </div>

              <div class="mb-3">
                <label for="confirmPassword" class="form-label">Confirm Password</label>
                <input
                  type="password"
                  class="form-control"
                  id="confirmPassword"
                  v-model="formData.confirmPassword"
                  required
                  placeholder="Re-enter password"
                  :class="{ 'is-invalid': formData.confirmPassword && formData.password !== formData.confirmPassword }"
                >
                <div v-if="formData.confirmPassword && formData.password !== formData.confirmPassword" class="invalid-feedback">
                  Passwords do not match
                </div>
              </div>

              <button
                type="submit"
                class="btn btn-primary w-100"
                :disabled="authStore.loading || formData.password !== formData.confirmPassword"
              >
                <span v-if="authStore.loading">
                  <span class="spinner-border spinner-border-sm me-2"></span>
                  Creating account...
                </span>
                <span v-else>
                  <i class="bi bi-person-plus"></i> Create Account
                </span>
              </button>
            </form>

            <hr class="my-4">

            <p class="text-center text-muted mb-0">
              Already have an account?
              <router-link to="/login">Login here</router-link>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useAuthStore } from '../stores/authStore'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const formData = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const handleRegister = async () => {
  authStore.clearError()

  if (formData.password !== formData.confirmPassword) {
    return
  }

  try {
    await authStore.register({
      name: formData.name,
      email: formData.email,
      password: formData.password
    })
    router.push('/')
  } catch (error) {
    // Error handled by store
    console.error('Registration failed:', error)
  }
}
</script>

<style scoped>
.register-view {
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
