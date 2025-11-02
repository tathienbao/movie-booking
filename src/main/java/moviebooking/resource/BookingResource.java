package moviebooking.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import moviebooking.App;
import moviebooking.model.Booking;
import moviebooking.service.BookingService;

import java.util.List;

/**
 * REST API endpoints for Booking operations.
 * Uses JAX-RS annotations to define HTTP methods and paths.
 *
 * DEPENDENCY INJECTION:
 * We get BookingService from App.getBookingService() instead of creating a new instance.
 * This ensures we use the same BookingService (with database connection) across all requests.
 *
 * WHY NOT: new BookingService()?
 * - Each request would create a new BookingService
 * - BookingService needs repositories which need EntityManagerFactory
 * - We want to reuse the same EntityManagerFactory (expensive to create)
 *
 * CRITICAL FIX: Lazy initialization for thread safety
 * Uses lazy initialization with null check to prevent NPE.
 * Follows the same pattern as MovieResource.
 *
 * In enterprise apps, you'd use:
 * - CDI: @Inject BookingService bookingService
 * - Spring: @Autowired BookingService bookingService
 * - Jersey HK2: @Inject with custom binder
 */
@Path("/api/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    /**
     * Lazy-initialized BookingService to prevent race condition.
     *
     * CRITICAL FIX: volatile for double-checked locking
     *
     * volatile ensures:
     * - Visibility: All threads see the same value
     * - Prevents instruction reordering that could break double-checked locking
     *
     * Without volatile, thread B might see partially constructed BookingService
     * from thread A due to CPU cache and instruction reordering.
     *
     * THREAD SAFETY:
     * - Field initialized to null
     * - getBookingService() called lazily on first use
     * - Prevents NPE if request arrives before App finishes initialization
     */
    private volatile BookingService bookingService;

    /**
     * Get BookingService instance with lazy initialization.
     *
     * CRITICAL FIX: Double-checked locking for performance
     *
     * PERFORMANCE PROBLEM WITH synchronized METHOD:
     * Synchronizing entire method creates global lock - all HTTP requests
     * execute sequentially even after initialization. This defeats multi-threading!
     *
     * SOLUTION: Double-checked locking pattern
     * 1. First check (no lock) - fast path for already-initialized case
     * 2. Synchronized block - only for initialization
     * 3. Second check (with lock) - prevent double initialization
     * 4. volatile field - ensures visibility across threads
     *
     * PERFORMANCE:
     * - After first initialization: NO synchronization overhead
     * - Requests execute concurrently (as they should)
     * - Only first few requests might synchronize
     *
     * CORRECTNESS (Thread-safe):
     * - volatile prevents instruction reordering
     * - Double check prevents race condition
     * - synchronized block prevents concurrent initialization
     * - Once initialized, zero overhead
     *
     * RACE CONDITION FIX:
     * JAX-RS may instantiate this resource before App.main() completes.
     * Lazy initialization ensures we only call getBookingService() when needed,
     * after the application is fully initialized.
     */
    private BookingService getService() {
        // First check (no locking) - fast path for 99.9% of requests
        if (bookingService == null) {
            // Synchronize only for initialization (rare)
            synchronized (this) {
                // Second check (with lock) - prevent double initialization
                if (bookingService == null) {
                    bookingService = App.getBookingService();
                    if (bookingService == null) {
                        throw new IllegalStateException(
                                "Application not fully initialized. BookingService is null. " +
                                        "This should not happen if App.main() completed successfully."
                        );
                    }
                }
            }
        }
        return bookingService;
    }

    /**
     * GET /api/bookings - List all bookings
     */
    @GET
    public Response getAllBookings() {
        List<Booking> bookings = getService().getAllBookings();
        return Response.ok(bookings).build();
    }

    /**
     * GET /api/bookings/{id} - Get a specific booking by ID
     */
    @GET
    @Path("/{id}")
    public Response getBookingById(@PathParam("id") Long id) {
        Booking booking = getService().getBookingById(id);
        if (booking == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Booking not found\"}")
                    .build();
        }
        return Response.ok(booking).build();
    }

    /**
     * GET /api/movies/{movieId}/bookings - Get all bookings for a specific movie
     *
     * This is a nested resource endpoint that shows all bookings for a given movie.
     * Useful for:
     * - Viewing booking history for a movie
     * - Calculating seat availability
     * - Analytics and reporting
     */
    @GET
    @Path("/movies/{movieId}")
    public Response getBookingsByMovieId(@PathParam("movieId") Long movieId) {
        List<Booking> bookings = getService().getBookingsByMovieId(movieId);
        return Response.ok(bookings).build();
    }

    /**
     * POST /api/bookings - Create a new booking
     *
     * Request body should contain:
     * {
     *   "movieId": 1,
     *   "customerName": "John Doe",
     *   "customerEmail": "john@example.com",
     *   "numberOfSeats": 2
     * }
     *
     * Returns 201 Created with the created booking.
     */
    @POST
    public Response createBooking(BookingRequest request) {
        try {
            Booking created = getService().createBooking(
                    request.getMovieId(),
                    request.getCustomerName(),
                    request.getCustomerEmail(),
                    request.getNumberOfSeats()
            );
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (IllegalArgumentException e) {
            // Return 400 Bad Request for validation errors
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * DELETE /api/bookings/{id} - Cancel a booking
     *
     * Returns 204 No Content if successful.
     * Returns 404 Not Found if booking doesn't exist.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteBooking(@PathParam("id") Long id) {
        boolean deleted = getService().deleteBooking(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Booking not found\"}")
                    .build();
        }
        return Response.noContent().build();
    }

    /**
     * Data Transfer Object for booking creation request.
     *
     * WHY A SEPARATE CLASS?
     * - The client sends movieId, customerName, email, seats
     * - The Booking entity has a Movie object (not just ID)
     * - We need to validate and look up the movie first
     * - This DTO represents what the client sends, not the database entity
     *
     * In enterprise apps, you'd put DTOs in a separate package like:
     * - moviebooking.dto.BookingRequest
     * - moviebooking.api.request.CreateBookingRequest
     */
    public static class BookingRequest {
        private Long movieId;
        private String customerName;
        private String customerEmail;
        private Integer numberOfSeats;

        // Default constructor (required by JAX-RS for JSON deserialization)
        public BookingRequest() {
        }

        // Constructor for testing
        public BookingRequest(Long movieId, String customerName, String customerEmail, Integer numberOfSeats) {
            this.movieId = movieId;
            this.customerName = customerName;
            this.customerEmail = customerEmail;
            this.numberOfSeats = numberOfSeats;
        }

        // Getters and Setters (required by Jackson for JSON serialization/deserialization)
        public Long getMovieId() {
            return movieId;
        }

        public void setMovieId(Long movieId) {
            this.movieId = movieId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        public Integer getNumberOfSeats() {
            return numberOfSeats;
        }

        public void setNumberOfSeats(Integer numberOfSeats) {
            this.numberOfSeats = numberOfSeats;
        }
    }
}
