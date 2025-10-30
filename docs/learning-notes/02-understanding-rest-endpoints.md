# Understanding REST API Endpoints (JAX-RS)

This document explains the MovieResource class - our REST API endpoints.

---

## 📝 Breaking Down MovieResource.java

### **1. Package and Imports**

```java
package moviebooking.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
```

**Why these imports?**
- `jakarta.ws.rs.*` - JAX-RS annotations for REST endpoints
- `MediaType` - Defines content types (JSON, XML, etc.)
- `Response` - Builds HTTP responses with status codes

---

### **2. Class-Level Annotations**

```java
@Path("/api/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieResource {
```

**Why `@Path("/api/movies")`?**
- Defines the base URL for all endpoints in this class
- All methods will be accessible under `/api/movies`
- Example: GET `/api/movies`, POST `/api/movies`, etc.

**Why `@Produces(MediaType.APPLICATION_JSON)`?**
- Tells Jersey: "All responses from this class will be JSON"
- Jersey automatically converts Java objects to JSON
- Client receives: `{"id": 1, "title": "Inception", ...}`

**Why `@Consumes(MediaType.APPLICATION_JSON)`?**
- Tells Jersey: "This class accepts JSON input"
- Jersey automatically converts JSON to Java objects
- Client sends: `{"title": "New Movie", ...}` → becomes Movie object

---

### **3. Service Dependency**

```java
private final MovieService movieService = new MovieService();
```

**Why create a service instance?**
- **Separation of concerns**: Resource handles HTTP, Service handles business logic
- **Reusability**: Same service can be used by multiple resources
- **Testability**: Can mock the service in tests

**Architecture:**
```
HTTP Request → MovieResource (handles HTTP) → MovieService (handles logic) → Data Store
```

---

### **4. GET All Movies Endpoint**

```java
@GET
public Response getAllMovies() {
    List<Movie> movies = movieService.getAllMovies();
    return Response.ok(movies).build();
}
```

**How it works:**

**Step 1: Client makes request**
```bash
GET http://localhost:8080/api/movies
```

**Step 2: JAX-RS routing**
- Sees `@Path("/api/movies")` + `@GET`
- Routes to this method

**Step 3: Get data**
```java
List<Movie> movies = movieService.getAllMovies();
// Returns: [Movie1, Movie2, Movie3]
```

**Step 4: Build response**
```java
return Response.ok(movies).build();
```
- `Response.ok()` = HTTP 200 status
- `movies` = body (Jersey converts to JSON)

**Step 5: Client receives**
```json
HTTP/1.1 200 OK
Content-Type: application/json

[
  {"id": 1, "title": "Inception", ...},
  {"id": 2, "title": "The Dark Knight", ...}
]
```

---

### **5. GET Single Movie Endpoint**

```java
@GET
@Path("/{id}")
public Response getMovieById(@PathParam("id") Long id) {
    Movie movie = movieService.getMovieById(id);
    if (movie == null) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Movie not found\"}")
                .build();
    }
    return Response.ok(movie).build();
}
```

**Why `@Path("/{id}")`?**
- `{id}` is a **path parameter** (variable in URL)
- Combined with class path: `/api/movies/{id}`
- Example: GET `/api/movies/1` → `id = 1`

**Why `@PathParam("id")`?**
- Extracts the `{id}` from URL and passes it to the method
- URL: `/api/movies/123` → `id = 123L`

**Why check for null?**
```java
if (movie == null) {
    return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"Movie not found\"}")
            .build();
}
```
- If movie doesn't exist, return HTTP 404
- Provide helpful error message
- **Good API design**: Always handle errors gracefully

**Response examples:**

**Success (200):**
```json
{
  "id": 1,
  "title": "Inception",
  "genre": "Sci-Fi",
  "price": 12.50
}
```

**Not Found (404):**
```json
{
  "error": "Movie not found"
}
```

---

### **6. POST Create Movie Endpoint**

```java
@POST
public Response createMovie(Movie movie) {
    Movie created = movieService.createMovie(movie);
    return Response.status(Response.Status.CREATED)
            .entity(created)
            .build();
}
```

**Why `@POST`?**
- POST is the HTTP method for **creating new resources**
- RESTful convention

**How does `Movie movie` parameter work?**
```java
public Response createMovie(Movie movie) {
```
- Jersey sees the parameter and `@Consumes(MediaType.APPLICATION_JSON)`
- **Automatically converts** incoming JSON to Movie object
- You don't write any JSON parsing code!

**Example:**

**Client sends:**
```bash
POST /api/movies
Content-Type: application/json

{
  "title": "Avatar",
  "description": "Aliens on Pandora",
  "genre": "Sci-Fi",
  "durationMinutes": 162,
  "price": 14.00
}
```

**Jersey converts to:**
```java
Movie movie = new Movie();
movie.setTitle("Avatar");
movie.setDescription("Aliens on Pandora");
// ... etc
```

