package moviebooking.util;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.test.JerseyTest;

/**
 * Helper class for authentication in integration tests.
 *
 * Provides methods to register users and obtain JWT tokens for testing
 * authenticated endpoints.
 */
public class AuthTestHelper {

    private final JerseyTest jerseyTest;

    public AuthTestHelper(JerseyTest jerseyTest) {
        this.jerseyTest = jerseyTest;
    }

    /**
     * Register and login a CUSTOMER user, returning the JWT token.
     *
     * @return JWT token for authenticated CUSTOMER user
     */
    public String getCustomerToken() {
        return getCustomerToken("test.customer@example.com", "Test Customer", "password123");
    }

    /**
     * Register and login a CUSTOMER user with custom details.
     *
     * @param email User email
     * @param name User name
     * @param password User password
     * @return JWT token for authenticated CUSTOMER user
     */
    public String getCustomerToken(String email, String name, String password) {
        // Register user (will fail silently if already exists)
        registerUser(email, name, password);

        // Login and extract token
        return login(email, password);
    }

    /**
     * Register and login an ADMIN user, returning the JWT token.
     *
     * @return JWT token for authenticated ADMIN user
     */
    public String getAdminToken() {
        return getAdminToken("test.admin@example.com", "Test Admin", "admin123");
    }

    /**
     * Register and login an ADMIN user with custom details.
     *
     * Note: This requires direct service access since registration endpoint
     * only creates CUSTOMER users. In production, admin users would be
     * created via a separate administrative process.
     *
     * @param email Admin email
     * @param name Admin name
     * @param password Admin password
     * @return JWT token for authenticated ADMIN user
     */
    public String getAdminToken(String email, String name, String password) {
        // Create admin user directly via service (bypass REST API)
        try {
            moviebooking.TestConfig.getAuthService().register(
                email, name, password, moviebooking.model.Role.ADMIN
            );
        } catch (IllegalArgumentException e) {
            // User already exists - that's fine
        }

        // Login and extract token
        return login(email, password);
    }

    /**
     * Register a new CUSTOMER user via REST API.
     *
     * @param email User email
     * @param name User name
     * @param password User password
     * @return true if registration succeeded, false if user already exists
     */
    public boolean registerUser(String email, String name, String password) {
        String registerJson = String.format("""
            {
                "email": "%s",
                "name": "%s",
                "password": "%s"
            }
            """, email, name, password);

        Response response = jerseyTest.target("/api/auth/register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(registerJson));

        int status = response.getStatus();
        response.close();

        // 201 = created successfully, 400 = already exists
        return status == 201;
    }

    /**
     * Login a user and extract the JWT token.
     *
     * @param email User email
     * @param password User password
     * @return JWT token
     * @throws RuntimeException if login fails
     */
    public String login(String email, String password) {
        String loginJson = String.format("""
            {
                "email": "%s",
                "password": "%s"
            }
            """, email, password);

        Response response = jerseyTest.target("/api/auth/login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(loginJson));

        if (response.getStatus() != 200) {
            throw new RuntimeException("Login failed with status: " + response.getStatus());
        }

        String responseBody = response.readEntity(String.class);
        response.close();

        // Extract token from JSON response: {"token":"...", "email":"...", ...}
        return JsonTestHelper.getStringField(responseBody, "token");
    }

    /**
     * Create an Authorization header value with Bearer token.
     *
     * @param token JWT token
     * @return Authorization header value (e.g., "Bearer eyJ...")
     */
    public static String bearerToken(String token) {
        return "Bearer " + token;
    }
}
