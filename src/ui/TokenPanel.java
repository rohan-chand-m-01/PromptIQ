package ui;

import database.AnalysisDAO;
import database.PromptDAO;
import model.Analysis;
import service.ModelSelector;
import service.TelegramNotifier;
import service.TokenAnalyzer;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

public class TokenPanel extends JPanel {

    private DashboardFrame dashboard;
    private JTextArea promptArea;
    private DefaultTableModel tableModel;
    private JTable resultTable;
    private ModernButton telegramBtn;
    private Analysis lastAnalysis;

    public TokenPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(0x0F, 0x17, 0x2A)); // Dark Navy
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== TOP: Input Card =====
        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setBackground(new Color(0x1E, 0x29, 0x3B)); // Card Background
        inputPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x33, 0x41, 0x55), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));
        inputPanel.setPreferredSize(new Dimension(0, 210));

        JLabel promptLabel = new JLabel("📝 Enter Prompt to Analyze:");
        promptLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        promptLabel.setForeground(new Color(0xE2, 0xE8, 0xF0));
        inputPanel.add(promptLabel, BorderLayout.NORTH);

        promptArea = new JTextArea(5, 50);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        promptArea.setBackground(new Color(0x0F, 0x17, 0x2A));
        promptArea.setForeground(Color.WHITE);
        promptArea.setCaretColor(Color.WHITE);
        promptArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(promptArea);
        scrollPane.setBorder(new LineBorder(new Color(0x33, 0x41, 0x55), 1));
        inputPanel.add(scrollPane, BorderLayout.CENTER);

        ModernButton analyzeBtn = new ModernButton("🔍 Analyze Prompt", new Color(0x10, 0xB9, 0x81), Color.WHITE); // Emerald Green
        analyzeBtn.setPreferredSize(new Dimension(160, 36));
        analyzeBtn.addActionListener(e -> onAnalyzeClicked(analyzeBtn));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(analyzeBtn);
        inputPanel.add(btnPanel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.NORTH);

        // ===== CENTER: Result Table =====
        JPanel tableContainer = new JPanel(new BorderLayout(5, 5));
        tableContainer.setOpaque(false);

        JLabel tableTitleLabel = new JLabel("📊 Real-Time Metrics & Cost Telemetry");
        tableTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitleLabel.setForeground(Color.WHITE);
        tableContainer.add(tableTitleLabel, BorderLayout.NORTH);

        String[] columns = {"Token Count", "Complexity", "Cost Category", "Recommended Model", "Actionable Optimization Suggestion"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(tableModel);
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultTable.setBackground(new Color(0x1E, 0x29, 0x3B));
        resultTable.setForeground(Color.WHITE);
        resultTable.setGridColor(new Color(0x33, 0x41, 0x55));
        resultTable.setRowHeight(28);
        
        // Header Customization
        resultTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultTable.getTableHeader().setBackground(new Color(0x0F, 0x17, 0x2A));
        resultTable.getTableHeader().setForeground(new Color(0x3B, 0x82, 0xF6));
        resultTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x33, 0x41, 0x55)));

        // Alternate row colors and status styles
        resultTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(0x1E, 0x29, 0x3B) : new Color(0x13, 0x1D, 0x31));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(new Color(0x1D, 0x4E, 0x89));
                    c.setForeground(Color.WHITE);
                }

                // Complexity Coloring
                if (column == 1 && value != null) {
                    String comp = value.toString();
                    if ("LOW".equals(comp)) {
                        c.setForeground(new Color(0x10, 0xB9, 0x81)); // Green
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("MEDIUM".equals(comp)) {
                        c.setForeground(new Color(0xF5, 0x9E, 0x0B)); // Orange
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("HIGH".equals(comp)) {
                        c.setForeground(new Color(0xEF, 0x44, 0x44)); // Red
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
                
                return c;
            }
        });

        // Set column widths
        TableColumnModel colModel = resultTable.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(100);
        colModel.getColumn(1).setPreferredWidth(100);
        colModel.getColumn(2).setPreferredWidth(120);
        colModel.getColumn(3).setPreferredWidth(150);
        colModel.getColumn(4).setPreferredWidth(350);

        JScrollPane tableScroll = new JScrollPane(resultTable);
        tableScroll.getViewport().setBackground(new Color(0x0F, 0x17, 0x2A));
        tableScroll.setBorder(new LineBorder(new Color(0x33, 0x41, 0x55), 1));
        tableContainer.add(tableScroll, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        // ===== BOTTOM: Actions =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bottomPanel.setOpaque(false);

        ModernButton copyBtn = new ModernButton("📋 Copy Table to Clipboard", new Color(0x33, 0x41, 0x55), Color.WHITE);
        copyBtn.addActionListener(e -> copyTableToClipboard());
        bottomPanel.add(copyBtn);

        telegramBtn = new ModernButton("📤 Forward to Telegram", new Color(0x3B, 0x82, 0xF6), Color.WHITE); // Telegram Blue
        telegramBtn.setEnabled(false);
        telegramBtn.addActionListener(e -> forwardLastAnalysisToTelegram());
        bottomPanel.add(telegramBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void onAnalyzeClicked(ModernButton analyzeBtn) {
        String promptText = promptArea.getText().trim();
        if (promptText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a prompt to analyze.",
                "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        dashboard.setStatus("Analyzing prompt tokens & cost structure...");
        analyzeBtn.setEnabled(false);
        analyzeBtn.setText("Analyzing...");
        telegramBtn.setEnabled(false);

        new SwingWorker<Analysis, Void>() {
            @Override
            protected Analysis doInBackground() {
                return runFullAnalysis(promptText);
            }

            @Override
            protected void done() {
                try {
                    Analysis analysis = get();
                    if (analysis != null) {
                        lastAnalysis = analysis;
                        telegramBtn.setEnabled(true);
                        
                        tableModel.addRow(new Object[]{
                            analysis.getTokenCount(),
                            analysis.getComplexity(),
                            analysis.getCostCategory(),
                            analysis.getRecommendedModel(),
                            analysis.getSuggestion()
                        });
                        dashboard.setStatus("✅ Token Analysis Complete");
                        dashboard.refreshStats();
                    }
                } catch (Exception ex) {
                    System.err.println("[TokenPanel] Error in analysis: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TokenPanel.this,
                        "Analysis failed: " + ex.getMessage(),
                        "Telemetry Error", JOptionPane.ERROR_MESSAGE);
                    dashboard.setStatus("Analysis failed.");
                } finally {
                    analyzeBtn.setEnabled(true);
                    analyzeBtn.setText("🔍 Analyze Prompt");
                }
            }
        }.execute();
    }

    public static Analysis runFullAnalysis(String promptText) {
        System.out.println("\n========== STARTING ANALYSIS ==========");
        System.out.println("[App] Prompt received: " + promptText);

        TokenAnalyzer tokenAnalyzer = new TokenAnalyzer();
        ModelSelector modelSelector = new ModelSelector();
        PromptDAO promptDAO = new PromptDAO();
        AnalysisDAO analysisDAO = new AnalysisDAO();
        TelegramNotifier telegram = new TelegramNotifier();

        // Step 1: Token analysis
        int tokens = tokenAnalyzer.estimateTokens(promptText);
        String complexity = tokenAnalyzer.getComplexity(tokens);
        String costCategory = tokenAnalyzer.getCostCategory(tokens);
        String suggestion = tokenAnalyzer.getSuggestion(promptText, tokens);
        String model = modelSelector.recommendModel(tokens);

        System.out.println("[App] Complexity: " + complexity);
        System.out.println("[App] Recommended Model: " + model);
        System.out.println("[App] Cost Category: " + costCategory);

        // Step 2: Save prompt to DB
        int promptId = promptDAO.insertPrompt(promptText);

        // Step 3: Save analysis to DB
        Analysis analysis = new Analysis();
        analysis.setPromptId(promptId);
        analysis.setTokenCount(tokens);
        analysis.setComplexity(complexity);
        analysis.setRecommendedModel(model);
        analysis.setCostCategory(costCategory);
        analysis.setSuggestion(suggestion);
        int analysisId = analysisDAO.insertAnalysis(analysis);
        analysis.setAnalysisId(analysisId);

        // Step 4: Automatic Telegram alerts (Only for High Complexity, High Token, or Weak prompts)
        List<String> alerts = new ArrayList<>();
        if (tokens > 300) {
            alerts.add("⚠️ <b>High Token Usage Alert</b>\nTokens: " + tokens + "\n💡 Recommendation: Break into smaller tasks.");
        }
        if (tokenAnalyzer.isWeakPrompt(promptText, tokens)) {
            alerts.add("⚠️ <b>Weak Prompt Detected</b>\nTokens: " + tokens + "\n💡 Recommendation: Add more context & concrete structure.");
        }
        if ("HIGH".equals(complexity)) {
            alerts.add("⚠️ <b>High Complexity Telemetry</b>\nTokens: " + tokens + "\n🤖 Recommended Model: " + model);
        }

        for (String alert : alerts) {
            boolean sent = telegram.sendAlert(alert);
            String status = sent ? "SENT" : "FAILED";
            analysisDAO.insertNotification(analysisId, alert, status);
        }

        System.out.println("========== ANALYSIS COMPLETE ==========\n");
        return analysis;
    }

    private void forwardLastAnalysisToTelegram() {
        if (lastAnalysis == null) return;

        telegramBtn.setEnabled(false);
        telegramBtn.setText("Sending...");
        dashboard.setStatus("Forwarding telemetry report to Telegram...");

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    TelegramNotifier telegram = new TelegramNotifier();
                    AnalysisDAO analysisDAO = new AnalysisDAO();
                    
                    String alert = "📈 <b>Prompt Analysis Summary</b>\n" +
                                   "• Tokens: " + lastAnalysis.getTokenCount() + "\n" +
                                   "• Complexity: " + lastAnalysis.getComplexity() + "\n" +
                                   "• Cost: " + lastAnalysis.getCostCategory() + "\n" +
                                   "• Model: " + lastAnalysis.getRecommendedModel() + "\n" +
                                   "• Suggestion: " + lastAnalysis.getSuggestion();

                    boolean sent = telegram.sendAlert(alert);
                    String status = sent ? "SENT" : "FAILED";
                    analysisDAO.insertNotification(lastAnalysis.getAnalysisId(), alert, status);
                    return sent;
                } catch (Exception e) {
                    System.err.println("[TokenPanel] Manual alert failure: " + e.getMessage());
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        dashboard.setStatus("✅ Summary report forwarded to Telegram!");
                        dashboard.refreshStats();
                        JOptionPane.showMessageDialog(TokenPanel.this,
                            "Successfully forwarded analysis summary to Telegram bot!",
                            "Telegram Alert Sent", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        dashboard.setStatus("❌ Failed to forward summary.");
                        JOptionPane.showMessageDialog(TokenPanel.this,
                            "Could not deliver notification to Telegram bot.",
                            "Delivery Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    dashboard.setStatus("❌ Delivery error: " + e.getMessage());
                } finally {
                    telegramBtn.setEnabled(true);
                    telegramBtn.setText("📤 Forward to Telegram");
                }
            }
        }.execute();
    }

    private void copyTableToClipboard() {
        StringBuilder sb = new StringBuilder();
        // Headers
        for (int col = 0; col < tableModel.getColumnCount(); col++) {
            if (col > 0) sb.append("\t");
            sb.append(tableModel.getColumnName(col));
        }
        sb.append("\n");
        // Rows
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                if (col > 0) sb.append("\t");
                Object val = tableModel.getValueAt(row, col);
                sb.append(val != null ? val.toString() : "");
            }
            sb.append("\n");
        }
        StringSelection selection = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        dashboard.setStatus("✅ Table copied to clipboard!");
    }
}
