# Branch Strategy

This project uses a feature branch workflow to demonstrate different levels of backend and full-stack development skills.

## Branch Overview

```
main (Production-ready core backend)
├── feature/core-backend-solid-foundation
├── feature/database-persistence
├── feature/automated-testing
├── feature/booking-api
├── feature/authentication
└── feature/vuejs-frontend
```

---

## Branch Descriptions

### `main` - Production-Ready Core Backend
**Status:** ✅ Complete

**What's Included:**
- Jakarta EE 10 REST API with JAX-RS (Jersey)
- Embedded Grizzly HTTP Server
- In-memory data storage (ConcurrentHashMap)
- Docker containerization
- Kubernetes deployment manifests
- Jenkins CI/CD pipeline
- GitHub Actions workflow
- Local development automation scripts
- Comprehensive documentation

**Tech Stack:**
- Java 17
- Jakarta EE 10
- Jersey 3.1.3 (JAX-RS)
- Maven
- Docker
- Kubernetes
- Jenkins

**Use Case:** Demonstrates solid backend fundamentals and DevOps knowledge

---

### `feature/core-backend-solid-foundation` - Reference Branch
**Status:** ✅ Complete (Mirror of main)

**Purpose:**
- Serves as a baseline reference
- Shows the "before" state for feature comparisons
- Demonstrates core backend knowledge without advanced features

**Use Case:** Interview reference - "This is the solid foundation I started with"

---

### `feature/database-persistence` - Data Engineering
**Status:** ✅ Complete

**What Will Be Added:**
- H2 database integration
- JPA/Hibernate persistence
- Database repositories
- Data initialization
- Migration from in-memory to persistent storage
- Database configuration management

**Tech Stack Additions:**
- H2 Database
- Hibernate 6.2.7
- JPA (Jakarta Persistence API)
- persistence.xml configuration

**Learning Outcomes:**
- Database design and normalization
- ORM (Object-Relational Mapping)
- Transaction management
- Data persistence patterns

**Use Case:** Demonstrates data engineering and database management skills

---

### `feature/automated-testing` - Quality Assurance
**Status:** ✅ Complete

**What Will Be Added:**
- JUnit 5 unit tests
- REST endpoint integration tests
- Service layer tests
- Mock testing
- Test coverage reporting
- CI/CD integration with automated test runs

**Tech Stack Additions:**
- JUnit 5
- Mockito (mocking framework)
- REST Assured (API testing)
- JaCoCo (code coverage)

**Test Types:**
- **Unit Tests:** Service layer business logic
- **Integration Tests:** REST API endpoints
- **Mock Tests:** Database and external dependencies

**Learning Outcomes:**
- Test-Driven Development (TDD)
- Unit testing best practices
- Integration testing
- Test automation in CI/CD

**Use Case:** Demonstrates testing knowledge and quality assurance practices

---

### `feature/booking-api` - Booking Management
**Status:** ✅ Complete

**What Was Added:**
- Complete Booking API with CRUD operations
- Booking entity with JPA persistence
- Booking service layer with validation
- REST endpoints for booking management
- Comprehensive booking tests

**Tech Stack Additions:**
- Booking entity (@ManyToOne relationships)
- BookingRepository with JPA
- BookingService with business logic
- BookingResource REST endpoints

**Features:**
- Create bookings for movies
- View all bookings
- Get bookings by ID
- Get bookings by movie ID
- Cancel bookings
- Validation and error handling

**Learning Outcomes:**
- Entity relationships (Movie-Booking @ManyToOne)
- Complex service layer logic
- REST API best practices
- Business validation rules

**Use Case:** Demonstrates ability to implement complete feature with database relationships

---

### `feature/authentication` - Security & RBAC
**Status:** ✅ Complete (Current Branch)

**What Was Added:**
- JWT-based authentication with HS384 algorithm
- Role-Based Access Control (RBAC) - CUSTOMER and ADMIN roles
- User registration and login
- Password hashing with BCrypt (cost factor 12)
- Secure token generation and validation
- Protected endpoints with Bearer authentication
- Admin-only operations
- Comprehensive security tests (171/172 passing)
- Security fixes (environment-based JWT secret, path matching)

**Tech Stack Additions:**
- JWT (JSON Web Tokens) with jjwt library
- BCrypt password hashing
- User entity with role management
- UserRepository with JPA
- AuthService for authentication
- JwtAuthenticationFilter for request filtering
- JwtUtil for token operations

**Security Features:**
- JWT tokens with 24-hour expiration
- Environment variable for JWT secret (not hardcoded)
- Password validation (min 8 chars, letter + number)
- Email validation and case-insensitive login
- Protected endpoints with proper authorization
- Admin-only movie CRUD operations
- Customer role for bookings

**Documentation Added:**
- 09-understanding-automated-testing.md (updated with auth testing)
- 10-understanding-authentication-authorization.md (comprehensive security guide)

**Learning Outcomes:**
- JWT authentication implementation
- RBAC and authorization patterns
- Password security and hashing
- Security best practices
- Authentication testing strategies

**Use Case:** Demonstrates enterprise-level security implementation and authentication knowledge

---

### `feature/vuejs-frontend` - Full-Stack Development
**Status:** ✅ Complete

