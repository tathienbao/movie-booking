package moviebooking.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import moviebooking.App;
import moviebooking.model.Role;
import moviebooking.model.User;
import moviebooking.service.AuthService;

/**
 * REST API endpoints for authentication.
 *
 * Endpoints:
 * - POST /api/auth/register - Register new user
 * - POST /api/auth/login - Login and get JWT token
 * - GET /api/auth/me - Get current user info (requires authentication)
 */
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private volatile AuthService authService;

    /**
     * Get AuthService instance with lazy initialization.
     * Uses double-checked locking pattern for thread safety.
     */
    private AuthService getService() {
        if (authService == null) {
            synchronized (this) {
                if (authService == null) {
                    authService = App.getAuthService();
                    if (authService == null) {
                        throw new IllegalStateException("AuthService not initialized");
                    }
                }
            }
        }
        return authService;
    }

    /**
     * POST /api/auth/register - Register new user
     *
     * Request body:
     * {
     *   "email": "user@example.com",
     *   "name": "John Doe",
     *   "password": "password123"
     * }
     *
     * Response:
     * {
     *   "id": 1,
     *   "email": "user@example.com",
     *   "name": "John Doe",
     *   "role": "CUSTOMER",
     *   "createdAt": "2025-11-02T12:00:00"
     * }
     */
    @POST
    @Path("/register")
    public Response register(RegisterRequest request) {
        try {
            // Default role is CUSTOMER
            Role role = Role.CUSTOMER;

            User user = getService().register(
                    request.getEmail(),
                    request.getName(),
                    request.getPassword(),
                    role
            );

            return Response.status(Response.Status.CREATED)
                    .entity(user)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * POST /api/auth/login - Login and get JWT token
     *
     * Request body:
     * {
     *   "email": "user@example.com",
     *   "password": "password123"
     * }
     *
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "email": "user@example.com",
     *   "name": "John Doe",
     *   "role": "CUSTOMER"
     * }
     */
    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            String token = getService().login(request.getEmail(), request.getPassword());

            // Extract user info from service for response
            io.jsonwebtoken.Claims claims = moviebooking.util.JwtUtil.extractClaims(token);
            String email = claims.get("email", String.class);
            String name = claims.get("name", String.class);
            String role = claims.get("role", String.class);

            LoginResponse response = new LoginResponse(token, email, name, role);

            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    // DTO Classes

    /**
     * Request DTO for user registration.
     */
    public static class RegisterRequest {
        private String email;
        private String name;
        private String password;

        public RegisterRequest() {}

        public RegisterRequest(String email, String name, String password) {
            this.email = email;
            this.name = name;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Request DTO for user login.
     */
    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {}

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Response DTO for successful login.
     */
    public static class LoginResponse {
        private String token;
        private String email;
        private String name;
        private String role;

        public LoginResponse() {}

        public LoginResponse(String token, String email, String name, String role) {
            this.token = token;
            this.email = email;
            this.name = name;
            this.role = role;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    /**
     * Response DTO for errors.
     */
    public static class ErrorResponse {
        private String error;

        public ErrorResponse() {}

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
