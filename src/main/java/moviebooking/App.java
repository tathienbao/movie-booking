package moviebooking;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import moviebooking.repository.BookingRepository;
import moviebooking.repository.MovieRepository;
import moviebooking.repository.UserRepository;
import moviebooking.service.AuthService;
import moviebooking.service.BookingService;
import moviebooking.service.MovieService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main application class.
 * Starts an embedded HTTP server using Grizzly and initializes database persistence.
 *
 * APPLICATION STARTUP FLOW:
 * 1. Create EntityManagerFactory from persistence.xml (JPA setup)
 * 2. Create MovieRepository with EntityManagerFactory
 * 3. Create MovieService with MovieRepository
 * 4. Initialize sample data in database (if empty)
 * 5. Start HTTP server
 * 6. Register shutdown hook to close EntityManagerFactory
 *
 * DEPENDENCY INJECTION WITHOUT FRAMEWORK:
 * We manually wire dependencies (Factory → Repository → Service).
 * In enterprise apps, you'd use CDI, Spring, or Guice for automatic injection.
 */
public class App {

    public static String BASE_URI;

    /**
     * Static service instances shared across all REST resources.
     *
     * WHY STATIC?
     * JAX-RS creates new instances of Resource classes for each request.
     * We want to share ONE service (and EntityManagerFactory) across all requests.
     *
     * Alternative approaches:
     * - Use CDI (@Inject)
     * - Use custom DI container
     * - Use Jersey's HK2 dependency injection
     */
    private static MovieService movieService;
    private static BookingService bookingService;
    private static AuthService authService;
    private static EntityManagerFactory entityManagerFactory;

    /**
     * Get the MovieService instance (used by REST resources).
     */
    public static MovieService getMovieService() {
        return movieService;
    }

    /**
     * Get the BookingService instance (used by REST resources).
     */
    public static BookingService getBookingService() {
        return bookingService;
    }

    /**
     * Get the AuthService instance (used by REST resources).
     */
    public static AuthService getAuthService() {
        return authService;
    }

    /**
     * Initialize database and create services.
     *
     * STEP-BY-STEP:
     * 1. Create EntityManagerFactory from "MovieBookingPU" persistence unit
     * 2. Create MovieRepository and BookingRepository with the factory
     * 3. Create MovieService and BookingService with the repositories
     * 4. Initialize sample data if database is empty
     */
    private static void initializeDatabase() {
        System.out.println("=== Initializing Database ===");

        // Step 1: Create EntityManagerFactory
        // This reads persistence.xml and connects to H2 database
        System.out.println("Creating EntityManagerFactory from persistence.xml...");
        entityManagerFactory = Persistence.createEntityManagerFactory("MovieBookingPU");
        System.out.println("✅ EntityManagerFactory created");

        // Step 2: Create Repositories
        System.out.println("Creating MovieRepository...");
        MovieRepository movieRepository = new MovieRepository(entityManagerFactory);
        System.out.println("✅ MovieRepository created");

        System.out.println("Creating BookingRepository...");
        BookingRepository bookingRepository = new BookingRepository(entityManagerFactory);
        System.out.println("✅ BookingRepository created");

        System.out.println("Creating UserRepository...");
        UserRepository userRepository = new UserRepository(entityManagerFactory);
        System.out.println("✅ UserRepository created");

        // Step 3: Create Services
        System.out.println("Creating MovieService...");
        movieService = new MovieService(movieRepository);
        System.out.println("✅ MovieService created");

        System.out.println("Creating BookingService...");
        bookingService = new BookingService(bookingRepository, movieRepository, userRepository);
        System.out.println("✅ BookingService created");

        System.out.println("Creating AuthService...");
        authService = new AuthService(userRepository);
        System.out.println("✅ AuthService created");

        // Step 4: Initialize sample data
        System.out.println("Initializing sample data...");
        movieRepository.initializeSampleData();

        // Step 5: Create default admin user (for testing)
        System.out.println("Creating default admin user...");
        try {
            authService.register("admin@example.com", "Admin User", "admin123", moviebooking.model.Role.ADMIN);
            System.out.println("✅ Default admin user created (admin@example.com / admin123)");
        } catch (Exception e) {
            System.out.println("ℹ️  Admin user already exists or creation failed: " + e.getMessage());
        }

        System.out.println("=== Database Initialization Complete ===\n");
    }

    public static HttpServer startServer(URI baseUri) {
        // Create a resource config that scans for JAX-RS resources and config
        final ResourceConfig rc = new ResourceConfig()
                .packages("moviebooking");

        // Create and start a new instance of grizzly http server
        return GrizzlyHttpServerFactory.createHttpServer(baseUri, rc);
    }

    /**
     * Main method.
     *
     * STARTUP SEQUENCE:
     * 1. Initialize database (EntityManagerFactory, Repository, Service)
     * 2. Start HTTP server
     * 3. Register shutdown hook (cleanup on exit)
     * 4. Wait indefinitely (until CTRL+C)
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // Step 1: Initialize database persistence
        initializeDatabase();

        // Get port from environment variable, default to 8080
        int port = Optional.ofNullable(System.getenv("PORT"))
                .map(Integer::parseInt)
                .orElse(8080);

        BASE_URI = "http://0.0.0.0:" + port + "/";

        // Step 2: Start HTTP server
        final HttpServer server = startServer(URI.create(BASE_URI));
        System.out.println("=== Movie Booking API Started ===");
        System.out.println("Server binding: " + BASE_URI + " (all interfaces)");
        System.out.println("API available at " + BASE_URI + "api");
        System.out.println("Press CTRL+C to stop the server...\n");

        // Step 3: Register shutdown hook to close EntityManagerFactory
        // This ensures proper cleanup when application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== Shutting Down ===");
            System.out.println("Stopping HTTP server...");
            server.shutdownNow();

            System.out.println("Closing EntityManagerFactory...");
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
                System.out.println("✅ EntityManagerFactory closed");
            }
            System.out.println("=== Shutdown Complete ===");
        }));

        // Step 4: Wait indefinitely (main thread blocks here)
        Thread.currentThread().join();
    }
}
