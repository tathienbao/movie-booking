# Understanding Application Startup

This document explains the App.java class - how our application starts and runs.

---

## 📝 Breaking Down App.java

### **1. Package and Imports**

```java
package moviebooking;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
```

**What are these libraries?**

**Grizzly (`org.glassfish.grizzly.*`):**
- **Embedded HTTP server** (like a mini web server)
- Lightweight, fast, perfect for development
- No need for Tomcat, WildFly, etc.

**Jersey (`org.glassfish.jersey.*`):**
- **JAX-RS implementation** (REST framework)
- Handles routing, JSON conversion, etc.

**Why use embedded server?**
- ✅ **Easy development**: Just run `main()`, no deployment
- ✅ **Self-contained**: Everything in one JAR
- ✅ **Fast startup**: Seconds, not minutes
- ✅ **Docker-friendly**: Package as simple container

**Traditional vs Embedded:**

**Traditional (e.g., WildFly):**
```
1. Install WildFly server
2. Package app as WAR file
3. Deploy WAR to WildFly
4. Start WildFly
5. Access app at http://localhost:8080/your-app
```

**Embedded (our approach):**
```
1. Run main() method
2. Access app at http://localhost:8080
```

---

### **2. Base URI Configuration**

```java
private static final String BASE_URI = "http://localhost:8080/";
```

**What is BASE_URI?**
- The **root URL** where your API will be accessible
- `localhost` = this computer only
- `8080` = port number (standard for development)

**Why port 8080?**
- Port 80 requires admin/root privileges
- 8080 is the convention for Java web apps
- Easy to remember

**What does this mean for endpoints?**
```
BASE_URI = http://localhost:8080/
Endpoint = /api/movies

Full URL = http://localhost:8080/api/movies
```

---

### **3. Starting the Server**

```java
public static HttpServer startServer() {
    // Create a resource config that scans for JAX-RS resources and config
    final ResourceConfig rc = new ResourceConfig()
            .packages("moviebooking");

    // Create and start a new instance of grizzly http server
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
}
```

**Step 1: Configure Jersey**
```java
final ResourceConfig rc = new ResourceConfig()
        .packages("moviebooking");
```

**What is `ResourceConfig`?**
- Jersey's configuration object
- Tells Jersey where to find your REST resources

**What does `.packages("moviebooking")` do?**
- **Package scanning**: Jersey scans all classes in `moviebooking` package
- **Finds annotations**: Looks for `@Path`, `@GET`, `@POST`, etc.
- **Registers resources**: Makes them available as endpoints

**What Jersey finds:**
```java
moviebooking/
├── resource/
│   └── MovieResource.java  ← Found! Has @Path("/api/movies")
├── service/
│   └── MovieService.java   ← Ignored (no @Path)
└── model/
    └── Movie.java          ← Ignored (no @Path)
```

**Why scan the whole package?**
- **Automatic discovery**: Add new resources, they're automatically found
- **No manual registration**: Don't need to list every resource
- **Convention over configuration**: Less code to write

**Step 2: Create HTTP Server**
```java
return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
```

**What does this do?**
1. Creates a Grizzly HTTP server instance
2. Binds it to `http://localhost:8080`
3. Configures it with Jersey (the ResourceConfig)
4. **Starts listening** for HTTP requests
5. Returns the server object

**What happens under the hood:**
```
1. Grizzly opens port 8080
2. Grizzly waits for HTTP requests
3. Request arrives: GET /api/movies
4. Grizzly passes to Jersey
5. Jersey routes to MovieResource.getAllMovies()
6. MovieResource returns Response
7. Jersey converts to JSON
8. Grizzly sends HTTP response
```

---

### **4. Main Method**

```java
public static void main(String[] args) throws IOException, InterruptedException {
    final HttpServer server = startServer();
    System.out.println(String.format("Movie Booking API started at %s", BASE_URI));
    System.out.println("API endpoints available at: http://localhost:8080/api/movies");
    System.out.println("Press CTRL+C to stop the server...");

    // Wait indefinitely
    Thread.currentThread().join();
}
```

**Step 1: Start Server**
```java
final HttpServer server = startServer();
```
- Calls our `startServer()` method
- Server is now running and accepting requests

**Step 2: Print Info**
```java
System.out.println(String.format("Movie Booking API started at %s", BASE_URI));
System.out.println("API endpoints available at: http://localhost:8080/api/movies");
System.out.println("Press CTRL+C to stop the server...");
```

**Why print this?**
- **User-friendly**: Shows server started successfully
- **Helpful**: Tells user where to access the API
- **Instructions**: How to stop the server

