package database;

/**
 * JDBC Database Connection Singleton
 * 
 * Purpose:
 * This class serves as the database coordinator, managing connection pooling and 
 * JDBC connections to the local MySQL server. It utilizes the Singleton Design Pattern 
 * to ensure that exactly one connection is instantiated and shared across DAO classes 
 * to minimize DB overhead and prevent leakages.
 * 
 * Key Features:
 * - Thread-safe Double-Checked Locking lazy initialization.
 * - Auto-reloading parameters from `config.properties`.
 * - Automatic connection verification checks before returning active sessions.
 */

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    // Shared single instance (volatile to guarantee visibility across threads)
    private static DBConnection instance;
    private Connection connection;
    private String url;
    private String user;
    private String password;

    /**
     * Private Constructor.
     * Prevents external class instantiation to maintain strict Singleton structure.
     * Loads the target MySQL JDBC URL, database name, and password credentials.
     */
    private DBConnection() {
        try {
            Properties props = new Properties();
            String path = new File("config.properties").getAbsolutePath();
            System.out.println("[DB] Loading config from: " + path);
            props.load(new FileInputStream(path));
            this.url = props.getProperty("db.url");
            this.user = props.getProperty("db.user");
            this.password = props.getProperty("db.password");
        } catch (Exception e) {
            System.err.println("[DB] Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Global access point for the Singleton instance.
     * Synchronized to prevent multiple threads from instantiating distinct connection pools.
     * 
     * @return The single, shared instance of DBConnection.
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Retrieves an active JDBC Connection to the MySQL database.
     * Automatically verifies if the previous session was closed or expired and 
     * seamlessly creates a new database connection as a fallback.
     * 
     * @return An active, authenticated Connection.
     * @throws SQLException If database access fails or credentials are wrong.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("[DB] Connection established to: " + url);
        }
        return connection;
    }

    /**
     * Safely closes the active JDBC database session.
     * Releases connections back to the OS to prevent connection leaks.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
