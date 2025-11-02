package moviebooking.service;

import moviebooking.model.Booking;
import moviebooking.model.Movie;
import moviebooking.repository.BookingRepository;
import moviebooking.repository.MovieRepository;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Service layer for Booking business logic.
 *
 * SERVICE LAYER RESPONSIBILITIES:
 * - Implements business logic and rules
 * - Coordinates between different repositories (Booking + Movie)
 * - Validates data before persistence
 * - Transforms data between API and database layers
 *
 * DESIGN PATTERN:
 * This follows the Layered Architecture pattern:
 * REST Resource → Service Layer → Repository → Database
 *
 * WHY NOT PUT LOGIC DIRECTLY IN REST RESOURCE?
 * - Separation of concerns (REST handles HTTP, Service handles business logic)
 * - Reusability (same service can be used by REST, CLI, scheduled jobs, etc.)
 * - Testability (easier to unit test without HTTP layer)
 */
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MovieRepository movieRepository;

    // Email validation pattern (basic RFC 5322 compliance)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    /**
     * Constructor - receives repositories via Dependency Injection pattern
     *
     * DEPENDENCY INJECTION:
     * Instead of creating repositories inside this class (tight coupling),
     * we receive them from outside (loose coupling).
     *
     * Benefits:
     * - Easy to test (can inject mock repositories)
     * - Flexible (can swap repository implementation)
     * - Clear dependencies (visible in constructor)
     *
     * NOTE: We need both BookingRepository and MovieRepository because
     * when creating a booking, we must verify the movie exists.
     *
     * CRITICAL FIX: Input validation
     * Added null checks to prevent NPE if called with null repositories.
     */
    public BookingService(BookingRepository bookingRepository, MovieRepository movieRepository) {
        this.bookingRepository = Objects.requireNonNull(bookingRepository,
                "BookingRepository cannot be null. Service requires valid repository instance.");
        this.movieRepository = Objects.requireNonNull(movieRepository,
                "MovieRepository cannot be null. Service requires valid repository instance.");
    }

    /**
     * Get all bookings.
     *
     * Simple delegation to repository.
     * If we needed business logic (e.g., filter only active bookings,
     * apply user-specific permissions), we'd add it here.
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Get a booking by ID.
     *
     * Returns null if not found.
     * The REST layer will convert this to 404 Not Found.
     */
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    /**
     * Get all bookings for a specific movie.
     *
     * Useful for:
     * - Showing booking history for a movie
     * - Calculating total seats booked
     * - Managing seat availability
     */
    public List<Booking> getBookingsByMovieId(Long movieId) {
        return bookingRepository.findByMovieId(movieId);
    }

    /**
     * Create a new booking.
     *
     * BUSINESS LOGIC:
     * - Validation: Check customer name/email, movie exists, seats > 0
     * - Normalization: Trim customer name/email
     * - Calculate total price: movie price * number of seats
     *
     * CRITICAL FIX: Input validation
     * Added comprehensive validation before persisting to database.
     *
     * SECURITY FIX: Added bounds checking to prevent DoS attacks
     * - String length limits prevent memory exhaustion
     * - Maximum value limits prevent overflow and abuse
     * - Email format validation prevents injection attacks
     *
     * COORDINATION: Uses both repositories
     * - MovieRepository: Verify movie exists
     * - BookingRepository: Save the booking
     */
    public Booking createBooking(Long movieId, String customerName, String customerEmail, Integer numberOfSeats) {
        // VALIDATION: Null checks
        Objects.requireNonNull(movieId, "Movie ID cannot be null");
        Objects.requireNonNull(customerName, "Customer name cannot be null");
        Objects.requireNonNull(customerEmail, "Customer email cannot be null");
        Objects.requireNonNull(numberOfSeats, "Number of seats cannot be null");

        // VALIDATION: Customer name
        String trimmedName = customerName.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        if (trimmedName.length() > 100) {
            throw new IllegalArgumentException("Customer name too long (max 100 characters, got: " + trimmedName.length() + ")");
        }

        // VALIDATION: Customer email
        String trimmedEmail = customerEmail.trim().toLowerCase();
        if (trimmedEmail.isEmpty()) {
            throw new IllegalArgumentException("Customer email cannot be empty");
        }
        if (trimmedEmail.length() > 255) {
            throw new IllegalArgumentException("Customer email too long (max 255 characters, got: " + trimmedEmail.length() + ")");
        }
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + trimmedEmail);
        }

        // VALIDATION: Number of seats
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive (got: " + numberOfSeats + ")");
        }
        if (numberOfSeats > 100) {
            throw new IllegalArgumentException("Number of seats too high (max 100 seats per booking, got: " + numberOfSeats + ")");
        }

        // BUSINESS LOGIC: Verify movie exists
        Movie movie = movieRepository.findById(movieId);
        if (movie == null) {
            throw new IllegalArgumentException("Movie not found with ID: " + movieId);
        }

        // CREATE: Build booking with validated data
        // The Booking constructor calculates totalPrice automatically
        Booking booking = new Booking(trimmedName, trimmedEmail, movie, numberOfSeats);

        // SAVE: Persist to database
        return bookingRepository.save(booking);
    }

    /**
     * Delete a booking by ID (cancel booking).
     *
     * Returns true if deleted, false if not found.
     *
     * BUSINESS LOGIC we could add:
     * - Check booking time (can't cancel within 24 hours of movie)
     * - Send cancellation confirmation email
     * - Issue refund
     * - Soft delete (mark as cancelled instead of deleting)
     */
    public boolean deleteBooking(Long id) {
        return bookingRepository.delete(id);
    }
}
