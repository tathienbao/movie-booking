package moviebooking.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Movie entity representing a movie in the booking system. Uses JPA annotations
 * for database persistence.
 */
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Double price;

    // Default constructor (required by JPA)
    public Movie() {
    }

    // Constructor for creating new movies
    public Movie(String title, String description, String genre, Integer durationMinutes, Double price) {
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    /**
     * CRITICAL FIX: Setter validation Prevents bypassing service layer
     * validation by calling setter directly.
     */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null) {
            this.description = description.trim();
        } else {
            this.description = null;
        }
    }

    public String getGenre() {
        return genre;
    }

    /**
     * CRITICAL FIX: Setter validation
     */
    public void setGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Genre cannot be empty");
        }
        this.genre = genre.trim();
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * CRITICAL FIX: Setter validation
     */
    public void setDurationMinutes(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be positive (got: " + durationMinutes + ")");
        }
        this.durationMinutes = durationMinutes;
    }

    public Double getPrice() {
        return price;
    }

    /**
     * CRITICAL FIX: Setter validation
     */
    public void setPrice(Double price) {
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("Price must be positive (got: " + price + ")");
        }
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Movie movie = (Movie) o;
        return Objects.equals(id, movie.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Movie{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", genre='" + genre + '\''
                + ", duration=" + durationMinutes + "min"
                + ", price=$" + price
                + '}';
    }
}
