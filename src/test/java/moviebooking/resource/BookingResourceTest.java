package moviebooking.resource;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import moviebooking.TestConfig;
import moviebooking.util.AuthTestHelper;
import moviebooking.util.JsonTestHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * REST API Integration Tests for BookingResource.
 *
 * Uses Jersey Test Framework to test actual HTTP requests/responses.
 * Tests run against a real (but test) server instance.
 *
 * Note: These tests use the H2 database initialized via TestConfig.
 */
@DisplayName("BookingResource REST API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingResourceTest extends JerseyTest {

    private AuthTestHelper authHelper;
    private String customerToken;

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

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Initialize auth helper and get customer token
        authHelper = new AuthTestHelper(this);
        customerToken = authHelper.getCustomerToken();
    }

    @AfterAll
    static void teardownClass() {
        // Cleanup test resources
        TestConfig.cleanup();
    }

    // ==================== GET /api/bookings Tests ====================

    @Test
    @Order(1)
    @DisplayName("GET /api/bookings should return 200 OK")
    void testGetAllBookings_ReturnsOK() {
        // When
        Response response = target("/api/bookings")
                .request().header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .get();

        // Then
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString("Content-Type"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/bookings should return JSON array")
    void testGetAllBookings_ReturnsJsonArray() {
        // When
        String json = target("/api/bookings")
                .request(MediaType.APPLICATION_JSON).header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .get(String.class);

        // Then
        assertNotNull(json);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
    }

    // ==================== POST /api/bookings Tests ====================

    @Test
    @Order(3)
    @DisplayName("POST /api/bookings should create new booking")
    void testCreateBooking_ValidData_Returns201() {
        // First, get a valid movie ID
        Response moviesResponse = target("/api/movies").request().get();
        String moviesJson = moviesResponse.readEntity(String.class);
        Long movieId = JsonTestHelper.extractFirstMovieId(moviesJson);

        // Skip test if no movies exist
        if (movieId == null) {
            System.out.println("Skipping test - no movies in database");
            return;
        }

        // Given
        String bookingJson = String.format("""
            {
                "movieId": %d,
                "numberOfSeats": 2
            }
            """, movieId);

        // When
        Response response = target("/api/bookings")
                .request(MediaType.APPLICATION_JSON).header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .post(Entity.json(bookingJson));

        // Then
        assertEquals(201, response.getStatus());

        String responseJson = response.readEntity(String.class);
        Long bookingId = JsonTestHelper.extractBookingId(responseJson);
        assertNotNull(bookingId, "Created booking should have an ID");
        assertTrue(responseJson.contains("Test Customer"));  // Name from auth token
        assertTrue(responseJson.contains("test.customer@example.com"));  // Email from auth token
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/bookings with invalid movie ID should return 400")
    void testCreateBooking_InvalidMovieId_Returns400() {
        // Given - non-existent movie ID
        String bookingJson = """
            {
                "movieId": 999,
                "numberOfSeats": 2
            }
            """;

        // When
        Response response = target("/api/bookings")
                .request(MediaType.APPLICATION_JSON).header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .post(Entity.json(bookingJson));

        // Then
        assertEquals(400, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("error") || responseJson.contains("not found"));
    }

    // Test removed: Email validation now happens at user registration, not booking creation

    @Test
    @Order(6)
    @DisplayName("POST /api/bookings with zero seats should return 400")
    void testCreateBooking_ZeroSeats_Returns400() {
        // First, get a valid movie ID
        Response moviesResponse = target("/api/movies").request().get();
        String moviesJson = moviesResponse.readEntity(String.class);
        Long movieId = JsonTestHelper.extractFirstMovieId(moviesJson);

        // Skip test if no movies exist
        if (movieId == null) {
            System.out.println("Skipping test - no movies in database");
            return;
        }

        // Given - zero seats
        String bookingJson = String.format("""
            {
                "movieId": %d,
                "numberOfSeats": 0
            }
            """, movieId);

        // When
        Response response = target("/api/bookings")
                .request(MediaType.APPLICATION_JSON).header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .post(Entity.json(bookingJson));

        // Then
        assertEquals(400, response.getStatus());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/bookings with too many seats should return 400")
    void testCreateBooking_TooManySeats_Returns400() {
        // First, get a valid movie ID
        Response moviesResponse = target("/api/movies").request().get();
        String moviesJson = moviesResponse.readEntity(String.class);
        Long movieId = JsonTestHelper.extractFirstMovieId(moviesJson);

        // Skip test if no movies exist
        if (movieId == null) {
            System.out.println("Skipping test - no movies in database");
            return;
        }

        // Given - 101 seats (exceeds max of 100)
        String bookingJson = String.format("""
            {
                "movieId": %d,
                "numberOfSeats": 101
            }
            """, movieId);

        // When
        Response response = target("/api/bookings")
                .request(MediaType.APPLICATION_JSON).header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .post(Entity.json(bookingJson));

        // Then
        assertEquals(400, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("error"));
    }

    // Test removed: Name validation now happens at user registration, not booking creation

    // ==================== GET /api/bookings/{id} Tests ====================

    @Test
    @Order(9)
    @DisplayName("GET /api/bookings/{id} should return specific booking")
    void testGetBookingById_ValidId_ReturnsBooking() {
        // First, create a booking
        Response moviesResponse = target("/api/movies").request().get();
        String moviesJson = moviesResponse.readEntity(String.class);
        Long movieId = JsonTestHelper.extractFirstMovieId(moviesJson);

        // Skip test if no movies exist
        if (movieId == null) {
            System.out.println("Skipping test - no movies in database");
            return;
        }

        String bookingJson = String.format("""
            {
                "movieId": %d,
                "numberOfSeats": 3
            }
            """, movieId);

        Response createResponse = target("/api/bookings")
                .request(MediaType.APPLICATION_JSON).header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .post(Entity.json(bookingJson));

        String createdJson = createResponse.readEntity(String.class);
        Long bookingId = JsonTestHelper.extractBookingId(createdJson);
        assertNotNull(bookingId, "Created booking should have an ID");

        // When - get the booking we just created
        Response response = target("/api/bookings/" + bookingId)
                .request().header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .get();

        // Then
        assertEquals(200, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("Test Customer"));  // Name from auth token
        assertTrue(responseJson.contains("test.customer@example.com"));  // Email from auth token
    }

    @Test
    @Order(10)
    @DisplayName("GET /api/bookings/999 should return 404 Not Found")
    void testGetBookingById_InvalidId_Returns404() {
        // When
        Response response = target("/api/bookings/999")
                .request().header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .get();

        // Then
        assertEquals(404, response.getStatus());

        String json = response.readEntity(String.class);
        assertTrue(json.contains("error") || json.contains("not found"));
    }

    // ==================== GET /api/bookings/movies/{movieId} Tests ====================

    @Test
    @Order(11)
    @DisplayName("GET /api/bookings/movies/{movieId} should return bookings for a movie")
    void testGetBookingsByMovieId_ValidId_ReturnsBookings() {
        // First, get a valid movie ID
        Response moviesResponse = target("/api/movies").request().get();
        String moviesJson = moviesResponse.readEntity(String.class);
        Long movieId = JsonTestHelper.extractFirstMovieId(moviesJson);

        // Skip test if no movies exist
        if (movieId == null) {
            System.out.println("Skipping test - no movies in database");
            return;
        }

        // When
        Response response = target("/api/bookings/movies/" + movieId)
                .request(MediaType.APPLICATION_JSON).header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .get();

        // Then
        assertEquals(200, response.getStatus());

        String json = response.readEntity(String.class);
        assertNotNull(json);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
    }

    // ==================== DELETE /api/bookings/{id} Tests ====================

    @Test
    @Order(12)
    @DisplayName("DELETE /api/bookings/{id} should delete booking")
    void testDeleteBooking_ValidId_Returns204() {
        // First, create a booking to delete
        Response moviesResponse = target("/api/movies").request().get();
        String moviesJson = moviesResponse.readEntity(String.class);
        Long movieId = JsonTestHelper.extractFirstMovieId(moviesJson);

        // Skip test if no movies exist
        if (movieId == null) {
            System.out.println("Skipping test - no movies in database");
            return;
        }

        String bookingJson = String.format("""
            {
                "movieId": %d,
                "numberOfSeats": 1
            }
            """, movieId);

        Response createResponse = target("/api/bookings")
                .request(MediaType.APPLICATION_JSON).header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .post(Entity.json(bookingJson));

        String createdJson = createResponse.readEntity(String.class);
        Long bookingId = JsonTestHelper.extractBookingId(createdJson);
        assertNotNull(bookingId, "Created booking should have an ID");

        // When - delete the booking we just created
        Response response = target("/api/bookings/" + bookingId)
                .request().header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .delete();

        // Then
        assertEquals(204, response.getStatus());

        // Verify booking is actually deleted
        Response verifyResponse = target("/api/bookings/" + bookingId)
                .request().header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .get();
        assertEquals(404, verifyResponse.getStatus());
    }

    @Test
    @Order(13)
    @DisplayName("DELETE /api/bookings/999 should return 404 Not Found")
    void testDeleteBooking_NonExistentId_Returns404() {
        // When
        Response response = target("/api/bookings/999")
                .request().header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .delete();

        // Then
        assertEquals(404, response.getStatus());
    }

    // ==================== Content-Type Tests ====================

    @Test
    @Order(14)
    @DisplayName("Endpoint should accept application/json")
    void testContentType_ApplicationJson_Accepted() {
        // When
        Response response = target("/api/bookings")
                .request(MediaType.APPLICATION_JSON).header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .get();

        // Then
        assertEquals(200, response.getStatus());
    }

    @Test
    @Order(15)
    @DisplayName("Response should have correct Content-Type header")
    void testResponse_HasCorrectContentType() {
        // When
        Response response = target("/api/bookings")
                .request().header("Authorization", AuthTestHelper.bearerToken(customerToken))
                .get();

        // Then
        String contentType = response.getHeaderString("Content-Type");
        assertTrue(contentType.contains("application/json"));
    }
}
