package database;

/**
 * Prompt Data Access Object (DAO)
 * 
 * Purpose:
 * This class isolates SQL query logic for the 'prompts' and 'optimized_prompts' tables.
 * It manages standard CRUD operations, performing parameterized SQL statements to 
 * prevent SQL Injection attacks and securely read/write prompts persistently.
 * 
 * Key Operations:
 * - insertPrompt(promptText): Saves an original prompt and fetches the auto-incremented primary key.
 * - insertOptimizedPrompt(id, optText): Stores the Gemini-optimized prompt linked to the original ID.
 * - getAllPrompts(): Fetches the joined prompts and optimized prompts for standard history logs.
 * - getJoinedAnalysis(): Performs a deep SQL JOIN operation merging prompts with tokens and suggestions.
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PromptDAO {

    /**
     * Inserts an original prompt into the prompts table.
     * Utilizes RETURN_GENERATED_KEYS to instantly fetch the MySQL auto-generated prompt ID 
     * so that the analysis table can map a foreign key link immediately.
     * 
     * @param originalPrompt the raw prompt text to insert.
     * @return the generated prompt_id, or -1 on failure.
     */
    public int insertPrompt(String originalPrompt) {
        String sql = "INSERT INTO prompts (original_prompt) VALUES (?)";
        System.out.println("[SQL] Executing: " + sql);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int generatedId = -1;

        try {
            // Fetch Singleton Connection
            conn = DBConnection.getInstance().getConnection();
            // Prepare statement with auto-increment ID retrieval instructions
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, originalPrompt);

            int rowsInserted = ps.executeUpdate();
            System.out.println("[DB] Rows inserted: " + rowsInserted);

            // Fetch auto-generated key from database response
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
                System.out.println("[DB] Generated prompt_id: " + generatedId);
            }
        } catch (Exception e) {
            System.err.println("[PromptDAO] Error in insertPrompt: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close open result sets and statements to prevent memory leakage
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        return generatedId;
    }

    /**
     * Inserts an optimized prompt linked to a given prompt_id.
     * 
     * @param promptId the parent prompt ID (Foreign Key).
     * @param optimizedPrompt the optimized prompt text.
     * @return the generated opt_id, or -1 on failure.
     */
    public int insertOptimizedPrompt(int promptId, String optimizedPrompt) {
        String sql = "INSERT INTO optimized_prompts (prompt_id, optimized_prompt) VALUES (?, ?)";
        System.out.println("[SQL] Executing: " + sql);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int generatedId = -1;

        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, promptId);
            ps.setString(2, optimizedPrompt);

            int rowsInserted = ps.executeUpdate();
            System.out.println("[DB] Rows inserted: " + rowsInserted);

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
                System.out.println("[DB] Generated opt_id: " + generatedId);
            }
        } catch (Exception e) {
            System.err.println("[PromptDAO] Error in insertOptimizedPrompt: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        return generatedId;
    }

    /**
     * Retrieves all prompts with their optimized versions via LEFT JOIN.
     * 
     * @return List of Object[] rows suitable for JTable display.
     */
    public List<Object[]> getAllPrompts() {
        String sql = "SELECT p.prompt_id, p.original_prompt, op.optimized_prompt, p.created_at " +
                     "FROM prompts p LEFT JOIN optimized_prompts op ON p.prompt_id = op.prompt_id " +
                     "ORDER BY p.created_at DESC";
        System.out.println("[SQL] Executing: " + sql);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Object[]> results = new ArrayList<>();

        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[]{
                    rs.getInt("prompt_id"),
                    rs.getString("original_prompt"),
                    rs.getString("optimized_prompt"),
                    rs.getTimestamp("created_at")
                };
                results.add(row);
            }

            System.out.println("[DB] Rows fetched: " + results.size());
        } catch (Exception e) {
            System.err.println("[PromptDAO] Error in getAllPrompts: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        return results;
    }

    /**
     * Retrieves prompts joined with their analysis data.
     * Merges prompts table with analysis table using prompt_id as primary/foreign keys.
     * 
     * @return List of Object[] rows containing prompt and analysis info.
     */
    public List<Object[]> getJoinedAnalysis() {
        String sql = "SELECT p.prompt_id, p.original_prompt, a.token_count, a.complexity, " +
                     "a.recommended_model, a.cost_category, a.suggestion " +
                     "FROM prompts p JOIN analysis a ON p.prompt_id = a.prompt_id " +
                     "ORDER BY a.analyzed_at DESC";
        System.out.println("[SQL] Executing: " + sql);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Object[]> results = new ArrayList<>();

        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[]{
                    rs.getInt("prompt_id"),
                    rs.getString("original_prompt"),
                    rs.getInt("token_count"),
                    rs.getString("complexity"),
                    rs.getString("recommended_model"),
                    rs.getString("cost_category"),
                    rs.getString("suggestion")
                };
                results.add(row);
            }

            System.out.println("[DB] Rows fetched: " + results.size());
        } catch (Exception e) {
            System.err.println("[PromptDAO] Error in getJoinedAnalysis: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        return results;
    }
}
