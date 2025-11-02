package moviebooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User entity representing a registered user in the system.
 *
 * Security considerations:
 * - Password is stored as BCrypt hash (never plain text)
 * - @JsonIgnore on passwordHash prevents it from being serialized to JSON
 * - Email is unique (enforced by database constraint)
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * BCrypt hashed password.
     *
     * SECURITY: Never expose this field in JSON responses!
     * @JsonIgnore prevents serialization to JSON
     * @JsonProperty(access = WRITE_ONLY) allows deserialization from JSON (for registration)
     */
    @Column(nullable = false, length = 60)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor (required by JPA)
    public User() {
        this.createdAt = LocalDateTime.now();
        this.role = Role.CUSTOMER; // Default role
    }

    /**
     * Constructor for creating a new user.
     *
     * @param email User's email (used for login)
     * @param name User's display name
     * @param passwordHash BCrypt hashed password
     * @param role User's role (CUSTOMER or ADMIN)
     */
    public User(String email, String name, String passwordHash, Role role) {
        this();
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    /**
     * Get password hash.
     *
     * SECURITY: This method should only be used internally for authentication.
     * The @JsonIgnore annotation on the field prevents this from being exposed in JSON.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}
