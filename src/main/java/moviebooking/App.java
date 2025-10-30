package moviebooking;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main application class.
 * Starts an embedded HTTP server using Grizzly.
 */
public class App {

    private static final String BASE_URI = "http://localhost:8080/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources.
     */
    public static HttpServer startServer() {
        // Create a resource config that scans for JAX-RS resources and config
        final ResourceConfig rc = new ResourceConfig()
                .packages("moviebooking");

        // Create and start a new instance of grizzly http server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final HttpServer server = startServer();
        System.out.println(String.format("Movie Booking API started at %s", BASE_URI));
        System.out.println("API endpoints available at: http://localhost:8080/api/movies");
        System.out.println("Press CTRL+C to stop the server...");

        // Wait indefinitely
        Thread.currentThread().join();
    }
}
