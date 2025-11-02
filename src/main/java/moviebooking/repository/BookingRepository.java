package moviebooking.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import moviebooking.model.Booking;

import java.util.List;
import java.util.Objects;

/**
 * Repository class for Booking entity data access.
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
public class BookingRepository {

    private final EntityManagerFactory emf;

    /**
     * Constructor - receives EntityManagerFactory from application startup
     *
     * Why pass EntityManagerFactory?
     * - It's created once at app startup (expensive operation)
     * - Thread-safe and can be shared
     * - We create EntityManagers from it (cheap, per-operation)
     */
    public BookingRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Get all bookings from the database.
     *
     * JPQL vs SQL:
     * - JPQL: "SELECT b FROM Booking b" (queries Java objects)
     * - SQL:  "SELECT * FROM bookings" (queries database tables)
     *
     * TypedQuery ensures type safety - we get List<Booking>, not List<Object>
     *
     * NO TRANSACTION NEEDED for read-only operations!
     */
    public List<Booking> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            // JPQL query: SELECT all Booking entities
            TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b", Booking.class);
            return query.getResultList();
        } finally {
            // Always close EntityManager to free resources
            em.close();
        }
    }

    /**
     * Find a booking by its ID.
     *
     * CRITICAL FIX: Input validation
     * Added null check with clear error message.
     *
     * em.find() is the simplest way to get an entity by primary key.
     * Returns null if not found.
     *
     * Alternative: em.getReference() which returns proxy (lazy loading)
     */
    public Booking findById(Long id) {
        // VALIDATION: Prevent NPE with clear error message
        Objects.requireNonNull(id, "Booking ID cannot be null");

        EntityManager em = emf.createEntityManager();
        try {
            // SELECT * FROM bookings WHERE id = ?
            return em.find(Booking.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Find all bookings for a specific movie.
     *
     * This is useful for showing which movies are popular,
     * managing seat availability, or viewing booking history for a movie.
     *
     * JPQL with parameter binding prevents SQL injection!
     */
    public List<Booking> findByMovieId(Long movieId) {
        // VALIDATION: Prevent NPE with clear error message
        Objects.requireNonNull(movieId, "Movie ID cannot be null");

        EntityManager em = emf.createEntityManager();
        try {
            // JPQL query with JOIN to movie entity
            // :movieId is a named parameter (prevents SQL injection)
            TypedQuery<Booking> query = em.createQuery(
                    "SELECT b FROM Booking b WHERE b.movie.id = :movieId",
                    Booking.class);
            query.setParameter("movieId", movieId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Save a new booking to the database.
     *
     * CRITICAL FIX: Input validation
     * Added null check with clear error message.
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
    public Booking save(Booking booking) {
        // VALIDATION: Prevent NPE with clear error message
        Objects.requireNonNull(booking, "Booking cannot be null");

        EntityManager em = emf.createEntityManager();
        try {
            // Start transaction (required for write operations)
            em.getTransaction().begin();

            // INSERT INTO bookings (...) VALUES (...)
            // After commit, booking.id will be auto-generated
            em.persist(booking);

            // Commit transaction - actually executes SQL
            em.getTransaction().commit();

            return booking;
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
     * Delete a booking by its ID (cancel booking).
     *
     * CRITICAL FIX: Input validation
     * Added null check with clear error message.
     *
     * DELETE process:
     * 1. Find the entity (must be managed to remove it)
     * 2. Call remove() to mark for deletion
     * 3. Commit to execute DELETE statement
     *
     * Returns true if deleted, false if not found.
     *
     * CRITICAL FIX: Resource leak prevention
     * Properly manages transaction state before returning.
     *
     * NOTE: Bookings typically don't have update operation
     * because once booked, you usually can only cancel (delete) them.
     * If you need to change booking details, you'd cancel and create a new one.
     */
    public boolean delete(Long id) {
        // VALIDATION: Prevent NPE with clear error message
        Objects.requireNonNull(id, "Booking ID cannot be null");

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // First, find the entity (can't remove what we don't have)
            Booking booking = em.find(Booking.class, id);

            if (booking == null) {
                // Booking not found, rollback and return false
                // IMPORTANT: Must rollback before return to keep transaction clean
                em.getTransaction().rollback();
                return false;
            }

            // DELETE FROM bookings WHERE id = ?
            em.remove(booking);
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
}
