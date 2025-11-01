package moviebooking.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import moviebooking.model.Movie;

import java.util.List;

/**
 * Repository class for Movie entity data access.
 * Implements the Repository pattern for database operations using JPA.
 *
 * REPOSITORY PATTERN:
 * The repository acts as a collection-like interface for accessing domain objects.
 * It encapsulates all database access logic in one place, making the code:
 * - Easier to test (can mock the repository)
 * - Easier to maintain (all DB code in one place)
 * - More flexible (can swap database implementation)
 *
 * KEY CONCEPTS:
 * - EntityManagerFactory: Heavy object created once, creates EntityManagers
 * - EntityManager: Lightweight object created per operation (like a database session)
 * - Transaction: em.getTransaction().begin() / commit() / rollback()
 * - Always close EntityManager in finally block to prevent memory leaks
 */
public class MovieRepository {

    private final EntityManagerFactory emf;

    /**
     * Constructor - receives EntityManagerFactory from application startup
     *
     * Why pass EntityManagerFactory?
     * - It's created once at app startup (expensive operation)
     * - Thread-safe and can be shared
     * - We create EntityManagers from it (cheap, per-operation)
     */
    public MovieRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Get all movies from the database.
     *
     * JPQL vs SQL:
     * - JPQL: "SELECT m FROM Movie m" (queries Java objects)
     * - SQL:  "SELECT * FROM movies" (queries database tables)
     *
     * TypedQuery ensures type safety - we get List<Movie>, not List<Object>
     *
     * NO TRANSACTION NEEDED for read-only operations!
     */
    public List<Movie> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            // JPQL query: SELECT all Movie entities
            TypedQuery<Movie> query = em.createQuery("SELECT m FROM Movie m", Movie.class);
            return query.getResultList();
        } finally {
            // Always close EntityManager to free resources
            em.close();
        }
    }

    /**
     * Find a movie by its ID.
     *
     * em.find() is the simplest way to get an entity by primary key.
     * Returns null if not found.
     *
     * Alternative: em.getReference() which returns proxy (lazy loading)
     */
    public Movie findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            // SELECT * FROM movies WHERE id = ?
            return em.find(Movie.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Save a new movie to the database.
     *
     * TRANSACTION PATTERN:
     * 1. begin()  - Start transaction
     * 2. persist() - Mark entity for INSERT
     * 3. commit() - Execute SQL and save to database
     * 4. rollback() - Undo changes if error occurs
     *
     * persist() vs merge():
     * - persist(): For NEW entities (will get auto-generated ID)
     * - merge(): For EXISTING entities (updates based on ID)
     */
    public Movie save(Movie movie) {
        EntityManager em = emf.createEntityManager();
        try {
            // Start transaction (required for write operations)
            em.getTransaction().begin();

            // INSERT INTO movies (...) VALUES (...)
            // After commit, movie.id will be auto-generated
            em.persist(movie);

            // Commit transaction - actually executes SQL
            em.getTransaction().commit();

            return movie;
        } catch (Exception e) {
            // If anything goes wrong, rollback the transaction
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;  // Re-throw exception to caller
        } finally {
            // Always close EntityManager (even if exception occurred)
            em.close();
        }
    }

    /**
     * Update an existing movie in the database.
     *
     * CRITICAL FIX: Verify existence before merge()
     * JPA's merge() creates a new entity if ID doesn't exist, which is wrong for updates.
     * We must explicitly check existence first to prevent orphaned records.
     *
     * merge() works for detached entities (entities not currently managed).
     * It:
     * 1. Finds the entity by ID in the database
     * 2. Copies all fields from the parameter to the found entity
     * 3. Returns the managed entity
     * 4. On commit, generates UPDATE statement
     *
     * IMPORTANT: Use the returned entity, not the parameter!
     * The parameter remains detached, the returned entity is managed.
     */
    public Movie update(Movie movie) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // CRITICAL FIX: Check existence WITHIN transaction to avoid TOCTOU
            // This is atomic - we check and update in same transaction
            Movie existing = em.find(Movie.class, movie.getId());
            if (existing == null) {
                em.getTransaction().rollback();
                return null;  // Movie doesn't exist, cannot update
            }

            // UPDATE movies SET ... WHERE id = ?
            // merge() returns a MANAGED entity
            Movie updatedMovie = em.merge(movie);

            em.getTransaction().commit();

            // Return the managed entity, not the parameter
            return updatedMovie;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Delete a movie by its ID.
     *
     * DELETE process:
     * 1. Find the entity (must be managed to remove it)
     * 2. Call remove() to mark for deletion
     * 3. Commit to execute DELETE statement
     *
     * Returns true if deleted, false if not found.
     *
     * CRITICAL FIX: Resource leak prevention
     * Previous code had early return that bypassed finally block in some cases.
     * Now properly manages transaction state before returning.
     */
    public boolean delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // First, find the entity (can't remove what we don't have)
            Movie movie = em.find(Movie.class, id);

            if (movie == null) {
                // Movie not found, rollback and return false
                // IMPORTANT: Must rollback before return to keep transaction clean
                em.getTransaction().rollback();
                return false;
            }

            // DELETE FROM movies WHERE id = ?
            em.remove(movie);
            em.getTransaction().commit();
            return true;

        } catch (Exception e) {
            // Always rollback on error to prevent transaction leaks
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            // CRITICAL: Always close EntityManager to prevent resource leak
            // This executes regardless of return path
            em.close();
        }
    }

    /**
     * Initialize database with sample data if empty.
     *
     * This method checks if the database is empty and adds sample movies.
     * Useful for development/demo purposes.
     *
     * In production, you'd use database migration tools like:
     * - Flyway
     * - Liquibase
     * - SQL scripts
     */
    public void initializeSampleData() {
        // Only initialize if database is empty
        if (findAll().isEmpty()) {
            System.out.println("Database is empty, initializing sample movies...");

            save(new Movie("Inception",
                    "A thief who steals corporate secrets through dream-sharing technology",
                    "Sci-Fi", 148, 12.50));

            save(new Movie("The Dark Knight",
                    "Batman fights the Joker in Gotham City",
                    "Action", 152, 11.00));

            save(new Movie("Interstellar",
                    "A team of explorers travel through a wormhole in space",
                    "Sci-Fi", 169, 13.00));

            System.out.println("✅ Sample movies initialized in database");
        } else {
            System.out.println("Database already contains data, skipping initialization");
        }
    }
}
