# Understanding JPA Persistence

## What is JPA?

**JPA (Jakarta Persistence API)** is a specification for Object-Relational Mapping (ORM) in Java. It allows you to work with database tables as if they were Java objects.

### The Problem JPA Solves

**Without JPA (Raw JDBC):**
```java
// Lots of boilerplate code!
String sql = "INSERT INTO movies (title, genre, price) VALUES (?, ?, ?)";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setString(1, movie.getTitle());
stmt.setString(2, movie.getGenre());
stmt.setDouble(3, movie.getPrice());
stmt.executeUpdate();
stmt.close();
```

**With JPA:**
```java
// Clean and simple!
entityManager.persist(movie);
```

---

## Key Components

### 1. **Entity** (@Entity)
A Java class that maps to a database table.

```java
@Entity
@Table(name = "movies")  // Optional: specify table name
public class Movie {
    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)  // Constraints
    private String title;

    // ... getters and setters
}
```

**What Happens:**
- `Movie` class → `movies` table
- `id` field → `id` column (primary key, auto-increment)
- `title` field → `title` column (NOT NULL)

### 2. **persistence.xml** (Configuration)
Located in `src/main/resources/META-INF/persistence.xml`

Defines:
- Database connection details (JDBC URL, username, password)
- JPA provider (Hibernate, EclipseLink, etc.)
- Entity classes to manage
- Hibernate settings (schema generation, SQL logging, etc.)

### 3. **EntityManagerFactory** (Factory)
Creates `EntityManager` instances. Think of it as a database connection pool.

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("MovieBookingPU");
```

- Created **once** at application startup
- Heavy object (expensive to create)
- Thread-safe
- Should be closed on application shutdown

### 4. **EntityManager** (Database Session)
The main interface for database operations.

```java
EntityManager em = emf.createEntityManager();
```

- One per request/operation
- **NOT thread-safe** (create new one for each operation)
- Must be closed after use (`em.close()`)
- Think of it as a database session

### 5. **Transaction**
A unit of work that either fully succeeds or fully fails.

```java
em.getTransaction().begin();   // Start transaction
// ... do database operations
em.getTransaction().commit();  // Save changes
// OR
em.getTransaction().rollback(); // Cancel changes on error
```

---

## CRUD Operations with JPA

### Create (INSERT)
```java
EntityManager em = emf.createEntityManager();
try {
    em.getTransaction().begin();

    Movie movie = new Movie("Inception", "Sci-Fi", 12.50);
    em.persist(movie);  // INSERT INTO movies...

    em.getTransaction().commit();
} catch (Exception e) {
    em.getTransaction().rollback();
    throw e;
} finally {
    em.close();
}
```

**What Happens:**
1. Start transaction
2. `persist()` marks object for saving
3. `commit()` executes SQL INSERT
4. Movie gets auto-generated ID
5. Close EntityManager

### Read (SELECT by ID)
```java
EntityManager em = emf.createEntityManager();
try {
    Movie movie = em.find(Movie.class, 1L);  // SELECT * FROM movies WHERE id = 1
    return movie;
} finally {
    em.close();
}
```

**No transaction needed** for read-only operations!

### Read All (SELECT with JPQL)
```java
EntityManager em = emf.createEntityManager();
try {
    TypedQuery<Movie> query = em.createQuery("SELECT m FROM Movie m", Movie.class);
    List<Movie> movies = query.getResultList();
    return movies;
} finally {
    em.close();
}
```

**JPQL vs SQL:**
- JPQL: `SELECT m FROM Movie m` (query Java objects)
- SQL: `SELECT * FROM movies` (query tables)

### Update (UPDATE)
```java
EntityManager em = emf.createEntityManager();
try {
    em.getTransaction().begin();

    Movie movie = em.find(Movie.class, 1L);  // Load from DB
    movie.setPrice(15.00);                    // Modify object

    em.getTransaction().commit();  // UPDATE movies SET price = 15.00 WHERE id = 1
} finally {
    em.close();
}
```

**Automatic dirty checking!** JPA detects changes and generates UPDATE automatically.

Alternatively, use `merge()` for detached entities:
```java
Movie updatedMovie = em.merge(movie);  // Merge changes into persistence context
```

### Delete (DELETE)
```java
EntityManager em = emf.createEntityManager();
try {
    em.getTransaction().begin();

    Movie movie = em.find(Movie.class, 1L);
    em.remove(movie);  // DELETE FROM movies WHERE id = 1

    em.getTransaction().commit();
} finally {
    em.close();
}
```

---

## The Repository Pattern

Instead of scattering EntityManager code everywhere, we use the **Repository Pattern**:

```java
public class MovieRepository {
    private final EntityManagerFactory emf;

