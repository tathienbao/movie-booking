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
     * CRITICAL FIX: volatile for double-checked locking
     *
     * volatile ensures:
     * - Visibility: All threads see the same value
     * - Prevents instruction reordering that could break double-checked locking
     *
     * Without volatile, thread B might see partially constructed MovieService
     * from thread A due to CPU cache and instruction reordering.
     *
     * THREAD SAFETY:
     * - Field initialized to null
     * - getMovieService() called lazily on first use
     * - Prevents NPE if request arrives before App finishes initialization
     */
    private volatile MovieService movieService;

    /**
     * Get MovieService instance with lazy initialization.
     *
     * CRITICAL FIX: Double-checked locking for performance
     *
     * PERFORMANCE PROBLEM WITH synchronized METHOD:
     * Synchronizing entire method creates global lock - all HTTP requests
     * execute sequentially even after initialization. This defeats multi-threading!
     *
     * SOLUTION: Double-checked locking pattern
     * 1. First check (no lock) - fast path for already-initialized case
     * 2. Synchronized block - only for initialization
     * 3. Second check (with lock) - prevent double initialization
     * 4. volatile field - ensures visibility across threads
     *
     * PERFORMANCE:
     * - After first initialization: NO synchronization overhead
     * - Requests execute concurrently (as they should)
     * - Only first few requests might synchronize
     *
     * CORRECTNESS (Thread-safe):
     * - volatile prevents instruction reordering
     * - Double check prevents race condition
     * - synchronized block prevents concurrent initialization
     * - Once initialized, zero overhead
     *
     * RACE CONDITION FIX:
     * JAX-RS may instantiate this resource before App.main() completes.
     * Lazy initialization ensures we only call getMovieService() when needed,
     * after the application is fully initialized.
     */
    private MovieService getService() {
        // First check (no locking) - fast path for 99.9% of requests
        if (movieService == null) {
            // Synchronize only for initialization (rare)
            synchronized (this) {
                // Second check (with lock) - prevent double initialization
                if (movieService == null) {
                    movieService = App.getMovieService();
                    if (movieService == null) {
                        throw new IllegalStateException(
                            "Application not fully initialized. MovieService is null. " +
                            "This should not happen if App.main() completed successfully."
                        );
                    }
                }
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
