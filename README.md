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
- **H2 Database** - In-memory database (ready for JPA integration)

## 📋 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/movies` | List all movies |
| GET | `/api/movies/{id}` | Get movie by ID |
| POST | `/api/movies` | Create new movie |
| PUT | `/api/movies/{id}` | Update existing movie |
| DELETE | `/api/movies/{id}` | Delete movie |

## 🛠️ Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker (optional, for containerization)

## 🏃 Running the Application

### Option 1: Maven

```bash
mvn exec:java -Dexec.mainClass="moviebooking.App"
```

### Option 2: Standalone JAR

```bash
mvn clean package
java -jar target/movie-booking-api-1.0.0.jar
```

### Option 3: Docker

```bash
docker build -t movie-booking-api .
docker run -p 8080:8080 movie-booking-api
```

## 🧪 Testing the API

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

## 🏗️ Project Structure

```
src/
├── main/
│   └── java/moviebooking/
│       ├── model/           # JPA entities
│       ├── resource/        # JAX-RS REST endpoints
│       ├── service/         # Business logic
│       └── App.java         # Main application
└── test/
    └── java/moviebooking/   # Unit tests
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

## 🎯 Features

- ✅ RESTful API design
- ✅ JSON request/response handling
- ✅ In-memory data storage
- ✅ CRUD operations
- ✅ Docker support
- ✅ CI/CD ready
- ✅ Standalone executable JAR

## 📝 License

This is a demo project for learning Jakarta EE 10 and REST API development.

## 👤 Author

Built as a demonstration of Jakarta EE 10, JAX-RS, and modern Java development practices.