**Why `Response.Status.CREATED`?**
- HTTP 201 = "Successfully created"
- RESTful best practice: Use 201 for POST that creates resources
- Different from 200 (OK) - more specific

**Server responds:**
```json
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 4,
  "title": "Avatar",
  "description": "Aliens on Pandora",
  "genre": "Sci-Fi",
  "durationMinutes": 162,
  "price": 14.00
}
```

---

### **7. PUT Update Movie Endpoint**

```java
@PUT
@Path("/{id}")
public Response updateMovie(@PathParam("id") Long id, Movie movie) {
    movie.setId(id);
    Movie updated = movieService.updateMovie(movie);
    if (updated == null) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Movie not found\"}")
                .build();
    }
    return Response.ok(updated).build();
}
```

**Why `@PUT`?**
- PUT is for **updating existing resources**
- RESTful convention

**Why both `@PathParam` and `Movie` parameter?**
```java
updateMovie(@PathParam("id") Long id, Movie movie)
```
- `id` comes from URL: `/api/movies/1`
- `movie` comes from request body (JSON)
- Need both: ID to know **which** movie, body to know **what** to update

**Why `movie.setId(id)`?**
```java
movie.setId(id);
```
- Ensure the movie object has the correct ID
- ID from URL is the "source of truth"
- Prevents client from changing IDs

**Example:**

**Client sends:**
```bash
PUT /api/movies/1
Content-Type: application/json

{
  "title": "Inception (Updated)",
  "description": "New description",
  "genre": "Sci-Fi",
  "durationMinutes": 148,
  "price": 15.00
}
```

**Server processes:**
```java
// id = 1 (from URL)
// movie = new Movie with data from JSON
movie.setId(1); // Force correct ID
// Update in database
```

---

### **8. DELETE Movie Endpoint**

```java
@DELETE
@Path("/{id}")
public Response deleteMovie(@PathParam("id") Long id) {
    boolean deleted = movieService.deleteMovie(id);
    if (!deleted) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Movie not found\"}")
                .build();
    }
    return Response.noContent().build();
}
```

**Why `@DELETE`?**
- DELETE is for **removing resources**
- RESTful convention

**Why `Response.noContent()`?**
```java
return Response.noContent().build();
```
- HTTP 204 = "No Content"
- Means: "Successfully deleted, nothing to return"
- RESTful best practice: DELETE returns no body

**Example:**

**Client sends:**
```bash
DELETE /api/movies/1
```

**Server responds:**
```
HTTP/1.1 204 No Content
```
(No body)

---

## 🎯 Complete REST API Summary

| HTTP Method | Endpoint | Action | Success Status | Error Status |
|-------------|----------|--------|----------------|--------------|
| GET | `/api/movies` | List all movies | 200 OK | - |
| GET | `/api/movies/{id}` | Get one movie | 200 OK | 404 Not Found |
| POST | `/api/movies` | Create movie | 201 Created | - |
| PUT | `/api/movies/{id}` | Update movie | 200 OK | 404 Not Found |
| DELETE | `/api/movies/{id}` | Delete movie | 204 No Content | 404 Not Found |

---

## 🔄 How Jersey (JAX-RS) Works

```
1. HTTP Request arrives
   ↓
2. Jersey scans @Path annotations
   ↓
3. Jersey matches URL pattern
   ↓
4. Jersey checks HTTP method (@GET, @POST, etc.)
   ↓
5. Jersey converts JSON to Java objects (if needed)
   ↓
6. Jersey calls your method
   ↓
7. Your method does business logic
   ↓
8. Your method returns Response
   ↓
9. Jersey converts Java objects to JSON
   ↓
10. Jersey sends HTTP response to client
```

---

## ✅ Key Concepts

### **1. Annotations Drive Everything**
- `@Path` = URL routing
- `@GET/@POST/@PUT/@DELETE` = HTTP methods
- `@PathParam` = Extract from URL
- `@Produces/@Consumes` = Content types

### **2. Automatic JSON Conversion**
- Jersey uses Jackson (JSON library) automatically
- Java Object → JSON: Uses getters
- JSON → Java Object: Uses setters

### **3. RESTful Principles**
- **Resource-based URLs**: `/api/movies` not `/getMovies`
- **HTTP methods for actions**: GET/POST/PUT/DELETE
- **Proper status codes**: 200, 201, 204, 404
- **Stateless**: Each request is independent

### **4. Error Handling**
- Always check if resource exists
- Return appropriate status codes
- Provide helpful error messages

---

## 🚀 Testing Your API

```bash
# Get all movies
curl http://localhost:8080/api/movies

# Get one movie
curl http://localhost:8080/api/movies/1

# Create a movie
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{"title":"New Movie","genre":"Action","durationMinutes":120,"price":10.0}'

# Update a movie
curl -X PUT http://localhost:8080/api/movies/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated Movie","genre":"Action","durationMinutes":120,"price":12.0}'

# Delete a movie
curl -X DELETE http://localhost:8080/api/movies/1
```
