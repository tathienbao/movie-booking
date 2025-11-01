package moviebooking.resource;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import moviebooking.model.Movie;
import moviebooking.TestConfig;
import moviebooking.util.JsonTestHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * REST API Integration Tests for MovieResource.
 *
 * Uses Jersey Test Framework to test actual HTTP requests/responses.
 * Tests run against a real (but test) server instance.
 *
 * Note: These tests use the H2 database initialized via TestConfig.
 */
@DisplayName("MovieResource REST API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MovieResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        // Configure Jersey to scan for REST resources
        return new ResourceConfig().packages("moviebooking");
    }

    @BeforeAll
    static void setupClass() {
        // Initialize test database using TestConfig helper
        TestConfig.initializeTestDatabase();
    }

    @AfterAll
    static void teardownClass() {
        // Cleanup test resources
        TestConfig.cleanup();
    }

    // ==================== GET /api/movies Tests ====================

    @Test
    @Order(1)
    @DisplayName("GET /api/movies should return 200 OK")
    void testGetAllMovies_ReturnsOK() {
        // When
        Response response = target("/api/movies").request().get();

        // Then
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString("Content-Type"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/movies should return JSON array")
    void testGetAllMovies_ReturnsJsonArray() {
        // When
        String json = target("/api/movies")
            .request(MediaType.APPLICATION_JSON)
            .get(String.class);

        // Then
        assertNotNull(json);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
    }

    // ==================== GET /api/movies/{id} Tests ====================

    @Test
    @Order(3)
    @DisplayName("GET /api/movies/{id} should return specific movie")
    void testGetMovieById_ValidId_ReturnsMovie() {
        // First, get all movies to find a valid ID
        Response allMoviesResponse = target("/api/movies").request().get();
        String allMoviesJson = allMoviesResponse.readEntity(String.class);

        // Skip test if no movies exist
        Long movieId = JsonTestHelper.extractFirstMovieId(allMoviesJson);
        if (movieId == null) {
            System.out.println("Skipping test - no movies in database");
            return;
        }

        // When
        Response response = target("/api/movies/" + movieId)
            .request(MediaType.APPLICATION_JSON)
            .get();

        // Then
        assertEquals(200, response.getStatus());

        String json = response.readEntity(String.class);
        Long returnedId = JsonTestHelper.extractMovieId(json);
        assertEquals(movieId, returnedId);
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/movies/999 should return 404 Not Found")
    void testGetMovieById_InvalidId_Returns404() {
        // When
        Response response = target("/api/movies/999")
            .request(MediaType.APPLICATION_JSON)
            .get();

        // Then
        assertEquals(404, response.getStatus());

        String json = response.readEntity(String.class);
        assertTrue(json.contains("error") || json.contains("not found"));
    }

    // ==================== POST /api/movies Tests ====================

    @Test
    @Order(5)
    @DisplayName("POST /api/movies should create new movie")
    void testCreateMovie_ValidData_Returns201() {
        // Given
        String movieJson = """
            {
                "title": "Test Movie",
                "description": "A test movie for API testing",
                "genre": "Action",
                "durationMinutes": 120,
                "price": 15.0
            }
            """;

        // When
        Response response = target("/api/movies")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(movieJson));

        // Then
        assertEquals(201, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("Test Movie"));
        assertTrue(responseJson.contains("\"id\""));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/movies with invalid data should return error")
    void testCreateMovie_InvalidData_ReturnsError() {
        // Given - missing required fields
        String invalidJson = """
            {
                "title": "",
                "genre": "Action"
            }
            """;

        // When
        Response response = target("/api/movies")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(invalidJson));

        // Then
        // Should be 400 (Bad Request) or 500 (Internal Server Error from validation)
        int status = response.getStatus();
        assertTrue(status == 400 || status == 500,
            "Expected 400 or 500, got " + status);
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/movies with title too long should return 400 or 500")
    void testCreateMovie_TitleTooLong_ReturnsError() {
        // Given - title exceeds 255 characters
        String longTitle = "A".repeat(300);
        String movieJson = String.format("""
            {
                "title": "%s",
                "description": "Test",
                "genre": "Action",
                "durationMinutes": 120,
                "price": 15.0
            }
            """, longTitle);

        // When
        Response response = target("/api/movies")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(movieJson));

        // Then - validation should fail
        int status = response.getStatus();
        assertTrue(status == 400 || status == 500,
            "Expected 400 or 500 for title too long, got " + status);
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/movies with price too high should return error")
    void testCreateMovie_PriceTooHigh_ReturnsError() {
        // Given - price exceeds $10,000
        String movieJson = """
            {
                "title": "Expensive Movie",
                "description": "Too expensive",
                "genre": "Action",
                "durationMinutes": 120,
                "price": 50000.0
            }
            """;

        // When
        Response response = target("/api/movies")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(movieJson));

        // Then
        assertTrue(response.getStatus() >= 400);
    }

    // ==================== PUT /api/movies/{id} Tests ====================

    @Test
    @Order(9)
    @DisplayName("PUT /api/movies/{id} should update existing movie")
    void testUpdateMovie_ValidData_Returns200() {
        // First, get all movies to find a valid ID
        Response allMoviesResponse = target("/api/movies").request().get();
        String allMoviesJson = allMoviesResponse.readEntity(String.class);

        // Skip test if no movies exist
        Long movieId = JsonTestHelper.extractFirstMovieId(allMoviesJson);
        if (movieId == null) {
            System.out.println("Skipping test - no movies in database");
            return;
        }

        // Given
        String movieJson = """
            {
                "title": "Updated Movie",
                "description": "Updated description",
                "genre": "Drama",
                "durationMinutes": 130,
                "price": 16.0
            }
            """;

        // When
        Response response = target("/api/movies/" + movieId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(movieJson));

        // Then
        assertEquals(200, response.getStatus());

        String responseJson = response.readEntity(String.class);
        String title = JsonTestHelper.getStringField(responseJson, "title");
        assertEquals("Updated Movie", title);
    }

    @Test
    @Order(10)
    @DisplayName("PUT /api/movies/999 should return 404 Not Found")
    void testUpdateMovie_NonExistentId_Returns404() {
        // Given
        String movieJson = """
            {
                "title": "Updated Movie",
                "genre": "Drama",
                "durationMinutes": 130,
                "price": 16.0
            }
            """;

        // When
        Response response = target("/api/movies/999")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(movieJson));

        // Then
        assertEquals(404, response.getStatus());
    }

    // ==================== DELETE /api/movies/{id} Tests ====================

    @Test
    @Order(11)
    @DisplayName("DELETE /api/movies/{id} should delete movie")
    void testDeleteMovie_ValidId_Returns204() {
        // First, create a movie to delete
        String movieJson = """
            {
                "title": "Movie To Delete",
                "genre": "Test",
                "durationMinutes": 100,
                "price": 10.0
            }
            """;

        Response createResponse = target("/api/movies")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(movieJson));

        String createdJson = createResponse.readEntity(String.class);
        Long movieId = JsonTestHelper.extractMovieId(createdJson);
        assertNotNull(movieId, "Created movie should have an ID");

        // When - delete the movie we just created
        Response response = target("/api/movies/" + movieId)
            .request()
            .delete();

        // Then
        assertEquals(204, response.getStatus());
    }

    @Test
    @Order(12)
    @DisplayName("DELETE /api/movies/999 should return 404 Not Found")
    void testDeleteMovie_NonExistentId_Returns404() {
        // When
        Response response = target("/api/movies/999")
            .request()
            .delete();

        // Then
        assertEquals(404, response.getStatus());
    }

    // ==================== Content-Type Tests ====================

    @Test
    @Order(13)
    @DisplayName("Endpoint should accept application/json")
    void testContentType_ApplicationJson_Accepted() {
        // When
        Response response = target("/api/movies")
            .request(MediaType.APPLICATION_JSON)
            .get();

        // Then
        assertEquals(200, response.getStatus());
    }

    @Test
    @Order(14)
    @DisplayName("Response should have correct Content-Type header")
    void testResponse_HasCorrectContentType() {
        // When
        Response response = target("/api/movies")
            .request()
            .get();

        // Then
        String contentType = response.getHeaderString("Content-Type");
        assertTrue(contentType.contains("application/json"));
    }
}
