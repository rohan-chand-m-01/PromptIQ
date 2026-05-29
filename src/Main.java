/**
 * AI Credit Cost Analyzer - Project Entry Point
 * 
 * Purpose:
 * This class serves as the startup coordinator for the Java Swing application.
 * It is responsible for testing the MySQL database connection on boot, initializing 
 * UI Look-and-Feel settings, and launching the Main Dashboard on the Event Dispatch Thread (EDT).
 * 
 * Key Components:
 * - MySQL JDBC Test Connection check.
 * - System Look-and-Feel binding.
 * - Dashboard frame instantiation on Swing EDT for thread-safe GUI boot.
 */

import database.DBConnection;
import ui.DashboardFrame;

import javax.swing.*;

public class Main {
    /**
     * The main execution method called by the JVM upon startup.
     * 
     * @param args Command-line arguments (unused in this desktop application).
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  AI Credit Cost Analyzer - Starting   ");
        System.out.println("========================================");

        // 1. Establish and test the MySQL Database connection immediately on startup.
        // This ensures the application fails-fast with a dialog warning if MySQL is not running.
        try {
            DBConnection.getInstance().getConnection();
            System.out.println("[Main] Database connection: OK");
        } catch (Exception e) {
            System.err.println("[Main] Database connection FAILED: " + e.getMessage());
            e.printStackTrace();
            // Present a visual popup to notify the developer/user about the DB configuration error.
            JOptionPane.showMessageDialog(null,
                "Database connection failed!\nPlease verify that MySQL is running and credentials in config.properties are correct.\n\nError details: " + e.getMessage(),
                "Startup Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // 2. Launch the graphical user interface (GUI) on the Event Dispatch Thread (EDT).
        // Swing is single-threaded, so all GUI creation and modification should run on the EDT.
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply the native operating system's visual Look-and-Feel (e.g. Windows style).
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fail silently and fallback to standard Java Swing styles if OS Look-and-Feel fails.
            }
            
            // Instantiates and renders the main premium dashboard frame.
            DashboardFrame frame = new DashboardFrame();
            frame.setVisible(true);
            System.out.println("[Main] UI launched successfully.");
        });
    }
}
