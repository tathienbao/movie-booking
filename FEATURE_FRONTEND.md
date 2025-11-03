# Feature: Vue.js 3 Frontend

**Branch:** `feature/vuejs-frontend`

**Status:** ✅ Completed

---

## Goal

Build a complete Vue.js 3 frontend application that integrates with the backend REST API, providing a modern, responsive user interface for the movie booking system.

---

## What We Implemented

### 1. Application Architecture

**Layered Frontend Architecture:**
```
UI Layer (Vue Components)
    ↓
State Management (Pinia Stores)
    ↓
API Services (Axios)
    ↓
Backend REST API
```

### 2. Complete Feature Set

**Authentication System:**
- User registration with validation
- Login with JWT token management
- Auto-login on page refresh
- Logout functionality
- Role-based UI (ADMIN vs CUSTOMER)

**Movies Features:**
- Public movie browsing (no auth required)
- Movie detail views
- Admin-only CRUD operations (create, edit, delete)
- Responsive card-based layout

**Bookings Features:**
- Create bookings from movie detail page
- Real-time price calculation
- View user's booking history
- Cancel bookings
- Total bookings and amount display

---

## Tech Stack

- Vue 3.5.22 (Composition API)
- Vue Router 4.6.3
- Pinia 3.0.3
- Axios 1.13.1
- Bootstrap 5.3.8 + Icons
- Vite 7.1.7

---

## Running the Application

**Development:**
```bash
cd frontend
npm install
npm run dev
```

**Production:**
```bash
npm run build
npm run preview
```

---

## Documentation

See `docs/learning-notes/11-understanding-vuejs-frontend.md` for comprehensive guide.

---

**Status:** ✅ **COMPLETE AND TESTED**
