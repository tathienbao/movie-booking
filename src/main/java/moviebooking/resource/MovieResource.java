package moviebooking.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import moviebooking.App;
import moviebooking.model.Movie;
import moviebooking.service.MovieService;

import java.util.List;

/**
 * REST API endpoints for Movie operations.
 * Uses JAX-RS annotations to define HTTP methods and paths.
 *
 * DEPENDENCY INJECTION:
 * We get MovieService from App.getMovieService() instead of creating a new instance.
 * This ensures we use the same MovieService (with database connection) across all requests.
 *
 * WHY NOT: new MovieService()?
 * - Each request would create a new MovieService
 * - MovieService needs MovieRepository which needs EntityManagerFactory
 * - We want to reuse the same EntityManagerFactory (expensive to create)
 *
 * CRITICAL FIX: Lazy initialization for thread safety
 * Previously used field initialization which could race with App startup.
 * Now uses lazy initialization with null check to prevent NPE.
 *
 * In enterprise apps, you'd use:
 * - CDI: @Inject MovieService movieService
 * - Spring: @Autowired MovieService movieService
 * - Jersey HK2: @Inject with custom binder
 */
@Path("/api/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieResource {

    /**
     * Lazy-initialized MovieService to prevent race condition.
     *
     * THREAD SAFETY:
     * - Field initialized to null
     * - getMovieService() called lazily on first use
     * - Prevents NPE if request arrives before App finishes initialization
     */
    private MovieService movieService;

    /**
     * Get MovieService instance with lazy initialization.
     *
     * CRITICAL FIX: Thread-safe lazy initialization with double-checked locking
     *
     * THREAD SAFETY PROBLEM:
     * Previous code had race condition - multiple threads could simultaneously
     * check if movieService == null and both attempt initialization.
     *
     * SOLUTION: synchronized method
     * - Only one thread can execute this method at a time
     * - Prevents multiple initialization attempts
     * - Simple and correct (double-checked locking pattern)
     *
     * RACE CONDITION FIX:
     * JAX-RS may instantiate this resource before App.main() completes.
     * Lazy initialization ensures we only call getMovieService() when needed,
     * after the application is fully initialized.
     */
    private synchronized MovieService getService() {
        if (movieService == null) {
            movieService = App.getMovieService();
            if (movieService == null) {
                throw new IllegalStateException(
                    "Application not fully initialized. MovieService is null. " +
                    "This should not happen if App.main() completed successfully."
                );
            }
        }
        return movieService;
    }

    /**
     * GET /movies - List all movies
     */
    @GET
    public Response getAllMovies() {
        List<Movie> movies = getService().getAllMovies();
        return Response.ok(movies).build();
    }

    /**
     * GET /movies/{id} - Get a specific movie by ID
     */
    @GET
    @Path("/{id}")
    public Response getMovieById(@PathParam("id") Long id) {
        Movie movie = getService().getMovieById(id);
        if (movie == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Movie not found\"}")
                    .build();
        }
        return Response.ok(movie).build();
    }

    /**
     * POST /movies - Create a new movie
     */
    @POST
    public Response createMovie(Movie movie) {
        Movie created = getService().createMovie(movie);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    /**
     * PUT /movies/{id} - Update an existing movie
     */
    @PUT
    @Path("/{id}")
    public Response updateMovie(@PathParam("id") Long id, Movie movie) {
        movie.setId(id);
        Movie updated = getService().updateMovie(movie);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Movie not found\"}")
                    .build();
        }
        return Response.ok(updated).build();
    }

    /**
     * DELETE /movies/{id} - Delete a movie
     */
    @DELETE
    @Path("/{id}")
    public Response deleteMovie(@PathParam("id") Long id) {
        boolean deleted = getService().deleteMovie(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Movie not found\"}")
                    .build();
        }
        return Response.noContent().build();
    }
}