**Output:**
```
Movie Booking API started at http://localhost:8080/
API endpoints available at: http://localhost:8080/api/movies
Press CTRL+C to stop the server...
```

**Step 3: Keep Running**
```java
Thread.currentThread().join();
```

**What does this do?**
- **Blocks the main thread** forever (until interrupted)
- Prevents the program from exiting
- Server keeps running

**Why is this needed?**
```java
// Without Thread.join():
public static void main(String[] args) {
    HttpServer server = startServer();
    System.out.println("Started!");
    // ← Program exits here!
    // ← Server stops!
}

// With Thread.join():
public static void main(String[] args) throws InterruptedException {
    HttpServer server = startServer();
    System.out.println("Started!");
    Thread.currentThread().join();  // ← Waits forever
    // ← Never reaches here (until CTRL+C)
}
```

**What is `throws IOException, InterruptedException`?**
- **IOException**: If server fails to start (e.g., port in use)
- **InterruptedException**: If thread is interrupted (CTRL+C)
- **Checked exceptions**: Java requires you to declare them

---

## 🔄 Complete Startup Flow

```
1. User runs: java -jar movie-booking-api.jar
   ↓
2. JVM calls main() method
   ↓
3. main() calls startServer()
   ↓
4. startServer() creates ResourceConfig
   ↓
5. ResourceConfig scans for @Path classes
   ↓
6. Found: MovieResource with @Path("/api/movies")
   ↓
7. Jersey registers MovieResource
   ↓
8. GrizzlyHttpServerFactory creates server
   ↓
9. Grizzly binds to localhost:8080
   ↓
10. Server starts listening for requests
   ↓
11. Print "Movie Booking API started..."
   ↓
12. Thread.join() keeps program running
   ↓
13. Server handles requests forever
   ↓
14. User presses CTRL+C
   ↓
15. InterruptedException thrown
   ↓
16. Program exits
   ↓
17. Server shuts down
```

---

## 🎯 Why This Architecture?

### **1. Simple to Run**

**Development:**
```bash
mvn exec:java -Dexec.mainClass="moviebooking.App"
```

**Production:**
```bash
java -jar movie-booking-api.jar
```

### **2. Easy to Debug**

```java
// Set breakpoint in main()
// Step through server startup
// See exactly what's happening
```

### **3. Self-Contained**

```
No external dependencies:
❌ No Tomcat installation
❌ No WildFly download
❌ No server configuration
✅ Just run main()
```

### **4. Container-Ready**

```dockerfile
# Dockerfile
FROM openjdk:17
COPY target/movie-booking-api.jar app.jar
CMD ["java", "-jar", "app.jar"]
```

That's it! One command, fully working server.

---

## 🚀 Running the Application

### **Method 1: Maven**

```bash
mvn exec:java -Dexec.mainClass="moviebooking.App"
```

**What this does:**
1. Maven compiles the code
2. Maven runs the main() method
3. Server starts
4. Press CTRL+C to stop

### **Method 2: IDE**

**In IntelliJ/Eclipse:**
1. Right-click on `App.java`
2. Click "Run 'App.main()'"
3. Server starts in IDE console
4. Click stop button to stop

### **Method 3: JAR File**

```bash
# Package as JAR
mvn package

# Run JAR
java -jar target/movie-booking-api-1.0.0.jar
```

---

## ✅ Key Concepts

### **1. Embedded Server Pattern**

**Pros:**
- ✅ Easy development
- ✅ Fast startup
- ✅ Self-contained
- ✅ Microservice-friendly

**Cons:**
- ❌ Less configuration options
- ❌ Not as powerful as full app servers

### **2. Package Scanning**

```java
.packages("moviebooking")
```
- Automatic resource discovery
- No manual registration
- Follows convention over configuration

### **3. Port Management**

```java
BASE_URI = "http://localhost:8080/"
```
- Port 8080 for development
- Can change in production (80, 443, etc.)
- Make it configurable for flexibility

### **4. Graceful Shutdown**

```java
Thread.currentThread().join();
```
- Keeps server running
- Responds to CTRL+C
- Clean shutdown

---

## 🔧 Making It Production-Ready

In a real application, you'd enhance this:

```java
public class App {

    public static void main(String[] args) {
        // Read configuration from environment
        String host = System.getenv().getOrDefault("HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String baseUri = String.format("http://%s:%d/", host, port);

        // Start server
        HttpServer server = startServer(baseUri);

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.shutdownNow();
        }));

        // Log startup
        System.out.println("Server started at " + baseUri);
        System.out.println("Health check: " + baseUri + "health");

        // Keep running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server interrupted");
        }
    }
}
```

But for learning and demos, our simple version is perfect!
