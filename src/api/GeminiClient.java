package api;

/**
 * Google Gemini API Client
 * 
 * Purpose:
 * This class coordinates the direct REST API connection to the Google Gemini AI Platform.
 * It reads credentials, constructs raw JSON payloads, performs asynchronous HTTP requests, 
 * handles HTTP status checks, parses successful prompt suggestions, and handles error details 
 * without external JSON parsing libraries to ensure zero external dependency lightweight footprint.
 * 
 * Main Methods:
 * - generateContent(userPrompt): Sends prompt to Gemini API and returns optimized response.
 * - escapeJson(text): Sanitizes input prompts to prevent invalid JSON strings.
 * - extractText(responseBody): Manually extracts the text response from the Gemini JSON envelope.
 * - extractError(responseBody): Parses error details (like Quota Exceeded) from error responses.
 */

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

public class GeminiClient {

    private String apiKey;
    private String apiUrl;

    /**
     * Constructor.
     * Loads the Gemini API key and model URL persistently from the 'config.properties' file.
     */
    public GeminiClient() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(new File("config.properties")));
            this.apiKey = props.getProperty("gemini.api.key");
            this.apiUrl = props.getProperty("gemini.api.url");
        } catch (Exception e) {
            System.out.println("[GeminiClient] Failed to load config: " + e.getMessage());
            this.apiKey = "";
            this.apiUrl = "";
        }
    }

    /**
     * Sends the user prompt to the Google Gemini REST API.
     * 
     * @param userPrompt The instruction prompt (e.g. prompt rewrite commands)
     * @return The optimized text response, or a formatted HTTP/network error message.
     */
    public String generateContent(String userPrompt) {
        try {
            System.out.println("[GeminiClient] Sending request to Gemini API...");

            // 1. Sanitize the user prompt text to escape newlines, backslashes, and quotes.
            String escapedPrompt = escapeJson(userPrompt);
            
            // 2. Prepare the exact raw JSON request body expected by Google's API.
            String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + escapedPrompt + "\"}]}]}";

            // 3. Create a thread-safe HttpClient with an active connection timeout.
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

            // 4. Construct the standard HTTP POST Request targeting the specific Gemini version.
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            // 5. Dispatch the HTTP call synchronously and extract the raw string response.
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String responseBody = response.body();
            System.out.println("[GeminiClient] Response received. Status: " + statusCode + ", Length: " + responseBody.length());

            // 6. Handle HTTP errors gracefully (e.g. 429 Quota Exceeded, 503 Spikes in traffic).
            if (statusCode != 200) {
                String errorMsg = extractError(responseBody);
                return "Error: HTTP " + statusCode + " - " + errorMsg;
            }

            // 7. Parse and extract the actual prompt response text.
            return extractText(responseBody);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Sanitizes string data to prevent JSON payload breakage by escaping control characters.
     * 
     * @param text The input prompt text.
     * @return Safe, JSON-escaped prompt string.
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * Extract detailed error messages from Google API error envelopes.
     * 
     * @param responseBody Raw JSON response body representing the error.
     * @return Deserialized descriptive error message string.
     */
    private String extractError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "Empty response from API";
        }
        try {
            int msgIndex = responseBody.indexOf("\"message\":");
            if (msgIndex == -1) {
                return "Raw Response: " + responseBody;
            }
            int start = responseBody.indexOf("\"", msgIndex + 10);
            if (start == -1) {
                return "Raw Response: " + responseBody;
            }
            start++;
            int end = responseBody.indexOf("\"", start);
            if (end == -1) {
                return "Raw Response: " + responseBody;
            }
            return responseBody.substring(start, end);
        } catch (Exception e) {
            return "Failed to parse error details: " + responseBody;
        }
    }

    /**
     * Manually extracts the generated content text from standard Gemini response envelopes.
     * Searches for the primary occurrence of '"text":' and reads the string value.
     * Handles standard Unicode backslash escaping inside the text block.
     * 
     * @param responseBody Raw JSON response envelope.
     * @return Extracted prompt text.
     */
    private String extractText(String responseBody) {
        try {
            int textIndex = responseBody.indexOf("\"text\":");
            if (textIndex == -1) {
                return "Error: No text field found in response.";
            }

            int start = responseBody.indexOf("\"", textIndex + 7);
            if (start == -1) {
                return "Error: Malformed response.";
            }
            start++;

            StringBuilder result = new StringBuilder();
            int i = start;
            while (i < responseBody.length()) {
                char c = responseBody.charAt(i);
                // Unescape backslash formatting tags inside the string
                if (c == '\\' && i + 1 < responseBody.length()) {
                    char next = responseBody.charAt(i + 1);
                    if (next == 'n') {
                        result.append('\n');
                        i += 2;
                        continue;
                    } else if (next == '"') {
                        result.append('"');
                        i += 2;
                        continue;
                    } else if (next == '\\') {
                        result.append('\\');
                        i += 2;
                        continue;
                    } else if (next == 't') {
                        result.append('\t');
                        i += 2;
                        continue;
                    } else if (next == 'r') {
                        result.append('\r');
                        i += 2;
                        continue;
                    }
                }
                if (c == '"') {
                    break; // Hit closing quote
                }
                result.append(c);
                i++;
            }

            return result.toString();
        } catch (Exception e) {
            return "Error: Failed to parse response - " + e.getMessage();
        }
    }
}
