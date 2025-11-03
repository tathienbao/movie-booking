import { createRouter, createWebHistory } from 'vue-router'
import authService from '../api/authService'

// Lazy-load views for better performance
const HomeView = () => import('../views/HomeView.vue')
const LoginView = () => import('../views/LoginView.vue')
const RegisterView = () => import('../views/RegisterView.vue')
const MoviesView = () => import('../views/MoviesView.vue')
const MovieDetailView = () => import('../views/MovieDetailView.vue')
const BookingsView = () => import('../views/BookingsView.vue')

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { requiresAuth: false }
  },
  {
    path: '/movies',
    name: 'movies',
    component: MoviesView,
    meta: { requiresAuth: false }
  },
  {
    path: '/movies/:id',
    name: 'movie-detail',
    component: MovieDetailView,
    meta: { requiresAuth: false }
  },
  {
    path: '/bookings',
    name: 'bookings',
    component: BookingsView,
    meta: { requiresAuth: true } // Protected route
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard for protected routes
router.beforeEach((to, from, next) => {
  const isAuthenticated = authService.isAuthenticated()

  if (to.meta.requiresAuth && !isAuthenticated) {
    // Redirect to login if route requires auth and user is not authenticated
    next({ name: 'login', query: { redirect: to.fullPath } })
  } else if ((to.name === 'login' || to.name === 'register') && isAuthenticated) {
    // Redirect to home if trying to access login/register while authenticated
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router