**What Was Added:**
- Complete Vue.js 3 frontend application with Composition API
- Authentication UI (Login, Register with JWT token management)
- Movie browsing (public listing and detailed views)
- Booking management (create, view, cancel bookings)
- Admin controls (create, edit, delete movies)
- Responsive design with Bootstrap 5
- Real-time state management
- Protected routes with navigation guards

**Tech Stack:**
- Vue 3.5.22 (Composition API with `<script setup>`)
- Vue Router 4.6.3 (SPA routing with guards)
- Pinia 3.0.3 (state management)
- Axios 1.13.1 (HTTP client with interceptors)
- Bootstrap 5.3.8 + Icons (responsive UI)
- Vite 7.1.7 (lightning-fast build tool)

**Features Implemented:**
- User authentication (login, register, logout, auto-login)
- Public movie browsing (no auth required)
- Movie details with booking form
- Admin-only movie CRUD operations
- User booking management
- Real-time booking creation and cancellation
- Responsive mobile-first design
- Form validation and error handling
- Loading states and success/error feedback

**Architecture:**
- API Layer: Axios with JWT interceptors
- State Management: Pinia stores (auth, movies, bookings)
- Routing: Protected routes with navigation guards
- Components: 6 views + reusable Navbar
- Services: Clean API separation (auth, movies, bookings)

**Documentation Added:**
- 11-understanding-vuejs-frontend.md (comprehensive guide)

**Learning Outcomes:**
- Vue.js 3 Composition API and modern patterns
- SPA architecture with Vue Router
- State management with Pinia
- JWT authentication flow
- REST API integration
- Responsive UI design
- Full-stack application integration

**Use Case:** Demonstrates complete full-stack capability with modern frontend framework

---

## Development Workflow

### Working on Features

1. **Switch to feature branch:**
   ```bash
   git checkout feature/database-persistence
   ```

2. **Make changes and commit:**
   ```bash
   git add .
   git commit -m "Add database persistence"
   ```

3. **Push to remote:**
   ```bash
   git push origin feature/database-persistence
   ```

4. **Optional: Merge to main when complete:**
   ```bash
   git checkout main
   git merge feature/database-persistence
   git push origin main
   ```

---

## Branch Status

| Branch | Status | Priority | Estimated Time |
|--------|--------|----------|----------------|
| `main` | ✅ Complete | - | Done |
| `feature/core-backend-solid-foundation` | ✅ Complete | Reference | Done |
| `feature/database-persistence` | ✅ Complete | - | Done |
| `feature/automated-testing` | ✅ Complete | - | Done |
| `feature/booking-api` | ✅ Complete | - | Done |
| `feature/authentication` | ✅ Complete | - | Done |
| `feature/vuejs-frontend` | ✅ Complete | Current | Done |

---

## Interview Strategy

**Recommended presentation order:**

1. **Start with `main`** - Show solid backend fundamentals
   - "This is a production-ready Jakarta EE backend with Docker and Kubernetes"

2. **Show `feature/database-persistence`** - Demonstrate data engineering
   - "Here I added database persistence with JPA/Hibernate"

3. **Show `feature/automated-testing`** - Highlight quality practices
   - "I believe in automated testing, so I added comprehensive test coverage (172 tests)"

4. **Show `feature/booking-api`** - Demonstrate feature implementation
   - "I implemented a complete Booking API with entity relationships and business logic"

5. **Show `feature/authentication`** - Showcase security expertise
   - "I added enterprise-level JWT authentication with RBAC and comprehensive security"

6. **Show `feature/vuejs-frontend`** - Prove full-stack capability
   - "I built a complete Vue.js 3 frontend with state management, routing, and full integration"

**Key Message:** "I organized the project in branches to show progression from core backend through security to a production-ready full-stack application"

---

## Enterprise Job Requirements Coverage

| Requirement | Branch | Status |
|-------------|--------|--------|
| **Java with Jakarta EE 10** | `main` | ✅ |
| **JAX-RS** | `main` | ✅ |
| **CI/CD (Jenkins, Docker, K8s)** | `main` | ✅ |
| **Git** | All branches | ✅ |
| **REST** | `main` | ✅ |
| **JPA/Hibernate** | `feature/database-persistence` | ✅ |
| **Automated Tests (172 tests)** | `feature/automated-testing` | ✅ |
| **JWT Authentication** | `feature/authentication` | ✅ |
| **RBAC (Role-Based Access)** | `feature/authentication` | ✅ |
| **Security Best Practices** | `feature/authentication` | ✅ |
| **Vue.js 3** | `feature/vuejs-frontend` | ✅ |
| **Vue Router** | `feature/vuejs-frontend` | ✅ |
| **Pinia State Management** | `feature/vuejs-frontend` | ✅ |
| **Responsive UI (Bootstrap)** | `feature/vuejs-frontend` | ✅ |
| **Full-Stack Integration** | `feature/vuejs-frontend` | ✅ |

---

## Repository
https://github.com/tathienbao/movie-booking

## Documentation
- `/docs/learning-notes/` - Technical learning resources
- `README.md` - Getting started guide
- `BRANCHES.md` - This file (branch strategy)
