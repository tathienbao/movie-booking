package moviebooking.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Booking entity representing a customer's movie booking.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    /**
     * CRITICAL FIX: Prevent infinite JSON recursion
     *
     * @JsonBackReference tells Jackson to skip serialization of this field
     * when serializing Booking to JSON. This prevents infinite loops when:
     * - Movie has @OneToMany List<Booking> bookings
     * - Booking has @ManyToOne Movie movie
     *
     * Without this annotation, Jackson would serialize:
     * Movie → Bookings → Movie → Bookings → ... (infinite loop!)
     *
     * The @JsonBackReference annotation breaks the cycle by not serializing
     * the "back" reference (Booking → Movie).
     *
     * If Movie entity adds @OneToMany bookings field in the future,
     * it should use @JsonManagedReference on that field.
     */
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonBackReference
    private Movie movie;

    @Column(nullable = false)
    private Integer numberOfSeats;

    @Column(nullable = false)
    private LocalDateTime bookingTime;

    @Column(nullable = false)
    private Double totalPrice;

    // Default constructor (required by JPA)
    public Booking() {
        this.bookingTime = LocalDateTime.now();
    }

    /**
     * Constructor with validation
     *
     * CRITICAL FIX: Null pointer prevention
     * Added validation to prevent NPE when accessing movie.getPrice()
     */
    public Booking(String customerName, String customerEmail, Movie movie, Integer numberOfSeats) {
        // VALIDATION: Prevent NPE
        Objects.requireNonNull(movie, "Movie cannot be null");
        Objects.requireNonNull(numberOfSeats, "Number of seats cannot be null");

        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }

        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.movie = movie;
        this.numberOfSeats = numberOfSeats;
        this.bookingTime = LocalDateTime.now();
        this.totalPrice = movie.getPrice() * numberOfSeats;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Movie getMovie() {
        return movie;
    }

    /**
     * CRITICAL FIX: Setter validation
     * Prevents bypassing constructor validation by calling setter directly.
     */
    public void setMovie(Movie movie) {
        Objects.requireNonNull(movie, "Movie cannot be null");
        this.movie = movie;
    }

    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }

    /**
     * CRITICAL FIX: Setter validation
     * Prevents setting invalid values after construction.
     */
    public void setNumberOfSeats(Integer numberOfSeats) {
        Objects.requireNonNull(numberOfSeats, "Number of seats cannot be null");
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive (got: " + numberOfSeats + ")");
        }
        this.numberOfSeats = numberOfSeats;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                ", movie=" + movie.getTitle() +
                ", seats=" + numberOfSeats +
                ", totalPrice=$" + totalPrice +
                ", time=" + bookingTime +
                '}';
    }
}
