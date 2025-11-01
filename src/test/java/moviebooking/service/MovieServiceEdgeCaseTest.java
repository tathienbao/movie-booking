package moviebooking.service;

import moviebooking.model.Movie;
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
 * Edge case and security tests for MovieService.
 *
 * Tests boundary values, special characters, SQL injection, XSS, and other edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Edge Case Tests")
class MovieServiceEdgeCaseTest {

    @Mock
    private MovieRepository mockRepository;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(mockRepository);
    }

    // ==================== Boundary Value Tests ====================

    @Test
    @DisplayName("Title at exact limit (255 chars) should be accepted")
    void testCreateMovie_TitleExactly255Chars_Accepted() {
        // Given - exactly 255 characters
        String title255 = "A".repeat(255);
        Movie movie = new Movie(title255, "Desc", "Action", 120, 10.0);

        when(mockRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then
        assertNotNull(result);
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    @Test
    @DisplayName("Title at 256 chars should be rejected")
    void testCreateMovie_TitleExactly256Chars_Rejected() {
        // Given - exactly 256 characters
        String title256 = "A".repeat(256);
        Movie movie = new Movie(title256, "Desc", "Action", 120, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("title too long"));
        assertTrue(exception.getMessage().contains("256"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("Price at exact maximum ($10,000) should be accepted")
    void testCreateMovie_PriceExactly10000_Accepted() {
        // Given - exactly $10,000
        Movie movie = new Movie("Title", "Desc", "Action", 120, 10000.0);

        when(mockRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then
        assertNotNull(result);
        assertEquals(10000.0, result.getPrice());
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    @Test
    @DisplayName("Price at $10,000.01 should be rejected")
    void testCreateMovie_PriceExceeds10000_Rejected() {
        // Given - slightly over limit
        Movie movie = new Movie("Title", "Desc", "Action", 120, 10000.01);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("price too high"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("Duration at exact limit (1000 mins) should be accepted")
    void testCreateMovie_DurationExactly1000_Accepted() {
        // Given - exactly 1000 minutes
        Movie movie = new Movie("Title", "Desc", "Action", 1000, 10.0);

        when(mockRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then
        assertNotNull(result);
        assertEquals(1000, result.getDurationMinutes());
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    @Test
    @DisplayName("Duration at 1001 mins should be rejected")
    void testCreateMovie_DurationExceeds1000_Rejected() {
        // Given - exceeds limit
        Movie movie = new Movie("Title", "Desc", "Action", 1001, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("duration too long"));
        assertTrue(exception.getMessage().contains("1001"));
        verify(mockRepository, never()).save(any());
    }

    // ==================== Special Character Tests ====================

    @Test
    @DisplayName("Title with Unicode characters should be accepted")
    void testCreateMovie_TitleWithUnicode_Accepted() {
        // Given - Unicode characters (Chinese, Arabic, Emoji)
        Movie movie = new Movie("电影 الفيلم 🎬", "Description", "Drama", 120, 10.0);

        when(mockRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then
        assertNotNull(result);
        assertEquals("电影 الفيلم 🎬", result.getTitle());
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    @Test
    @DisplayName("Title with special characters should be accepted")
    void testCreateMovie_TitleWithSpecialChars_Accepted() {
        // Given - special characters
        Movie movie = new Movie("Title: (2024) - Part 1/2", "Desc", "Action", 120, 10.0);

        when(mockRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then
        assertNotNull(result);
        assertTrue(result.getTitle().contains(":"));
        assertTrue(result.getTitle().contains("/"));
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    // ==================== SQL Injection Tests ====================

    @Test
    @DisplayName("Title with SQL injection attempt should be handled safely")
    void testCreateMovie_SQLInjectionInTitle_HandledSafely() {
        // Given - SQL injection attempt
        String sqlInjection = "'; DROP TABLE movies; --";
        Movie movie = new Movie(sqlInjection, "Desc", "Action", 120, 10.0);

        when(mockRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then - should pass validation and be stored as plain text
        assertNotNull(result);
        assertEquals(sqlInjection, result.getTitle());
        verify(mockRepository, times(1)).save(any(Movie.class));
        // JPA will use parameterized queries, preventing actual SQL injection
    }

    @Test
    @DisplayName("Description with SQL injection attempt should be handled safely")
    void testCreateMovie_SQLInjectionInDescription_HandledSafely() {
        // Given - SQL injection in description
        String sqlInjection = "Normal description'; DELETE FROM bookings WHERE '1'='1";
        Movie movie = new Movie("Title", sqlInjection, "Action", 120, 10.0);

        when(mockRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then - stored safely as text
        assertNotNull(result);
        assertEquals(sqlInjection, result.getDescription());
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    // ==================== XSS (Cross-Site Scripting) Tests ====================

    @Test
    @DisplayName("Title with XSS attempt should be stored (escaping happens at presentation layer)")
    void testCreateMovie_XSSInTitle_StoredAsIs() {
        // Given - XSS attempt
        String xssAttempt = "<script>alert('XSS')</script>";
        Movie movie = new Movie(xssAttempt, "Desc", "Action", 120, 10.0);

        when(mockRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then - business layer stores as-is; presentation layer must escape
        assertNotNull(result);
        assertEquals(xssAttempt, result.getTitle());
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    @Test
    @DisplayName("Description with HTML tags should be stored")
    void testCreateMovie_HTMLInDescription_Stored() {
        // Given - HTML content
        String htmlContent = "Movie about <b>heroes</b> & <i>villains</i>";
        Movie movie = new Movie("Title", htmlContent, "Action", 120, 10.0);

        when(mockRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then
        assertNotNull(result);
        assertTrue(result.getDescription().contains("<b>"));
        assertTrue(result.getDescription().contains("&"));
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    // ==================== Null Description Tests ====================

    @Test
    @DisplayName("Null description should be accepted")
    void testCreateMovie_NullDescription_Accepted() {
        // Given - null description (optional field)
        Movie movie = new Movie("Title", null, "Action", 120, 10.0);

        when(mockRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then
        assertNotNull(result);
        assertNull(result.getDescription());
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    // ==================== Parameterized Tests for Invalid Prices ====================

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -1.0, -100.0, -9999.99})
    @DisplayName("Negative prices should be rejected")
    void testCreateMovie_NegativePrices_Rejected(double invalidPrice) {
        // Given
        Movie movie = new Movie("Title", "Desc", "Action", 120, invalidPrice);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("price must be positive"));
        verify(mockRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(doubles = {10001.0, 50000.0, 999999.0, Double.MAX_VALUE})
    @DisplayName("Prices above $10,000 should be rejected")
    void testCreateMovie_ExcessivePrices_Rejected(double excessivePrice) {
        // Given
        Movie movie = new Movie("Title", "Desc", "Action", 120, excessivePrice);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("price too high"));
        verify(mockRepository, never()).save(any());
    }

    // ==================== Parameterized Tests for Invalid Durations ====================

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -100, 0})
    @DisplayName("Zero and negative durations should be rejected")
    void testCreateMovie_InvalidDurations_Rejected(int invalidDuration) {
        // Given
        Movie movie = new Movie("Title", "Desc", "Action", invalidDuration, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("duration must be positive"));
        verify(mockRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1001, 2000, 10000, Integer.MAX_VALUE})
    @DisplayName("Durations above 1000 minutes should be rejected")
    void testCreateMovie_ExcessiveDurations_Rejected(int excessiveDuration) {
        // Given
        Movie movie = new Movie("Title", "Desc", "Action", excessiveDuration, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("duration too long"));
        verify(mockRepository, never()).save(any());
    }
}
