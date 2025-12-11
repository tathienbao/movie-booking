# Movie Booking Full-Stack Application

A complete full-stack application with Jakarta EE 10 backend and Vue.js 3 frontend for movie booking management.

## 🌐 Live Demo

**Production Deployment:**
- **Frontend:** https://movie-booking-cyan-five.vercel.app/ (Deployed on Vercel)
- **Backend API:** https://movie-booking-yq8p.onrender.com/api (Deployed on Render)

**Try it out:**
- Register a new account or use demo admin credentials
- Admin: `admin@example.com` / `admin123`

## 🚀 Tech Stack

### Backend
- **Java 17**
- **Jakarta EE 10** - Enterprise Java specification
- **JAX-RS** - REST API framework
- **Jersey 3.1.3** - JAX-RS implementation
- **Grizzly HTTP Server** - Embedded web server
- **Maven** - Build tool
- **Docker** - Containerization
- **H2 Database** - Persistent file-based database with JPA/Hibernate
- **JWT Authentication** - Secure token-based authentication
- **BCrypt** - Password hashing for security
- **JUnit 5 & Mockito** - Comprehensive testing (172 tests)

### Frontend
- **Vue.js 3.5.22** - Progressive JavaScript framework
- **Vue Router 4.6.3** - SPA routing with navigation guards
- **Pinia 3.0.3** - State management
- **Axios 1.13.1** - HTTP client with interceptors
- **Bootstrap 5.3.8** - Responsive UI framework
- **Vite 7.1.7** - Lightning-fast build tool

### Deployment
- **Backend:** Render (Cloud Platform)
- **Frontend:** Vercel (Edge Network)
- **CI/CD:** GitHub Actions + Automated deployments

## 📋 API Endpoints

### Authentication API (Public)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login and get JWT token | No |

### Movie API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/movies` | List all movies | No (Public) |
| GET | `/api/movies/{id}` | Get movie by ID | No (Public) |
| POST | `/api/movies` | Create new movie | **Yes (ADMIN only)** |
| PUT | `/api/movies/{id}` | Update existing movie | **Yes (ADMIN only)** |
| DELETE | `/api/movies/{id}` | Delete movie | **Yes (ADMIN only)** |

### Booking API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/bookings` | List all bookings | **Yes** |
| GET | `/api/bookings/{id}` | Get booking by ID | **Yes** |
| GET | `/api/bookings/movies/{movieId}` | Get bookings for movie | **Yes** |
| POST | `/api/bookings` | Create new booking | **Yes** |
| DELETE | `/api/bookings/{id}` | Cancel booking | **Yes** |

## 🛠️ Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker (optional, for containerization)

## 🏃 Running the Application

### Full-Stack Development

**Terminal 1 - Backend:**
```bash
export JWT_SECRET_KEY="test-secret-key-for-automated-testing-min-48-chars-long-secure"
mvn exec:java -Dexec.mainClass="moviebooking.App"
# Backend runs on http://localhost:8080
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm install  # First time only
npm run dev
# Frontend runs on http://localhost:5173
```

**Access the application:**
- Frontend UI: http://localhost:5173
- Backend API: http://localhost:8080/api
- Demo admin: admin@example.com / admin123

### Backend Only Options

**Option 1: Automated Local Development (Recommended)**

```bash
./scripts/run-local.sh
```

This script automatically:
- Cleans up old containers
- Builds Maven package
- Builds Docker image
- Starts container
- Tests the API
- Shows you the status

**Option 2: Maven**

```bash
export JWT_SECRET_KEY="your-secret-key-min-48-chars-here"
mvn exec:java -Dexec.mainClass="moviebooking.App"
```

**Option 3: Standalone JAR**

```bash
export JWT_SECRET_KEY="your-secret-key-min-48-chars-here"
mvn clean package
java -jar target/movie-booking-api-1.0.0.jar
```

**Option 4: Docker Manual**

```bash
docker build -t movie-booking-api .
docker run -p 8080:8080 -e JWT_SECRET_KEY="your-secret-key" movie-booking-api
```

## 🧪 Testing the API

### Authentication Examples

```bash
# Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","name":"John Doe","password":"password123"}'

# Login and get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
# Returns: {"token":"eyJhbG...","email":"user@example.com","name":"John Doe","role":"CUSTOMER"}

# Save token for subsequent requests
TOKEN="eyJhbGciOiJIUzM4NCJ9..."  # Use token from login response
```

