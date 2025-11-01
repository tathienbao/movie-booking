# Feature: Automated Testing

**Branch:** `feature/automated-testing`

**Status:** ✅ Completed

---

## Goal

Add comprehensive automated tests for the backend API using JUnit 5, covering REST endpoints, service layer, and integration tests.

---

## What We'll Implement

### 1. Unit Tests - Service Layer
- Test `MovieService` business logic
- Test CRUD operations
- Test edge cases and error handling
- Mock dependencies

### 2. Integration Tests - REST API
- Test all REST endpoints (GET, POST, PUT, DELETE)
- Test request/response formats
- Test HTTP status codes
- Test error responses

### 3. Test Configuration
- Set up test dependencies (JUnit 5, Mockito, REST Assured)
- Configure test environment
- Add test resources

### 4. CI/CD Integration
- Update Jenkins to run tests
- Ensure GitHub Actions runs tests
- Add test reporting

### 5. Documentation
- Document testing strategy
- Add examples of running tests
- Explain test structure

---

## Files to Create

### Test Files:
- `src/test/java/moviebooking/service/MovieServiceTest.java` - Service unit tests
- `src/test/java/moviebooking/resource/MovieResourceTest.java` - REST API tests
- `src/test/java/moviebooking/integration/MovieIntegrationTest.java` - End-to-end tests
- `docs/learning-notes/08-understanding-automated-testing.md` - Testing documentation

### Configuration:
- `src/test/resources/test-persistence.xml` - Test database config (if needed)

---

## Tech Stack

**Already Have:**
- JUnit 5 (jupiter) 5.10.0 ✅

**What We'll Add:**
- Mockito - For mocking dependencies
- REST Assured - For API testing
- AssertJ - Better assertions (optional)

**Update pom.xml:**
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.5.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.3.2</version>
    <scope>test</scope>
</dependency>
```

---

## Test Structure

### Unit Tests (Fast, Isolated)
```java
@Test
void testGetAllMovies() {
    // Given
    MovieService service = new MovieService();

    // When
    List<Movie> movies = service.getAllMovies();

    // Then
    assertNotNull(movies);
    assertTrue(movies.size() > 0);
}
```

### Integration Tests (Real HTTP)
```java
@Test
void testGetMoviesEndpoint() {
    given()
        .when().get("/api/movies")
        .then()
        .statusCode(200)
        .body("size()", greaterThan(0));
}
```

---

## Test Coverage Goals

**Minimum Coverage:**
- Service Layer: 80%+
- REST Endpoints: 100% (all endpoints tested)
- Error Handling: Key error paths tested

**Test Scenarios:**
- ✅ Happy path (normal operations)
- ✅ Edge cases (empty data, null values)
- ✅ Error cases (404, 400, 500)
- ✅ Validation (invalid input)

---

## Implementation Steps

1. ⏳ Switch to `feature/automated-testing` branch
2. ⏳ Add test dependencies to `pom.xml`
3. ⏳ Create `MovieServiceTest` (unit tests)
4. ⏳ Create `MovieResourceTest` (API tests)
5. ⏳ Create integration tests
6. ⏳ Run tests: `mvn test`
7. ⏳ Verify CI/CD runs tests
8. ⏳ Add documentation
9. ⏳ Commit and push

---

## Testing Commands

**Run all tests:**
```bash
mvn test
```

**Run specific test class:**
```bash
mvn test -Dtest=MovieServiceTest
```

**Run with coverage report:**
```bash
mvn test jacoco:report
```

**Run in CI/CD:**
```bash
# Already configured in Jenkins and GitHub Actions!
mvn clean test
```

---

## Success Criteria

✅ At least 15+ test cases created
✅ All tests pass (`mvn test` succeeds)
✅ Service layer tested
✅ All REST endpoints tested
✅ CI/CD runs tests automatically
✅ Documentation updated

---

## Estimated Time

⏱️ 1 hour

---

## Learning Outcomes

- JUnit 5 test structure
- Mocking with Mockito
- REST API testing
- Test-Driven Development (TDD)
- CI/CD test integration
- Testing best practices
