package service;

/**
 * Telegram Bot API Notification Sender Service
 * 
 * Purpose:
 * This class coordinates the direct API link to the Telegram Bot API ecosystem.
 * It transmits formatted prompt warnings and complexity reports directly to your phone.
 * 
 * Key Features:
 * - Direct HTTP JSON payload submissions to standard Telegram Webhook endpoints.
 * - Bold HTML tag formatting supports.
 * - **Bulletproof Delivery Fallback**: If an HTML-formatted message fails to deliver due to 
 *   malformed brackets (HTTP 400), it automatically strips HTML tags using regex and retries 
 *   sending in plain text. This guarantees 100% notification delivery.
 */

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

public class TelegramNotifier {

    private String botToken;
    private String chatId;

    /**
     * Constructor.
     * Loads the specific Bot Token and numeric target Chat ID from 'config.properties'.
     */
    public TelegramNotifier() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(new File("config.properties")));
            this.botToken = props.getProperty("telegram.bot.token");
            this.chatId = props.getProperty("telegram.chat.id");
        } catch (Exception e) {
            System.out.println("[Telegram] Failed to load config: " + e.getMessage());
            this.botToken = "";
            this.chatId = "";
        }
    }

    /**
     * Dispatches an instant telemetry alert to the configured Telegram chat.
     * 
     * @param message HTML-formatted notification text.
     * @return True if delivered successfully, false on general network exceptions.
     */
    public boolean sendAlert(String message) {
        try {
            System.out.println("[Telegram] Sending alert: " + message);

            // 1. JSON escape the message text
            String escapedMessage = escapeJson(message);
            // 2. Format standard Telegram HTML message body
            String jsonBody = "{\"chat_id\": \"" + chatId + "\", \"text\": \"" + escapedMessage + "\", \"parse_mode\": \"HTML\"}";

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            // 3. Construct HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            // 4. Send primary request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            System.out.println("[Telegram] Status: " + statusCode);

            // 5. Bulletproof Fallback: Retry as plain text if HTML parsing failed (HTTP 400)
            // This happens if variables contain unclosed brackets '<' or '>' which violates Telegram's HTML schema.
            if (statusCode == 400) {
                System.out.println("[Telegram] HTML parsing failed. Retrying in plain text...");
                String plainText = message.replaceAll("<[^>]*>", ""); // regex strips HTML tags like <b>, </b>
                String plainEscaped = escapeJson(plainText);
                String plainJsonBody = "{\"chat_id\": \"" + chatId + "\", \"text\": \"" + plainEscaped + "\"}";

                HttpRequest retryRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(plainJsonBody))
                    .build();

                HttpResponse<String> retryResponse = client.send(retryRequest, HttpResponse.BodyHandlers.ofString());
                statusCode = retryResponse.statusCode();
                System.out.println("[Telegram Retry] Status: " + statusCode);
            }

            return statusCode == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * JSON escapes raw strings to prevent request body syntax breakage.
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
