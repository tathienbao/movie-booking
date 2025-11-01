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
     * Get MovieService from App (shared instance with database connection).
     * Initialized on first request and reused for all subsequent requests.
     */
    private final MovieService movieService = App.getMovieService();

    /**
     * GET /movies - List all movies
     */
    @GET
    public Response getAllMovies() {
        List<Movie> movies = movieService.getAllMovies();
        return Response.ok(movies).build();
    }

    /**
     * GET /movies/{id} - Get a specific movie by ID
     */
    @GET
    @Path("/{id}")
    public Response getMovieById(@PathParam("id") Long id) {
        Movie movie = movieService.getMovieById(id);
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
        Movie created = movieService.createMovie(movie);
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
        Movie updated = movieService.updateMovie(movie);
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
        boolean deleted = movieService.deleteMovie(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Movie not found\"}")
                    .build();
        }
        return Response.noContent().build();
    }
}
