package model;

/**
 * Plain Old Java Object (POJO) representing the 'notifications' database table.
 * 
 * Purpose:
 * This model holds outgoing alert notification transaction logs. It maps to 
 * the 'notifications' MySQL table, helping trace delivered Telegram messages, 
 * including delivery status (SENT or FAILED) and exact timestamps.
 * 
 * Fields:
 * - notificationId: Unique auto-increment primary key in the MySQL database.
 * - analysisId: Foreign key mapping this notification log to a parent 'analysis' record.
 * - message: The actual text/HTML payload transmitted to Telegram.
 * - status: The delivery status (SENT or FAILED).
 * - sentTime: Database-generated transmission timestamp.
 */
public class Notification {
    private int notificationId;
    private int analysisId;
    private String message;
    private String status;
    private String sentTime;

    /**
     * Default Empty Constructor.
     */
    public Notification() {}

    /**
     * All-Arguments Constructor.
     */
    public Notification(int notificationId, int analysisId, String message, String status, String sentTime) {
        this.notificationId = notificationId;
        this.analysisId = analysisId;
        this.message = message;
        this.status = status;
        this.sentTime = sentTime;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getPromptId() {
        return analysisId; // Kept for backward compatibility
    }

    public int getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(int analysisId) {
        this.analysisId = analysisId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSentTime() {
        return sentTime;
    }

    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }
}
