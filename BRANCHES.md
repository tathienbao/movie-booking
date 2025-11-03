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
**Status:** 🚧 Planned

**What Will Be Added:**
- Vue.js 3 frontend application
- Composition API
- Movie listing component
- Movie creation/editing forms
- API integration with backend
- Responsive UI design
- Docker setup for frontend
- Full-stack deployment

**Tech Stack Additions:**
- Vue.js 3
- Vite (build tool)
- Axios (HTTP client)
- Vue Router
- Pinia (state management)

**Features:**
- View all movies
- Create new movies
- Edit existing movies
- Delete movies
- Responsive design
- Form validation

**Learning Outcomes:**
- Frontend framework (Vue.js 3)
- Component-based architecture
- State management
- REST API consumption
- Full-stack integration

**Use Case:** Demonstrates full-stack capability and Vue.js 3 knowledge

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
| `feature/authentication` | ✅ Complete | Current | Done |
| `feature/vuejs-frontend` | 🚧 Planned | Medium | 2-3 hours |

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

6. **Show `feature/vuejs-frontend`** (when complete) - Prove full-stack capability
   - "I built a Vue.js 3 frontend to demonstrate my full-stack skills"

**Key Message:** "I organized the project in branches to show progression from core backend through security to full-stack application"

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
| **Vue.js 3** | `feature/vuejs-frontend` | 🚧 |

---

## Repository
https://github.com/tathienbao/movie-booking

## Documentation
- `/docs/learning-notes/` - Technical learning resources
- `README.md` - Getting started guide
- `BRANCHES.md` - This file (branch strategy)
