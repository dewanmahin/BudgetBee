package org.example;

import org.example.tools.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BudgetBee extends JFrame {
    private static BudgetBee instance;

    // UI Components
    private JTextField descriptionField, amountField, quantityField;
    private JComboBox<String> categoryCombo;
    private DefaultTableModel tableModel;
    private JLabel totalLabel, quantityLabel, averageLabel;
    private JPanel chartPanel;
    private JTable table;

    // Patterns & Logic
    private BudgetManagerFacade facade;
    private ChartStrategy chartStrategy = new PieChartStrategy();

    // Modern Color Palette
    private final Color BG_COLOR = new Color(245, 247, 250); // Light Gray Background
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(44, 62, 80);
    private final Color TEXT_SECONDARY = new Color(149, 165, 166);
    private final Color ACCENT_BLUE = new Color(64, 123, 255);

    // Chart Colors
    private final Color[] categoryColors = {
            new Color(255, 107, 107), new Color(78, 205, 196), new Color(255, 230, 109),
            new Color(26, 83, 92), new Color(85, 98, 112), new Color(199, 244, 100)
    };

    private BudgetBee() {
        setTitle("BudgetBee - Modern Expense Tracker");
        setSize(1250, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 1. Main Layout
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(BG_COLOR);
        add(mainPanel);

        // 2. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        JLabel title = new JLabel("BudgetBee");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Smart Financial Tracking");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SECONDARY);
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 3. Table (Center) - Modern Look
        tableModel = new DefaultTableModel(new String[]{"Date", "Description", "Category", "Qty", "Amount", "Total"}, 0);
        table = new JTable(tableModel);
        customizeTable(table);

        // ScrollPane with Card Style
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
        tableCard.add(scrollPane);

        mainPanel.add(tableCard, BorderLayout.CENTER);

        // 4. Initialize Logic
        facade = new BudgetManagerFacade(tableModel);

        // 5. Bottom Section (Inputs + Buttons + Stats)
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setBackground(BG_COLOR);

        // --- ROW 1: INPUTS ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.weighty = 0;

        descriptionField = createModernField("Description");
        categoryCombo = createModernCombo(new String[]{"Food", "Transport", "Shopping", "Entertainment", "Bills", "Other"});
        quantityField = createModernField("Qty");
        amountField = createModernField("Amount");

        gbc.gridx = 0; gbc.weightx = 0.4; inputPanel.add(descriptionField, gbc);
        gbc.gridx = 1; gbc.weightx = 0.3; inputPanel.add(categoryCombo, gbc);
        gbc.gridx = 2; gbc.weightx = 0.15; inputPanel.add(quantityField, gbc);
        gbc.gridx = 3; gbc.weightx = 0.15; inputPanel.add(amountField, gbc);

        // --- ROW 2: BUTTONS ---
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 6, 10, 0));
        buttonsPanel.setBackground(BG_COLOR);
        buttonsPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        JButton btnAdd = createModernBtn("Add", new Color(46, 204, 113));
        JButton btnSave = createModernBtn("Save", new Color(52, 152, 219));
        JButton btnDel = createModernBtn("Delete", new Color(231, 76, 60));
        JButton btnView = createModernBtn("Switch View", new Color(155, 89, 182));
        JButton btnUndo = createModernBtn("Undo", new Color(241, 196, 15));
        JButton btnReport = createModernBtn("Report", new Color(52, 73, 94));

        buttonsPanel.add(btnAdd); buttonsPanel.add(btnSave); buttonsPanel.add(btnDel);
        buttonsPanel.add(btnView); buttonsPanel.add(btnUndo); buttonsPanel.add(btnReport);

        // --- ROW 3: STATS (Fixed Height) ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(BG_COLOR);

        // FIX: Force the panel to be shorter (80px height)
        statsPanel.setPreferredSize(new Dimension(1000, 80));
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        totalLabel = createStatCard("TOTAL SPENT", "$0.00", new Color(255, 107, 107));
        quantityLabel = createStatCard("TOTAL ITEMS", "0", new Color(78, 205, 196));
        averageLabel = createStatCard("AVERAGE COST", "$0.00", new Color(255, 206, 86));

        statsPanel.add(totalLabel);
        statsPanel.add(quantityLabel);
        statsPanel.add(averageLabel);

        // Assemble Bottom
        bottomContainer.add(inputPanel);
        bottomContainer.add(buttonsPanel);
        bottomContainer.add(Box.createVerticalStrut(10)); // Add small gap
        bottomContainer.add(statsPanel);

        mainPanel.add(bottomContainer, BorderLayout.SOUTH);

        // 6. Chart Section (Right)
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Rounded Card
                chartStrategy.drawChart(g, this, facade.getTotal(), facade.getCategoryTotals(), categoryColors);
            }
        };
        chartPanel.setPreferredSize(new Dimension(400, 0));
        chartPanel.setBackground(BG_COLOR);
        chartPanel.setBorder(new EmptyBorder(0, 15, 0, 0));
        mainPanel.add(chartPanel, BorderLayout.EAST);

        // ===== EVENT LISTENERS =====

        // --- UPDATED ADD BUTTON LOGIC ---
        btnAdd.addActionListener(e -> {
            String desc = descriptionField.getText().trim();
            String qtyStr = quantityField.getText().trim();
            String amtStr = amountField.getText().trim();

            // 1. Validation: Check if fields are empty or still have placeholders
            if (desc.isEmpty() || desc.equals("Description") ||
                    qtyStr.isEmpty() || qtyStr.equals("Qty") ||
                    amtStr.isEmpty() || amtStr.equals("Amount")) {

                JOptionPane.showMessageDialog(this,
                        "Please fill in all fields (Description, Qty, Amount).",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return; // STOP execution
            }

            try {
                // 2. Parse Numbers
                int q = Integer.parseInt(qtyStr);
                double a = Double.parseDouble(amtStr);

                if (q <= 0 || a <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity and Amount must be positive numbers.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 3. Add Data
                String c = (String) categoryCombo.getSelectedItem();
                String date = new SimpleDateFormat("MMM dd").format(new Date());
                facade.addExpense(date, desc, c, q, a);

                // 4. Clear Fields
                resetFields();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for Quantity and Amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDel.addActionListener(e -> facade.deleteExpense(table.getSelectedRow()));
        btnUndo.addActionListener(e -> facade.undo());
        btnSave.addActionListener(e -> facade.saveData(new File("expenses.csv")));

        btnView.addActionListener(e -> {
            boolean isPie = (chartStrategy instanceof PieChartStrategy);
            chartStrategy = ChartFactory.create(isPie);
            btnView.setText(isPie ? "View Pie" : "View Bars");
            chartPanel.repaint();
        });

        btnReport.addActionListener(e -> showCompositeReport());

        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 5) return;
            facade.recalculateAll();
            updateUIStats();
        });

        facade.loadData(new File("expenses.csv"));
        updateUIStats();
    }

    public static synchronized BudgetBee getInstance() {
        if (instance == null) instance = new BudgetBee();
        return instance;
    }

    private void updateUIStats() {
        updateStatCardText(totalLabel, "TOTAL SPENT", String.format("$%.2f", facade.getTotal()), new Color(255, 107, 107));
        updateStatCardText(quantityLabel, "TOTAL ITEMS", String.valueOf(facade.getTotalItems()), new Color(78, 205, 196));
        double avg = facade.getTotalItems() > 0 ? facade.getTotal() / facade.getTotalItems() : 0;
        updateStatCardText(averageLabel, "AVERAGE COST", String.format("$%.2f", avg), new Color(255, 206, 86));
        chartPanel.repaint();
    }

    private void showCompositeReport() {
        CategoryComponent budget = new CategoryGroup("Total Budget");
        CategoryComponent needs = new CategoryGroup("Needs");
        CategoryComponent wants = new CategoryGroup("Wants");
        var totals = facade.getCategoryTotals();

        needs.add(new CategoryLeaf("Food", totals.getOrDefault("Food", 0.0)));
        needs.add(new CategoryLeaf("Bills", totals.getOrDefault("Bills", 0.0)));
        needs.add(new CategoryLeaf("Transport", totals.getOrDefault("Transport", 0.0)));
        wants.add(new CategoryLeaf("Shopping", totals.getOrDefault("Shopping", 0.0)));
        wants.add(new CategoryLeaf("Entertainment", totals.getOrDefault("Entertainment", 0.0)));
        wants.add(new CategoryLeaf("Other", totals.getOrDefault("Other", 0.0)));

        budget.add(needs);
        budget.add(wants);

        JOptionPane.showMessageDialog(this,
                String.format("Needs: $%.2f\nWants: $%.2f\n\nTotal: $%.2f",
                        needs.getAmount(), wants.getAmount(), budget.getAmount()),
                "Group Report", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== UI HELPERS (MODERN STYLE) =====

    private void customizeTable(JTable t) {
        t.setRowHeight(50); // Taller rows
        t.setShowVerticalLines(false); // No vertical grid
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setGridColor(new Color(240, 240, 240));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setSelectionBackground(new Color(232, 240, 254)); // Soft Blue Selection
        t.setSelectionForeground(Color.BLACK);

        // Modern Header
        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(Color.WHITE);
                setForeground(new Color(120, 120, 120));
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));
                setHorizontalAlignment(JLabel.CENTER);
                return this;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }

    private JTextField createModernField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(Color.GRAY);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT_PRIMARY); }
                f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ACCENT_BLUE, 1, true), new EmptyBorder(10, 10, 10, 10)));
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(Color.GRAY); }
                f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(220, 220, 220), 1, true), new EmptyBorder(10, 10, 10, 10)));
            }
        });
        return f;
    }

    private JComboBox<String> createModernCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        return combo;
    }

    private JButton createModernBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel createStatCard(String title, String value, Color accent) {
        JLabel l = new JLabel();
        l.setOpaque(true);
        l.setBackground(CARD_BG);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
        updateStatCardText(l, title, value, accent);
        return l;
    }

    private void updateStatCardText(JLabel label, String title, String value, Color accent) {
        String hex = String.format("#%02x%02x%02x", accent.getRed(), accent.getGreen(), accent.getBlue());
        label.setText("<html><div style='text-align: center; padding: 15px;'>" +
                "<span style='font-size: 11px; color: gray; letter-spacing: 1px;'>" + title + "</span><br/>" +
                "<span style='font-size: 24px; color: " + hex + "; font-weight: bold;'>" + value + "</span></div></html>");
    }

    private void resetFields() {
        descriptionField.setText("Description");
        descriptionField.setForeground(Color.GRAY);
        quantityField.setText("Qty");
        quantityField.setForeground(Color.GRAY);
        amountField.setText("Amount");
        amountField.setForeground(Color.GRAY);
    }
}
