# Understanding the Service Layer

This document explains the MovieService class - our business logic layer.

---

## 📝 Breaking Down MovieService.java

### **1. Package Declaration**

```java
package moviebooking.service;
```

**Why a separate `service` package?**
- **Separation of concerns**: Services handle business logic
- **Resources** (MovieResource) handle HTTP concerns
- **Services** (MovieService) handle data manipulation
- **Models** (Movie) represent data structure

**Architecture layers:**
```
MovieResource (HTTP/REST layer)
      ↓
MovieService (Business logic layer)
      ↓
Data Storage (Database/In-memory)
```

---

### **2. In-Memory Storage**

```java
private static final Map<Long, Movie> movieStore = new ConcurrentHashMap<>();
private static final AtomicLong idGenerator = new AtomicLong(1);
```

**Why `static`?**
```java
private static final Map<Long, Movie> movieStore
```
- **Shared across all instances** of MovieService
- If we create multiple MovieService objects, they all see the same data
- Simulates a database (one central data store)

**Why `final`?**
- The reference cannot be changed (always points to same Map)
- You can still add/remove items from the Map
- Good practice: Prevents accidental reassignment

**Why `ConcurrentHashMap`?**
```java
new ConcurrentHashMap<>()
```
- **Thread-safe**: Multiple requests can access simultaneously
- In a real server, many HTTP requests happen at once
- ConcurrentHashMap prevents data corruption

**Regular HashMap problem:**
```java
// Thread 1: movieStore.put(1, movie1)
// Thread 2: movieStore.put(2, movie2)  ← Can corrupt data!
```

**ConcurrentHashMap solution:**
```java
// Thread 1: movieStore.put(1, movie1)  ← Safe
// Thread 2: movieStore.put(2, movie2)  ← Safe
// No corruption!
```

**Why `AtomicLong` for ID generation?**
```java
private static final AtomicLong idGenerator = new AtomicLong(1);
```
- **Thread-safe counter** for generating unique IDs
- Starts at 1, increments for each new movie

**Regular long problem:**
```java
private static long idCounter = 0;

// Thread 1: id = idCounter++; (gets 1)
// Thread 2: id = idCounter++; (might also get 1!)  ← Duplicate IDs!
```

**AtomicLong solution:**
```java
// Thread 1: id = idGenerator.getAndIncrement(); (gets 1)
// Thread 2: id = idGenerator.getAndIncrement(); (gets 2)  ← Always unique!
```

---

### **3. Static Initialization Block**

```java
static {
    Movie movie1 = new Movie("Inception", "...", "Sci-Fi", 148, 12.50);
    movie1.setId(idGenerator.getAndIncrement());
    movieStore.put(movie1.getId(), movie1);

    // ... more movies
}
```

**What is a `static { }` block?**
- Runs **once** when the class is first loaded
- Happens **before any instance is created**
- Perfect for initializing static data

**Execution order:**
```java
1. JVM loads MovieService class
2. static { } block executes
3. movieStore gets 3 sample movies
4. Now ready for use
```

**Why pre-populate with sample data?**
- **Demo purposes**: API has data immediately
- **Testing**: Can test endpoints without manual setup
- **Development**: Don't need a database yet

---

### **4. Get All Movies Method**

```java
public List<Movie> getAllMovies() {
    return new ArrayList<>(movieStore.values());
}
```

**Why `new ArrayList<>(movieStore.values())`?**

**What `movieStore.values()` returns:**
```java
Collection<Movie>  // All movies in the map
```

**Why wrap in `new ArrayList<>()`?**
1. **Defensive copying**: Caller can't modify our internal data
2. **Type consistency**: Returns List (standard interface)

**Example of the problem without copying:**
```java
// BAD: Direct return
public Collection<Movie> getAllMovies() {
    return movieStore.values();  // ← Caller could modify!
}

// In calling code:
Collection<Movie> movies = service.getAllMovies();
movies.clear();  // ← Just deleted all movies!
```

**With defensive copy:**
```java
// GOOD: Return a copy
public List<Movie> getAllMovies() {
    return new ArrayList<>(movieStore.values());
}

// In calling code:
List<Movie> movies = service.getAllMovies();
movies.clear();  // ← Only clears the copy, original data safe!
```

---

### **5. Get Movie By ID Method**

```java
public Movie getMovieById(Long id) {
    return movieStore.get(id);
}
```

**How `Map.get()` works:**
```java
movieStore.get(1);   // Returns Movie with id=1
movieStore.get(999); // Returns null (doesn't exist)
```

**Why return null instead of throwing exception?**
- **Caller decides what to do**: Return 404, log error, etc.
- **Service layer stays simple**: Just data retrieval
- **Resource layer handles HTTP**: Converts null to 404

**Flow:**
```java
// In MovieResource:
Movie movie = movieService.getMovieById(1);
if (movie == null) {
    return Response.status(404).build();  // Resource decides response
}
return Response.ok(movie).build();
```

---

### **6. Create Movie Method**

```java
public Movie createMovie(Movie movie) {
    Long id = idGenerator.getAndIncrement();
    movie.setId(id);
    movieStore.put(id, movie);
    return movie;
}
```

**Step-by-step:**

**1. Generate unique ID**
```java
Long id = idGenerator.getAndIncrement();
// Example: id = 4
```

**2. Assign ID to movie**
```java
movie.setId(id);
// Movie now has id=4
```

**3. Store in map**
```java
movieStore.put(id, movie);
// Map: {1: Movie1, 2: Movie2, 3: Movie3, 4: NewMovie}
```

**4. Return the movie**
```java
return movie;
// Caller gets the movie with its new ID
```

