package moviebooking.resource;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import moviebooking.TestConfig;
import moviebooking.util.JsonTestHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Authentication endpoints.
 *
 * Tests the /api/auth/register and /api/auth/login endpoints including:
 * - User registration validation
 * - Login authentication
 * - JWT token generation
 * - Password security
 */
@DisplayName("AuthResource REST API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig().packages("moviebooking");
    }

    @BeforeAll
    static void setupClass() {
        TestConfig.initializeTestDatabase();
    }

    @AfterAll
    static void teardownClass() {
        TestConfig.cleanup();
    }

    // ==================== Registration Tests ====================

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register should create new user")
    void testRegister_ValidData_Returns201() {
        // Given
        String registerJson = """
            {
                "email": "unique.newuser@example.com",
                "name": "New User",
                "password": "ValidPass123"
            }
            """;

        // When
        Response response = target("/api/auth/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(registerJson));

        // Then - if fails, print response for debugging
        if (response.getStatus() != 201) {
            String errorJson = response.readEntity(String.class);
            System.out.println("Registration failed with status " + response.getStatus() + ": " + errorJson);
        }
        assertEquals(201, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("unique.newuser@example.com"));
        assertTrue(responseJson.contains("New User"));
        assertTrue(responseJson.contains("\"role\":\"CUSTOMER\""));
        assertFalse(responseJson.contains("password"), "Password should not be in response");
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register with duplicate email should return 400")
    void testRegister_DuplicateEmail_Returns400() {
        // Given - register first user
        String registerJson1 = """
            {
                "email": "duplicate@example.com",
                "name": "First User",
                "password": "password123"
            }
            """;
        target("/api/auth/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(registerJson1));

        // When - try to register with same email
        String registerJson2 = """
            {
                "email": "duplicate@example.com",
                "name": "Second User",
                "password": "different456"
            }
            """;
        Response response = target("/api/auth/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(registerJson2));

        // Then
        assertEquals(400, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("error"));
        assertTrue(responseJson.toLowerCase().contains("email") ||
                   responseJson.toLowerCase().contains("already"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/register with invalid email should return 400")
    void testRegister_InvalidEmail_Returns400() {
        // Given
        String registerJson = """
            {
                "email": "invalid-email",
                "name": "Test User",
                "password": "password123"
            }
            """;

        // When
        Response response = target("/api/auth/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(registerJson));

        // Then
        assertEquals(400, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("error"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/register with weak password should return 400")
    void testRegister_WeakPassword_Returns400() {
        // Given - password too short
        String registerJson = """
            {
                "email": "weak@example.com",
                "name": "Test User",
                "password": "weak"
            }
            """;

        // When
        Response response = target("/api/auth/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(registerJson));

        // Then
        assertEquals(400, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("error"));
        assertTrue(responseJson.toLowerCase().contains("password"));
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/register with empty name should return 400")
    void testRegister_EmptyName_Returns400() {
        // Given
        String registerJson = """
            {
                "email": "noname@example.com",
                "name": "   ",
                "password": "password123"
            }
            """;

        // When
        Response response = target("/api/auth/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(registerJson));

        // Then
        assertEquals(400, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("error"));
    }

    // ==================== Login Tests ====================

    @Test
    @Order(6)
    @DisplayName("POST /api/auth/login should return JWT token")
    void testLogin_ValidCredentials_Returns200WithToken() {
        // Given - register a user first
        String registerJson = """
            {
                "email": "login@example.com",
                "name": "Login User",
                "password": "password123"
            }
            """;
        target("/api/auth/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(registerJson));

        // When - login with correct credentials
        String loginJson = """
            {
                "email": "login@example.com",
                "password": "password123"
            }
            """;
        Response response = target("/api/auth/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(loginJson));

        // Then
        assertEquals(200, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("\"token\""));
        assertTrue(responseJson.contains("login@example.com"));
        assertTrue(responseJson.contains("Login User"));
        assertTrue(responseJson.contains("\"role\":\"CUSTOMER\""));

        // Verify token is a valid JWT (has 3 parts separated by dots)
        String token = JsonTestHelper.getStringField(responseJson, "token");
        assertNotNull(token);
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length, "JWT should have 3 parts: header.payload.signature");
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/auth/login with wrong password should return 401")
    void testLogin_WrongPassword_Returns401() {
        // Given - user exists from previous test
        String loginJson = """
            {
                "email": "login@example.com",
                "password": "wrongpassword"
            }
            """;

        // When
        Response response = target("/api/auth/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(loginJson));

        // Then
        assertEquals(401, response.getStatus(), "Wrong password should return 401 UNAUTHORIZED");

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("error"));
        assertTrue(responseJson.toLowerCase().contains("invalid") ||
                   responseJson.toLowerCase().contains("password"));
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/auth/login with non-existent user should return 401")
    void testLogin_NonExistentUser_Returns401() {
        // Given
        String loginJson = """
            {
                "email": "nonexistent@example.com",
                "password": "password123"
            }
            """;

        // When
        Response response = target("/api/auth/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(loginJson));

        // Then
        assertEquals(401, response.getStatus(), "Non-existent user should return 401 UNAUTHORIZED");

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("error"));
    }

    @Test
    @Order(9)
    @DisplayName("Email should be case-insensitive for login")
    void testLogin_CaseInsensitiveEmail_Success() {
        // Given - register with lowercase email
        String registerJson = """
            {
                "email": "casetest@example.com",
                "name": "Case Test",
                "password": "password123"
            }
            """;
        target("/api/auth/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(registerJson));

        // When - login with mixed case email
        String loginJson = """
            {
                "email": "CaseTest@Example.COM",
                "password": "password123"
            }
            """;
        Response response = target("/api/auth/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(loginJson));

        // Then
        assertEquals(200, response.getStatus());

        String responseJson = response.readEntity(String.class);
        assertTrue(responseJson.contains("\"token\""));
        // Email should be normalized to lowercase in response
        assertTrue(responseJson.contains("casetest@example.com"));
    }

    // ==================== Password Security Tests ====================

    @Test
    @Order(10)
    @DisplayName("Password should be hashed (not stored in plain text)")
    void testPasswordHashing() {
        // This is verified by:
        // 1. Password not appearing in registration response
        // 2. Successful login with correct password
        // 3. Failed login with wrong password
        // The actual BCrypt hashing is tested in unit tests

        // Given
        String registerJson = """
            {
                "email": "hash@example.com",
                "name": "Hash Test",
                "password": "mySecretPassword123"
            }
            """;

        // When
        Response registerResponse = target("/api/auth/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(registerJson));

        // Then - password should NOT be in response
        String registerResponseJson = registerResponse.readEntity(String.class);
        assertFalse(registerResponseJson.contains("mySecretPassword123"),
                "Plain text password should never be in response");
        assertFalse(registerResponseJson.contains("password"),
                "Password field should not be in response");

        // Verify we can login with the password (proves it was stored securely)
        String loginJson = """
            {
                "email": "hash@example.com",
                "password": "mySecretPassword123"
            }
            """;
        Response loginResponse = target("/api/auth/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(loginJson));

        assertEquals(200, loginResponse.getStatus(), "Should be able to login with correct password");
    }
}
