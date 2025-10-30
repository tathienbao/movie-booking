# Understanding Maven Shade Plugin

This document explains why we need the Maven Shade Plugin for Docker and deployment.

---

## 🎯 The Problem

Your app works fine with `mvn exec:java`, but what about deployment?

### **Current Situation (Without Maven Shade Plugin)**

**What Maven does now:**
```bash
mvn package
```

**Creates:**
```
target/
└── movie-booking-api-1.0.0.jar  (Only YOUR code, ~10 KB)
```

**Problem when you try to run it:**
```bash
java -jar target/movie-booking-api-1.0.0.jar
```
```
Error: Could not find or load main class moviebooking.App
```

**Why it fails:**
- JAR only contains YOUR classes
- Doesn't include dependencies (Jersey, Grizzly, Jackson, etc.)
- Java can't find the libraries your code needs

---

## 📦 The Solution: Maven Shade Plugin

**Creates a "Fat JAR" or "Uber JAR":**

```bash
mvn package  (with Shade plugin)
```

**Creates:**
```
target/
├── movie-booking-api-1.0.0.jar          (Original, ~10 KB)
└── movie-booking-api-1.0.0-shaded.jar   (Fat JAR, ~15 MB)
    ├── Your classes
    ├── Jersey classes
    ├── Grizzly classes
    ├── Jackson classes
    └── All other dependencies
```

**Now this works:**
```bash
java -jar target/movie-booking-api-1.0.0.jar
```
✅ Runs successfully! Everything included!

---

## 🔍 Detailed Comparison

### **Without Shade Plugin**

**pom.xml:**
```xml
<!-- No Shade plugin -->
<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

**Result after `mvn package`:**
```
target/
├── movie-booking-api-1.0.0.jar  (~10 KB)
│   └── Contains:
│       ├── moviebooking/model/Movie.class
│       ├── moviebooking/resource/MovieResource.class
│       └── moviebooking/service/MovieService.class
│
└── (No dependencies included!)
```

**To run, you need:**
```bash
# Complex classpath with all dependencies
java -cp target/movie-booking-api-1.0.0.jar:~/.m2/repository/org/glassfish/jersey/core/jersey-server/3.1.3/jersey-server-3.1.3.jar:~/.m2/repository/... moviebooking.App

# ← This is a nightmare!
```

---

### **With Shade Plugin**

**pom.xml:**
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.0</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <transformers>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>moviebooking.App</mainClass>
                            </transformer>
                        </transformers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Result after `mvn package`:**
```
target/
└── movie-booking-api-1.0.0.jar  (~15 MB)
    └── Contains:
        ├── moviebooking/model/Movie.class          (your code)
        ├── moviebooking/resource/MovieResource.class (your code)
        ├── org/glassfish/jersey/**/*.class         (Jersey)
        ├── org/glassfish/grizzly/**/*.class        (Grizzly)
        ├── com/fasterxml/jackson/**/*.class        (Jackson)
        └── ... all other dependencies
```

**To run:**
```bash
java -jar target/movie-booking-api-1.0.0.jar