**Why return the movie?**
- Client needs to know the generated ID
- Confirmation that creation succeeded
- RESTful pattern: POST returns created resource

---

### **7. Update Movie Method**

```java
public Movie updateMovie(Movie movie) {
    if (!movieStore.containsKey(movie.getId())) {
        return null;
    }
    movieStore.put(movie.getId(), movie);
    return movie;
}
```

**Why check `containsKey()` first?**
```java
if (!movieStore.containsKey(movie.getId())) {
    return null;
}
```
- **Validation**: Ensure movie exists before updating
- **Prevent accidental creation**: `put()` would create if doesn't exist
- **Communicate failure**: Return null so caller knows it failed

**What if we didn't check?**
```java
// Without check:
movieStore.put(999, movie);  // Creates new movie with id=999!
// Should have failed (999 doesn't exist)
```

**With check:**
```java
// With check:
if (!movieStore.containsKey(999)) {
    return null;  // ← Caller knows update failed
}
```

**Why use `put()` for update?**
```java
movieStore.put(movie.getId(), movie);
```
- `put()` **replaces** existing value with same key
- Perfect for updates: old movie → new movie

**Example:**
```java
// Before:
movieStore: {1: Movie{title: "Inception", price: 12.50}}

// Update:
Movie updated = new Movie();
updated.setId(1);
updated.setTitle("Inception");
updated.setPrice(15.00);  // ← Changed price

movieStore.put(1, updated);

// After:
movieStore: {1: Movie{title: "Inception", price: 15.00}}
```

---

### **8. Delete Movie Method**

```java
public boolean deleteMovie(Long id) {
    return movieStore.remove(id) != null;
}
```

**How `Map.remove()` works:**
```java
Movie removed = movieStore.remove(1);
// Returns the removed movie if it existed
// Returns null if id doesn't exist
```

**Why `!= null`?**
```java
return movieStore.remove(id) != null;
```
- **Convert to boolean**: true if deleted, false if not found
- Simpler for caller: Just check true/false

**Example:**
```java
// Movie exists:
movieStore.remove(1);  // Returns Movie object
// Movie object != null → true ✓

// Movie doesn't exist:
movieStore.remove(999);  // Returns null
// null != null → false ✓
```

**Alternative without this pattern:**
```java
// Caller would need to do:
Movie removed = movieService.deleteMovie(1);
if (removed != null) {
    // Success
} else {
    // Failed
}

// With boolean return:
boolean deleted = movieService.deleteMovie(1);
if (deleted) {
    // Success
} else {
    // Failed
}
```

---

## 🎯 Why Use a Service Layer?

### **1. Separation of Concerns**

**Without Service:**
```java
@POST
public Response createMovie(Movie movie) {
    // Resource does EVERYTHING:
    Long id = idGenerator.getAndIncrement();
    movie.setId(id);
    movieStore.put(id, movie);
    return Response.status(201).entity(movie).build();
}
```

**With Service:**
```java
// Resource: HTTP concerns only
@POST
public Response createMovie(Movie movie) {
    Movie created = movieService.createMovie(movie);
    return Response.status(201).entity(created).build();
}

// Service: Business logic only
public Movie createMovie(Movie movie) {
    Long id = idGenerator.getAndIncrement();
    movie.setId(id);
    movieStore.put(id, movie);
    return movie;
}
```

### **2. Reusability**

```java
// Can use service from multiple resources:
MovieResource.createMovie()  → movieService.createMovie()
AdminResource.importMovie()  → movieService.createMovie()
BatchResource.bulkCreate()   → movieService.createMovie()
```

### **3. Testability**

```java
// Easy to test service without HTTP:
@Test
public void testCreateMovie() {
    MovieService service = new MovieService();
    Movie movie = new Movie("Test", "Desc", "Genre", 120, 10.0);

    Movie created = service.createMovie(movie);

    assertNotNull(created.getId());
    assertEquals("Test", created.getTitle());
}
```

### **4. Future Database Integration**

```java
// Current: In-memory
public class MovieService {
    private static final Map<Long, Movie> movieStore = ...;
}

// Future: Database
public class MovieService {
    @PersistenceContext
    private EntityManager em;  // JPA database access

    public Movie createMovie(Movie movie) {
        em.persist(movie);  // Save to database
        return movie;
    }
}

// MovieResource doesn't change at all!
```

---

## ✅ Key Concepts

### **1. Thread Safety**
- Use `ConcurrentHashMap` for shared data
- Use `AtomicLong` for counters
- Important for multi-threaded servers

### **2. Defensive Programming**
- Check if resource exists before updating/deleting
- Return copies, not internal data structures
- Return null/false to indicate failure

### **3. Single Responsibility**
- Service handles business logic
- Resource handles HTTP
- Model represents data

### **4. Stateless Design**
- Each method is independent
- No instance variables (except static store)
- Easy to scale and test

---

## 🚀 Real-World Database Version

In production, you'd replace the in-memory store with JPA:

```java
public class MovieService {

    @PersistenceContext
    private EntityManager em;

    public List<Movie> getAllMovies() {
        return em.createQuery("SELECT m FROM Movie m", Movie.class)
                .getResultList();
    }

    public Movie getMovieById(Long id) {
        return em.find(Movie.class, id);
    }

    @Transactional
    public Movie createMovie(Movie movie) {
        em.persist(movie);
        return movie;
    }

    @Transactional
    public Movie updateMovie(Movie movie) {
        return em.merge(movie);
    }

    @Transactional
    public boolean deleteMovie(Long id) {
        Movie movie = em.find(Movie.class, id);
        if (movie == null) return false;
        em.remove(movie);
        return true;
    }
}
```

But the **Resource layer stays exactly the same**! That's the power of layered architecture.
