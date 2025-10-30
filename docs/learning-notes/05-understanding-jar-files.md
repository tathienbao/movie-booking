# Understanding JAR Files

This document explains what JAR files are and why they're important.

---

## 📦 What is JAR?

**JAR = Java ARchive**

It's a **ZIP file** that contains Java code and resources.

---

## 🔍 Simple Explanation

### **Think of it like:**

**Without JAR:**
```
MyApp/
├── Movie.class        (100 files scattered)
├── Booking.class
├── MovieResource.class
├── MovieService.class
├── ...96 more files
```

**With JAR:**
```
MyApp.jar              (1 file containing everything)
```

**It's like:**
- **Folder of files** → **ZIP file**
- **Many .class files** → **One .jar file**

---

## 📂 What's Inside a JAR?

```bash
# Unzip a JAR to see inside:
unzip movie-booking-api.jar

# You'll see:
moviebooking/
├── model/
│   ├── Movie.class
│   └── Booking.class
├── resource/
│   └── MovieResource.class
└── service/
    └── MovieService.class
META-INF/
└── MANIFEST.MF        (Metadata: which class is main?)
```

**It's literally a ZIP file:**
```bash
file movie-booking-api.jar
# Output: Zip archive data
```

---

## 🎯 Why Use JAR?

### **1. Distribution**
```bash
# Without JAR: Send 100 files
MyApp.zip (contains 100 .class files)

# With JAR: Send 1 file
MyApp.jar (contains same 100 files)
```

### **2. Easy Execution**
```bash
# Run the app:
java -jar MyApp.jar

# Java knows:
# 1. Extract files (virtually, in memory)
# 2. Find main class
# 3. Run it
```

### **3. Classpath Management**
```bash
# Java can read classes directly from JAR
# No need to extract first
```

---

## 🔑 Key Concept: .java vs .class vs .jar

**Source code (.java):**
```java
// Movie.java (Human-readable)
public class Movie {
    private String title;
}
```

**Compiled code (.class):**
```
// Movie.class (Machine-readable bytecode)
CA FE BA BE 00 00 00 34 00 1F 0A 00 06 00 11 09...
```

**Archive (.jar):**
```
// movie-booking-api.jar (ZIP of .class files)
[ZIP containing all .class files]
```

**Flow:**
```
1. Write: Movie.java
   ↓ (javac compiles)
2. Get: Movie.class
   ↓ (jar packages)
3. Get: app.jar
   ↓ (java runs)
4. Application runs!
```

---

## 💡 Types of JARs

### **1. Regular JAR** (What Maven creates by default)
```
movie-booking-api.jar
├── Your .class files only
└── No dependencies
```

**Can't run standalone:**
```bash
java -jar movie-booking-api.jar
# Error: Can't find Jersey, Grizzly, etc.
```

**Why?**
- Only contains YOUR code
- Dependencies (Jersey, Grizzly, Jackson) are separate JARs
- Java can't find them

**When to use:**
- Library/dependency for other projects
- Will be used with classpath
- Not meant to run standalone

---

### **2. Fat JAR / Uber JAR** (What Shade plugin creates)
```
movie-booking-api.jar
├── Your .class files
├── Jersey .class files
├── Grizzly .class files
├── Jackson .class files
└── All dependencies
```

**Can run standalone:**
```bash
java -jar movie-booking-api.jar
# Works! Everything included!
```

**Why?**
- Contains YOUR code + ALL dependencies
- Self-contained
- No classpath issues

**When to use:**
- Standalone applications
- Microservices
- Docker containers
- Simple deployment

---

## 🔍 Detailed Comparison

### **Regular JAR Example**

**Build:**
```bash
mvn package
```

**Creates:**
```
target/
├── movie-booking-api-1.0.0.jar    (~10 KB - only your code)
└── lib/                           (~15 MB - dependencies)
    ├── jersey-server-3.1.3.jar
    ├── grizzly-http-server-3.0.1.jar
    └── ...many more
```

**To run:**
```bash
java -cp target/movie-booking-api-1.0.0.jar:target/lib/* moviebooking.App
       ↑ Need to specify ALL dependencies in classpath
```

**Deployment:**
```bash
# Must copy:
- movie-booking-api-1.0.0.jar
- All JARs in lib/ folder
- Maintain directory structure
```

---

### **Fat JAR Example**

**Build:**
```bash
mvn package  (with Shade plugin)
```

**Creates:**
```
target/
└── movie-booking-api-1.0.0.jar    (~15 MB - everything!)
```

**To run:**
```bash
java -jar target/movie-booking-api-1.0.0.jar
     ↑ Simple! Everything included
```

