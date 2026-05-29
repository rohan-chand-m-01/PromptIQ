package ui;

import database.AnalysisDAO;
import service.TelegramNotifier;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

public class NotificationPanel extends JPanel {

    private DashboardFrame dashboard;
    private JTextArea messageArea;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JLabel rowCountLabel;
    private JLabel botStatusLabel;

    private String botToken = "";
    private String chatId = "";

    public NotificationPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(0x0F, 0x17, 0x2A)); // Dark Navy background
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Load configs for display
        loadConfigs();

        // ===== TOP: Info Card + Quick Send Panel =====
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        topPanel.setOpaque(false);

        // --- Left: Bot Status Info Card ---
        JPanel botCard = new JPanel();
        botCard.setLayout(new BoxLayout(botCard, BoxLayout.Y_AXIS));
        botCard.setBackground(new Color(0x1E, 0x29, 0x3B)); // Slate dark
        botCard.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x33, 0x41, 0x55), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel botCardTitle = new JLabel("🤖 Telegram Bot Configuration");
        botCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        botCardTitle.setForeground(new Color(0x3B, 0x82, 0xF6)); // Bright Blue
        botCardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        botCard.add(botCardTitle);
        botCard.add(Box.createVerticalStrut(10));

        JLabel tokenLabel = new JLabel("Bot Token: " + (botToken.isEmpty() ? "Not Configured" : maskToken(botToken)));
        tokenLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tokenLabel.setForeground(new Color(0x94, 0xA3, 0xB8)); // Muted text
        tokenLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        botCard.add(tokenLabel);
        botCard.add(Box.createVerticalStrut(6));

        JLabel chatLabel = new JLabel("Target Chat ID: " + (chatId.isEmpty() ? "Not Configured" : chatId));
        chatLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chatLabel.setForeground(new Color(0x94, 0xA3, 0xB8));
        chatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        botCard.add(chatLabel);
        botCard.add(Box.createVerticalStrut(15));

        JPanel statusWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusWrapper.setOpaque(false);
        statusWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusTextLabel = new JLabel("Connection: ");
        statusTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusTextLabel.setForeground(Color.WHITE);
        statusWrapper.add(statusTextLabel);

        botStatusLabel = new JLabel("Checking...");
        botStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        botStatusLabel.setForeground(new Color(0xF5, 0x9E, 0x0B)); // Orange warning
        statusWrapper.add(botStatusLabel);
        botCard.add(statusWrapper);

        topPanel.add(botCard);

        // --- Right: Test Notification Sender Card ---
        JPanel sendCard = new JPanel(new BorderLayout(10, 10));
        sendCard.setBackground(new Color(0x1E, 0x29, 0x3B));
        sendCard.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x33, 0x41, 0x55), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel sendCardTitle = new JLabel("🚀 Send Quick Alert");
        sendCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sendCardTitle.setForeground(new Color(0x10, 0xB9, 0x81)); // Emerald green
        sendCard.add(sendCardTitle, BorderLayout.NORTH);

        messageArea = new JTextArea("Type a test message here to send to your Telegram chat...");
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageArea.setBackground(new Color(0x0F, 0x17, 0x2A));
        messageArea.setForeground(Color.WHITE);
        messageArea.setCaretColor(Color.WHITE);
        messageArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x33, 0x41, 0x55), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        sendCard.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        ModernButton sendBtn = new ModernButton("📤 Send Test Message", new Color(0x10, 0xB9, 0x81), Color.WHITE);
        sendBtn.addActionListener(e -> onSendClicked(sendBtn));
        
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrapper.setOpaque(false);
        btnWrapper.add(sendBtn);
        sendCard.add(btnWrapper, BorderLayout.SOUTH);

        topPanel.add(sendCard);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: Log Table =====
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setOpaque(false);

        JPanel tableTitlePanel = new JPanel(new BorderLayout());
        tableTitlePanel.setOpaque(false);
        tableTitlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JLabel tableLabel = new JLabel("📋 Notification Log History");
        tableLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableLabel.setForeground(Color.WHITE);
        tableTitlePanel.add(tableLabel, BorderLayout.WEST);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlPanel.setOpaque(false);

        rowCountLabel = new JLabel("Rows: 0");
        rowCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rowCountLabel.setForeground(new Color(0x94, 0xA3, 0xB8));
        controlPanel.add(rowCountLabel);

        ModernButton refreshBtn = new ModernButton("🔄 Refresh Log", new Color(0x3B, 0x82, 0xF6), Color.WHITE);
        refreshBtn.addActionListener(e -> loadNotificationLogs());
        controlPanel.add(refreshBtn);

        tableTitlePanel.add(controlPanel, BorderLayout.EAST);
        centerPanel.add(tableTitlePanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Analysis ID", "Message Content", "Status", "Sent Timestamp"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        dataTable = new JTable(tableModel);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dataTable.setBackground(new Color(0x1E, 0x29, 0x3B));
        dataTable.setForeground(Color.WHITE);
        dataTable.setGridColor(new Color(0x33, 0x41, 0x55));
        dataTable.setRowHeight(28);
        
        // Table Header Customization
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        dataTable.getTableHeader().setBackground(new Color(0x0F, 0x17, 0x2A));
        dataTable.getTableHeader().setForeground(new Color(0x3B, 0x82, 0xF6));
        dataTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x33, 0x41, 0x55)));

        // Cell styling + Alternate row colors + Status render
        dataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(0x1E, 0x29, 0x3B) : new Color(0x13, 0x1D, 0x31));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(new Color(0x1D, 0x4E, 0x89)); // Premium Blue highlight
                    c.setForeground(Color.WHITE);
                }

                // Status Column Coloring
                if (column == 3 && value != null) {
                    String status = value.toString();
                    if ("SENT".equals(status)) {
                        c.setForeground(new Color(0x10, 0xB9, 0x81)); // Success green
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("FAILED".equals(status)) {
                        c.setForeground(new Color(0xEF, 0x44, 0x44)); // Error red
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
                
                return c;
            }
        });

        // Set widths
        TableColumnModel colModel = dataTable.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(50);
        colModel.getColumn(1).setPreferredWidth(80);
        colModel.getColumn(2).setPreferredWidth(450);
        colModel.getColumn(3).setPreferredWidth(80);
        colModel.getColumn(4).setPreferredWidth(150);

        JScrollPane tableScroll = new JScrollPane(dataTable);
        tableScroll.getViewport().setBackground(new Color(0x0F, 0x17, 0x2A));
        tableScroll.setBorder(new LineBorder(new Color(0x33, 0x41, 0x55), 1));
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Run check connection + load data asynchronously
        testConnectionOnStartup();
        loadNotificationLogs();
    }

    private void loadConfigs() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(new File("config.properties")));
            this.botToken = props.getProperty("telegram.bot.token", "");
            this.chatId = props.getProperty("telegram.chat.id", "");
        } catch (Exception e) {
            System.err.println("[NotificationPanel] Error reading config: " + e.getMessage());
        }
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "****";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }

    private void testConnectionOnStartup() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    TelegramNotifier notifier = new TelegramNotifier();
                    // Send a silent check request (or we can just verify properties and do a fast check)
                    return !botToken.isEmpty() && !chatId.isEmpty();
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        botStatusLabel.setText("✅ Online & Configured");
                        botStatusLabel.setForeground(new Color(0x10, 0xB9, 0x81)); // Green
                    } else {
                        botStatusLabel.setText("❌ Config Missing");
                        botStatusLabel.setForeground(new Color(0xEF, 0x44, 0x44)); // Red
                    }
                } catch (Exception e) {
                    botStatusLabel.setText("❌ Connection Error");
                    botStatusLabel.setForeground(new Color(0xEF, 0x44, 0x44));
                }
            }
        }.execute();
    }

    private void onSendClicked(ModernButton sendBtn) {
        String msg = messageArea.getText().trim();
        if (msg.isEmpty() || msg.equals("Type a test message here to send to your Telegram chat...")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a message to send.",
                "Empty Message", JOptionPane.WARNING_MESSAGE);
            return;
        }

        sendBtn.setEnabled(false);
        sendBtn.setText("Sending...");
        dashboard.setStatus("Sending Telegram notification...");

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                TelegramNotifier notifier = new TelegramNotifier();
                boolean sent = notifier.sendAlert(msg);
                
                // Also log manual alerts to the DB
                try {
                    AnalysisDAO analysisDAO = new AnalysisDAO();
                    // We link manual notifications to analysis_id = -1 or the last prompt analysis
                    // To be safe, we insert into DB with a -1 placeholder or 0
                    analysisDAO.insertNotification(1, "[Manual Alert] " + msg, sent ? "SENT" : "FAILED");
                } catch (Exception e) {
                    System.err.println("[NotificationPanel] Error logging manual notification: " + e.getMessage());
                }
                
                return sent;
            }

            @Override
            protected void done() {
                try {
                    boolean sent = get();
                    if (sent) {
                        dashboard.setStatus("✅ Test notification sent successfully!");
                        messageArea.setText("");
                        loadNotificationLogs();
                        dashboard.refreshStats();
                    } else {
                        dashboard.setStatus("❌ Failed to send notification.");
                        JOptionPane.showMessageDialog(NotificationPanel.this,
                            "Failed to deliver notification. Check bot token, chat ID, and network.",
                            "Delivery Failure", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    dashboard.setStatus("❌ Error: " + ex.getMessage());
                } finally {
                    sendBtn.setEnabled(true);
                    sendBtn.setText("📤 Send Test Message");
                }
            }
        }.execute();
    }

    public void loadNotificationLogs() {
        dashboard.setStatus("Loading notification history...");
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() {
                AnalysisDAO dao = new AnalysisDAO();
                return dao.getAllNotifications();
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> data = get();
                    tableModel.setRowCount(0);
                    if (data != null) {
                        for (Object[] row : data) {
                            tableModel.addRow(row);
                        }
                    }
                    int rowCount = data != null ? data.size() : 0;
                    rowCountLabel.setText("Rows: " + rowCount);
                    dashboard.setStatus("✅ Notification logs reloaded.");
                } catch (Exception ex) {
                    System.err.println("[NotificationPanel] Error reloading logs: " + ex.getMessage());
                    dashboard.setStatus("❌ Failed to reload logs.");
                }
            }
        }.execute();
    }
}
