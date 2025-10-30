# Understanding JPA Entity Models

This document explains the Movie and Booking entity models in detail.

---

## 📝 Breaking Down the Movie Model

Let me explain each part of `Movie.java`:

---

### **1. Package Declaration**
```java
package moviebooking.model;
```
**Why?**
- Tells Java this class belongs to the `moviebooking.model` package
- File MUST be in: `src/main/java/moviebooking/model/Movie.java`
- Allows other classes to import it: `import moviebooking.model.Movie;`

---

### **2. JPA Annotations - Making it a Database Entity**

```java
@Entity
@Table(name = "movies")
public class Movie {
```

**Why `@Entity`?**
- Tells JPA (Jakarta Persistence API): "This class represents a database table"
- JPA will automatically create CRUD operations for this
- Hibernate (our JPA implementation) will handle all SQL for us

**Why `@Table(name = "movies")`?**
- Specifies the actual table name in the database
- Without this, table would be named `Movie` (class name)
- We use `movies` (plural, lowercase) - common database convention

**What happens:**
```
Java Class: Movie  →  Database Table: movies
```

---

### **3. Primary Key (ID)**

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**Why `@Id`?**
- Every database table needs a primary key (unique identifier)
- This field is the primary key

**Why `@GeneratedValue`?**
- Database will auto-generate this value (1, 2, 3, 4...)
- We don't manually set IDs

**Why `Long` instead of `int`?**
- `Long` can hold bigger numbers (up to 9,223,372,036,854,775,807)
- `int` maxes out at 2,147,483,647
- For IDs that keep incrementing, `Long` is safer

**Database equivalent:**
```sql
CREATE TABLE movies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ...
);
```

---

### **4. Fields with Constraints**

```java
@Column(nullable = false)
private String title;

@Column(length = 1000)
private String description;

@Column(nullable = false)
private String genre;

@Column(nullable = false)
private Integer durationMinutes;

@Column(nullable = false)
private Double price;
```

**Why `@Column(nullable = false)`?**
- Means this field is REQUIRED (cannot be null)
- Database will reject inserting a movie without a title

**Why `@Column(length = 1000)` for description?**
- Default string length in database is often 255 characters
- Descriptions can be long, so we allow 1000 characters

**Why `Integer` not `int`?**
- `Integer` can be null (object type)
- `int` cannot be null (primitive type)
- JPA works better with object types

**Database equivalent:**
```sql
CREATE TABLE movies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    genre VARCHAR(255) NOT NULL,
    durationMinutes INT NOT NULL,
    price DOUBLE NOT NULL
);
```

---

### **5. Constructors**

```java
// Default constructor (required by JPA)
public Movie() {
}

// Constructor for creating new movies
public Movie(String title, String description, String genre,
             Integer durationMinutes, Double price) {
    this.title = title;
    this.description = description;
    this.genre = genre;
    this.durationMinutes = durationMinutes;
    this.price = price;
}
```

**Why TWO constructors?**

**Constructor #1 (empty):**
- **REQUIRED by JPA/Hibernate**
- When Hibernate reads from database, it creates an empty object first
- Then fills in the fields
- Without this, JPA will crash!

**Constructor #2 (with parameters):**
- **For US to use** when creating new movies
- Makes it easy to create a movie in one line:
```java
Movie movie = new Movie("Inception", "Dream heist", "Sci-Fi", 148, 12.50);
```

---

### **6. Getters and Setters**

```java
public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public String getTitle() {
    return title;
}

public void setTitle(String title) {
    this.title = title;
}
// ... etc for all fields
```

**Why do we need these?**

1. **Java convention** - Fields are private, access via methods
2. **Frameworks need them** - Jersey/Jackson use getters to convert to JSON
3. **Flexibility** - Can add validation later:
```java
public void setPrice(Double price) {
    if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
    this.price = price;
}
```

**How it works with JSON:**
```java
Movie movie = new Movie("Inception", ..., 12.50);
// Jersey calls getTitle(), getGenre(), getPrice()...
// Converts to:
{
  "id": 1,
  "title": "Inception",
  "genre": "Sci-Fi",
  "price": 12.50
}
```

---

### **7. equals() and hashCode()**

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Movie movie = (Movie) o;
    return Objects.equals(id, movie.id);
}

@Override
public int hashCode() {
    return Objects.hash(id);
}
```

**Why override these?**

**For comparing objects:**
```java
Movie movie1 = findMovieById(1);
Movie movie2 = findMovieById(1);

// Without equals(): false (different objects in memory)
// With equals(): true (same ID = same movie)
movie1.equals(movie2);
```

**For using in collections:**
```java
Set<Movie> movies = new HashSet<>();
movies.add(movie1);
movies.add(movie2); // Won't add duplicate because equals() says they're the same
```

**Why compare by ID only?**
- Two movies with same ID = same database row = same movie
- Even if other fields differ, same ID = same entity

---

### **8. toString()**

```java
@Override
public String toString() {
    return "Movie{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", genre='" + genre + '\'' +
            ", duration=" + durationMinutes + "min" +
            ", price=$" + price +
            '}';
}
```

**Why override toString()?**

**For debugging/logging:**
```java
System.out.println(movie);
// Without toString(): moviebooking.model.Movie@3f0ee7cb (memory address)
// With toString(): Movie{id=1, title='Inception', genre='Sci-Fi', duration=148min, price=$12.5}
```

Much more useful!

---

## 📊 The Complete Flow:

```
1. We create a Movie object in Java
   ↓
2. JPA (Hibernate) sees @Entity annotation
   ↓
3. Hibernate converts it to SQL
   ↓
4. Saves to database table 'movies'
   ↓
5. When we read it back, Hibernate:
   - Creates empty Movie() using default constructor
   - Fills fields from database
   - Returns the object
   ↓
6. Jersey (JAX-RS) sees we want JSON
   ↓
7. Calls getters (getTitle(), getPrice()...)
   ↓
8. Converts to JSON and sends to client
```

---

## 🎯 Why This Pattern?

This is called **POJO (Plain Old Java Object)** or **Entity Pattern**:

✅ **Separation of concerns** - Just data, no business logic
✅ **Framework compatibility** - Works with JPA, Jersey, Jackson
✅ **Reusable** - Can use in REST API, database, tests
✅ **Industry standard** - Every Java developer knows this pattern
