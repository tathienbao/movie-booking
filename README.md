# Movie Booking REST API

A Jakarta EE 10 REST API demo project built with JAX-RS, Jersey, and Grizzly HTTP Server.

## 🚀 Tech Stack

- **Java 17**
- **Jakarta EE 10** - Enterprise Java specification
- **JAX-RS** - REST API framework
- **Jersey 3.1.3** - JAX-RS implementation
- **Grizzly HTTP Server** - Embedded web server
- **Maven** - Build tool
- **Docker** - Containerization
- **H2 Database** - Persistent file-based database with JPA/Hibernate
- **JUnit 5 & Mockito** - Comprehensive testing (149 tests)

## 📋 API Endpoints

### Movie API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/movies` | List all movies |
| GET | `/api/movies/{id}` | Get movie by ID |
| POST | `/api/movies` | Create new movie |
| PUT | `/api/movies/{id}` | Update existing movie |
| DELETE | `/api/movies/{id}` | Delete movie |

### Booking API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bookings` | List all bookings |
| GET | `/api/bookings/{id}` | Get booking by ID |
| GET | `/api/bookings/movies/{movieId}` | Get all bookings for a specific movie |
| POST | `/api/bookings` | Create new booking |
| DELETE | `/api/bookings/{id}` | Cancel booking |

## 🛠️ Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker (optional, for containerization)

## 🏃 Running the Application

### Option 1: Automated Local Development (Recommended)

**One command to build, run, and test everything:**

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

**Quick API test (when already running):**

```bash
./scripts/test-api.sh
```

### Option 2: Maven

```bash
mvn exec:java -Dexec.mainClass="moviebooking.App"
```

### Option 3: Standalone JAR

```bash
mvn clean package
java -jar target/movie-booking-api-1.0.0.jar
```

### Option 4: Docker Manual

```bash
docker build -t movie-booking-api .
docker run -p 8080:8080 movie-booking-api
```

## 🧪 Testing the API

### Movie API Examples

```bash
# Get all movies
curl http://localhost:8080/api/movies

# Get specific movie
curl http://localhost:8080/api/movies/1

# Create new movie
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{"title":"Avatar","description":"Aliens on Pandora","genre":"Sci-Fi","durationMinutes":162,"price":14.0}'

# Update movie
curl -X PUT http://localhost:8080/api/movies/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Inception Updated","description":"New description","genre":"Sci-Fi","durationMinutes":148,"price":15.0}'

# Delete movie
curl -X DELETE http://localhost:8080/api/movies/1
```

### Booking API Examples

```bash
# Get all bookings
curl http://localhost:8080/api/bookings

# Get specific booking
curl http://localhost:8080/api/bookings/1

# Get bookings for a movie
curl http://localhost:8080/api/bookings/movies/1

# Create new booking
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"movieId":1,"customerName":"John Doe","customerEmail":"john@example.com","numberOfSeats":2}'

# Cancel booking
curl -X DELETE http://localhost:8080/api/bookings/1
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

## 📚 Learning Resources

Check the `/docs/learning-notes` folder for comprehensive explanations:

1. **01-understanding-jpa-entities.md** - JPA Entity models
2. **02-understanding-rest-endpoints.md** - REST API and JAX-RS
3. **03-understanding-service-layer.md** - Business logic layer
4. **04-understanding-application-startup.md** - Application startup
5. **05-understanding-jar-files.md** - JAR files explained
6. **06-understanding-maven-shade-plugin.md** - Fat JAR creation

## 🔄 CI/CD

This project includes:

- **Jenkinsfile** - For Jenkins CI/CD pipelines
- **GitHub Actions** - `.github/workflows/ci.yml` for automated testing

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

- ✅ RESTful API design (Movie & Booking endpoints)
- ✅ JSON request/response handling
- ✅ Database persistence with JPA/Hibernate (H2)
- ✅ Full CRUD operations
- ✅ Comprehensive validation & error handling
- ✅ Entity relationships (@ManyToOne)
- ✅ Automated testing (149 tests: unit, integration, edge cases)
- ✅ Docker support with multi-stage builds
- ✅ Kubernetes deployment ready
- ✅ CI/CD ready (GitHub Actions + Jenkins)
- ✅ Standalone executable JAR

## 📝 License

This is a demo project for learning Jakarta EE 10 and REST API development.

## 👤 Author

Built as a demonstration of Jakarta EE 10, JAX-RS, and modern Java development practices.
