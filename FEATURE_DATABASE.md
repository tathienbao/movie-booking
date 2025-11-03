# Feature: Database Persistence

**Branch:** `feature/database-persistence`

**Status:** ✅ Completed

---

## Goal

Replace in-memory storage (ConcurrentHashMap) with H2 database persistence using JPA/Hibernate.

---

## What We'll Implement

### 1. Database Configuration
- Create `persistence.xml` for JPA configuration
- Configure H2 database connection
- Set up Hibernate properties

### 2. JPA Repository
- Create `MovieRepository` interface/class
- Implement CRUD operations with JPA EntityManager
- Handle transactions properly

### 3. Update MovieService
- Replace ConcurrentHashMap with database calls
- Use MovieRepository for data access
- Maintain same REST API interface (no breaking changes)

### 4. Database Initialization
- Auto-create tables on startup
- Seed initial movie data
- Handle database lifecycle

### 5. Update Documentation
- Add database setup instructions to README
- Document JPA entities and repositories
- Add database architecture notes

---

## Files to Create/Modify

### New Files:
- `src/main/resources/META-INF/persistence.xml` - JPA configuration
- `src/main/java/moviebooking/repository/MovieRepository.java` - Data access layer
- `docs/learning-notes/07-understanding-jpa-persistence.md` - Database documentation

### Modified Files:
- `src/main/java/moviebooking/service/MovieService.java` - Use repository instead of HashMap
- `src/main/java/moviebooking/model/Movie.java` - Ensure JPA annotations are correct
- `README.md` - Add database information

---

## Tech Stack

**Already Have:**
- H2 Database 2.2.224 ✅
- Hibernate 6.2.7.Final ✅
- JPA (Jakarta Persistence API) 3.1.0 ✅

**What We'll Add:**
- JPA EntityManager
- Transaction management
- Repository pattern

---

## Implementation Steps

1. ✅ Switch to `feature/database-persistence` branch
2. ✅ Create `persistence.xml`
3. ✅ Create `MovieRepository` class
4. ✅ Update `MovieService` to use repository
5. ✅ Test CRUD operations
6. ✅ Add database initialization
7. ✅ Update documentation
8. ✅ Test with automation script
9. ✅ Commit and push

---

## Testing Strategy

**Manual Testing:**
```bash
./scripts/run-local.sh
curl http://localhost:8080/api/movies
```

**Verification:**
- All existing API endpoints still work
- Data persists between restarts (not in-memory anymore)
- CRUD operations function correctly

---

## Success Criteria

✅ Movies stored in H2 database (not memory)
✅ All REST endpoints work as before
✅ Data persists across application restarts
✅ JPA entities properly configured
✅ Repository pattern implemented
✅ Documentation updated

---

## Estimated Time

⏱️ 1 hour

---

## Learning Outcomes

- JPA/Hibernate configuration
- EntityManager usage
- Repository pattern
- Database transaction management
- H2 database setup
- Data persistence concepts
