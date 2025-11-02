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
 * SECURITY NOTES:
 * - Secret key must be at least 256 bits (32 characters) for HS256
 * - In production, use environment variables or secrets management
 * - Never commit secret keys to version control
 * - Tokens should have reasonable expiration time (1-24 hours)
 */
public class JwtUtil {

    // SECRET KEY - In production, use environment variable or secrets management!
    // This must be at least 256 bits (32 characters) for HS256 algorithm
    private static final String SECRET_KEY = "movie-booking-secret-key-change-in-production-32chars-minimum";

    // Token expiration time: 24 hours (in milliseconds)
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

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
                .signWith(key)
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
                .verifyWith(key)
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
