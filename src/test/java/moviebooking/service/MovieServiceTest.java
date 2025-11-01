package moviebooking.service;

import moviebooking.model.Movie;
import moviebooking.repository.MovieRepository;
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
 * Unit tests for MovieService.
 *
 * Uses Mockito to mock MovieRepository, testing business logic in isolation.
 *
 * Test Structure: Given-When-Then pattern
 * - Given: Setup test data and mock behavior
 * - When: Execute the method being tested
 * - Then: Assert expected outcomes
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Unit Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository mockRepository;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(mockRepository);
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Constructor should throw exception when repository is null")
    void testConstructor_NullRepository_ThrowsException() {
        // When & Then
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new MovieService(null)
        );

        assertTrue(exception.getMessage().contains("MovieRepository cannot be null"));
    }

    // ==================== getAllMovies Tests ====================

    @Test
    @DisplayName("getAllMovies should return all movies from repository")
    void testGetAllMovies_ReturnsAllMovies() {
        // Given
        List<Movie> expectedMovies = Arrays.asList(
            new Movie("Inception", "Dream heist", "Sci-Fi", 148, 12.50),
            new Movie("The Matrix", "Red pill", "Sci-Fi", 136, 11.00)
        );
        when(mockRepository.findAll()).thenReturn(expectedMovies);

        // When
        List<Movie> actualMovies = movieService.getAllMovies();

        // Then
        assertEquals(2, actualMovies.size());
        assertEquals("Inception", actualMovies.get(0).getTitle());
        verify(mockRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllMovies should return empty list when no movies exist")
    void testGetAllMovies_NoMovies_ReturnsEmptyList() {
        // Given
        when(mockRepository.findAll()).thenReturn(List.of());

        // When
        List<Movie> actualMovies = movieService.getAllMovies();

        // Then
        assertTrue(actualMovies.isEmpty());
        verify(mockRepository, times(1)).findAll();
    }

    // ==================== getMovieById Tests ====================

    @Test
    @DisplayName("getMovieById should return movie when it exists")
    void testGetMovieById_MovieExists_ReturnsMovie() {
        // Given
        Movie expectedMovie = new Movie("Inception", "Dream heist", "Sci-Fi", 148, 12.50);
        expectedMovie.setId(1L);
        when(mockRepository.findById(1L)).thenReturn(expectedMovie);

        // When
        Movie actualMovie = movieService.getMovieById(1L);

        // Then
        assertNotNull(actualMovie);
        assertEquals("Inception", actualMovie.getTitle());
        verify(mockRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getMovieById should return null when movie doesn't exist")
    void testGetMovieById_MovieNotFound_ReturnsNull() {
        // Given
        when(mockRepository.findById(999L)).thenReturn(null);

        // When
        Movie actualMovie = movieService.getMovieById(999L);

        // Then
        assertNull(actualMovie);
        verify(mockRepository, times(1)).findById(999L);
    }

    // ==================== createMovie Tests ====================

    @Test
    @DisplayName("createMovie should save valid movie")
    void testCreateMovie_ValidMovie_SavesSuccessfully() {
        // Given
        Movie inputMovie = new Movie("Inception", "Dream heist", "Sci-Fi", 148, 12.50);
        Movie savedMovie = new Movie("Inception", "Dream heist", "Sci-Fi", 148, 12.50);
        savedMovie.setId(1L);

        when(mockRepository.save(any(Movie.class))).thenReturn(savedMovie);

        // When
        Movie result = movieService.createMovie(inputMovie);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Inception", result.getTitle());
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    @Test
    @DisplayName("createMovie should throw exception when movie is null")
    void testCreateMovie_NullMovie_ThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            movieService.createMovie(null);
        });

        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when title is empty")
    void testCreateMovie_EmptyTitle_ThrowsException() {
        // Given
        Movie movie = new Movie("", "Description", "Action", 120, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("title cannot be empty"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when title is too long")
    void testCreateMovie_TitleTooLong_ThrowsException() {
        // Given - title with 256 characters
        String longTitle = "A".repeat(256);
        Movie movie = new Movie(longTitle, "Description", "Action", 120, 10.0);

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
    @DisplayName("createMovie should throw exception when genre is empty")
    void testCreateMovie_EmptyGenre_ThrowsException() {
        // Given
        Movie movie = new Movie("Title", "Description", "", 120, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("genre cannot be empty"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when genre is too long")
    void testCreateMovie_GenreTooLong_ThrowsException() {
        // Given - genre with 101 characters
        String longGenre = "A".repeat(101);
        Movie movie = new Movie("Title", "Description", longGenre, 120, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("genre too long"));
        assertTrue(exception.getMessage().contains("101"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when description is too long")
    void testCreateMovie_DescriptionTooLong_ThrowsException() {
        // Given - description with 5001 characters
        String longDescription = "A".repeat(5001);
        Movie movie = new Movie("Title", longDescription, "Action", 120, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("description too long"));
        assertTrue(exception.getMessage().contains("5001"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when price is null")
    void testCreateMovie_NullPrice_ThrowsException() {
        // Given
        Movie movie = new Movie("Title", "Description", "Action", 120, null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("price must be positive"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when price is zero")
    void testCreateMovie_ZeroPrice_ThrowsException() {
        // Given
        Movie movie = new Movie("Title", "Description", "Action", 120, 0.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("price must be positive"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when price is negative")
    void testCreateMovie_NegativePrice_ThrowsException() {
        // Given
        Movie movie = new Movie("Title", "Description", "Action", 120, -10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("price must be positive"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when price exceeds maximum")
    void testCreateMovie_PriceTooHigh_ThrowsException() {
        // Given
        Movie movie = new Movie("Title", "Description", "Action", 120, 10001.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("price too high"));
        assertTrue(exception.getMessage().contains("10001"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when duration is null")
    void testCreateMovie_NullDuration_ThrowsException() {
        // Given
        Movie movie = new Movie("Title", "Description", "Action", null, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("duration must be positive"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when duration is zero")
    void testCreateMovie_ZeroDuration_ThrowsException() {
        // Given
        Movie movie = new Movie("Title", "Description", "Action", 0, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("duration must be positive"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should throw exception when duration exceeds maximum")
    void testCreateMovie_DurationTooLong_ThrowsException() {
        // Given
        Movie movie = new Movie("Title", "Description", "Action", 1001, 10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.createMovie(movie)
        );

        assertTrue(exception.getMessage().contains("duration too long"));
        assertTrue(exception.getMessage().contains("1001"));
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie should trim whitespace from title and genre")
    void testCreateMovie_TrimsWhitespace() {
        // Given
        Movie movie = new Movie("  Inception  ", "Description", "  Sci-Fi  ", 148, 12.50);
        Movie savedMovie = new Movie("Inception", "Description", "Sci-Fi", 148, 12.50);
        savedMovie.setId(1L);

        when(mockRepository.save(any(Movie.class))).thenReturn(savedMovie);

        // When
        Movie result = movieService.createMovie(movie);

        // Then
        assertEquals("Inception", result.getTitle());
        assertEquals("Sci-Fi", result.getGenre());
        verify(mockRepository, times(1)).save(any(Movie.class));
    }

    // ==================== updateMovie Tests ====================

    @Test
    @DisplayName("updateMovie should update existing movie")
    void testUpdateMovie_ValidMovie_UpdatesSuccessfully() {
        // Given
        Movie updateData = new Movie("Updated Title", "Updated desc", "Drama", 130, 15.0);
        updateData.setId(1L);

        Movie updatedMovie = new Movie("Updated Title", "Updated desc", "Drama", 130, 15.0);
        updatedMovie.setId(1L);

        when(mockRepository.update(any(Movie.class))).thenReturn(updatedMovie);

        // When
        Movie result = movieService.updateMovie(updateData);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Drama", result.getGenre());
        verify(mockRepository, times(1)).update(any(Movie.class));
    }

    @Test
    @DisplayName("updateMovie should return null when movie doesn't exist")
    void testUpdateMovie_MovieNotFound_ReturnsNull() {
        // Given
        Movie movie = new Movie("Title", "Description", "Action", 120, 10.0);
        movie.setId(999L);

        when(mockRepository.update(any(Movie.class))).thenReturn(null);

        // When
        Movie result = movieService.updateMovie(movie);

        // Then
        assertNull(result);
        verify(mockRepository, times(1)).update(any(Movie.class));
    }

    @Test
    @DisplayName("updateMovie should throw exception when movie ID is null")
    void testUpdateMovie_NullId_ThrowsException() {
        // Given
        Movie movie = new Movie("Title", "Description", "Action", 120, 10.0);
        // ID is null

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> movieService.updateMovie(movie)
        );

        assertTrue(exception.getMessage().contains("ID must be positive"));
        verify(mockRepository, never()).update(any());
    }

    @Test
    @DisplayName("updateMovie should validate all fields like createMovie")
    void testUpdateMovie_InvalidData_ThrowsException() {
        // Given - invalid price
        Movie movie = new Movie("Title", "Description", "Action", 120, -10.0);
        movie.setId(1L);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            movieService.updateMovie(movie);
        });

        verify(mockRepository, never()).update(any());
    }

    // ==================== deleteMovie Tests ====================

    @Test
    @DisplayName("deleteMovie should delete existing movie")
    void testDeleteMovie_MovieExists_DeletesSuccessfully() {
        // Given
        when(mockRepository.delete(1L)).thenReturn(true);

        // When
        boolean result = movieService.deleteMovie(1L);

        // Then
        assertTrue(result);
        verify(mockRepository, times(1)).delete(1L);
    }

    @Test
    @DisplayName("deleteMovie should return false when movie doesn't exist")
    void testDeleteMovie_MovieNotFound_ReturnsFalse() {
        // Given
        when(mockRepository.delete(999L)).thenReturn(false);

        // When
        boolean result = movieService.deleteMovie(999L);

        // Then
        assertFalse(result);
        verify(mockRepository, times(1)).delete(999L);
    }
}