    public MovieRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Movie save(Movie movie) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(movie);
            em.getTransaction().commit();
            return movie;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // ... other methods
}
```

**Benefits:**
- Centralized data access logic
- Easier to test (can mock repository)
- Cleaner service layer code
- Consistent transaction handling

---

## Entity Lifecycle States

An entity can be in different states:

### 1. **Transient** (New)
```java
Movie movie = new Movie("Inception", "Sci-Fi", 12.50);
// Not in database, not tracked by EntityManager
```

### 2. **Managed** (Persistent)
```java
em.persist(movie);  // Now managed
movie.setPrice(15.00);  // Changes will be saved automatically
```

### 3. **Detached**
```java
em.close();  // EntityManager closed
// Movie object still exists but not tracked
movie.setPrice(20.00);  // Changes NOT saved automatically
```

### 4. **Removed**
```java
em.remove(movie);  // Marked for deletion
em.getTransaction().commit();  // Now deleted from DB
```

---

## Common JPA Annotations

### Entity-Level
- `@Entity` - Mark class as JPA entity
- `@Table(name = "movies")` - Specify table name
- `@NamedQuery` - Define reusable queries

### Field-Level
- `@Id` - Primary key
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` - Auto-increment
- `@Column(nullable = false, length = 100)` - Column constraints
- `@Transient` - Don't persist this field
- `@Temporal(TemporalType.DATE)` - Date/time mapping
- `@Enumerated(EnumType.STRING)` - Enum mapping

### Relationship Annotations
- `@OneToOne` - One-to-one relationship
- `@OneToMany` - One-to-many relationship
- `@ManyToOne` - Many-to-one relationship (used in Booking → Movie)
- `@ManyToMany` - Many-to-many relationship
- `@JoinColumn(name = "movie_id")` - Foreign key column

---

## JPQL (Jakarta Persistence Query Language)

SQL-like syntax but works with Java objects, not tables.

### Basic Queries
```java
// Select all
SELECT m FROM Movie m

// Where clause
SELECT m FROM Movie m WHERE m.genre = 'Sci-Fi'

// Order by
SELECT m FROM Movie m ORDER BY m.price DESC

// Join
SELECT b FROM Booking b JOIN b.movie m WHERE m.title = 'Inception'
```

### Named Parameters
```java
TypedQuery<Movie> query = em.createQuery(
    "SELECT m FROM Movie m WHERE m.genre = :genre",
    Movie.class
);
query.setParameter("genre", "Sci-Fi");
List<Movie> movies = query.getResultList();
```

---

## Transaction Best Practices

### ✅ DO:
```java
EntityManager em = emf.createEntityManager();
try {
    em.getTransaction().begin();
    // ... operations
    em.getTransaction().commit();
} catch (Exception e) {
    if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();  // Always rollback on error
    }
    throw e;
} finally {
    em.close();  // Always close EntityManager
}
```

### ❌ DON'T:
```java
// DON'T: Forget to close EntityManager (memory leak!)
EntityManager em = emf.createEntityManager();
em.persist(movie);

// DON'T: Forget to handle rollback
em.getTransaction().begin();
em.persist(movie);
em.getTransaction().commit();  // What if this throws exception?

// DON'T: Share EntityManager between threads
private static EntityManager em;  // NOT thread-safe!
```

---

## Schema Generation (hibernate.hbm2ddl.auto)

Controls how Hibernate manages database schema:

