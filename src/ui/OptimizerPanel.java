package ui;

import database.PromptDAO;
import service.PromptOptimizer;
import service.TelegramNotifier;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class OptimizerPanel extends JPanel {

    private DashboardFrame dashboard;
    private JTextArea originalArea;
    private JTextArea optimizedArea;
    private ModernButton optimizeBtn;
    private ModernButton telegramBtn;
    private String lastOptimized = "";

    public OptimizerPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(0x0F, 0x17, 0x2A)); // Dark Navy
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== TOP: Title Block =====
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("✨ Shadow Prompt Optimizer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel descLabel = new JLabel("Rewrite weak prompts automatically using Gemini 2.5 Flash Lite");
        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        descLabel.setForeground(new Color(0x94, 0xA3, 0xB8));
        headerPanel.add(descLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // ===== CENTER: Split Pane =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        splitPane.setOpaque(false);
        splitPane.setBackground(new Color(0x0F, 0x17, 0x2A));
        splitPane.setBorder(null);

        // --- LEFT: Original Prompt Panel ---
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(new Color(0x1E, 0x29, 0x3B));
        leftPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x33, 0x41, 0x55), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel originalLabel = new JLabel("📝 Original Input Prompt:");
        originalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        originalLabel.setForeground(new Color(0xE2, 0xE8, 0xF0));
        leftPanel.add(originalLabel, BorderLayout.NORTH);

        originalArea = new JTextArea(8, 30);
        originalArea.setLineWrap(true);
        originalArea.setWrapStyleWord(true);
        originalArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        originalArea.setBackground(new Color(0x0F, 0x17, 0x2A));
        originalArea.setForeground(Color.WHITE);
        originalArea.setCaretColor(Color.WHITE);
        
        JScrollPane originalScroll = new JScrollPane(originalArea);
        originalScroll.setBorder(new LineBorder(new Color(0x33, 0x41, 0x55), 1));
        leftPanel.add(originalScroll, BorderLayout.CENTER);

        optimizeBtn = new ModernButton("✨ Optimize with Gemini", new Color(0x8B, 0x5C, 0xF6), Color.WHITE); // Violet
        optimizeBtn.setPreferredSize(new Dimension(190, 36));
        optimizeBtn.addActionListener(e -> onOptimizeClicked());

        JPanel leftBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftBtnPanel.setOpaque(false);
        leftBtnPanel.add(optimizeBtn);
        leftPanel.add(leftBtnPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);

        // --- RIGHT: Optimized Prompt Panel ---
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(new Color(0x1E, 0x29, 0x3B));
        rightPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x33, 0x41, 0x55), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel optimizedLabel = new JLabel("🚀 AI Optimized Output:");
        optimizedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        optimizedLabel.setForeground(new Color(0x8B, 0x5C, 0xF6));
        rightPanel.add(optimizedLabel, BorderLayout.NORTH);

        optimizedArea = new JTextArea(8, 30);
        optimizedArea.setLineWrap(true);
        optimizedArea.setWrapStyleWord(true);
        optimizedArea.setEditable(false);
        optimizedArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        optimizedArea.setBackground(new Color(0x13, 0x1D, 0x31)); // Shaded background
        optimizedArea.setForeground(new Color(0xE2, 0xE8, 0xF0));
        
        JScrollPane optimizedScroll = new JScrollPane(optimizedArea);
        optimizedScroll.setBorder(new LineBorder(new Color(0x33, 0x41, 0x55), 1));
        rightPanel.add(optimizedScroll, BorderLayout.CENTER);

        JPanel rightBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rightBtnPanel.setOpaque(false);

        ModernButton copyBtn = new ModernButton("📋 Copy Optimized", new Color(0x33, 0x41, 0x55), Color.WHITE);
        copyBtn.setPreferredSize(new Dimension(140, 36));
        copyBtn.addActionListener(e -> {
            String text = optimizedArea.getText();
            if (!text.isEmpty() && !text.startsWith("Error:")) {
                StringSelection selection = new StringSelection(text);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                dashboard.setStatus("✅ Optimized prompt copied to clipboard!");
            }
        });
        rightBtnPanel.add(copyBtn);

        telegramBtn = new ModernButton("📤 Send to Telegram", new Color(0x3B, 0x82, 0xF6), Color.WHITE);
        telegramBtn.setPreferredSize(new Dimension(150, 36));
        telegramBtn.setEnabled(false);
        telegramBtn.addActionListener(e -> forwardOptimizedToTelegram());
        rightBtnPanel.add(telegramBtn);

        rightPanel.add(rightBtnPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    private void onOptimizeClicked() {
        String text = originalArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a prompt to optimize.",
                "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        optimizeBtn.setEnabled(false);
        optimizeBtn.setText("Optimizing...");
        telegramBtn.setEnabled(false);
        dashboard.setStatus("Contacting Gemini for prompt optimization...");
        optimizedArea.setText("");
        lastOptimized = "";

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    PromptOptimizer optimizer = new PromptOptimizer();
                    String optimized = optimizer.optimizePrompt(text);

                    if (optimized != null && !optimized.startsWith("Error:")) {
                        // Save to DB
                        PromptDAO promptDAO = new PromptDAO();
                        int promptId = promptDAO.insertPrompt(text);
                        promptDAO.insertOptimizedPrompt(promptId, optimized);
                    }

                    return optimized;
                } catch (Exception e) {
                    System.err.println("[OptimizerPanel] Error: " + e.getMessage());
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    optimizedArea.setText(result);
                    if (result != null && result.startsWith("Error:")) {
                        dashboard.setStatus("❌ Optimization failed.");
                        JOptionPane.showMessageDialog(OptimizerPanel.this,
                            result, "Optimization Telemetry Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        lastOptimized = result;
                        telegramBtn.setEnabled(true);
                        dashboard.setStatus("✅ Prompt optimized successfully!");
                        dashboard.refreshStats();
                    }
                } catch (Exception ex) {
                    System.err.println("[OptimizerPanel] Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(OptimizerPanel.this,
                        "Optimization failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    dashboard.setStatus("❌ Optimization failed.");
                } finally {
                    optimizeBtn.setEnabled(true);
                    optimizeBtn.setText("✨ Optimize with Gemini");
                }
            }
        }.execute();
    }

    private void forwardOptimizedToTelegram() {
        if (lastOptimized == null || lastOptimized.isEmpty()) return;

        telegramBtn.setEnabled(false);
        telegramBtn.setText("Sending...");
        dashboard.setStatus("Forwarding optimized prompt to Telegram...");

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    TelegramNotifier telegram = new TelegramNotifier();
                    String alert = "✨ <b>Gemini Optimized Prompt Alert</b>\n\n" +
                                   "<b>Original:</b>\n" + originalArea.getText().trim() + "\n\n" +
                                   "<b>Optimized:</b>\n" + lastOptimized;
                    return telegram.sendAlert(alert);
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        dashboard.setStatus("✅ Optimized prompt sent to Telegram!");
                        JOptionPane.showMessageDialog(OptimizerPanel.this,
                            "Successfully forwarded the optimized prompt to Telegram!",
                            "Telegram Alert Sent", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        dashboard.setStatus("❌ Failed to send optimized prompt.");
                        JOptionPane.showMessageDialog(OptimizerPanel.this,
                            "Could not deliver notification to Telegram bot.",
                            "Delivery Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    dashboard.setStatus("❌ Delivery error: " + e.getMessage());
                } finally {
                    telegramBtn.setEnabled(true);
                    telegramBtn.setText("📤 Send to Telegram");
                }
            }
        }.execute();
    }
}
