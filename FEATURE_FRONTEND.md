# Feature: Vue.js 3 Frontend

**Branch:** `feature/vuejs-frontend`

**Status:** 🚧 Planned

---

## Goal

Create a Vue.js 3 frontend application that consumes the REST API and provides a complete user interface for managing movies.

---

## What We'll Implement

### 1. Vue.js 3 Setup
- Create Vue 3 project with Vite
- Install required dependencies
- Configure development environment

### 2. Components
- **MovieList.vue** - Display all movies in a table/grid
- **MovieForm.vue** - Create/Edit movie form
- **MovieCard.vue** - Individual movie display
- **App.vue** - Main application shell

### 3. API Integration
- Create API service using Axios
- Connect to backend REST API
- Handle HTTP requests/responses
- Error handling

### 4. State Management
- Use Pinia for state management
- Manage movies list
- Handle loading states
- Error states

### 5. Routing
- Vue Router setup
- Routes: Home, Create, Edit, View
- Navigation between pages

### 6. Styling
- Responsive design
- Modern UI (Bootstrap/Tailwind)
- User-friendly interface

### 7. Docker & Deployment
- Dockerfile for frontend
- Nginx configuration
- Full-stack Docker Compose
- K8s manifests for frontend

---

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── MovieList.vue
│   │   ├── MovieForm.vue
│   │   └── MovieCard.vue
│   ├── views/
│   │   ├── Home.vue
│   │   ├── CreateMovie.vue
│   │   └── EditMovie.vue
│   ├── services/
│   │   └── movieApi.js
│   ├── stores/
│   │   └── movieStore.js
│   ├── router/
│   │   └── index.js
│   ├── App.vue
│   └── main.js
├── public/
│   └── index.html
├── Dockerfile
├── nginx.conf
├── package.json
└── vite.config.js
```

---

## Tech Stack

**Frontend:**
- Vue.js 3 (Composition API)
- Vite (build tool)
- Vue Router 4
- Pinia (state management)
- Axios (HTTP client)
- Bootstrap 5 or Tailwind CSS

**Deployment:**
- Nginx (production server)
- Docker
- Kubernetes

---

## Features

### User Can:
- ✅ View all movies in a list
- ✅ Search/filter movies
- ✅ View movie details
- ✅ Create new movie
- ✅ Edit existing movie
- ✅ Delete movie
- ✅ See loading states
- ✅ See error messages

---

## API Integration Example

```javascript
// src/services/movieApi.js
import axios from 'axios'

const API_URL = 'http://localhost:8080/api/movies'

export const movieApi = {
  getAllMovies() {
    return axios.get(API_URL)
  },

  getMovie(id) {
    return axios.get(`${API_URL}/${id}`)
  },

  createMovie(movie) {
    return axios.post(API_URL, movie)
  },

  updateMovie(id, movie) {
    return axios.put(`${API_URL}/${id}`, movie)
  },

  deleteMovie(id) {
    return axios.delete(`${API_URL}/${id}`)
  }
}
```

---

## Component Example

```vue
<!-- MovieList.vue -->
<template>
  <div class="movie-list">
    <h2>Movies</h2>
    <div v-if="loading">Loading...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else class="movies-grid">
      <MovieCard
        v-for="movie in movies"
        :key="movie.id"
        :movie="movie"
        @delete="handleDelete"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { movieApi } from '@/services/movieApi'

const movies = ref([])
const loading = ref(false)
const error = ref(null)

onMounted(async () => {
  await fetchMovies()
})

async function fetchMovies() {
  loading.value = true
  try {
    const response = await movieApi.getAllMovies()
    movies.value = response.data
  } catch (err) {
    error.value = 'Failed to load movies'
  } finally {
    loading.value = false
  }
}
</script>
```

---

## Implementation Steps

1. ⏳ Switch to `feature/vuejs-frontend` branch
2. ⏳ Create Vue 3 project: `npm create vite@latest frontend -- --template vue`
3. ⏳ Install dependencies (Vue Router, Pinia, Axios, Bootstrap)
4. ⏳ Create API service layer
5. ⏳ Build components (MovieList, MovieForm, MovieCard)
6. ⏳ Set up routing
7. ⏳ Add state management
8. ⏳ Test frontend with backend
9. ⏳ Create Dockerfile for frontend
10. ⏳ Update Docker Compose for full stack
11. ⏳ Add K8s manifests
12. ⏳ Update documentation
13. ⏳ Commit and push

---

## Development Commands

**Install dependencies:**
```bash
cd frontend
npm install
```

**Run development server:**
```bash
npm run dev
# Runs on http://localhost:5173
```

**Build for production:**
```bash
npm run build
```

**Preview production build:**
```bash
npm run preview
```

---

## Docker Setup

**Dockerfile (Multi-stage):**
```dockerfile
# Build stage
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**docker-compose.yml (Full Stack):**
```yaml
version: '3.8'
services:
  backend:
    build: .
    ports:
      - "8080:8080"

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
```

---

## CORS Configuration

**Update backend to allow frontend:**
```java
// Add CORS filter to allow Vue.js frontend
response.setHeader("Access-Control-Allow-Origin", "*");
response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
```

---

## Success Criteria

✅ Vue.js 3 app created with Vite
✅ All CRUD operations work from UI
✅ Responsive design
✅ Error handling implemented
✅ Loading states shown
✅ Docker configuration working
✅ Full-stack deployment possible
✅ Documentation updated

---

## Estimated Time

⏱️ 2-3 hours

---

## Learning Outcomes

- Vue.js 3 Composition API
- Component-based architecture
- State management with Pinia
- API integration
- Vue Router
- Full-stack deployment
- Docker multi-container setup
- Frontend build optimization
