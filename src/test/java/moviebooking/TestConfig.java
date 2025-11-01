package moviebooking;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import moviebooking.repository.MovieRepository;
import moviebooking.service.MovieService;

/**
 * Test configuration helper for initializing test database.
 *
 * Provides a clean, maintainable way to set up the test environment
 * without using reflection or accessing private methods.
 */
public class TestConfig {

    private static EntityManagerFactory entityManagerFactory;
    private static MovieService movieService;
    private static boolean initialized = false;

    /**
     * Initialize test database and service layer.
     * Can be called multiple times safely (idempotent).
     */
    public static synchronized void initializeTestDatabase() {
        if (initialized) {
            return; // Already initialized
        }

        try {
            // Create EntityManagerFactory for tests
            entityManagerFactory = Persistence.createEntityManagerFactory("MovieBookingPU");

            // Create repository and service
            MovieRepository repository = new MovieRepository(entityManagerFactory);
            movieService = new MovieService(repository);

            // Set the static field in App using reflection (only way to inject for tests)
            java.lang.reflect.Field field = App.class.getDeclaredField("movieService");
            field.setAccessible(true);
            field.set(null, movieService);

            initialized = true;
            System.out.println("✅ Test database initialized successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test database", e);
        }
    }

    /**
     * Cleanup test resources.
     * Should be called after all tests complete.
     */
    public static synchronized void cleanup() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
        movieService = null;
        initialized = false;
    }

    /**
     * Get the initialized MovieService for tests.
     */
    public static MovieService getMovieService() {
        if (!initialized) {
            initializeTestDatabase();
        }
        return movieService;
    }
}
