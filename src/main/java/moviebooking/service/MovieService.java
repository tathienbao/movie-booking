package moviebooking.service;

import moviebooking.model.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service layer for Movie business logic.
 * For now uses in-memory storage. Will be replaced with JPA later.
 */
public class MovieService {

    private static final Map<Long, Movie> movieStore = new ConcurrentHashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);

    // Initialize with some sample data
    static {
        Movie movie1 = new Movie("Inception", "A thief who steals corporate secrets through dream-sharing technology",
                "Sci-Fi", 148, 12.50);
        movie1.setId(idGenerator.getAndIncrement());
        movieStore.put(movie1.getId(), movie1);

        Movie movie2 = new Movie("The Dark Knight", "Batman fights the Joker in Gotham City",
                "Action", 152, 11.00);
        movie2.setId(idGenerator.getAndIncrement());
        movieStore.put(movie2.getId(), movie2);

        Movie movie3 = new Movie("Interstellar", "A team of explorers travel through a wormhole in space",
                "Sci-Fi", 169, 13.00);
        movie3.setId(idGenerator.getAndIncrement());
        movieStore.put(movie3.getId(), movie3);
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movieStore.values());
    }

    public Movie getMovieById(Long id) {
        return movieStore.get(id);
    }

    public Movie createMovie(Movie movie) {
        Long id = idGenerator.getAndIncrement();
        movie.setId(id);
        movieStore.put(id, movie);
        return movie;
    }

    public Movie updateMovie(Movie movie) {
        if (!movieStore.containsKey(movie.getId())) {
            return null;
        }
        movieStore.put(movie.getId(), movie);
        return movie;
    }

    public boolean deleteMovie(Long id) {
        return movieStore.remove(id) != null;
    }
}
