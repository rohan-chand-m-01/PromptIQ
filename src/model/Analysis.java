package model;

/**
 * Plain Old Java Object (POJO) representing the 'analysis' database table.
 * 
 * Purpose:
 * This model encapsulates real-time prompt telemetry and cost analysis results. 
 * It stores estimated token usage, derived complexity, cost brackets, recommended 
 * model engine versions, and improvement suggestions for standard SQL insertions.
 * 
 * Fields:
 * - analysisId: Unique auto-increment primary key in the MySQL database.
 * - promptId: Foreign key mapping this telemetry record to a parent 'prompts' row.
 * - tokenCount: The estimated total tokens.
 * - complexity: Complexity load rating (LOW, MEDIUM, HIGH).
 * - recommendedModel: Recommended AI engine (Gemini Flash, Pro, or Advanced).
 * - costCategory: Cost category bracket rating (Low, Medium, High).
 * - suggestion: Practical tips to optimize context or reduce tokens.
 * - analyzedAt: Database-generated analytical timestamp.
 */
public class Analysis {
    private int analysisId;
    private int promptId;
    private int tokenCount;
    private String complexity;
    private String recommendedModel;
    private String costCategory;
    private String suggestion;
    private String analyzedAt;

    /**
     * Default Empty Constructor.
     */
    public Analysis() {}

    /**
     * All-Arguments Constructor.
     */
    public Analysis(int analysisId, int promptId, int tokenCount, String complexity,
                    String recommendedModel, String costCategory, String suggestion, String analyzedAt) {
        this.analysisId = analysisId;
        this.promptId = promptId;
        this.tokenCount = tokenCount;
        this.complexity = complexity;
        this.recommendedModel = recommendedModel;
        this.costCategory = costCategory;
        this.suggestion = suggestion;
        this.analyzedAt = analyzedAt;
    }

    public int getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(int analysisId) {
        this.analysisId = analysisId;
    }

    public int getPromptId() {
        return promptId;
    }

    public void setPromptId(int promptId) {
        this.promptId = promptId;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public String getRecommendedModel() {
        return recommendedModel;
    }

    public void setRecommendedModel(String recommendedModel) {
        this.recommendedModel = recommendedModel;
    }

    public String getCostCategory() {
        return costCategory;
    }

    public void setCostCategory(String costCategory) {
        this.costCategory = costCategory;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(String analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
