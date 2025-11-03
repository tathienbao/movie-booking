# Understanding Vue.js 3 Frontend Architecture

This guide explains how the Vue.js 3 frontend works in the Movie Booking application, covering modern frontend architecture patterns and best practices.

## Table of Contents

1. [Vue.js 3 Overview](#vuejs-3-overview)
2. [Project Architecture](#project-architecture)
3. [API Layer with Axios](#api-layer-with-axios)
4. [State Management with Pinia](#state-management-with-pinia)
5. [Routing with Vue Router](#routing-with-vue-router)
6. [Components and Views](#components-and-views)
7. [Authentication Flow](#authentication-flow)
8. [Code Walkthrough](#code-walkthrough)
9. [Best Practices](#best-practices)

---

## Vue.js 3 Overview

### What is Vue.js?

Vue.js is a progressive JavaScript framework for building user interfaces. Version 3 introduces:

- **Composition API**: More flexible and reusable code organization
- **Better TypeScript support**: Improved type inference
- **Smaller bundle size**: Better performance
- **Faster rendering**: Optimized virtual DOM

### Why Vue.js 3?

✅ **Easy to learn**: Simple syntax, gradual learning curve
✅ **Reactive**: Automatically updates UI when data changes
✅ **Component-based**: Reusable, maintainable code
✅ **Rich ecosystem**: Vue Router, Pinia, Vite
✅ **Great documentation**: Comprehensive official guides

---

## Project Architecture

### Folder Structure

```
frontend/
├── src/
│   ├── api/              # HTTP client and API services
│   │   ├── axios.js      # Axios configuration
│   │   ├── authService.js    # Authentication API calls
│   │   ├── movieService.js   # Movie API calls
│   │   └── bookingService.js # Booking API calls
│   │
│   ├── stores/           # Pinia state management
│   │   ├── authStore.js      # Authentication state
│   │   ├── movieStore.js     # Movies state
│   │   └── bookingStore.js   # Bookings state
│   │
│   ├── router/           # Vue Router configuration
│   │   └── index.js          # Routes and navigation guards
│   │
│   ├── views/            # Page components (routes)
│   │   ├── HomeView.vue
│   │   ├── LoginView.vue
│   │   ├── RegisterView.vue
│   │   ├── MoviesView.vue
│   │   ├── MovieDetailView.vue
│   │   └── BookingsView.vue
│   │
│   ├── components/       # Reusable components
│   │   └── Navbar.vue
│   │
│   ├── App.vue          # Root component
│   ├── main.js          # Application entry point
│   └── style.css        # Global styles
│
├── public/              # Static assets
├── index.html          # HTML template
├── package.json        # Dependencies
└── vite.config.js      # Vite configuration
```

### Architecture Layers

```
┌─────────────────────────────────────┐
│         Vue Components              │  ← User Interface
│    (Views, Navbar, Forms)           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Pinia Stores                │  ← State Management
│   (authStore, movieStore, etc.)     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         API Services                │  ← Business Logic
│   (authService, movieService, etc.) │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Axios HTTP Client           │  ← Network Layer
│    (Interceptors, Base Config)      │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Backend REST API            │  ← Server
│      (http://localhost:8080)        │
└─────────────────────────────────────┘
```

---

## API Layer with Axios

### What is Axios?

Axios is a promise-based HTTP client for making API requests. It provides:

- Automatic JSON transformation
- Request/response interceptors
- Error handling
- Timeout configuration

### Base Configuration (`api/axios.js`)

```javascript
import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',  // Backend URL
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000  // 10 seconds
})
```

**Why this approach?**
- Single place to configure base URL
- Consistent headers across all requests
- Easy to change backend URL for production

### Request Interceptor (Adding JWT Token)

```javascript
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  }
)
```

**What happens:**
1. Before each API request
2. Check if JWT token exists in localStorage
3. If yes, add `Authorization: Bearer <token>` header
4. Backend verifies token and identifies user

### Response Interceptor (Error Handling)

```javascript
apiClient.interceptors.response.use(
  (response) => response,  // Success - return as is
  (error) => {
    if (error.response?.status === 401) {
      // Unauthorized - token expired or invalid
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
```

**Why this is useful:**
- Automatic logout on 401 Unauthorized
- Centralized error handling
- No need to handle auth errors in every component

### Service Pattern

Each API domain has its own service module:

**Example: `movieService.js`**

```javascript
import apiClient from './axios'

export default {
  getAllMovies() {
    return apiClient.get('/movies')
  },

  getMovieById(id) {
    return apiClient.get(`/movies/${id}`)
  },

  createMovie(movieData) {
    return apiClient.post('/movies', movieData)
  }
}
```

**Benefits:**
- Clean separation of concerns
- Easy to test
- Reusable across components
- Type safety (with TypeScript)

---

## State Management with Pinia

### What is Pinia?

Pinia is the official state management library for Vue 3. It replaces Vuex with a simpler, more intuitive API.

**Why use state management?**
- Share data between components without prop drilling
- Centralized application state
- Easier debugging with DevTools
- Predictable state changes

### Store Structure

A Pinia store has three parts:

1. **State**: The data
2. **Getters**: Computed values derived from state
3. **Actions**: Functions that modify state

### Example: Auth Store (`stores/authStore.js`)

```javascript
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  // 1. STATE - The data
  state: () => ({
    user: null,
    token: localStorage.getItem('token'),
    isAuthenticated: false,
    loading: false,
    error: null
  }),

  // 2. GETTERS - Computed values
  getters: {
    isAdmin: (state) => state.user?.role === 'ADMIN',
    userName: (state) => state.user?.name || ''
  },

  // 3. ACTIONS - Functions to modify state
  actions: {
    async login(credentials) {
      this.loading = true
      try {
        const response = await authService.login(credentials)
        this.token = response.data.token
        this.user = response.data
        this.isAuthenticated = true
        localStorage.setItem('token', this.token)
      } catch (error) {
        this.error = error.message
      } finally {
        this.loading = false
      }
    }
  }
})
```

### Using a Store in Components

```vue
<script setup>
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

// Access state
console.log(authStore.isAuthenticated)

// Access getters
console.log(authStore.isAdmin)

// Call actions
authStore.login({ email: '...', password: '...' })
</script>

<template>
  <div v-if="authStore.isAuthenticated">
    Welcome, {{ authStore.userName }}!
  </div>
</template>
```

### Why Pinia vs Props?

**❌ Without Pinia (Props Hell):**
```
App → Navbar → UserMenu → ProfileButton (needs user)
                           ↑
                     Pass props through 3 levels!
```

**✅ With Pinia:**
```
ProfileButton → useAuthStore() → Get user directly!
```

---

## Routing with Vue Router

### What is Vue Router?

Vue Router is the official routing library for Vue.js, enabling Single Page Application (SPA) navigation.

**SPA vs Traditional:**

Traditional Web App:
```
Click link → Browser requests new HTML → Full page reload
```

SPA:
```
Click link → Vue Router changes view → No page reload (faster!)
```

### Route Configuration (`router/index.js`)

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: false }  // Public route
  },
  {
    path: '/bookings',
    name: 'bookings',
    component: BookingsView,
    meta: { requiresAuth: true }   // Protected route
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})
```

### Navigation Guards (Authentication)

```javascript
router.beforeEach((to, from, next) => {
  const isAuthenticated = authService.isAuthenticated()

  if (to.meta.requiresAuth && !isAuthenticated) {
    // Redirect to login if trying to access protected route
    next({ name: 'login', query: { redirect: to.fullPath } })
  } else {
    next()  // Allow navigation
  }
})
```

**What this does:**
1. Runs before every navigation
2. Checks if route requires authentication
3. Redirects to login if not authenticated
4. Saves original destination in query parameter

### Programmatic Navigation

In components:

```vue
<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goToMovies() {
  router.push('/movies')  // Navigate programmatically
}

function goBack() {
  router.back()  // Go to previous page
}
</script>
```

### Route Parameters

```javascript
// Route definition
{ path: '/movies/:id', component: MovieDetailView }

// Navigate
router.push(`/movies/5`)

// Access in component
const route = useRoute()
const movieId = route.params.id  // "5"
```

---

## Components and Views

### Vue Component Structure

A Vue Single File Component (SFC) has three sections:

```vue
<template>
  <!-- HTML structure -->
  <div>{{ message }}</div>
</template>

<script setup>
  // JavaScript logic
  import { ref } from 'vue'
  const message = ref('Hello Vue!')
</script>

<style scoped>
  /* CSS styles (scoped to this component) */
  div { color: blue; }
</style>
```

### Composition API with `<script setup>`

**Old Options API:**
```vue
<script>
export default {
  data() {
    return { count: 0 }
  },
  methods: {
    increment() { this.count++ }
  }
}
</script>
```

**New Composition API:**
```vue
<script setup>
import { ref } from 'vue'

const count = ref(0)
const increment = () => { count.value++ }
</script>
```

**Benefits:**
- Less boilerplate
- Better TypeScript support
- Easier to organize complex logic
- Better code reuse

### Reactivity with `ref` and `reactive`

```javascript
import { ref, reactive } from 'vue'

// ref - for primitive values
const count = ref(0)
count.value++  // Must use .value

// reactive - for objects
const user = reactive({
  name: 'John',
  email: 'john@example.com'
})
user.name = 'Jane'  // No .value needed
```

### Lifecycle Hooks

```vue
<script setup>
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  console.log('Component mounted!')
  // Fetch data, set up listeners, etc.
})

onUnmounted(() => {
  console.log('Component destroyed!')
  // Cleanup
})
</script>
```

### Props and Emits

**Parent component:**
```vue
<MovieCard :movie="movieData" @book="handleBooking" />
```

**Child component:**
```vue
<script setup>
const props = defineProps({
  movie: Object
})

const emit = defineEmits(['book'])

function bookMovie() {
  emit('book', props.movie.id)
}
</script>
```

---

## Authentication Flow

### Complete Login Flow

```
1. User enters credentials
   ↓
2. LoginView.vue calls authStore.login()
   ↓
3. authStore.login() calls authService.login()
   ↓
4. authService sends POST /api/auth/login
   ↓
5. Backend validates credentials
   ↓
6. Backend returns { token, email, name, role }
   ↓
7. authStore saves token to localStorage
   ↓
8. authStore updates state (user, isAuthenticated)
   ↓
9. Router redirects to home/previous page
   ↓
10. Navbar updates (shows user name, logout button)
```

### Token Persistence

```javascript
// On login - save token
localStorage.setItem('token', response.data.token)
localStorage.setItem('user', JSON.stringify(userData))

// On app load - restore session
const authStore = useAuthStore()
authStore.user = JSON.parse(localStorage.getItem('user'))
authStore.token = localStorage.getItem('token')

// On logout - clear
localStorage.removeItem('token')
localStorage.removeItem('user')
```

### Protected API Calls

```javascript
// Axios automatically adds token to every request
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

---

## Code Walkthrough

### Example: Creating a Booking

**1. User clicks "Book Now" button**

```vue
<!-- MovieDetailView.vue -->
<template>
  <form @submit.prevent="handleBooking">
    <input v-model.number="numberOfSeats" type="number" />
    <button type="submit">Book Now</button>
  </form>
</template>
```

**2. Component calls bookingStore action**

```vue
<script setup>
import { ref } from 'vue'
import { useBookingStore } from '@/stores/bookingStore'

const bookingStore = useBookingStore()
const numberOfSeats = ref(1)

async function handleBooking() {
  await bookingStore.createBooking({
    movieId: movie.value.id,
    numberOfSeats: numberOfSeats.value
  })
}
</script>
```

**3. Store action calls API service**

```javascript
// stores/bookingStore.js
async createBooking(bookingData) {
  this.loading = true
  try {
    const response = await bookingService.createBooking(bookingData)
    this.bookings.push(response.data)  // Add to state
    return response.data
  } catch (error) {
    this.error = error.message
  } finally {
    this.loading = false
  }
}
```

**4. Service makes HTTP request**

```javascript
// api/bookingService.js
createBooking(bookingData) {
  return apiClient.post('/bookings', bookingData)
  // Automatically includes JWT token via interceptor
}
```

**5. Backend processes request**
- Validates JWT token
- Extracts user ID from token
- Creates booking in database
- Returns booking data

**6. Response flows back**
```
Backend → apiClient → bookingService → bookingStore → Component → UI updates
```

---

## Best Practices

### 1. Component Organization

**✅ Good:**
```
views/        # Page-level components (routes)
components/   # Reusable components
```

**❌ Avoid:**
```
components/   # Everything mixed together
```

### 2. State Management

**✅ Use Pinia for:**
- User authentication state
- Global data (movies, bookings)
- Data shared between multiple components

**✅ Use local state for:**
- Form inputs
- UI state (modal open/closed)
- Component-specific data

### 3. API Calls

**✅ Good:**
```javascript
// In store action
async fetchMovies() {
  const response = await movieService.getAllMovies()
  this.movies = response.data
}
```

**❌ Avoid:**
```javascript
// Directly in component
const response = await axios.get('http://localhost:8080/api/movies')
```

### 4. Error Handling

**✅ Good:**
```javascript
try {
  await authStore.login(credentials)
  router.push('/')
} catch (error) {
  // Error already handled by store
  // Show user-friendly message
}
```

### 5. Loading States

**✅ Always show loading indicators:**
```vue
<template>
  <div v-if="loading">Loading...</div>
  <div v-else-if="error">Error: {{ error }}</div>
  <div v-else>{{ data }}</div>
</template>
```

### 6. Form Validation

**✅ Use HTML5 validation + custom logic:**
```vue
<input
  type="email"
  required
  v-model="email"
  :class="{ 'is-invalid': emailError }"
/>
```

---

## Key Takeaways

1. **Layered Architecture**: UI → Stores → Services → API → Backend
2. **Composition API**: Modern, flexible Vue 3 patterns
3. **State Management**: Pinia for global state, local state for components
4. **API Layer**: Axios with interceptors for clean HTTP handling
5. **Routing**: Vue Router with navigation guards for protection
6. **Reactivity**: Vue's reactive system automatically updates UI
7. **Best Practices**: Separation of concerns, error handling, loading states

---

## Further Learning

- [Vue 3 Official Docs](https://vuejs.org/)
- [Pinia Documentation](https://pinia.vuejs.org/)
- [Vue Router Guide](https://router.vuejs.org/)
- [Axios Documentation](https://axios-http.com/)
- [Vite Guide](https://vitejs.dev/)

---

**Next Steps:**
1. Experiment with the code
2. Add new features
3. Improve UI/UX
4. Deploy to production

Happy coding! 🚀
