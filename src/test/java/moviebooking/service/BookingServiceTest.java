package moviebooking.service;

import moviebooking.model.Booking;
import moviebooking.model.Movie;
import moviebooking.repository.BookingRepository;
import moviebooking.repository.MovieRepository;
import moviebooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingService.
 *
 * Uses Mockito to mock repositories, testing business logic in isolation.
 *
 * Test Structure: Given-When-Then pattern
 * - Given: Setup test data and mock behavior
 * - When: Execute the method being tested
 * - Then: Assert expected outcomes
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository mockBookingRepository;

    @Mock
    private MovieRepository mockMovieRepository;

    @Mock
    private UserRepository mockUserRepository;

    private BookingService bookingService;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(mockBookingRepository, mockMovieRepository, mockUserRepository);

        // Create a test movie that can be reused across tests
        testMovie = new Movie("Inception", "Dream heist", "Sci-Fi", 148, 12.50);
        testMovie.setId(1L);
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Constructor should throw exception when booking repository is null")
    void testConstructor_NullBookingRepository_ThrowsException() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new BookingService(null, mockMovieRepository, mockUserRepository)
        );

        assertTrue(exception.getMessage().contains("BookingRepository cannot be null"));
    }

    @Test
    @DisplayName("Constructor should throw exception when movie repository is null")
    void testConstructor_NullMovieRepository_ThrowsException() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new BookingService(mockBookingRepository, null, mockUserRepository)
        );

        assertTrue(exception.getMessage().contains("MovieRepository cannot be null"));
    }

    @Test
    @DisplayName("Constructor should throw exception when user repository is null")
    void testConstructor_NullUserRepository_ThrowsException() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new BookingService(mockBookingRepository, mockMovieRepository, null)
        );

        assertTrue(exception.getMessage().contains("UserRepository cannot be null"));
    }

    // ==================== getAllBookings Tests ====================

    @Test
    @DisplayName("getAllBookings should return all bookings from repository")
    void testGetAllBookings_ReturnsAllBookings() {
        // Given
        Booking booking1 = new Booking("John Doe", "john@example.com", testMovie, 2);
        Booking booking2 = new Booking("Jane Smith", "jane@example.com", testMovie, 3);
        List<Booking> expectedBookings = Arrays.asList(booking1, booking2);

        when(mockBookingRepository.findAll()).thenReturn(expectedBookings);

        // When
        List<Booking> actualBookings = bookingService.getAllBookings();

        // Then
        assertEquals(2, actualBookings.size());
        assertEquals("John Doe", actualBookings.get(0).getCustomerName());
        verify(mockBookingRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllBookings should return empty list when no bookings exist")
    void testGetAllBookings_NoBookings_ReturnsEmptyList() {
        // Given
        when(mockBookingRepository.findAll()).thenReturn(List.of());

        // When
        List<Booking> actualBookings = bookingService.getAllBookings();

        // Then
        assertTrue(actualBookings.isEmpty());
        verify(mockBookingRepository, times(1)).findAll();
    }

    // ==================== getBookingById Tests ====================

    @Test
    @DisplayName("getBookingById should return booking when it exists")
    void testGetBookingById_BookingExists_ReturnsBooking() {
        // Given
        Booking expectedBooking = new Booking("John Doe", "john@example.com", testMovie, 2);
        expectedBooking.setId(1L);

        when(mockBookingRepository.findById(1L)).thenReturn(expectedBooking);

        // When
        Booking actualBooking = bookingService.getBookingById(1L);

        // Then
        assertNotNull(actualBooking);
        assertEquals("John Doe", actualBooking.getCustomerName());
        assertEquals(2, actualBooking.getNumberOfSeats());
        verify(mockBookingRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getBookingById should return null when booking doesn't exist")
    void testGetBookingById_BookingNotFound_ReturnsNull() {
        // Given
        when(mockBookingRepository.findById(999L)).thenReturn(null);

        // When
        Booking actualBooking = bookingService.getBookingById(999L);

        // Then
        assertNull(actualBooking);
        verify(mockBookingRepository, times(1)).findById(999L);
    }

    // ==================== getBookingsByMovieId Tests ====================

    @Test
    @DisplayName("getBookingsByMovieId should return all bookings for a movie")
    void testGetBookingsByMovieId_ReturnsBookingsForMovie() {
        // Given
        Booking booking1 = new Booking("John Doe", "john@example.com", testMovie, 2);
        Booking booking2 = new Booking("Jane Smith", "jane@example.com", testMovie, 3);
        List<Booking> expectedBookings = Arrays.asList(booking1, booking2);

        when(mockBookingRepository.findByMovieId(1L)).thenReturn(expectedBookings);

        // When
        List<Booking> actualBookings = bookingService.getBookingsByMovieId(1L);

        // Then
        assertEquals(2, actualBookings.size());
        verify(mockBookingRepository, times(1)).findByMovieId(1L);
    }

    // ==================== createBooking Tests ====================

    @Test
    @DisplayName("createBooking should save valid booking")
    void testCreateBooking_ValidBooking_SavesSuccessfully() {
        // Given
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);

        Booking savedBooking = new Booking("John Doe", "john@example.com", testMovie, 2);
        savedBooking.setId(1L);

        when(mockBookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // When
        Booking result = bookingService.createBooking(1L, "John Doe", "john@example.com", 2);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getCustomerName());
        assertEquals("john@example.com", result.getCustomerEmail());
        assertEquals(2, result.getNumberOfSeats());
        verify(mockMovieRepository, times(1)).findById(1L);
        verify(mockBookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking should throw exception when movie ID is null")
    void testCreateBooking_NullMovieId_ThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            bookingService.createBooking(null, "John Doe", "john@example.com", 2);
        });

        verify(mockMovieRepository, never()).findById(any());
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when customer name is null")
    void testCreateBooking_NullCustomerName_ThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            bookingService.createBooking(1L, null, "john@example.com", 2);
        });

        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when customer email is null")
    void testCreateBooking_NullCustomerEmail_ThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            bookingService.createBooking(1L, "John Doe", null, 2);
        });

        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when number of seats is null")
    void testCreateBooking_NullNumberOfSeats_ThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            bookingService.createBooking(1L, "John Doe", "john@example.com", null);
        });

        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when customer name is empty")
    void testCreateBooking_EmptyCustomerName_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "   ", "john@example.com", 2)
        );

        assertTrue(exception.getMessage().contains("Customer name cannot be empty"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when customer name is too long")
    void testCreateBooking_CustomerNameTooLong_ThrowsException() {
        // Given - name with 101 characters
        String longName = "A".repeat(101);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, longName, "john@example.com", 2)
        );

        assertTrue(exception.getMessage().contains("Customer name too long"));
        assertTrue(exception.getMessage().contains("101"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when customer email is empty")
    void testCreateBooking_EmptyCustomerEmail_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", "   ", 2)
        );

        assertTrue(exception.getMessage().contains("Customer email cannot be empty"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when customer email is too long")
    void testCreateBooking_CustomerEmailTooLong_ThrowsException() {
        // Given - email with 256 characters (including @ and domain)
        // "a" * 244 + "@example.com" = 244 + 12 = 256 characters
        String longEmail = "a".repeat(244) + "@example.com";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", longEmail, 2)
        );

        assertTrue(exception.getMessage().contains("Customer email too long"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when email format is invalid")
    void testCreateBooking_InvalidEmailFormat_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", "invalid-email", 2)
        );

        assertTrue(exception.getMessage().contains("Invalid email format"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when number of seats is zero")
    void testCreateBooking_ZeroSeats_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", "john@example.com", 0)
        );

        assertTrue(exception.getMessage().contains("Number of seats must be positive"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when number of seats is negative")
    void testCreateBooking_NegativeSeats_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", "john@example.com", -5)
        );

        assertTrue(exception.getMessage().contains("Number of seats must be positive"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when number of seats exceeds maximum")
    void testCreateBooking_TooManySeats_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", "john@example.com", 101)
        );

        assertTrue(exception.getMessage().contains("Number of seats too high"));
        assertTrue(exception.getMessage().contains("101"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should throw exception when movie doesn't exist")
    void testCreateBooking_MovieNotFound_ThrowsException() {
        // Given
        when(mockMovieRepository.findById(999L)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(999L, "John Doe", "john@example.com", 2)
        );

        assertTrue(exception.getMessage().contains("Movie not found"));
        assertTrue(exception.getMessage().contains("999"));
        verify(mockMovieRepository, times(1)).findById(999L);
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking should normalize customer name and email")
    void testCreateBooking_NormalizesInputs() {
        // Given
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);

        Booking savedBooking = new Booking("John Doe", "john@example.com", testMovie, 2);
        savedBooking.setId(1L);

        when(mockBookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // When - provide inputs with whitespace and mixed case email
        Booking result = bookingService.createBooking(
                1L,
                "  John Doe  ",
                "  John@Example.COM  ",
                2
        );

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals("john@example.com", result.getCustomerEmail());
        verify(mockBookingRepository, times(1)).save(any(Booking.class));
    }

    // ==================== deleteBooking Tests ====================

    @Test
    @DisplayName("deleteBooking should delete existing booking")
    void testDeleteBooking_BookingExists_DeletesSuccessfully() {
        // Given
        when(mockBookingRepository.delete(1L)).thenReturn(true);

        // When
        boolean result = bookingService.deleteBooking(1L);

        // Then
        assertTrue(result);
        verify(mockBookingRepository, times(1)).delete(1L);
    }

    @Test
    @DisplayName("deleteBooking should return false when booking doesn't exist")
    void testDeleteBooking_BookingNotFound_ReturnsFalse() {
        // Given
        when(mockBookingRepository.delete(999L)).thenReturn(false);

        // When
        boolean result = bookingService.deleteBooking(999L);

        // Then
        assertFalse(result);
        verify(mockBookingRepository, times(1)).delete(999L);
    }
}
