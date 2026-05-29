package ui;

import database.AnalysisDAO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;

public class DashboardFrame extends JFrame {

    private JLabel statusLabel;
    private JLabel totalPromptsValue;
    private JLabel avgTokensValue;
    private JLabel alertsSentValue;
    private JLabel topModelValue;
    private NotificationPanel notificationPanel;

    public DashboardFrame() {
        setTitle("AI Credit Cost Analyzer — Premium Dev edition");
        setSize(1240, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(0x0F, 0x17, 0x2A)); // Dark navy background
        setLayout(new BorderLayout());

        // ===== TOP HEADER WITH GRADIENT PAINT =====
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Create elegant purple-to-navy gradient
                GradientPaint gp = new GradientPaint(
                    new Point2D.Float(0, 0), new Color(0x31, 0x10, 0x7E), // Indigo/violet
                    new Point2D.Float(getWidth(), 0), new Color(0x0F, 0x17, 0x2A) // Dark Navy
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Bottom divider line
                g2d.setColor(new Color(0x33, 0x41, 0x55, 120));
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(new EmptyBorder(12, 25, 12, 25));

        JLabel titleLabel = new JLabel("⚡ AI CREDIT COST ANALYZER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel poweredByLabel = new JLabel("Powered by Gemini 2.5 Flash Lite & Telegram");
        poweredByLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        poweredByLabel.setForeground(new Color(0x94, 0xA3, 0xB8)); // Muted silver
        headerPanel.add(poweredByLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ===== CENTER WRAPPER: Stats + Tabs =====
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);

        // --- Stats Dashboard Panel ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        statsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        statsPanel.setOpaque(false);

        totalPromptsValue = new JLabel("0");
        avgTokensValue = new JLabel("0");
        alertsSentValue = new JLabel("0");
        topModelValue = new JLabel("N/A");

        statsPanel.add(createStatCard("TOTAL PROMPTS", totalPromptsValue, new Color(0x3B, 0x82, 0xF6), "📊"));
        statsPanel.add(createStatCard("AVERAGE TOKENS", avgTokensValue, new Color(0x8B, 0x5C, 0xF6), "🪙"));
        statsPanel.add(createStatCard("TELEGRAM ALERTS SENT", alertsSentValue, new Color(0x10, 0xB9, 0x81), "🔔"));
        statsPanel.add(createStatCard("TOP RECOMMENDED MODEL", topModelValue, new Color(0xF5, 0x9E, 0x0B), "🤖"));

        centerWrapper.add(statsPanel, BorderLayout.NORTH);

        // --- Tabbed Pane Custom Styling ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(new Color(0x1E, 0x29, 0x3B));
        tabbedPane.setForeground(new Color(0xE2, 0xE8, 0xF0));
        
        // Add Tabs
        tabbedPane.addTab("📈 Token Analyzer", new TokenPanel(this));
        tabbedPane.addTab("✨ Shadow Optimizer", new OptimizerPanel(this));
        tabbedPane.addTab("🔀 Smart Model Switcher", new ModelSwitcherPanel(this));
        
        notificationPanel = new NotificationPanel(this);
        tabbedPane.addTab("🔔 Telegram Manager", notificationPanel);
        
        tabbedPane.addTab("🗄️ Database Explorer", new DatabasePanel(this));

        // High contrast custom tab components to fix Windows System Look and Feel white-out issue
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            String title = tabbedPane.getTitleAt(i);
            JLabel tabLabel = new JLabel(title);
            tabLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            tabLabel.setForeground(new Color(0x33, 0x41, 0x55)); // High contrast dark slate gray for inactive tab text
            tabLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            tabbedPane.setTabComponentAt(i, tabLabel);
        }

        // Set active color for the initial selected tab
        if (tabbedPane.getTabCount() > 0) {
            JLabel activeLabel = (JLabel) tabbedPane.getTabComponentAt(0);
            if (activeLabel != null) {
                activeLabel.setForeground(new Color(0x25, 0x63, 0xEB)); // High-contrast Royal Blue for selected active text
                activeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            }
        }

        // Selection Listener to update active/inactive tab colors on selection changes
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Component c = tabbedPane.getTabComponentAt(i);
                if (c instanceof JLabel) {
                    JLabel lbl = (JLabel) c;
                    if (i == selectedIndex) {
                        lbl.setForeground(new Color(0x25, 0x63, 0xEB)); // Active blue text
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        lbl.setForeground(new Color(0x33, 0x41, 0x55)); // Inactive dark slate text
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
            }
        });

        // Custom borders for the tab pane
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 15, 15, 15),
            new LineBorder(new Color(0x33, 0x41, 0x55), 1, true)
        ));

        centerWrapper.add(tabbedPane, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // ===== BOTTOM STATUS BAR =====
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        statusBar.setPreferredSize(new Dimension(0, 32));
        statusBar.setBackground(new Color(0x0F, 0x17, 0x2A));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x33, 0x41, 0x55)));

        statusLabel = new JLabel("System loaded. Ready for prompt telemetry.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0x94, 0xA3, 0xB8));
        statusBar.add(statusLabel);

        add(statusBar, BorderLayout.SOUTH);

        // Load stats on startup
        refreshStats();
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String icon) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Glassmorphic drop border
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        card.setLayout(new BorderLayout(10, 5));
        card.setBackground(new Color(0x1E, 0x29, 0x3B)); // Dark card background
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x33, 0x41, 0x55), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(new Color(0x94, 0xA3, 0xB8)); // Muted silver/gray
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        card.add(textPanel, BorderLayout.CENTER);

        // Large icon label
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        iconLabel.setVerticalAlignment(JLabel.CENTER);
        card.add(iconLabel, BorderLayout.EAST);

        return card;
    }

    public void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }

    public void refreshStats() {
        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() {
                try {
                    AnalysisDAO dao = new AnalysisDAO();
                    return dao.getDashboardStats();
                } catch (Exception e) {
                    System.err.println("[DashboardFrame] Error loading stats: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    if (stats != null) {
                        totalPromptsValue.setText(String.valueOf(stats.getOrDefault("totalPrompts", 0)));
                        Object avg = stats.getOrDefault("avgTokens", 0.0);
                        if (avg instanceof Double) {
                            avgTokensValue.setText(String.format("%.1f", (Double) avg));
                        } else {
                            avgTokensValue.setText(String.valueOf(avg));
                        }
                        alertsSentValue.setText(String.valueOf(stats.getOrDefault("notificationsSent", 0)));
                        topModelValue.setText(String.valueOf(stats.getOrDefault("mostUsedModel", "N/A")));
                    }
                    // Also refresh notifications logs panel if active
                    if (notificationPanel != null) {
                        notificationPanel.loadNotificationLogs();
                    }
                } catch (Exception e) {
                    System.err.println("[DashboardFrame] Error displaying stats: " + e.getMessage());
                }
            }
        }.execute();
    }
}