**Default Admin Account:**
- Email: `admin@example.com`
- Password: `admin123`

### Movie API Examples

```bash
# Get all movies (public - no auth required)
curl http://localhost:8080/api/movies

# Get specific movie (public)
curl http://localhost:8080/api/movies/1

# Create new movie (ADMIN only)
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Avatar","description":"Aliens on Pandora","genre":"Sci-Fi","durationMinutes":162,"price":14.0}'

# Update movie (ADMIN only)
curl -X PUT http://localhost:8080/api/movies/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Inception Updated","description":"New description","genre":"Sci-Fi","durationMinutes":148,"price":15.0}'

# Delete movie (ADMIN only)
curl -X DELETE http://localhost:8080/api/movies/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Booking API Examples

```bash
# Get all bookings (requires authentication)
curl http://localhost:8080/api/bookings \
  -H "Authorization: Bearer $TOKEN"

# Create new booking (user info from JWT token)
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"movieId":1,"numberOfSeats":2}'

# Cancel booking
curl -X DELETE http://localhost:8080/api/bookings/1 \
  -H "Authorization: Bearer $TOKEN"
```

## 🏗️ Project Structure

```
src/
├── main/
│   └── java/moviebooking/
│       ├── model/           # JPA entities (Movie, Booking)
│       ├── repository/      # Data access layer
│       ├── resource/        # JAX-RS REST endpoints
│       ├── service/         # Business logic & validation
│       └── App.java         # Main application
└── test/
    └── java/moviebooking/
        ├── resource/        # REST integration tests
        ├── service/         # Unit & edge case tests
        └── util/            # Test helpers
```

## 🔄 CI/CD & Deployment

**Continuous Integration:**
- **GitHub Actions** - `.github/workflows/ci.yml` for automated testing
- **Jenkinsfile** - For Jenkins CI/CD pipelines

**Production Deployment:**
- **Backend:** Deployed on [Render](https://render.com) with automatic deployments from main branch
- **Frontend:** Deployed on [Vercel](https://vercel.com) with automatic deployments from main branch
- Environment variables configured for JWT secret and database persistence

## 🐳 Docker

Multi-stage Dockerfile for optimized image size:
- Build stage: Compiles and packages the application
- Runtime stage: Runs the Fat JAR with minimal footprint

## ☸️ Kubernetes

Deploy to Kubernetes cluster:

```bash
# Apply deployment and service
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Check status
kubectl get pods
kubectl get services

# Access the API
kubectl port-forward service/movie-booking-api 8080:80
curl http://localhost:8080/api/movies
```

**Kubernetes resources included:**
- `deployment.yaml` - 3 replicas with health checks and resource limits
- `service.yaml` - LoadBalancer service exposing port 80

## 🎯 Features

- ✅ **Production Deployment** - Live on Render (backend) + Vercel (frontend)
- ✅ RESTful API design (Authentication, Movie & Booking endpoints)
- ✅ **JWT Authentication** with secure token-based auth
- ✅ **Role-Based Access Control (RBAC)** - CUSTOMER and ADMIN roles
- ✅ **Password Security** with BCrypt hashing (cost factor 12)
- ✅ JSON request/response handling
- ✅ Database persistence with JPA/Hibernate (H2)
- ✅ Full CRUD operations with authorization
- ✅ Comprehensive validation & error handling
- ✅ Entity relationships (@ManyToOne)
- ✅ Automated testing (172 tests: unit, integration, auth, RBAC, edge cases)
- ✅ Docker support with multi-stage builds
- ✅ Kubernetes deployment ready
- ✅ CI/CD with GitHub Actions + automated deployments
- ✅ Standalone executable JAR

### Security Features

- JWT tokens with HS384 algorithm (24-hour expiration)
- Password validation (min 8 chars, must contain letter + number)
- Email validation and normalization
- Case-insensitive email login
- Protected endpoints with Bearer token authentication
- Admin-only operations (Movie CRUD)
- User context from JWT claims

## 📝 License

This is a demo project for learning Jakarta EE 10 and REST API development.

## 👤 Author

Built as a demonstration of Jakarta EE 10, JAX-RS, and modern Java development practices.
