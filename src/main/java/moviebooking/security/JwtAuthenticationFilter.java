package moviebooking.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import moviebooking.model.Role;
import moviebooking.util.JwtUtil;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 *
 * This filter intercepts incoming HTTP requests and validates JWT tokens.
 * It runs for all requests except public endpoints (login, register).
 *
 * Flow:
 * 1. Extract JWT token from Authorization header
 * 2. Validate token
 * 3. Extract user info from token
 * 4. Store user info in request context for use by resources
 * 5. Allow request to proceed
 *
 * If token is missing or invalid, return 401 Unauthorized.
 *
 * @Provider annotation registers this as a JAX-RS provider
 */
@Provider
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    // Public endpoints that don't require authentication
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/movies"  // Allow viewing movies without auth
    };

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        // Allow public endpoints
        if (isPublicEndpoint(path, method)) {
            return;
        }

        // Extract Authorization header
        String authHeader = requestContext.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abortWithUnauthorized(requestContext, "Missing or invalid Authorization header");
            return;
        }

        // Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // Validate token
        if (!JwtUtil.validateToken(token)) {
            abortWithUnauthorized(requestContext, "Invalid or expired token");
            return;
        }

        // Extract user info from token and store in context
        try {
            Long userId = JwtUtil.extractUserId(token);
            String email = JwtUtil.extractEmail(token);
            Role role = JwtUtil.extractRole(token);

            // Store user info in request context for use by resources
            requestContext.setProperty("userId", userId);
            requestContext.setProperty("userEmail", email);
            requestContext.setProperty("userRole", role);

            // Check if endpoint requires admin role
            if (requiresAdminRole(path, method)) {
                if (role != Role.ADMIN) {
                    abortWithForbidden(requestContext, "Admin role required for this operation");
                    return;
                }
            }

        } catch (Exception e) {
            abortWithUnauthorized(requestContext, "Invalid token claims");
        }
    }

    /**
     * Check if the endpoint is public (doesn't require authentication).
     *
     * Public endpoints:
     * - POST /api/auth/register
     * - POST /api/auth/login
     * - GET /api/movies (view movies without login)
     * - GET /api/movies/{id}
     *
     * @param path Request path
     * @param method HTTP method
     * @return true if endpoint is public, false if it requires authentication
     */
    private boolean isPublicEndpoint(String path, String method) {
        // Auth endpoints are always public
        if (path.startsWith("api/auth/register") || path.startsWith("api/auth/login")) {
            return true;
        }

        // GET /api/movies and GET /api/movies/{id} are public (browse movies)
        if (path.startsWith("api/movies") && method.equals("GET")) {
            return true;
        }

        return false;
    }

    /**
     * Check if the endpoint requires admin role.
     *
     * Admin-only endpoints:
     * - POST /api/movies (create movie)
     * - PUT /api/movies/{id} (update movie)
     * - DELETE /api/movies/{id} (delete movie)
     *
     * @param path Request path
     * @param method HTTP method
     * @return true if endpoint requires admin role
     */
    private boolean requiresAdminRole(String path, String method) {
        // Movie CRUD operations (except GET) require ADMIN role
        if (path.startsWith("api/movies")) {
            return method.equals("POST") || method.equals("PUT") || method.equals("DELETE");
        }
        return false;
    }

    /**
     * Abort request with 401 Unauthorized response.
     *
     * @param requestContext Request context
     * @param message Error message
     */
    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        Response response = Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"" + message + "\"}")
                .build();
        requestContext.abortWith(response);
    }

    /**
     * Abort request with 403 Forbidden response.
     *
     * @param requestContext Request context
     * @param message Error message
     */
    private void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        Response response = Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"" + message + "\"}")
                .build();
        requestContext.abortWith(response);
    }
}
