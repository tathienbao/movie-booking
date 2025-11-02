package moviebooking.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import moviebooking.model.Role;
import moviebooking.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for JWT token generation and validation.
 *
 * JWT (JSON Web Token) is a compact, URL-safe token format for securely
 * transmitting information between parties as a JSON object.
 *
 * JWT Structure: header.payload.signature
 * - Header: Token type and signing algorithm
 * - Payload: Claims (user data, expiration, etc.)
 * - Signature: Cryptographic signature to verify integrity
 *
 * SECURITY IMPROVEMENTS:
 * 1. Secret key loaded from environment variable (NOT hardcoded)
 * 2. Validation on startup ensures secret is set
 * 3. Uses HMAC-SHA384 (HS384) algorithm
 * 4. 24-hour token expiration
 *
 * SETUP:
 * Set JWT_SECRET_KEY environment variable before starting the application:
 * export JWT_SECRET_KEY="your-secure-random-secret-key-min-48-chars"
 *
 * Generate a secure random key:
 * openssl rand -base64 48
 */
public class JwtUtil {

    /**
     * CRITICAL SECURITY FIX:
     * Secret key is loaded from environment variable, NOT hardcoded.
     * This prevents anyone with code access from forging valid tokens.
     */
    private static final String SECRET_KEY_STRING;
    private static final SecretKey SECRET_KEY;

    /**
     * Token expiration time: 24 hours (in milliseconds).
     */
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    static {
        // Load secret from environment variable
        SECRET_KEY_STRING = System.getenv("JWT_SECRET_KEY");

        // Validate that secret is set
        if (SECRET_KEY_STRING == null || SECRET_KEY_STRING.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT_SECRET_KEY environment variable is not set. " +
                "Please set it before starting the application. " +
                "Generate a secure key with: openssl rand -base64 48"
            );
        }

        // Validate minimum length (48 chars for HS384 = 384 bits / 8 = 48 bytes)
        if (SECRET_KEY_STRING.length() < 48) {
            throw new IllegalStateException(
                "JWT_SECRET_KEY must be at least 48 characters long for HS384 algorithm. " +
                "Current length: " + SECRET_KEY_STRING.length() + ". " +
                "Generate a secure key with: openssl rand -base64 48"
            );
        }

        // Create the secret key
        SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate JWT token for a user.
     *
     * Token contains:
     * - sub (subject): User ID
     * - email: User's email
     * - name: User's name
     * - role: User's role (CUSTOMER or ADMIN)
     * - iat (issued at): Timestamp when token was created
     * - exp (expiration): Timestamp when token expires
     *
     * @param user User to generate token for
     * @return JWT token string
     */
    public static String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("role", user.getRole().name());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Extract all claims from a JWT token.
     *
     * @param token JWT token
     * @return Claims object containing all token data
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    public static Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract user ID from token.
     *
     * @param token JWT token
     * @return User ID
     */
    public static Long extractUserId(String token) {
        return Long.parseLong(extractClaims(token).getSubject());
    }

    /**
     * Extract email from token.
     *
     * @param token JWT token
     * @return User's email
     */
    public static String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    /**
     * Extract role from token.
     *
     * @param token JWT token
     * @return User's role
     */
    public static Role extractRole(String token) {
        String roleString = extractClaims(token).get("role", String.class);
        return Role.valueOf(roleString);
    }

    /**
     * Check if token is expired.
     *
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * Validate JWT token.
     *
     * Checks:
     * - Token can be parsed (signature is valid)
     * - Token is not expired
     *
     * @param token JWT token
     * @return true if token is valid, false otherwise
     */
    public static boolean validateToken(String token) {
        try {
            extractClaims(token); // Will throw exception if invalid
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
