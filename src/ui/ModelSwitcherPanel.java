package ui;

import service.ModelSelector;
import service.TokenAnalyzer;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ModelSwitcherPanel extends JPanel {

    private DashboardFrame dashboard;
    private JTextArea promptArea;
    private JProgressBar complexityBar;
    private JLabel modelNameLabel;
    private JLabel tokenCountLabel;
    private JLabel complexityLabel;
    private JPanel resultPanel;

    public ModelSwitcherPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(0x0F, 0x17, 0x2A)); // Dark Navy
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== TOP: Title Block =====
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("🔀 Smart Model Switcher");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel descLabel = new JLabel("Match your prompt complexity to the most cost-effective Gemini model version");
        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        descLabel.setForeground(new Color(0x94, 0xA3, 0xB8));
        headerPanel.add(descLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // ===== CENTER: Content Card =====
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(new Color(0x1E, 0x29, 0x3B));
        cardPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x33, 0x41, 0x55), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Prompt Input
        JLabel promptLabel = new JLabel("📝 Enter Prompt to Switch-Analyze:");
        promptLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        promptLabel.setForeground(new Color(0xE2, 0xE8, 0xF0));
        promptLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(promptLabel);
        cardPanel.add(Box.createVerticalStrut(8));

        promptArea = new JTextArea(4, 50);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        promptArea.setBackground(new Color(0x0F, 0x17, 0x2A));
        promptArea.setForeground(Color.WHITE);
        promptArea.setCaretColor(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(promptArea);
        scrollPane.setBorder(new LineBorder(new Color(0x33, 0x41, 0x55), 1));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        cardPanel.add(scrollPane);
        cardPanel.add(Box.createVerticalStrut(15));

        // Recommend Button
        ModernButton recommendBtn = new ModernButton("🔀 Recommend Best Model", new Color(0xEA, 0x58, 0x0C), Color.WHITE); // Flat orange-red accent
        recommendBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        recommendBtn.setMaximumSize(new Dimension(240, 36));
        recommendBtn.addActionListener(e -> onRecommendClicked(recommendBtn));
        cardPanel.add(recommendBtn);
        cardPanel.add(Box.createVerticalStrut(25));

        // ===== RESULT DISPLAY CARD =====
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(new Color(0x13, 0x1D, 0x31)); // Shaded inner panel
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x33, 0x41, 0x55), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.setVisible(false);

        // Complexity Progress Bar
        JLabel barLabel = new JLabel("Estimated Complexity Load:");
        barLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        barLabel.setForeground(Color.WHITE);
        barLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.add(barLabel);
        resultPanel.add(Box.createVerticalStrut(8));

        complexityBar = new JProgressBar(0, 100);
        complexityBar.setStringPainted(true);
        complexityBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        complexityBar.setBackground(new Color(0x0F, 0x17, 0x2A));
        complexityBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        complexityBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        complexityBar.setPreferredSize(new Dimension(500, 26));
        resultPanel.add(complexityBar);
        resultPanel.add(Box.createVerticalStrut(18));

        // Recommended Model
        JLabel recLabel = new JLabel("🤖 RECOMMENDED ENGINE:");
        recLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        recLabel.setForeground(new Color(0x94, 0xA3, 0xB8));
        recLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.add(recLabel);
        resultPanel.add(Box.createVerticalStrut(4));

        modelNameLabel = new JLabel("");
        modelNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        modelNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.add(modelNameLabel);
        resultPanel.add(Box.createVerticalStrut(15));

        // Footer details
        tokenCountLabel = new JLabel("Token Count: 0");
        tokenCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tokenCountLabel.setForeground(new Color(0x94, 0xA3, 0xB8));
        tokenCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.add(tokenCountLabel);
        resultPanel.add(Box.createVerticalStrut(5));

        complexityLabel = new JLabel("Complexity Level: N/A");
        complexityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        complexityLabel.setForeground(new Color(0x94, 0xA3, 0xB8));
        complexityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.add(complexityLabel);

        cardPanel.add(resultPanel);
        cardPanel.add(Box.createVerticalGlue());

        JScrollPane containerScroll = new JScrollPane(cardPanel);
        containerScroll.setBorder(null);
        containerScroll.setOpaque(false);
        containerScroll.getViewport().setOpaque(false);
        add(containerScroll, BorderLayout.CENTER);
    }

    private void onRecommendClicked(ModernButton btn) {
        String promptText = promptArea.getText().trim();
        if (promptText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a prompt to analyze.",
                "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btn.setEnabled(false);
        btn.setText("Analyzing...");
        dashboard.setStatus("Analyzing prompt load...");

        new SwingWorker<Void, Void>() {
            private int tokens;
            private String complexity;
            private String model;

            @Override
            protected Void doInBackground() {
                TokenAnalyzer tokenAnalyzer = new TokenAnalyzer();
                ModelSelector modelSelector = new ModelSelector();

                tokens = tokenAnalyzer.estimateTokens(promptText);
                complexity = tokenAnalyzer.getComplexity(tokens);
                model = modelSelector.recommendModel(tokens);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions

                    // Update progress bar
                    switch (complexity) {
                        case "LOW":
                            complexityBar.setValue(25);
                            complexityBar.setForeground(new Color(0x10, 0xB9, 0x81)); // Success green
                            complexityBar.setString("LOW COMPLEXITY (25%)");
                            break;
                        case "MEDIUM":
                            complexityBar.setValue(60);
                            complexityBar.setForeground(new Color(0xF5, 0x9E, 0x0B)); // Orange
                            complexityBar.setString("MEDIUM COMPLEXITY (60%)");
                            break;
                        case "HIGH":
                            complexityBar.setValue(100);
                            complexityBar.setForeground(new Color(0xEF, 0x44, 0x44)); // Crimson red
                            complexityBar.setString("HIGH COMPLEXITY (100%)");
                            break;
                    }

                    // Update model label with color
                    modelNameLabel.setText(model);
                    switch (model) {
                        case "Gemini Flash":
                            modelNameLabel.setForeground(new Color(0x3B, 0x82, 0xF6)); // Blue
                            break;
                        case "Gemini Pro":
                            modelNameLabel.setForeground(new Color(0x10, 0xB9, 0x81)); // Green
                            break;
                        case "Gemini Advanced":
                            modelNameLabel.setForeground(new Color(0xEF, 0x44, 0x44)); // Red
                            break;
                    }

                    tokenCountLabel.setText("Estimated Token Count: " + tokens);
                    complexityLabel.setText("Complexity Classification: " + complexity);

                    resultPanel.setVisible(true);
                    dashboard.setStatus("✅ Model recommendation complete.");
                } catch (Exception ex) {
                    System.err.println("[ModelSwitcherPanel] Error: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ModelSwitcherPanel.this,
                        "Recommendation failed: " + ex.getMessage(),
                        "Telemetry Error", JOptionPane.ERROR_MESSAGE);
                    dashboard.setStatus("Recommendation failed.");
                } finally {
                    btn.setEnabled(true);
                    btn.setText("🔀 Recommend Best Model");
                }
            }
        }.execute();
    }
}
