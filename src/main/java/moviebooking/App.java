package moviebooking;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import moviebooking.repository.BookingRepository;
import moviebooking.repository.MovieRepository;
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

    /**
     * BASE_URI - Server binding address
     *
     * 0.0.0.0 vs localhost (127.0.0.1):
     *
     * 0.0.0.0 = "Listen on ALL network interfaces" (what we use)
     * - Accepts connections from localhost, Docker, LAN, etc.
     * - REQUIRED for Docker containers to accept external connections
     * - More flexible for different deployment scenarios
     *
     * 127.0.0.1 = "Listen only on loopback interface"
     * - Only accepts connections from same machine
     * - Would BREAK Docker deployment (can't reach from host)
     *
     * CLIENT ACCESS:
     * Even though server binds to 0.0.0.0, clients use:
     * - http://localhost:8080/api/movies (local access)
     * - http://<machine-ip>:8080/api/movies (remote access)
     */
    private static final String BASE_URI = "http://0.0.0.0:8080/";

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

        // Step 3: Create Services
        System.out.println("Creating MovieService...");
        movieService = new MovieService(movieRepository);
        System.out.println("✅ MovieService created");

        System.out.println("Creating BookingService...");
        bookingService = new BookingService(bookingRepository, movieRepository);
        System.out.println("✅ BookingService created");

        // Step 4: Initialize sample data
        System.out.println("Initializing sample data...");
        movieRepository.initializeSampleData();

        System.out.println("=== Database Initialization Complete ===\n");
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources.
     */
    public static HttpServer startServer() {
        // Create a resource config that scans for JAX-RS resources and config
        final ResourceConfig rc = new ResourceConfig()
                .packages("moviebooking");

        // Create and start a new instance of grizzly http server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
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

        // Step 2: Start HTTP server
        final HttpServer server = startServer();
        System.out.println("=== Movie Booking API Started ===");
        System.out.println("Server binding: " + BASE_URI + " (all interfaces)");
        System.out.println("API Endpoints:");
        System.out.println("  Movies:   http://localhost:8080/api/movies");
        System.out.println("  Bookings: http://localhost:8080/api/bookings");
        System.out.println("Database file: ./data/moviebooking.mv.db");
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