# ← Simple! Everything is inside
```

---

## 🐳 Relevance to Docker

### **Without Shade Plugin - Complex Dockerfile**

```dockerfile
FROM maven:3.8.7-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Need to copy JAR
COPY --from=builder /app/target/*.jar app.jar

# Need to copy ALL dependencies separately
COPY --from=builder /app/target/lib/* /app/lib/

# Complex ENTRYPOINT with classpath
ENTRYPOINT ["java", "-cp", "app.jar:lib/*", "moviebooking.App"]
```

**Problems:**
- More files to copy
- More complex command
- Bigger image (more layers)
- Need to manage dependencies separately

---

### **With Shade Plugin - Simple Dockerfile**

```dockerfile
FROM maven:3.8.7-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Just copy ONE file!
COPY --from=builder /app/target/*.jar app.jar

# Simple ENTRYPOINT
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Benefits:**
- ✅ Simple Dockerfile
- ✅ One file to copy
- ✅ Standard Docker pattern
- ✅ Easy to understand and maintain

---

## 🔧 Relevance to Jenkins CI/CD

### **Without Shade Plugin - Complex Pipeline**

```groovy
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'mvn package'

                // Need to collect JAR + dependencies
                sh 'mkdir deploy'
                sh 'cp target/*.jar deploy/'
                sh 'cp -r target/lib deploy/'
            }
        }

        stage('Test') {
            steps {
                // Complex command with classpath
                sh 'java -cp deploy/movie-booking-api-1.0.0.jar:deploy/lib/* moviebooking.App &'
                sh 'sleep 5'
                sh 'curl http://localhost:8080/api/movies'
            }
        }

        stage('Deploy') {
            steps {
                // Deploy multiple files
                sh 'scp -r deploy/* server:/opt/app/'

                // Complex run command on server
                sh 'ssh server "java -cp /opt/app/movie-booking-api-1.0.0.jar:/opt/app/lib/* moviebooking.App"'
            }
        }
    }
}
```

**Problems:**
- Need to handle classpath
- Multiple files to deploy
- Different commands for each environment
- Hard to maintain

---

### **With Shade Plugin - Simple Pipeline**

```groovy
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'mvn package'
                // That's it! One JAR with everything
            }
        }

        stage('Test') {
            steps {
                // Simple command
                sh 'java -jar target/*.jar &'
                sh 'sleep 5'
                sh 'curl http://localhost:8080/api/movies'
            }
        }

        stage('Deploy') {
            steps {
                // Deploy single file
                sh 'scp target/*.jar server:/opt/app/'

                // Simple run command
                sh 'ssh server "java -jar /opt/app/movie-booking-api-1.0.0.jar"'
            }
        }
    }
}
```

**Benefits:**
- ✅ Simple commands
- ✅ One file to deploy
- ✅ Works same everywhere
- ✅ Easy to maintain

---

## 🔑 Key Configuration Options

### **1. Basic Configuration**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**What this does:**
- Runs during `mvn package`
- Creates Fat JAR with all dependencies

---

### **2. Set Main Class**

```xml
<configuration>
    <transformers>
        <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
            <mainClass>moviebooking.App</mainClass>
        </transformer>
    </transformers>
</configuration>
```

**What this does:**
- Adds `Main-Class: moviebooking.App` to MANIFEST.MF
- Allows `java -jar app.jar` to work

**Without this:**
```bash
java -jar app.jar
# Error: no main manifest attribute
```

---

### **3. Handle Service Files**

```xml
<transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
```

**What this does:**
- Merges META-INF/services files from dependencies
- Needed for JAX-RS, Jersey, and other service-based frameworks

**Why needed:**
```
jersey-server.jar has:    META-INF/services/javax.ws.rs.ext.MessageBodyReader
jackson.jar also has:     META-INF/services/javax.ws.rs.ext.MessageBodyReader

Without ServicesResourceTransformer: One overwrites the other
With ServicesResourceTransformer:    Both are merged
```

---

### **4. Exclude Signature Files**

```xml
<filters>
    <filter>
        <artifact>*:*</artifact>
        <excludes>
            <exclude>META-INF/*.SF</exclude>
            <exclude>META-INF/*.DSA</exclude>
            <exclude>META-INF/*.RSA</exclude>
        </excludes>
    </filter>
</filters>
```

**What this does:**
- Removes cryptographic signature files
- Prevents "Invalid signature file" errors

**Why needed:**
- Signed JARs have signature files
- When you combine JARs, signatures become invalid
- Must remove them

---

## 📊 Summary Table

| Aspect | Without Shade | With Shade |
|--------|---------------|------------|
| **Local dev (`mvn exec:java`)** | ✅ Works | ✅ Works |
| **Standalone JAR** | ❌ Fails | ✅ Works |
| **Docker image** | Complex | ✅ Simple |
| **Jenkins CI/CD** | Complex | ✅ Simple |
| **Deployment** | JAR + libs | ✅ One file |
| **JAR size** | ~10 KB | ~15 MB |
| **Command to run** | Complex classpath | ✅ `java -jar` |

---

## 💡 Real-World Analogy

**Without Shade Plugin:**
```
Like shipping a computer without:
- Monitor
- Keyboard
- Mouse
- Cables

Recipient needs to find compatible parts themselves
```

**With Shade Plugin:**
```
Like shipping a complete laptop:
- Everything built-in
- Just open and use
- Self-contained
```

---

## ✅ When Do You NEED Shade Plugin?

### **DON'T need it for:**
- ❌ Local development (`mvn exec:java` works)
- ❌ Library projects (should not bundle dependencies)
- ❌ Projects deployed to app servers (WildFly handles dependencies)

### **DO need it for:**
- ✅ Standalone applications
- ✅ **Docker containers**
- ✅ **Jenkins CI/CD**
- ✅ Microservices
- ✅ Production deployment
- ✅ Simple distribution

---

## 🚀 Complete Example

**Full pom.xml plugin configuration:**

```xml
<build>
    <plugins>
        <!-- Maven Shade Plugin -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.0</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <!-- Set main class -->
                        <transformers>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>moviebooking.App</mainClass>
                            </transformer>
                            <!-- Merge service files -->
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                        </transformers>
                        <!-- Remove signature files -->
                        <filters>
                            <filter>
                                <artifact>*:*</artifact>
                                <excludes>
                                    <exclude>META-INF/*.SF</exclude>
                                    <exclude>META-INF/*.DSA</exclude>
                                    <exclude>META-INF/*.RSA</exclude>
                                </excludes>
                            </filter>
                        </filters>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Usage:**
```bash
# Build
mvn clean package

# Run
java -jar target/movie-booking-api-1.0.0.jar

# Done!
```

---

## 🎯 Bottom Line

**You DON'T need Shade plugin for development:**
- `mvn exec:java` works fine

**You DO need Shade plugin for:**
- Docker (makes Dockerfile simple)
- Jenkins (makes pipeline simple)
- Deployment (one file, easy to distribute)
- Production (standard practice for microservices)

**That's why we add it!** Not for current development, but for the next steps (Docker, Jenkins, production).

---

This completes the explanation of Maven Shade Plugin!
