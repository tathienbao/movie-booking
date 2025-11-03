package moviebooking.resource;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import moviebooking.TestConfig;
import moviebooking.util.AuthTestHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Role-Based Access Control (RBAC).
 *
 * Tests authorization rules:
 * - CUSTOMER users can view movies and create bookings
 * - CUSTOMER users CANNOT create/update/delete movies
 * - ADMIN users can perform all movie CRUD operations
 * - Unauthenticated users can only view movies
 */
@DisplayName("RBAC Authorization Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RbacAuthorizationTest extends JerseyTest {

    private AuthTestHelper authHelper;
    private String customerToken;
    private String adminToken;

    @Override
    protected Application configure() {
        return new ResourceConfig().packages("moviebooking");
    }

    @BeforeAll
    static void setupClass() {
        TestConfig.initializeTestDatabase();
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        authHelper = new AuthTestHelper(this);
        customerToken = authHelper.getCustomerToken("customer@rbac.com", "Customer User", "password123");
        adminToken = authHelper.getAdminToken("admin@rbac.com", "Admin User", "admin123");
    }

    @AfterAll
    static void teardownClass() {
        TestConfig.cleanup();
    }

    // ==================== Public Access Tests ====================

    @Test
    @Order(1)
    @DisplayName("Unauthenticated users CAN view movies (GET /api/movies)")
    void testPublicAccess_ViewMovies_Allowed() {
        // When - access without token
        Response response = target("/api/movies")
                .request()
                .get();

        // Then
        assertEquals(200, response.getStatus(), "Public should be able to view movies");
    }

    @Test
    @Order(2)
    @DisplayName("Unauthenticated users CANNOT create bookings (POST /api/bookings)")
    void testPublicAccess_CreateBooking_Denied() {
        // Given
        String bookingJson = """
            {
                "movieId": 1,
                "numberOfSeats": 2
            }
            """;

        // When - try to create booking without token
        Response response = target("/api/bookings")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(bookingJson));

        // Then
        assertEquals(401, response.getStatus(), "Public should NOT be able to create bookings");
    }

    // ==================== CUSTOMER Role Tests ====================

    @Test
    @Order(3)
    @DisplayName("CUSTOMER CAN view movies (GET /api/movies)")
    void testCustomerRole_ViewMovies_Allowed() {
        // When
        Response response = target("/api/movies")
                .request()
                .header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .get();

        // Then
        assertEquals(200, response.getStatus());
    }

    @Test
    @Order(4)
    @DisplayName("CUSTOMER CAN create bookings (POST /api/bookings)")
    void testCustomerRole_CreateBooking_Allowed() {
        // Given
        String bookingJson = """
            {
                "movieId": 1,
                "numberOfSeats": 2
            }
            """;

        // When
        Response response = target("/api/bookings")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .post(Entity.json(bookingJson));

        // Then
        assertEquals(201, response.getStatus(), "CUSTOMER should be able to create bookings");
    }

    @Test
    @Order(5)
    @DisplayName("CUSTOMER CANNOT create movies (POST /api/movies)")
    void testCustomerRole_CreateMovie_Denied() {
        // Given
        String movieJson = """
            {
                "title": "Unauthorized Movie",
                "description": "Should fail",
                "genre": "Action",
                "durationMinutes": 120,
                "price": 15.0
            }
            """;

        // When
        Response response = target("/api/movies")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .post(Entity.json(movieJson));

        // Then
        assertEquals(403, response.getStatus(), "CUSTOMER should NOT be able to create movies");

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("error"));
        assertTrue(responseJson.toLowerCase().contains("admin") ||
                   responseJson.toLowerCase().contains("forbidden"));
    }

    @Test
    @Order(6)
    @DisplayName("CUSTOMER CANNOT update movies (PUT /api/movies/{id})")
    void testCustomerRole_UpdateMovie_Denied() {
        // Given
        String movieJson = """
            {
                "title": "Updated Movie",
                "description": "Should fail",
                "genre": "Drama",
                "durationMinutes": 90,
                "price": 12.0
            }
            """;

        // When
        Response response = target("/api/movies/1")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .put(Entity.json(movieJson));

        // Then
        assertEquals(403, response.getStatus(), "CUSTOMER should NOT be able to update movies");
    }

    @Test
    @Order(7)
    @DisplayName("CUSTOMER CANNOT delete movies (DELETE /api/movies/{id})")
    void testCustomerRole_DeleteMovie_Denied() {
        // When
        Response response = target("/api/movies/1")
                .request()
                .header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .delete();

        // Then
        assertEquals(403, response.getStatus(), "CUSTOMER should NOT be able to delete movies");
    }

    // ==================== ADMIN Role Tests ====================

    @Test
    @Order(8)
    @DisplayName("ADMIN CAN view movies (GET /api/movies)")
    void testAdminRole_ViewMovies_Allowed() {
        // When
        Response response = target("/api/movies")
                .request()
                .header("Authorization", AuthTestHelper.bearerToken(adminToken))
                .get();

        // Then
        assertEquals(200, response.getStatus());
    }

    @Test
    @Order(9)
    @DisplayName("ADMIN CAN create movies (POST /api/movies)")
    void testAdminRole_CreateMovie_Allowed() {
        // Given
        String movieJson = """
            {
                "title": "Admin Created Movie",
                "description": "Created by admin",
                "genre": "Sci-Fi",
                "durationMinutes": 150,
                "price": 18.0
            }
            """;

        // When
        Response response = target("/api/movies")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", AuthTestHelper.bearerToken(adminToken))
                .post(Entity.json(movieJson));

        // Then
        assertEquals(201, response.getStatus(), "ADMIN should be able to create movies");

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("Admin Created Movie"));
    }

    @Test
    @Order(10)
    @DisplayName("ADMIN CAN update movies (PUT /api/movies/{id})")
    void testAdminRole_UpdateMovie_Allowed() {
        // Given - create a movie first
        String createJson = """
            {
                "title": "To Be Updated",
                "description": "Original description",
                "genre": "Drama",
                "durationMinutes": 100,
                "price": 10.0
            }
            """;

        Response createResponse = target("/api/movies")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", AuthTestHelper.bearerToken(adminToken))
                .post(Entity.json(createJson));

        assertEquals(201, createResponse.getStatus());

        // Get the movie ID
        String createdJson = createResponse.readEntity(String.class);
        Long movieId = moviebooking.util.JsonTestHelper.extractMovieId(createdJson);

        // When - update the movie
        String updateJson = """
            {
                "title": "Updated Title",
                "description": "Updated description",
                "genre": "Comedy",
                "durationMinutes": 110,
                "price": 12.0
            }
            """;

        Response updateResponse = target("/api/movies/" + movieId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", AuthTestHelper.bearerToken(adminToken))
                .put(Entity.json(updateJson));

        // Then
        assertEquals(200, updateResponse.getStatus(), "ADMIN should be able to update movies");

        String updatedJson = updateResponse.readEntity(String.class);
        assertTrue(updatedJson.contains("Updated Title"));
        assertTrue(updatedJson.contains("Updated description"));
    }

    @Test
    @Order(11)
    @DisplayName("ADMIN CAN delete movies (DELETE /api/movies/{id})")
    void testAdminRole_DeleteMovie_Allowed() {
        // Given - create a movie to delete
        String createJson = """
            {
                "title": "To Be Deleted",
                "description": "Will be deleted",
                "genre": "Horror",
                "durationMinutes": 90,
                "price": 11.0
            }
            """;

        Response createResponse = target("/api/movies")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", AuthTestHelper.bearerToken(adminToken))
                .post(Entity.json(createJson));

        String createdJson = createResponse.readEntity(String.class);
        Long movieId = moviebooking.util.JsonTestHelper.extractMovieId(createdJson);

        // When - delete the movie
        Response deleteResponse = target("/api/movies/" + movieId)
                .request()
                .header("Authorization", AuthTestHelper.bearerToken(adminToken))
                .delete();

        // Then
        assertEquals(204, deleteResponse.getStatus(), "ADMIN should be able to delete movies");

        // Verify movie is deleted
        Response getResponse = target("/api/movies/" + movieId)
                .request()
                .get();
        assertEquals(404, getResponse.getStatus(), "Deleted movie should not be found");
    }

    // ==================== Token Validation Tests ====================

    @Test
    @Order(12)
    @DisplayName("Invalid token should return 401 Unauthorized")
    void testInvalidToken_Returns401() {
        // When - use invalid token
        Response response = target("/api/bookings")
                .request()
                .header("Authorization", "Bearer invalid.token.here")
                .get();

        // Then
        assertEquals(401, response.getStatus());
    }

    @Test
    @Order(13)
    @DisplayName("Missing Authorization header should return 401 Unauthorized")
    void testMissingAuthHeader_Returns401() {
        // When - no auth header
        Response response = target("/api/bookings")
                .request()
                .get();

        // Then
        assertEquals(401, response.getStatus());
    }

    @Test
    @Order(14)
    @DisplayName("Malformed Authorization header should return 401 Unauthorized")
    void testMalformedAuthHeader_Returns401() {
        // When - malformed header (missing 'Bearer' prefix)
        Response response = target("/api/bookings")
                .request()
                .header("Authorization", "just-a-token-without-bearer")
                .get();

        // Then
        assertEquals(401, response.getStatus());
    }
}