**Deployment:**
```bash
# Copy only:
- movie-booking-api-1.0.0.jar

# That's it!
```

---

## 🆚 JAR vs Other Package Formats

| Format | Language | Description |
|--------|----------|-------------|
| **JAR** | Java | ZIP of .class files |
| **WAR** | Java (web apps) | JAR + web files (HTML, CSS, JSP) |
| **EAR** | Java (enterprise) | Multiple JARs + WARs |
| **executable** | Go | Compiled binary (not archive) |
| **wheel** | Python | Similar (Python packages) |
| **npm package** | JavaScript | Similar (node modules) |
| **gem** | Ruby | Similar (Ruby packages) |

---

## 📋 MANIFEST.MF File

Every JAR has a manifest file that contains metadata:

```
META-INF/MANIFEST.MF
```

**Example content:**
```
Manifest-Version: 1.0
Main-Class: moviebooking.App
Class-Path: jersey-server-3.1.3.jar grizzly-http-server-3.0.1.jar
```

**What it tells Java:**
- **Main-Class**: Which class has `main()` method
- **Class-Path**: Where to find dependencies (for regular JARs)
- **Version info**: Build details

**How Java uses it:**
```bash
java -jar app.jar

# Java reads MANIFEST.MF
# Finds: Main-Class: moviebooking.App
# Runs: moviebooking.App.main()
```

---

## 🔧 Creating JARs Manually

**You can create JARs without Maven:**

```bash
# Compile Java files
javac -d classes src/main/java/moviebooking/**/*.java

# Create JAR
cd classes
jar cvf ../my-app.jar .
jar cvfe ../my-app.jar moviebooking.App .  # With main class

# View JAR contents
jar tf my-app.jar

# Extract JAR
jar xf my-app.jar
```

**But Maven does this automatically!**

---

## 🎯 Real-World Usage

### **Development:**
```bash
# Maven handles everything
mvn exec:java -Dexec.mainClass="moviebooking.App"

# No need to think about JARs
```

### **Testing:**
```bash
# Build JAR
mvn package

# Test standalone
java -jar target/movie-booking-api-1.0.0.jar
```

### **Production:**
```bash
# Deploy single JAR
scp target/movie-booking-api-1.0.0.jar server:/opt/app/

# Run on server
java -jar /opt/app/movie-booking-api-1.0.0.jar
```

### **Docker:**
```dockerfile
# Copy single JAR
COPY target/*.jar app.jar

# Run it
CMD ["java", "-jar", "app.jar"]
```

---

## ✅ Key Takeaways

1. **JAR = ZIP file** of compiled Java code
2. **Regular JAR** = Only your code
3. **Fat JAR** = Your code + all dependencies
4. **Fat JAR** is better for:
   - Standalone apps
   - Docker
   - Simple deployment
5. **Maven Shade Plugin** creates Fat JARs
6. **MANIFEST.MF** tells Java which class to run

---

## 🚀 Best Practices

### **For Libraries:**
```xml
<!-- Don't use Shade plugin -->
<!-- Create regular JAR -->
<packaging>jar</packaging>
```

**Why?**
- Libraries shouldn't bundle dependencies
- Let consuming projects manage dependencies
- Avoid dependency conflicts

### **For Applications:**
```xml
<!-- Use Shade plugin -->
<!-- Create Fat JAR -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
</plugin>
```

**Why?**
- Applications are end products
- Should be self-contained
- Easy to deploy and run

---

## 🔍 Debugging JAR Issues

### **"Could not find or load main class"**

**Problem:** Wrong or missing Main-Class in MANIFEST.MF

**Solution:**
```bash
# Check manifest
jar xf app.jar META-INF/MANIFEST.MF
cat META-INF/MANIFEST.MF

# Should have:
Main-Class: moviebooking.App
```

### **"ClassNotFoundException"**

**Problem:** Dependencies not included

**Solution:**
- Use Shade plugin for Fat JAR
- Or specify correct classpath

### **"UnsupportedClassVersionError"**

**Problem:** JAR compiled for newer Java version

**Solution:**
```bash
# Check Java version used to compile
javap -verbose Movie.class | grep "major version"

# Run with same or newer Java version
```

---

## 📊 Size Comparison

**Our project:**
```
Regular JAR:     ~15 KB   (just our code)
Dependencies:    ~15 MB   (Jersey, Grizzly, etc.)
Fat JAR:         ~15 MB   (everything combined)
```

**Trade-offs:**
- **Regular JAR:** Small, but complex deployment
- **Fat JAR:** Large, but simple deployment

**Modern practice:** Fat JAR
- Disk space is cheap
- Simplicity is valuable
- Works perfectly with Docker/Kubernetes

---

This is everything you need to know about JAR files!
