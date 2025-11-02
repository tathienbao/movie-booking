package moviebooking.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import moviebooking.model.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repository class for User entity data access.
 * Implements the Repository pattern for database operations using JPA.
 *
 * REPOSITORY PATTERN:
 * The repository acts as a collection-like interface for accessing domain objects.
 * It encapsulates all database access logic in one place.
 */
public class UserRepository {

    private final EntityManagerFactory emf;

    public UserRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Get all users from the database.
     *
     * NO TRANSACTION NEEDED for read-only operations!
     */
    public List<User> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find a user by ID.
     *
     * @param id User ID
     * @return User if found, null otherwise
     */
    public User findById(Long id) {
        Objects.requireNonNull(id, "User ID cannot be null");

        EntityManager em = emf.createEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Find a user by email (used for login).
     *
     * @param email User's email address
     * @return Optional containing the user if found, empty otherwise
     */
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email, "Email cannot be null");

        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email",
                    User.class);
            query.setParameter("email", email.toLowerCase());

            try {
                return Optional.of(query.getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        } finally {
            em.close();
        }
    }

    /**
     * Check if a user with the given email already exists.
     *
     * @param email Email to check
     * @return true if user exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        Objects.requireNonNull(email, "Email cannot be null");

        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.email = :email",
                    Long.class);
            query.setParameter("email", email.toLowerCase());

            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Save a new user to the database.
     *
     * TRANSACTION PATTERN:
     * 1. begin()  - Start transaction
     * 2. persist() - Mark entity for INSERT
     * 3. commit() - Execute SQL and save to database
     * 4. rollback() - Undo changes if error occurs
     */
    public User save(User user) {
        Objects.requireNonNull(user, "User cannot be null");

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
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
     * Update an existing user.
     *
     * Uses JPA dirty checking - update managed entity directly,
     * JPA auto-detects changes and generates UPDATE on commit.
     */
    public User update(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(user.getId(), "User ID cannot be null for update");

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            User existing = em.find(User.class, user.getId());
            if (existing == null) {
                em.getTransaction().rollback();
                return null;
            }

            // Update managed entity directly (JPA dirty checking)
            existing.setEmail(user.getEmail());
            existing.setName(user.getName());
            existing.setPasswordHash(user.getPasswordHash());
            existing.setRole(user.getRole());

            em.getTransaction().commit();
            return existing;
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
     * Delete a user by ID.
     *
     * Returns true if deleted, false if not found.
     */
    public boolean delete(Long id) {
        Objects.requireNonNull(id, "User ID cannot be null");

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            User user = em.find(User.class, id);
            if (user == null) {
                em.getTransaction().rollback();
                return false;
            }

            em.remove(user);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
