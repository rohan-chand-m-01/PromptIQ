package model;

/**
 * Plain Old Java Object (POJO) representing the 'prompts' database table.
 * 
 * Purpose:
 * This class serves as a lightweight data holder (Model) encapsulating a single prompt record.
 * It contains standard getter and setter methods to pass prompt properties between 
 * the database layer (DAO) and the visual interface layer (UI).
 * 
 * Fields:
 * - promptId: Primary key representing the prompt index in MySQL.
 * - originalPrompt: The raw text inputted by the developer.
 * - createdAt: The database-generated transaction timestamp.
 */
public class Prompt {
    private int promptId;
    private String originalPrompt;
    private String createdAt;

    /**
     * Default Empty Constructor.
     */
    public Prompt() {}

    /**
     * All-Arguments Constructor.
     * 
     * @param promptId the unique prompt primary key ID.
     * @param originalPrompt the raw prompt query text.
     * @param createdAt database insertion timestamp.
     */
    public Prompt(int promptId, String originalPrompt, String createdAt) {
        this.promptId = promptId;
        this.originalPrompt = originalPrompt;
        this.createdAt = createdAt;
    }

    public int getPromptId() {
        return promptId;
    }

    public void setPromptId(int promptId) {
        this.promptId = promptId;
    }

    public String getOriginalPrompt() {
        return originalPrompt;
    }

    public void setOriginalPrompt(String originalPrompt) {
        this.originalPrompt = originalPrompt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
