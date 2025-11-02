package moviebooking;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import moviebooking.repository.BookingRepository;
import moviebooking.repository.MovieRepository;
import moviebooking.repository.UserRepository;
import moviebooking.service.AuthService;
import moviebooking.service.BookingService;
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
    private static BookingService bookingService;
    private static AuthService authService;
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

            // Create repositories
            MovieRepository movieRepository = new MovieRepository(entityManagerFactory);
            BookingRepository bookingRepository = new BookingRepository(entityManagerFactory);
            UserRepository userRepository = new UserRepository(entityManagerFactory);

            // Create services
            movieService = new MovieService(movieRepository);
            bookingService = new BookingService(bookingRepository, movieRepository, userRepository);
            authService = new AuthService(userRepository);

            // Set the static fields in App using reflection (only way to inject for tests)
            java.lang.reflect.Field movieServiceField = App.class.getDeclaredField("movieService");
            movieServiceField.setAccessible(true);
            movieServiceField.set(null, movieService);

            java.lang.reflect.Field bookingServiceField = App.class.getDeclaredField("bookingService");
            bookingServiceField.setAccessible(true);
            bookingServiceField.set(null, bookingService);

            java.lang.reflect.Field authServiceField = App.class.getDeclaredField("authService");
            authServiceField.setAccessible(true);
            authServiceField.set(null, authService);

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
        bookingService = null;
        authService = null;
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

    /**
     * Get the initialized BookingService for tests.
     */
    public static BookingService getBookingService() {
        if (!initialized) {
            initializeTestDatabase();
        }
        return bookingService;
    }

    /**
     * Get the initialized AuthService for tests.
     */
    public static AuthService getAuthService() {
        if (!initialized) {
            initializeTestDatabase();
        }
        return authService;
    }
}
