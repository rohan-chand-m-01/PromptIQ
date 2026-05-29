# AI Credit Cost Analyzer

An elegant enterprise grade Java Swing desktop application designed to analyze prompts for token usage, complexity, cost classification, and optimized recommendations. Powered by the Google Gemini API for intelligent prompt rewriting and the Telegram Bot API for instant notifications.

***

## Key Modules

<table>
  <thead>
    <tr>
      <th align="left">Module</th>
      <th align="left">Capability Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>Prompt Analyzer</b></td>
      <td>Evaluates any input prompt to compute token count, assign complexity level (LOW, MEDIUM, HIGH), recommend the most cost efficient model, and offer actionable improvement suggestions.</td>
    </tr>
    <tr>
      <td><b>Prompt Optimizer</b></td>
      <td>Leverages the Google Gemini API to restructure prompts, removing redundancies and reducing token footprint while maintaining complete original intent.</td>
    </tr>
    <tr>
      <td><b>History Tracker</b></td>
      <td>Stores analysis records persistently in MySQL, enabling users to review previous runs and track historical metrics.</td>
    </tr>
    <tr>
      <td><b>Telegram Alerts</b></td>
      <td>Dispatches formatted analysis digests directly to a Telegram channel or chat through a bot integration.</td>
    </tr>
  </tbody>
</table>

***

## System Prerequisites

<table>
  <thead>
    <tr>
      <th align="left">Component</th>
      <th align="left">Required Version</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>Java JDK</b></td>
      <td>Version 17 or higher</td>
    </tr>
    <tr>
      <td><b>MySQL Server</b></td>
      <td>Version 8.0 or higher</td>
    </tr>
    <tr>
      <td><b>JDBC Driver</b></td>
      <td>MySQL Connector J 8.x</td>
    </tr>
  </tbody>
</table>

***

## Setup Guide

### 1. Database Creation

Initialize the database schema by executing the provided SQL script:

```sql
source schema.sql
```

This creates the database `ai_analyzer` along with tables: `prompts`, `optimized_prompts`, `analysis`, and `notifications`.

### 2. Configuration Setup

Copy `config.properties.example` to `config.properties` and populate it with your database connection details, API keys, and Telegram credentials.

```properties
db.url=jdbc:mysql://localhost:3306/ai_analyzer
db.user=root
db.password=YOUR_DATABASE_PASSWORD

gemini.api.key=YOUR_GEMINI_API_KEY
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent

telegram.bot.token=YOUR_TELEGRAM_BOT_TOKEN
telegram.chat.id=YOUR_TELEGRAM_CHAT_ID
```

### 3. Driver Dependency

Place the MySQL connector JAR under the `lib` folder inside the project root:

```text
AICreditCostAnalyzer/lib/mysql-connector-j-8.4.0.jar
```

***

## Compilation and Execution

### Compilation

Compile the source files to the output directory using the classpath argument:

```cmd
javac -cp "lib/*" -d out src/**/*.java
```

### Execution

Run the compiled application:

* Windows:
  ```cmd
  java -cp "out;lib/*" Main
  ```
* Unix/macOS:
  ```bash
  java -cp "out:lib/*" Main
  ```

***

## Architecture Overview

```text
AICreditCostAnalyzer
 ├── lib (External dependencies)
 ├── out (Compiled class files)
 ├── src
 │    ├── Main.java (App entry point)
 │    ├── api
 │    │    └── GeminiClient.java (Gemini API bridge)
 │    ├── database
 │    │    ├── AnalysisDAO.java
 │    │    ├── DBConnection.java (MySQL pooling connection manager)
 │    │    └── PromptDAO.java
 │    ├── model
 │    │    ├── Analysis.java
 │    │    ├── Notification.java
 │    │    └── Prompt.java
 │    ├── service
 │    │    ├── ModelSelector.java (Selection logic)
 │    │    ├── PromptOptimizer.java (AI enhancement)
 │    │    ├── TelegramNotifier.java (Telegram API notifier)
 │    │    └── TokenAnalyzer.java (Cost and token counting engine)
 │    └── ui
 │         ├── DashboardFrame.java (Main GUI Shell)
 │         ├── DatabasePanel.java
 │         ├── ModelSwitcherPanel.java
 │         ├── ModernButton.java
 │         ├── NotificationPanel.java
 │         ├── OptimizerPanel.java
 │         └── TokenPanel.java
 ├── config.properties.example (Safe config template)
 ├── schema.sql (Database script)
 └── sources.txt (List of source files)
```

***

## License

This software is developed for educational and professional demonstration purposes under the Heapify AIT Hackathon.
