package moviebooking.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helper utility for JSON parsing in tests.
 * Provides robust JSON parsing instead of brittle string manipulation.
 */
public class JsonTestHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extract the first movie ID from a JSON array response.
     *
     * @param jsonArray JSON array string (e.g., "[{\"id\":1,...},{\"id\":2,...}]")
     * @return The first movie ID, or null if array is empty
     */
    public static Long extractFirstMovieId(String jsonArray) {
        try {
            JsonNode root = objectMapper.readTree(jsonArray);
            if (root.isArray() && root.size() > 0) {
                JsonNode firstMovie = root.get(0);
                if (firstMovie.has("id")) {
                    return firstMovie.get("id").asLong();
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + jsonArray, e);
        }
    }

    /**
     * Extract movie ID from a single movie JSON object.
     *
     * @param jsonObject JSON object string (e.g., "{\"id\":1,...}")
     * @return The movie ID
     */
    public static Long extractMovieId(String jsonObject) {
        try {
            JsonNode root = objectMapper.readTree(jsonObject);
            if (root.has("id")) {
                return root.get("id").asLong();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + jsonObject, e);
        }
    }

    /**
     * Check if JSON string contains a specific field.
     *
     * @param json JSON string
     * @param fieldName Field name to check
     * @return true if field exists
     */
    public static boolean containsField(String json, String fieldName) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.has(fieldName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get string value of a field from JSON.
     *
     * @param json JSON string
     * @param fieldName Field name
     * @return Field value as string, or null if not found
     */
    public static String getStringField(String json, String fieldName) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has(fieldName)) {
                return root.get(fieldName).asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
