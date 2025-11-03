package moviebooking.service;

import moviebooking.model.Role;
import moviebooking.model.User;
import moviebooking.repository.UserRepository;
import moviebooking.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service layer for authentication and user management.
 *
 * Responsibilities:
 * - User registration with password hashing
 * - User login with password verification
 * - JWT token generation
 * - Input validation (email format, password strength, etc.)
 */
public class AuthService {

    private final UserRepository userRepository;

    // Email validation pattern (basic RFC 5322 compliance)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Password requirements
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 100;

    /**
     * Constructor with dependency injection.
     *
     * @param userRepository User repository for data access
     */
    public AuthService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository,
                "UserRepository cannot be null");
    }

    /**
     * Register a new user.
     *
     * Process:
     * 1. Validate input (email format, password strength, etc.)
     * 2. Check if email already exists
     * 3. Hash password with BCrypt
     * 4. Create and save user
     * 5. Return user (without password hash)
     *
     * @param email User's email
     * @param name User's display name
     * @param password Plain text password (will be hashed)
     * @param role User's role (CUSTOMER or ADMIN)
     * @return Created user
     * @throws IllegalArgumentException if validation fails or email already exists
     */
    public User register(String email, String name, String password, Role role) {
        // Validation
        validateRegistrationInput(email, name, password);

        // Normalize email to lowercase
        String normalizedEmail = email.trim().toLowerCase();

        // Check if email already exists
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered: " + normalizedEmail);
        }

        // Hash password with BCrypt
        // BCrypt automatically generates a salt and includes it in the hash
        // Cost factor 12 = 2^12 iterations (good balance of security and performance)
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        // Create user
        User user = new User(normalizedEmail, name.trim(), passwordHash, role);

        // Save to database
        return userRepository.save(user);
    }

    /**
     * Authenticate user and generate JWT token.
     *
     * Process:
     * 1. Find user by email
     * 2. Verify password with BCrypt
     * 3. Generate JWT token
     * 4. Return token
     *
     * @param email User's email
     * @param password Plain text password
     * @return JWT token
     * @throws IllegalArgumentException if authentication fails
     */
    public String login(String email, String password) {
        // Validation
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");

        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Normalize email
        String normalizedEmail = email.trim().toLowerCase();

        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOpt.get();

        // Verify password with BCrypt
        // BCrypt.checkpw() compares plain password with hashed password
        // It handles salt extraction automatically
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Generate and return JWT token
        return JwtUtil.generateToken(user);
    }

    /**
     * Get user by ID.
     *
     * @param id User ID
     * @return User if found, null otherwise
     */
    public User getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Validate registration input.
     *
     * @param email User's email
     * @param name User's name
     * @param password User's password
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRegistrationInput(String email, String name, String password) {
        // Null checks
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");

        // Email validation
        String trimmedEmail = email.trim();
        if (trimmedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (trimmedEmail.length() > 255) {
            throw new IllegalArgumentException("Email too long (max 255 characters)");
        }

        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Name validation
        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (trimmedName.length() > 100) {
            throw new IllegalArgumentException("Name too long (max 100 characters)");
        }

        // Password validation
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password too short (min " + MIN_PASSWORD_LENGTH + " characters)");
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password too long (max " + MAX_PASSWORD_LENGTH + " characters)");
        }

        // Check password complexity (at least one letter and one number)
        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one letter and one number");
        }
    }
}
