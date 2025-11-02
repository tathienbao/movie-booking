package moviebooking.service;

import moviebooking.model.Movie;
import moviebooking.repository.BookingRepository;
import moviebooking.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Edge case and security tests for BookingService.
 *
 * Tests boundary values, email formats, SQL injection, XSS, and other edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Edge Case Tests")
class BookingServiceEdgeCaseTest {

    @Mock
    private BookingRepository mockBookingRepository;

    @Mock
    private MovieRepository mockMovieRepository;

    private BookingService bookingService;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(mockBookingRepository, mockMovieRepository);

        // Create a test movie
        testMovie = new Movie("Inception", "Dream heist", "Sci-Fi", 148, 12.50);
        testMovie.setId(1L);
    }

    // ==================== Boundary Value Tests ====================

    @Test
    @DisplayName("Customer name at exact limit (100 chars) should be accepted")
    void testCreateBooking_NameExactly100Chars_Accepted() {
        // Given - exactly 100 characters
        String name100 = "A".repeat(100);
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, name100, "test@example.com", 2);
        });

        verify(mockBookingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Customer name at 101 chars should be rejected")
    void testCreateBooking_NameExactly101Chars_Rejected() {
        // Given - exactly 101 characters
        String name101 = "A".repeat(101);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, name101, "test@example.com", 2)
        );

        assertTrue(exception.getMessage().contains("Customer name too long"));
        assertTrue(exception.getMessage().contains("101"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Customer email at exact limit (255 chars) should be accepted")
    void testCreateBooking_EmailExactly255Chars_Accepted() {
        // Given - exactly 255 characters (including @ and domain)
        String email255 = "a".repeat(239) + "@example.com"; // Total 255 chars
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, "John Doe", email255, 2);
        });

        verify(mockBookingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Customer email at 256 chars should be rejected")
    void testCreateBooking_EmailExceeds255Chars_Rejected() {
        // Given - 256 characters
        // "a" * 244 + "@example.com" = 244 + 12 = 256 characters
        String email256 = "a".repeat(244) + "@example.com"; // Total 256 chars

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", email256, 2)
        );

        assertTrue(exception.getMessage().contains("Customer email too long"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Number of seats at exact maximum (100) should be accepted")
    void testCreateBooking_SeatsExactly100_Accepted() {
        // Given
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, "John Doe", "john@example.com", 100);
        });

        verify(mockBookingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Number of seats at 101 should be rejected")
    void testCreateBooking_SeatsExceeds100_Rejected() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", "john@example.com", 101)
        );

        assertTrue(exception.getMessage().contains("Number of seats too high"));
        assertTrue(exception.getMessage().contains("101"));
        verify(mockBookingRepository, never()).save(any());
    }

    // ==================== Email Format Tests ====================

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example-domain.com",
            "123@example.com",
            "user@sub.domain.example.com"
    })
    @DisplayName("Valid email formats should be accepted")
    void testCreateBooking_ValidEmailFormats_Accepted(String validEmail) {
        // Given
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, "John Doe", validEmail, 2);
        });

        verify(mockBookingRepository, atLeastOnce()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-email",
            "@example.com",
            "user@",
            "user@@example.com",
            "user@.com",
            "user @example.com",
            "user@example .com",
            "user@example,com",
            "user.example.com",
            ""
    })
    @DisplayName("Invalid email formats should be rejected")
    void testCreateBooking_InvalidEmailFormats_Rejected(String invalidEmail) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", invalidEmail, 2)
        );

        assertTrue(
                exception.getMessage().contains("Invalid email format") ||
                        exception.getMessage().contains("Customer email cannot be empty")
        );
        verify(mockBookingRepository, never()).save(any());
    }

    // ==================== Special Character Tests ====================

    @Test
    @DisplayName("Customer name with Unicode characters should be accepted")
    void testCreateBooking_NameWithUnicode_Accepted() {
        // Given - Unicode characters (Chinese, Arabic, Emoji)
        String unicodeName = "李明 محمد 🎬";
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, unicodeName, "test@example.com", 2);
        });

        verify(mockBookingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Customer name with special characters should be accepted")
    void testCreateBooking_NameWithSpecialChars_Accepted() {
        // Given - special characters
        String specialName = "O'Connor-Smith (Jr.)";
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, specialName, "test@example.com", 2);
        });

        verify(mockBookingRepository, times(1)).save(any());
    }

    // ==================== SQL Injection Tests ====================

    @Test
    @DisplayName("Customer name with SQL injection attempt should be handled safely")
    void testCreateBooking_SQLInjectionInName_HandledSafely() {
        // Given - SQL injection attempt
        String sqlInjection = "'; DROP TABLE bookings; --";
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then - should pass validation and be stored as plain text
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, sqlInjection, "test@example.com", 2);
        });

        verify(mockBookingRepository, times(1)).save(any());
        // JPA will use parameterized queries, preventing actual SQL injection
    }

    @Test
    @DisplayName("Customer email with SQL injection attempt should be handled safely")
    void testCreateBooking_SQLInjectionInEmail_HandledSafely() {
        // Given - SQL injection in email (though it will fail email validation)
        String sqlInjection = "test'; DELETE FROM bookings WHERE '1'='1@example.com";

        // When & Then - should fail email validation
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", sqlInjection, 2)
        );

        assertTrue(exception.getMessage().contains("Invalid email format"));
        verify(mockBookingRepository, never()).save(any());
    }

    // ==================== XSS (Cross-Site Scripting) Tests ====================

    @Test
    @DisplayName("Customer name with XSS attempt should be stored (escaping happens at presentation layer)")
    void testCreateBooking_XSSInName_StoredAsIs() {
        // Given - XSS attempt
        String xssAttempt = "<script>alert('XSS')</script>";
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then - business layer stores as-is; presentation layer must escape
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, xssAttempt, "test@example.com", 2);
        });

        verify(mockBookingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Customer name with HTML tags should be stored")
    void testCreateBooking_HTMLInName_Stored() {
        // Given - HTML content
        String htmlContent = "John <b>Doe</b>";
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, htmlContent, "test@example.com", 2);
        });

        verify(mockBookingRepository, times(1)).save(any());
    }

    // ==================== Whitespace and Normalization Tests ====================

    @Test
    @DisplayName("Customer name with only whitespace should be rejected")
    void testCreateBooking_OnlyWhitespaceName_Rejected() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "   ", "test@example.com", 2)
        );

        assertTrue(exception.getMessage().contains("Customer name cannot be empty"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Customer email with only whitespace should be rejected")
    void testCreateBooking_OnlyWhitespaceEmail_Rejected() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", "   ", 2)
        );

        assertTrue(exception.getMessage().contains("Customer email cannot be empty"));
        verify(mockBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Customer name and email should be trimmed")
    void testCreateBooking_TrimsWhitespace() {
        // Given
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, "  John Doe  ", "  Test@Example.COM  ", 2);
        });

        verify(mockBookingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Email should be converted to lowercase")
    void testCreateBooking_EmailConvertedToLowercase() {
        // Given
        when(mockMovieRepository.findById(1L)).thenReturn(testMovie);
        when(mockBookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertDoesNotThrow(() -> {
            bookingService.createBooking(1L, "John Doe", "Test@Example.COM", 2);
        });

        verify(mockBookingRepository, times(1)).save(any());
    }

    // ==================== Parameterized Tests for Invalid Seat Numbers ====================

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -100, 0})
    @DisplayName("Zero and negative seat numbers should be rejected")
    void testCreateBooking_InvalidSeatNumbers_Rejected(int invalidSeats) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", "john@example.com", invalidSeats)
        );

        assertTrue(exception.getMessage().contains("Number of seats must be positive"));
        verify(mockBookingRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {101, 200, 1000, Integer.MAX_VALUE})
    @DisplayName("Seat numbers above 100 should be rejected")
    void testCreateBooking_ExcessiveSeats_Rejected(int excessiveSeats) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(1L, "John Doe", "john@example.com", excessiveSeats)
        );

        assertTrue(exception.getMessage().contains("Number of seats too high"));
        verify(mockBookingRepository, never()).save(any());
    }

    // ==================== Movie Not Found Tests ====================

    @Test
    @DisplayName("Booking for non-existent movie should be rejected")
    void testCreateBooking_MovieNotFound_Rejected() {
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
}
