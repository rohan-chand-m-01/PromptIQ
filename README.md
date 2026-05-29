# AI Credit Cost Analyzer

A comprehensive Java desktop application that analyzes AI prompts for token usage, complexity, cost estimation, and provides intelligent optimization suggestions. It integrates with the **Google Gemini API** for AI-powered prompt optimization and **Telegram Bot API** for real-time notifications.

---

## Features

The application provides a tabbed Swing GUI with four main modules:

| Tab | Description |
|-----|-------------|
| **Prompt Analyzer** | Paste any AI prompt to get instant token count, complexity rating (LOW / MEDIUM / HIGH), recommended model, cost category, and actionable optimization suggestions. |
| **Prompt Optimizer** | Leverages the Gemini 1.5 Flash API to rewrite your prompt for maximum efficiency — reducing token count while preserving intent and clarity. |
| **History** | View all previously analyzed prompts with their full analysis results, stored persistently in MySQL. |
| **Notifications** | Sends a detailed analysis summary to your Telegram chat via a bot, and displays the notification log with timestamps and delivery status. |

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| **Java JDK** | 17 or higher |
| **MySQL Server** | 8.0 or higher |
| **MySQL Connector/J** | 8.x (JDBC driver JAR) |
| **Google Gemini API Key** | From [Google AI Studio](https://aistudio.google.com/app/apikey) |
| **Telegram Bot Token** | From [@BotFather](https://t.me/BotFather) |

---

## Setup

### 1. Create the Database

Open a MySQL client (MySQL Workbench, command line, etc.) and run the schema file:

```bash
mysql -u root -p < schema.sql
```

This creates the `ai_analyzer` database and the four required tables: `prompts`, `optimized_prompts`, `analysis`, and `notifications`.

### 2. Configure Credentials

Edit `config.properties` in the project root with your actual credentials:

```properties
# MySQL connection
db.url=jdbc:mysql://localhost:3306/ai_analyzer
db.user=root
db.password=YOUR_ACTUAL_MYSQL_PASSWORD

# Gemini API
gemini.api.key=YOUR_ACTUAL_GEMINI_API_KEY
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent

# Telegram Bot
telegram.bot.token=YOUR_ACTUAL_BOT_TOKEN
telegram.chat.id=YOUR_ACTUAL_CHAT_ID
```

### 3. Download MySQL Connector/J

1. Go to the [MySQL Connector/J Downloads](https://dev.mysql.com/downloads/connector/j/) page.
2. Select **Platform Independent** and download the ZIP or TAR archive.
3. Extract the archive and copy the `mysql-connector-j-8.x.x.jar` file.
4. Create a `lib/` directory in the project root and place the JAR inside:

```
AICreditCostAnalyzer/
├── lib/
│   └── mysql-connector-j-8.x.x.jar
```

### 4. Get a Gemini API Key

1. Visit [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Sign in with your Google account.
3. Click **Create API Key** and select or create a Google Cloud project.
4. Copy the generated API key and paste it into `config.properties` as the value for `gemini.api.key`.

### 5. Create a Telegram Bot

1. Open Telegram and search for **@BotFather**.
2. Send `/newbot` and follow the prompts to name your bot.
3. Copy the **bot token** provided by BotFather and paste it into `config.properties` as `telegram.bot.token`.
4. To get your **chat ID**:
   - Send any message to your new bot.
   - Open `https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates` in a browser.
   - Find the `"chat":{"id": ...}` value in the JSON response.
   - Paste that numeric ID into `config.properties` as `telegram.chat.id`.

---

## Compile

### Windows

```cmd
javac -cp "lib/*" -d out src/**/*.java
```

### Mac / Linux

```bash
find src -name "*.java" | xargs javac -cp "lib/*" -d out/
```

---

## Run

### Mac / Linux

```bash
java -cp "out:lib/*" Main
```

### Windows

```cmd
java -cp "out;lib/*" Main
```

---

## Expected Terminal Output

When the application starts and you analyze a prompt, you should see output similar to:

```
============================================
   AI Credit Cost Analyzer Started
============================================
[DB] Connected to MySQL database: ai_analyzer
[INFO] Loading configuration from config.properties...
[INFO] Gemini API endpoint configured.
[INFO] Telegram bot configured.
[INFO] Launching GUI...

--- Prompt Analysis ---
Original Prompt: "Explain the theory of relativity in simple terms with examples"
Token Count:     11
Complexity:      LOW
Recommended Model: gemini-1.5-flash
Cost Category:   LOW
Suggestion:      Prompt is concise and well-structured. No optimization needed.

[DB] Prompt saved with ID: 1
[DB] Analysis saved with ID: 1

--- Prompt Optimization (Gemini API) ---
Optimized Prompt: "Simply explain relativity theory with examples"
[DB] Optimized prompt saved with ID: 1

--- Telegram Notification ---
[TELEGRAM] Sending analysis summary to chat ID: 123456789
[TELEGRAM] Response: OK (200)
[DB] Notification saved with ID: 1, Status: SENT

============================================
   Analysis Complete
============================================
```

---

## Project Structure

```
AICreditCostAnalyzer/
│
├── config.properties          # Database, API, and Telegram credentials
├── schema.sql                 # MySQL database schema
├── README.md                  # This file
│
├── lib/                       # External JARs (MySQL Connector/J)
│   └── mysql-connector-j-8.x.x.jar
│
├── src/
│   ├── Main.java              # Application entry point
│   │
│   ├── db/
│   │   └── DBConnection.java  # MySQL connection manager
│   │
│   ├── model/
│   │   ├── Prompt.java        # Prompt entity
│   │   ├── Analysis.java      # Analysis result entity
│   │   └── Notification.java  # Notification entity
│   │
│   ├── dao/
│   │   ├── PromptDAO.java     # CRUD operations for prompts
│   │   ├── AnalysisDAO.java   # CRUD operations for analysis results
│   │   └── NotificationDAO.java # CRUD operations for notifications
│   │
│   ├── service/
│   │   ├── AnalyzerService.java    # Token counting, complexity, cost logic
│   │   ├── GeminiService.java      # Gemini API integration for optimization
│   │   └── TelegramService.java    # Telegram Bot API notification sender
│   │
│   └── ui/
│       ├── MainFrame.java          # Main JFrame with tabbed pane
│       ├── AnalyzerPanel.java      # Prompt Analyzer tab
│       ├── OptimizerPanel.java     # Prompt Optimizer tab
│       ├── HistoryPanel.java       # History tab
│       └── NotificationPanel.java  # Notifications tab
│
└── out/                       # Compiled .class files (generated)
```

---

## Troubleshooting

### Database Connection Errors

**Error:** `Communications link failure` or `Access denied for user 'root'`

- Ensure MySQL Server is running. Check with:
  ```bash
  # Windows (Services)
  net start MySQL80

  # Linux / Mac
  sudo systemctl status mysql
  ```
- Verify the credentials in `config.properties` match your MySQL `root` password.
- Confirm the `ai_analyzer` database exists by running:
  ```sql
  SHOW DATABASES LIKE 'ai_analyzer';
  ```
- If using MySQL 8+ with `caching_sha2_password`, ensure the Connector/J version is 8.x. Older 5.x connectors do not support this authentication plugin.

### Classpath / Compilation Issues

**Error:** `ClassNotFoundException: com.mysql.cj.jdbc.Driver`

- Ensure `mysql-connector-j-8.x.x.jar` is in the `lib/` directory.
- On Windows, use `;` as the classpath separator:
  ```cmd
  java -cp "out;lib/*" Main
  ```
- On Mac/Linux, use `:` as the classpath separator:
  ```bash
  java -cp "out:lib/*" Main
  ```

**Error:** `cannot find symbol` during compilation

- Ensure all `.java` files are being compiled. On Mac/Linux, use the `find` + `xargs` command shown above.
- Verify the directory structure matches the package declarations in each source file.

### Gemini API Errors

**Error:** `400 Bad Request` or `API key not valid`

- Double-check the API key in `config.properties`. It should not contain extra spaces or quotes.
- Ensure the Gemini API is enabled in your Google Cloud project.
- Verify the API URL is correct: `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent`
- Check your API quota at [Google AI Studio](https://aistudio.google.com/).

**Error:** `429 Too Many Requests`

- You have exceeded the rate limit. The free tier allows 15 requests per minute. Wait and retry.

### Telegram Notification Errors

**Error:** `401 Unauthorized`

- The bot token is invalid. Regenerate it via @BotFather using `/token`.

**Error:** `400 Bad Request: chat not found`

- The `telegram.chat.id` is incorrect. Ensure you have sent at least one message to the bot before fetching updates.
- Re-fetch the chat ID from:
  ```
  https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates
  ```

**Error:** `Connection timed out`

- Check your internet connection.
- If behind a corporate proxy, configure Java proxy settings:
  ```cmd
  java -Dhttps.proxyHost=proxy.example.com -Dhttps.proxyPort=8080 -cp "out;lib/*" Main
  ```

### General Tips

- Always recompile after making code changes before running.
- Use `java --version` to confirm JDK 17+ is on your PATH.
- For verbose SQL debugging, add `?useSSL=false&allowPublicKeyRetrieval=true` to the `db.url` in `config.properties`:
  ```properties
  db.url=jdbc:mysql://localhost:3306/ai_analyzer?useSSL=false&allowPublicKeyRetrieval=true
  ```

---

## License

This project is developed for educational purposes as part of the Heapify AIT Hackathon.
