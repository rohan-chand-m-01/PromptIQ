package ui;

import database.AnalysisDAO;
import database.PromptDAO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DatabasePanel extends JPanel {

    private DashboardFrame dashboard;
    private JComboBox<String> tableSelector;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JLabel rowCountLabel;

    public DatabasePanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(0x0F, 0x17, 0x2A)); // Dark Navy
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== TOP: Control Card =====
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        controlPanel.setBackground(new Color(0x1E, 0x29, 0x3B)); // Card Background
        controlPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x33, 0x41, 0x55), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));

        JLabel viewLabel = new JLabel("🗄️ Select DB Table:");
        viewLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewLabel.setForeground(new Color(0xE2, 0xE8, 0xF0));
        controlPanel.add(viewLabel);

        tableSelector = new JComboBox<>(new String[]{
            "prompts + optimized", "analysis", "notifications", "joined view"
        });
        tableSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableSelector.setBackground(new Color(0x0F, 0x17, 0x2A));
        tableSelector.setForeground(Color.WHITE);
        tableSelector.setBorder(new LineBorder(new Color(0x33, 0x41, 0x55), 1));
        controlPanel.add(tableSelector);

        ModernButton loadBtn = new ModernButton("🔄 Load Table Data", new Color(0x3B, 0x82, 0xF6), Color.WHITE); // Blue
        loadBtn.addActionListener(e -> loadSelectedTable());
        controlPanel.add(loadBtn);

        ModernButton clearBtn = new ModernButton("🗑️ Clear View", new Color(0x33, 0x41, 0x55), Color.WHITE);
        clearBtn.addActionListener(e -> {
            tableModel.setRowCount(0);
            rowCountLabel.setText("Rows loaded: 0");
            dashboard.setStatus("Table cleared from view.");
        });
        controlPanel.add(clearBtn);

        add(controlPanel, BorderLayout.NORTH);

        // ===== CENTER: Data Table =====
        JPanel tableContainer = new JPanel(new BorderLayout(5, 5));
        tableContainer.setOpaque(false);

        JLabel tableTitleLabel = new JLabel("📋 Database Record Telemetry");
        tableTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitleLabel.setForeground(Color.WHITE);
        tableContainer.add(tableTitleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel() {
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
        dataTable.setRowHeight(26);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Header Customization
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        dataTable.getTableHeader().setBackground(new Color(0x0F, 0x17, 0x2A));
        dataTable.getTableHeader().setForeground(new Color(0x3B, 0x82, 0xF6));
        dataTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x33, 0x41, 0x55)));

        // Alternate row colors and styles
        dataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.getViewport().setBackground(new Color(0x0F, 0x17, 0x2A));
        scrollPane.setBorder(new LineBorder(new Color(0x33, 0x41, 0x55), 1));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        // ===== BOTTOM: Row Count + Export =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        rowCountLabel = new JLabel("Rows loaded: 0");
        rowCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rowCountLabel.setForeground(new Color(0x94, 0xA3, 0xB8));
        bottomPanel.add(rowCountLabel, BorderLayout.WEST);

        ModernButton exportBtn = new ModernButton("📤 Export SQL Commands", new Color(0x10, 0xB9, 0x81), Color.WHITE); // Emerald Green
        exportBtn.setPreferredSize(new Dimension(195, 34));
        exportBtn.addActionListener(e -> exportSQL());
        bottomPanel.add(exportBtn, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Auto load prompts + optimized table on startup
        loadSelectedTable();
    }

    private void loadSelectedTable() {
        String selected = (String) tableSelector.getSelectedItem();
        if (selected == null) return;

        System.out.println("[DatabasePanel] Loading table: " + selected);
        dashboard.setStatus("Loading " + selected + "...");

        new SwingWorker<Void, Void>() {
            private List<Object[]> data;
            private String[] columns;

            @Override
            protected Void doInBackground() {
                PromptDAO promptDAO = new PromptDAO();
                AnalysisDAO analysisDAO = new AnalysisDAO();

                switch (selected) {
                    case "prompts + optimized":
                        columns = new String[]{"ID", "Original Prompt", "Optimized Prompt", "Created At"};
                        data = promptDAO.getAllPrompts();
                        break;
                    case "analysis":
                        columns = new String[]{"ID", "Prompt ID", "Tokens", "Complexity", "Model", "Cost", "Suggestion", "Date"};
                        data = analysisDAO.getAllAnalysis();
                        break;
                    case "notifications":
                        columns = new String[]{"ID", "Analysis ID", "Message", "Status", "Sent Time"};
                        data = analysisDAO.getAllNotifications();
                        break;
                    case "joined view":
                        columns = new String[]{"Prompt ID", "Original Prompt", "Tokens", "Complexity", "Model", "Cost", "Suggestion"};
                        data = promptDAO.getJoinedAnalysis();
                        break;
                    default:
                        columns = new String[]{};
                        data = new java.util.ArrayList<>();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions

                    tableModel.setColumnCount(0);
                    tableModel.setRowCount(0);

                    for (String col : columns) {
                        tableModel.addColumn(col);
                    }

                    if (data != null) {
                        for (Object[] row : data) {
                            tableModel.addRow(row);
                        }
                    }

                    int rowCount = data != null ? data.size() : 0;
                    rowCountLabel.setText("Rows loaded: " + rowCount);
                    System.out.println("[DatabasePanel] Rows loaded: " + rowCount);
                    dashboard.setStatus("✅ Loaded " + rowCount + " rows from " + selected);
                } catch (Exception ex) {
                    System.err.println("[DatabasePanel] Error loading data: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(DatabasePanel.this,
                        "Failed to load data: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                    dashboard.setStatus("Failed to load data.");
                }
            }
        }.execute();
    }

    private void exportSQL() {
        String selected = (String) tableSelector.getSelectedItem();
        if (selected == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("-- Exported from AI Credit Cost Analyzer\n");
        sb.append("-- Table: ").append(selected).append("\n\n");

        // Show the query
        switch (selected) {
            case "prompts + optimized":
                sb.append("SELECT p.prompt_id, p.original_prompt, op.optimized_prompt, p.created_at\n");
                sb.append("FROM prompts p\n");
                sb.append("LEFT JOIN optimized_prompts op ON p.prompt_id = op.prompt_id\n");
                sb.append("ORDER BY p.created_at DESC;\n\n");
                break;
            case "analysis":
                sb.append("SELECT * FROM analysis ORDER BY analyzed_at DESC;\n\n");
                break;
            case "notifications":
                sb.append("SELECT * FROM notifications ORDER BY sent_time DESC;\n\n");
                break;
            case "joined view":
                sb.append("SELECT p.prompt_id, p.original_prompt, a.token_count, a.complexity,\n");
                sb.append("       a.recommended_model, a.cost_category, a.suggestion\n");
                sb.append("FROM prompts p\n");
                sb.append("JOIN analysis a ON p.prompt_id = a.prompt_id\n");
                sb.append("ORDER BY a.analyzed_at DESC;\n\n");
                break;
        }

        // Append data rows
        sb.append("-- Results (").append(tableModel.getRowCount()).append(" rows):\n");
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            sb.append("-- ");
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                if (col > 0) sb.append(" | ");
                Object val = tableModel.getValueAt(row, col);
                sb.append(val != null ? val.toString() : "NULL");
            }
            sb.append("\n");
        }

        JTextArea exportArea = new JTextArea(sb.toString());
        exportArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        exportArea.setBackground(new Color(0x0F, 0x17, 0x2A));
        exportArea.setForeground(Color.WHITE);
        exportArea.setCaretColor(Color.WHITE);
        exportArea.setLineWrap(true);
        exportArea.setWrapStyleWord(true);
        exportArea.setEditable(false);
        exportArea.setRows(20);
        exportArea.setColumns(60);

        JScrollPane scrollPane = new JScrollPane(exportArea);
        scrollPane.setBorder(new LineBorder(new Color(0x33, 0x41, 0x55), 1));

        JOptionPane.showMessageDialog(this,
            scrollPane,
            "SQL Export - " + selected,
            JOptionPane.INFORMATION_MESSAGE);
    }
}