| Value | Behavior | Use Case |
|-------|----------|----------|
| `create` | Drop and recreate tables on startup | **Dangerous!** Lose all data |
| `create-drop` | Create on start, drop on shutdown | Unit tests |
| `update` | Add new tables/columns, keep data | Development |
| `validate` | Check schema matches entities | Production |
| `none` | Do nothing | Production (use Flyway/Liquibase) |

**Our Setting:** `update` - Safe for development, keeps data between restarts

---

## H2 Database

We use **H2** - a lightweight Java database perfect for development.

### File-Based Storage
```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:h2:file:./data/moviebooking"/>
```

**Result:** Database stored in `./data/moviebooking.mv.db`

### In-Memory Storage (Alternative)
```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:h2:mem:moviebooking"/>
```

**Result:** Data exists only in RAM (lost on shutdown)

### H2 Console (Web UI)
To view database:
```java
Server.createWebServer("-web", "-webPort", "8082").start();
```
Then visit: http://localhost:8082

---

## Migration from In-Memory to Database

### Before (In-Memory with HashMap):
```java
public class MovieService {
    private static final Map<Long, Movie> movieStore = new ConcurrentHashMap<>();

    public Movie save(Movie movie) {
        Long id = idGenerator.getAndIncrement();
        movie.setId(id);
        movieStore.put(id, movie);
        return movie;
    }
}
```

**Problems:**
- Data lost on restart
- No ACID guarantees
- Doesn't scale
- No relationships

### After (JPA with Database):
```java
public class MovieService {
    private final MovieRepository repository;

    public Movie save(Movie movie) {
        return repository.save(movie);
    }
}
```

**Benefits:**
- ✅ Data persists forever
- ✅ ACID transactions
- ✅ Can handle millions of records
- ✅ Supports relationships (Movie ↔ Booking)

---

## Common Issues and Solutions

### Issue: EntityManagerFactory not found
```
No Persistence provider for EntityManager named MovieBookingPU
```

**Solution:** Check `persistence.xml` is in `src/main/resources/META-INF/`

### Issue: Table already exists
```
Table "MOVIES" already exists
```

**Solution:** Change `hibernate.hbm2ddl.auto` to `update` instead of `create`

### Issue: LazyInitializationException
```
org.hibernate.LazyInitializationException: could not initialize proxy
```

**Solution:** Load relationships before closing EntityManager:
```java
Movie movie = em.find(Movie.class, 1L);
movie.getBookings().size();  // Force loading
em.close();
```

### Issue: Transaction not active
```
jakarta.persistence.TransactionRequiredException
```

**Solution:** Wrap write operations in transaction:
```java
em.getTransaction().begin();
em.persist(movie);
em.getTransaction().commit();
```

---

## Testing Database Persistence

After starting the application:

```bash
# 1. Create a movie
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Movie","genre":"Action","price":10.0}'

# 2. Stop the application (Ctrl+C)

# 3. Start the application again

# 4. Check if movie still exists
curl http://localhost:8080/api/movies

# If you see "Test Movie", persistence is working! ✅
```

---

## Summary

| Concept | What It Is | Example |
|---------|------------|---------|
| **JPA** | Specification for ORM | Like JDBC but object-oriented |
| **Hibernate** | JPA implementation | Does the actual work |
| **Entity** | Java class mapped to table | `Movie.java` → `movies` table |
| **EntityManagerFactory** | Creates EntityManagers | Created once at startup |
| **EntityManager** | Database session | Create per operation, close after use |
| **Transaction** | Unit of work | Begin → operations → commit/rollback |
| **Repository** | Data access pattern | Centralizes database logic |
| **JPQL** | Query language | Like SQL but for objects |
| **persistence.xml** | Configuration file | Database connection, entities, settings |

---

## Next Steps

1. ✅ Created `persistence.xml` with H2 configuration
2. ⏳ Create `MovieRepository` for data access
3. ⏳ Update `MovieService` to use repository
4. ⏳ Update `App.java` to initialize EntityManagerFactory
5. ⏳ Test persistence (data survives restart)

---

## Further Reading

- [Jakarta Persistence Specification](https://jakarta.ee/specifications/persistence/)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [H2 Database Documentation](https://www.h2database.com/)
- [JPA Best Practices](https://vladmihalcea.com/tutorials/hibernate/)
