package database;

/**
 * Analysis & Notifications Data Access Object (DAO)
 * 
 * Purpose:
 * This class isolates database persistence operations for prompt metadata metrics, cost suggestions, 
 * and Telegram alert status logs. It also constructs complex database metrics to power the 
 * high-level visual widgets inside the main dashboard.
 * 
 * Main Queries:
 * - insertAnalysis(analysis): Logs estimated token count, recommended engine model, complexity levels, and tips.
 * - insertNotification(analysisId, alertMessage, deliveryStatus): Records outgoing Telegram bot notification histories.
 * - getAllAnalysis() & getAllNotifications(): Extracts full transaction logs for Database panels.
 * - getDashboardStats(): Executes a series of optimized aggregate queries (COUNT, AVG, GROUP BY) to 
 *   compute real-time stats including average tokens, top recommended models, and prompt counts.
 */

import model.Analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisDAO {

    /**
     * Inserts an analysis record linked to a prompt.
     * 
     * @param analysis the Analysis model object.
     * @return the generated analysis_id, or -1 on failure.
     */
    public int insertAnalysis(Analysis analysis) {
        String sql = "INSERT INTO analysis (prompt_id, token_count, complexity, recommended_model, cost_category, suggestion) VALUES (?, ?, ?, ?, ?, ?)";
        System.out.println("[SQL] Executing: " + sql);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int generatedId = -1;

        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, analysis.getPromptId());
            ps.setInt(2, analysis.getTokenCount());
            ps.setString(3, analysis.getComplexity());
            ps.setString(4, analysis.getRecommendedModel());
            ps.setString(5, analysis.getCostCategory());
            ps.setString(6, analysis.getSuggestion());

            int rowsInserted = ps.executeUpdate();
            System.out.println("[DB] Rows inserted: " + rowsInserted);

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
                System.out.println("[DB] Generated analysis_id: " + generatedId);
            }
        } catch (Exception e) {
            System.err.println("[AnalysisDAO] Error in insertAnalysis: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        return generatedId;
    }

    /**
     * Inserts an outgoing notification alert record linked to an analysis.
     * 
     * @param analysisId the parent analysis ID.
     * @param message the notification message.
     * @param status the notification delivery status (SENT or FAILED).
     * @return the generated notification_id, or -1 on failure.
     */
    public int insertNotification(int analysisId, String message, String status) {
        String sql = "INSERT INTO notifications (analysis_id, message, status) VALUES (?, ?, ?)";
        System.out.println("[SQL] Executing: " + sql);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int generatedId = -1;

        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, analysisId);
            ps.setString(2, message);
            ps.setString(3, status);

            int rowsInserted = ps.executeUpdate();
            System.out.println("[DB] Rows inserted: " + rowsInserted);

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
                System.out.println("[DB] Generated notification_id: " + generatedId);
            }
        } catch (Exception e) {
            System.err.println("[AnalysisDAO] Error in insertNotification: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        return generatedId;
    }

    /**
     * Retrieves all analysis records ordered by most recent first.
     * Uses dynamic ResultSetMetaData to read varying columns dynamically.
     * 
     * @return List of Object[] rows representing DB data.
     */
    public List<Object[]> getAllAnalysis() {
        String sql = "SELECT * FROM analysis ORDER BY analyzed_at DESC";
        System.out.println("[SQL] Executing: " + sql);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Object[]> results = new ArrayList<>();

        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                results.add(row);
            }

            System.out.println("[DB] Rows fetched: " + results.size());
        } catch (Exception e) {
            System.err.println("[AnalysisDAO] Error in getAllAnalysis: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        return results;
    }

    /**
     * Retrieves all notification records ordered by most recent first.
     * 
     * @return List of Object[] rows representing outgoing alerts.
     */
    public List<Object[]> getAllNotifications() {
        String sql = "SELECT * FROM notifications ORDER BY sent_time DESC";
        System.out.println("[SQL] Executing: " + sql);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Object[]> results = new ArrayList<>();

        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                results.add(row);
            }

            System.out.println("[DB] Rows fetched: " + results.size());
        } catch (Exception e) {
            System.err.println("[AnalysisDAO] Error in getAllNotifications: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        return results;
    }

    /**
     * Retrieves dashboard statistics from multiple tables.
     * Performs a series of SQL aggregate selections to drive real-time GUI analytics.
     * 
     * @return Map with keys: totalPrompts, avgTokens, notificationsSent, mostUsedModel.
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        ResultSet rs3 = null;
        ResultSet rs4 = null;

        try {
            conn = DBConnection.getInstance().getConnection();

            // Query 1: Total prompts processed historically
            String sql1 = "SELECT COUNT(*) FROM prompts";
            System.out.println("[SQL] Executing: " + sql1);
            ps1 = conn.prepareStatement(sql1);
            rs1 = ps1.executeQuery();
            if (rs1.next()) {
                stats.put("totalPrompts", rs1.getInt(1));
                System.out.println("[DB] totalPrompts: " + rs1.getInt(1));
            }

            // Query 2: Mathematical average token count across all prompts
            String sql2 = "SELECT AVG(token_count) FROM analysis";
            System.out.println("[SQL] Executing: " + sql2);
            ps2 = conn.prepareStatement(sql2);
            rs2 = ps2.executeQuery();
            if (rs2.next()) {
                stats.put("avgTokens", rs2.getDouble(1));
                System.out.println("[DB] avgTokens: " + rs2.getDouble(1));
            }

            // Query 3: Total notifications logged historically
            String sql3 = "SELECT COUNT(*) FROM notifications";
            System.out.println("[SQL] Executing: " + sql3);
            ps3 = conn.prepareStatement(sql3);
            rs3 = ps3.executeQuery();
            if (rs3.next()) {
                stats.put("notificationsSent", rs3.getInt(1));
                System.out.println("[DB] notificationsSent: " + rs3.getInt(1));
            }

            // Query 4: Most frequently recommended model using GROUP BY and LIMIT
            String sql4 = "SELECT recommended_model, COUNT(*) as cnt FROM analysis GROUP BY recommended_model ORDER BY cnt DESC LIMIT 1";
            System.out.println("[SQL] Executing: " + sql4);
            ps4 = conn.prepareStatement(sql4);
            rs4 = ps4.executeQuery();
            if (rs4.next()) {
                stats.put("mostUsedModel", rs4.getString("recommended_model"));
                System.out.println("[DB] mostUsedModel: " + rs4.getString("recommended_model"));
            } else {
                stats.put("mostUsedModel", "N/A");
                System.out.println("[DB] mostUsedModel: N/A (no data)");
            }

            System.out.println("[DB] Dashboard stats fetched successfully.");
        } catch (Exception e) {
            System.err.println("[AnalysisDAO] Error in getDashboardStats: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up resources cleanly
            try { if (rs1 != null) rs1.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (rs2 != null) rs2.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (rs3 != null) rs3.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (rs4 != null) rs4.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps1 != null) ps1.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps2 != null) ps2.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps3 != null) ps3.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps4 != null) ps4.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        return stats;
    }
}
