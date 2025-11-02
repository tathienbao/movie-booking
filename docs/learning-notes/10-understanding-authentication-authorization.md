# Understanding Authentication & Authorization

This guide explains how JWT-based authentication and role-based authorization work in the Movie Booking API.

## Table of Contents

1. [Authentication vs Authorization](#authentication-vs-authorization)
2. [JWT (JSON Web Tokens)](#jwt-json-web-tokens)
3. [System Architecture](#system-architecture)
4. [Security Implementation](#security-implementation)
5. [RBAC (Role-Based Access Control)](#rbac-role-based-access-control)
6. [Code Walkthrough](#code-walkthrough)
7. [Security Best Practices](#security-best-practices)
8. [Testing Authentication](#testing-authentication)

---

## Authentication vs Authorization

### Authentication
**Who are you?** - Verifying user identity

- User provides credentials (email + password)
- System validates credentials against database
- If valid, system issues a JWT token
- User includes token in subsequent requests

### Authorization
**What can you do?** - Verifying user permissions

- System extracts user role from JWT token
- Checks if user has permission for requested action
- Allows or denies access based on role

**Example:**
```
Authentication: "I'm admin@example.com" + correct password → Get JWT token
Authorization: JWT says role=ADMIN → Can create/update/delete movies
              JWT says role=CUSTOMER → Can only view movies and create bookings
```

---

## JWT (JSON Web Tokens)

### What is JWT?

JWT is a compact, URL-safe token format for securely transmitting information between parties.

### JWT Structure

```
eyJhbGc...  .  eyJzdWI...  .  SflKxwRJ...
   ↓             ↓              ↓
 Header       Payload       Signature
```

**Three parts separated by dots:**

1. **Header**: Token type and algorithm
   ```json
   {
     "alg": "HS384",
     "typ": "JWT"
   }
   ```

2. **Payload**: Claims (user data)
   ```json
   {
     "sub": "1",              // User ID
     "email": "user@example.com",
     "name": "John Doe",
     "role": "CUSTOMER",
     "iat": 1699000000,       // Issued at
     "exp": 1699086400        // Expiration (24h)
   }
   ```

3. **Signature**: Cryptographic signature
   ```
   HMACSHA384(
     base64UrlEncode(header) + "." + base64UrlEncode(payload),
     secret_key
   )
   ```

### Why JWT?

✅ **Stateless**: Server doesn't need to store sessions
✅ **Scalable**: Works across multiple servers
✅ **Self-contained**: Token contains all user info
✅ **Secure**: Cryptographically signed (can't be tampered)

---

## System Architecture

### Request Flow with JWT

```
┌─────────┐                   ┌─────────────────┐                   ┌──────────┐
│ Client  │                   │  API Server     │                   │ Database │
└─────────┘                   └─────────────────┘                   └──────────┘
     │                                │                                    │
     │  1. POST /api/auth/login      │                                    │
     │  {email, password}             │                                    │
     ├───────────────────────────────>│                                    │
     │                                │  2. Query user by email            │
     │                                ├───────────────────────────────────>│
     │                                │<───────────────────────────────────┤
     │                                │  3. Verify password (BCrypt)       │
     │                                │                                    │
     │  4. Return JWT token           │  5. Generate JWT with user info    │
     │<───────────────────────────────┤                                    │
     │  {token: "eyJhbGc..."}         │                                    │
     │                                │                                    │
     │  6. GET /api/bookings          │                                    │
     │  Authorization: Bearer token   │                                    │
     ├───────────────────────────────>│                                    │
     │                                │  7. JwtAuthenticationFilter        │
     │                                │     - Extract token                │
     │                                │     - Validate signature           │
     │                                │     - Check expiration             │
     │                                │     - Extract user info            │
     │                                │                                    │
     │                                │  8. Check authorization (RBAC)     │
     │                                │     - Does user role allow this?   │
     │                                │                                    │
     │                                │  9. Execute business logic         │
     │                                ├───────────────────────────────────>│
     │                                │<───────────────────────────────────┤
     │  10. Return data               │                                    │
     │<───────────────────────────────┤                                    │
```

---

## Security Implementation

### 1. Password Security with BCrypt

**BCrypt** is a password hashing algorithm with built-in salt and configurable cost.

```java
// Registration: Hash password before storing
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
user.setPasswordHash(hashedPassword);

// Login: Verify password against hash
boolean isValid = BCrypt.checkpw(plainPassword, user.getPasswordHash());
```

**Why BCrypt?**
- ✅ **Salted**: Prevents rainbow table attacks
- ✅ **Slow**: Makes brute-force attacks impractical (cost factor 12 = ~300ms)
- ✅ **Adaptive**: Can increase cost as computers get faster

**Cost Factor:**
```
Cost 10 = 2^10 = 1024 iterations  (~100ms)
Cost 12 = 2^12 = 4096 iterations  (~300ms)  ← We use this
Cost 14 = 2^14 = 16384 iterations (~1200ms)
```

### 2. JWT Secret Key Management

**CRITICAL SECURITY FIX**: Never hardcode secrets!

```java
// ❌ BAD: Hardcoded secret (anyone with code can forge tokens!)
private static final String SECRET_KEY = "my-secret-key";

// ✅ GOOD: Environment variable
private static final String SECRET_KEY_STRING = System.getenv("JWT_SECRET_KEY");

// Validate on startup
if (SECRET_KEY_STRING == null || SECRET_KEY_STRING.length() < 48) {
    throw new IllegalStateException("JWT_SECRET_KEY not set or too short");
}
```

**Generate a secure key:**
```bash
openssl rand -base64 48
# Output: qJ8z3K7mN9pR5tV8wY1xA4bC6dE9fH2jK5lM8nP0qS3tU6vX9yZ1aC4dF7gI0jL3m
```

**Set environment variable:**
```bash
# Linux/Mac
export JWT_SECRET_KEY="qJ8z3K7mN9pR5tV8wY1xA4bC6dE9fH2jK5lM8nP0qS3tU6vX9yZ1aC4dF7gI0jL3m"

# Windows
set JWT_SECRET_KEY=qJ8z3K7mN9pR5tV8wY1xA4bC6dE9fH2jK5lM8nP0qS3tU6vX9yZ1aC4dF7gI0jL3m

# Docker
docker run -e JWT_SECRET_KEY="..." movie-booking-api
```

### 3. Path Matching Security

**CRITICAL FIX**: Use exact matching to prevent bypass attacks

```java
// ❌ VULNERABLE: startsWith() can be bypassed
if (path.startsWith("api/auth/register")) {
    return true;  // /api/auth/register-admin would also match!
}

// ✅ SECURE: Exact matching with equals()
if (path.equals("api/auth/register") || path.equals("/api/auth/register")) {
    return true;  // Only exact match works
}

// ✅ SECURE: Regex for parameterized paths
if (path.matches("^/?api/movies/\\d+$")) {
    return true;  // Matches /api/movies/123 but not /api/movies/123/admin
}
```

### 4. Database Index for Performance

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
public class User { ... }
```

**Why index on email?**
- ✅ **Fast lookups**: O(log n) instead of O(n) for authentication
- ✅ **Unique constraint**: Prevents duplicate emails at database level
- ✅ **Security**: Reduces timing attack surface (consistent query time)

---

## RBAC (Role-Based Access Control)

### Roles in the System

```java
public enum Role {
    CUSTOMER,  // Regular user: view movies, create bookings
    ADMIN      // Administrator: full CRUD on movies
}
```

### Permission Matrix

| Endpoint | Method | PUBLIC | CUSTOMER | ADMIN |
|----------|--------|--------|----------|-------|
| `/api/auth/register` | POST | ✅ | ✅ | ✅ |
| `/api/auth/login` | POST | ✅ | ✅ | ✅ |
| `/api/movies` | GET | ✅ | ✅ | ✅ |
| `/api/movies/{id}` | GET | ✅ | ✅ | ✅ |
| `/api/movies` | POST | ❌ | ❌ | ✅ |
| `/api/movies/{id}` | PUT | ❌ | ❌ | ✅ |
| `/api/movies/{id}` | DELETE | ❌ | ❌ | ✅ |
| `/api/bookings` | GET | ❌ | ✅ | ✅ |
| `/api/bookings` | POST | ❌ | ✅ | ✅ |
| `/api/bookings/{id}` | DELETE | ❌ | ✅ | ✅ |

### Authorization Flow

```java
// 1. Extract role from JWT token
Role userRole = JwtUtil.extractRole(token);

// 2. Check if endpoint requires admin
if (requiresAdminRole(path, method)) {
    if (userRole != Role.ADMIN) {
        // Return 403 Forbidden
        return Response.status(403).entity("{\"error\": \"Admin role required\"}").build();
    }
}

// 3. Proceed with request
```

---

## Code Walkthrough

### 1. User Registration (`AuthResource.java`)

```java
@POST
@Path("/register")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response register(User user) {
    try {
        // 1. Validate input (email format, password strength, etc.)
        validateRegistrationData(user);

        // 2. Hash password with BCrypt (cost factor 12)
        String hashedPassword = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt(12));
        user.setPasswordHash(hashedPassword);

        // 3. Set default role (CUSTOMER)
        user.setRole(Role.CUSTOMER);

        // 4. Save to database
        authService.register(user);

        // 5. Return user info (without password hash!)
        return Response.status(201).entity(user).build();
    } catch (IllegalArgumentException e) {
        return Response.status(400).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
    }
}
```

### 2. User Login (`AuthResource.java`)

```java
@POST
@Path("/login")
public Response login(LoginRequest request) {
    // 1. Find user by email (case-insensitive)
    User user = authService.findByEmail(request.getEmail().toLowerCase());
    if (user == null) {
        return Response.status(401).entity("{\"error\": \"Invalid credentials\"}").build();
    }

    // 2. Verify password
    if (!BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
        return Response.status(401).entity("{\"error\": \"Invalid credentials\"}").build();
    }

    // 3. Generate JWT token
    String token = JwtUtil.generateToken(user);

    // 4. Return token and user info
    return Response.ok()
        .entity(new LoginResponse(token, user.getEmail(), user.getName(), user.getRole()))
        .build();
}
```

### 3. JWT Token Generation (`JwtUtil.java`)

```java
public static String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("email", user.getEmail());
    claims.put("name", user.getName());
    claims.put("role", user.getRole().name());

    return Jwts.builder()
            .claims(claims)                    // Add custom claims
            .subject(user.getId().toString())  // User ID as subject
            .issuedAt(new Date())              // Current time
            .expiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24h
            .signWith(SECRET_KEY)              // Sign with secret key
            .compact();                        // Build token string
}
```

### 4. JWT Authentication Filter (`JwtAuthenticationFilter.java`)

```java
@Provider
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        // 1. Skip public endpoints
        if (isPublicEndpoint(path, method)) {
            return;
        }

        // 2. Extract Authorization header
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abortWithUnauthorized(requestContext, "Missing or invalid Authorization header");
            return;
        }

        // 3. Extract token
        String token = authHeader.substring(7); // Remove "Bearer "

        // 4. Validate token
        if (!JwtUtil.validateToken(token)) {
            abortWithUnauthorized(requestContext, "Invalid or expired token");
            return;
        }

        // 5. Extract user info
        Long userId = JwtUtil.extractUserId(token);
        String email = JwtUtil.extractEmail(token);
        Role role = JwtUtil.extractRole(token);

        // 6. Store in request context for resources to use
        requestContext.setProperty("userId", userId);
        requestContext.setProperty("userEmail", email);
        requestContext.setProperty("userRole", role);

        // 7. Check admin-only endpoints
        if (requiresAdminRole(path, method)) {
            if (role != Role.ADMIN) {
                abortWithForbidden(requestContext, "Admin role required");
                return;
            }
        }
    }
}
```

### 5. Using Authenticated User in Resources (`BookingResource.java`)

```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response createBooking(BookingRequest request, @Context ContainerRequestContext context) {
    // Extract user info from context (set by JwtAuthenticationFilter)
    Long userId = (Long) context.getProperty("userId");
    String userEmail = (String) context.getProperty("userEmail");

    // Create booking with authenticated user info
    Booking booking = bookingService.createBooking(
        userId,
        userEmail,
        request.getMovieId(),
        request.getNumberOfSeats()
    );

    return Response.status(201).entity(booking).build();
}
```

---

## Security Best Practices

### 1. Never Log Sensitive Data

```java
// ❌ BAD: Logs password in plain text
logger.info("User login: " + email + " with password: " + password);

// ✅ GOOD: Log only email
logger.info("Login attempt for user: " + email);
```

### 2. Fail Securely

```java
// ❌ BAD: Reveals if email exists
if (user == null) {
    return "Email not found";
} else if (!passwordMatches) {
    return "Wrong password";
}

// ✅ GOOD: Generic error message
if (user == null || !passwordMatches) {
    return "Invalid email or password";
}
```

### 3. Use HTTPS in Production

```
❌ HTTP:  http://api.example.com/api/auth/login
         Token sent in plain text - can be intercepted!

✅ HTTPS: https://api.example.com/api/auth/login
         Token encrypted in transit
```

### 4. Set Reasonable Token Expiration

```java
// ❌ Too long: 30 days (security risk if token stolen)
private static final long EXPIRATION_TIME = 30 * 24 * 60 * 60 * 1000;

// ✅ Good: 24 hours (balance between UX and security)
private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

// ✅ Better: Short-lived access token + refresh token pattern
//    Access token: 15 minutes
//    Refresh token: 7 days
```

### 5. Validate All Inputs

```java
// Always validate:
// - Email format
// - Password strength (min length, complexity)
// - Required fields not null/empty
// - String length limits (prevent DoS)

if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
    throw new IllegalArgumentException("Invalid email format");
}

if (password == null || password.length() < 8) {
    throw new IllegalArgumentException("Password must be at least 8 characters");
}
```

---

## Testing Authentication

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "name": "John Doe",
    "password": "SecurePass123"
  }'
```

Response:
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "role": "CUSTOMER",
  "createdAt": "2024-11-02T10:30:00"
}
```

### 2. Login and Get Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiQ1VTVE9NRVIiLCJzdWIiOiIxIiwiaWF0IjoxNjk5MDAwMDAwLCJleHAiOjE2OTkwODY0MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "CUSTOMER"
}
```

### 3. Use Token in Requests

```bash
# Save token
TOKEN="eyJhbGciOiJIUzM4NCJ9..."

# Access protected endpoint
curl -X GET http://localhost:8080/api/bookings \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Test Admin-Only Endpoint

```bash
# Try to create movie as CUSTOMER (should fail)
curl -X POST http://localhost:8080/api/movies \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Movie",
    "description": "Description",
    "genre": "Action",
    "durationMinutes": 120,
    "price": 15.0
  }'
```

Response (403 Forbidden):
```json
{
  "error": "Admin role required for this operation"
}
```

---

## Common Security Vulnerabilities

### 1. Hardcoded Secrets ❌

**Problem**: Anyone with code access can forge tokens
```java
private static final String SECRET = "my-secret";
```

**Fix**: Use environment variables ✅
```java
private static final String SECRET = System.getenv("JWT_SECRET_KEY");
```

### 2. Path Traversal/Bypass ❌

**Problem**: Attacker can bypass auth with crafted URLs
```java
if (path.startsWith("api/auth")) {
    // /api/auth-bypass would match!
}
```

**Fix**: Use exact matching ✅
```java
if (path.equals("api/auth/login") || path.equals("api/auth/register")) {
    // Only exact paths match
}
```

### 3. SQL Injection ❌

**Problem**: Attacker can manipulate queries
```java
"SELECT * FROM users WHERE email = '" + email + "'"
// Input: admin@example.com' OR '1'='1
```

**Fix**: Use JPA (auto-prevents SQL injection) ✅
```java
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);
```

### 4. Missing Rate Limiting ⚠️

**Problem**: Attacker can brute-force passwords

**Fix**: Implement rate limiting (future improvement)
```java
// Allow max 5 login attempts per 15 minutes per IP
```

---

## Summary

✅ **Authentication** = Who are you? (Login with email/password)
✅ **Authorization** = What can you do? (Check role from JWT)
✅ **JWT** = Secure, stateless token containing user info
✅ **BCrypt** = Secure password hashing (slow + salted)
✅ **RBAC** = Role-based permissions (CUSTOMER vs ADMIN)
✅ **Security** = Environment variables, exact path matching, input validation

**Next Steps:**
- Implement refresh tokens for better UX
- Add rate limiting to prevent brute-force
- Add audit logging for security events
- Implement password reset flow
- Add 2FA (Two-Factor Authentication)
