package org.example;

import org.example.tools.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
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

    // PATTERNS
    private BudgetManagerFacade facade; // <--- The Facade handles logic now
    private ChartStrategy chartStrategy = new PieChartStrategy();

    private final Color[] categoryColors = {
            new Color(255, 99, 132), new Color(54, 162, 235), new Color(255, 206, 86),
            new Color(75, 192, 192), new Color(153, 102, 255), new Color(255, 159, 64)
    };

    private BudgetBee() {
        setTitle("💰 BudgetBee");
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 1. Setup UI Layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 245, 255));
        add(mainPanel);

        // 2. Setup Table
        tableModel = new DefaultTableModel(new String[]{"Date", "Desc", "Category", "Qty", "Amount", "Total"}, 0);
        table = new JTable(tableModel);
        customizeTable(table);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. Initialize Facade (Must be after tableModel is created)
        facade = new BudgetManagerFacade(tableModel);

        // 4. Input & Button Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel fieldsPanel = new JPanel(new GridLayout(1, 10, 10, 10));

        descriptionField = createField("Description");
        categoryCombo = new JComboBox<>(new String[]{"Food", "Transport", "Shopping", "Entertainment", "Bills", "Other"});
        quantityField = createField("Quantity");
        amountField = createField("Amount");

        JButton btnAdd = createBtn("Add", new Color(76, 175, 80));
        JButton btnSave = createBtn("Save", new Color(33, 150, 243));
        JButton btnDel = createBtn("Del", new Color(244, 67, 54));
        JButton btnView = createBtn("View Bars", new Color(156, 39, 176));
        JButton btnUndo = createBtn("Undo", new Color(255, 193, 7));
        JButton btnReport = createBtn("Report", new Color(96, 125, 139));

        fieldsPanel.add(descriptionField); fieldsPanel.add(categoryCombo);
        fieldsPanel.add(quantityField); fieldsPanel.add(amountField);
        fieldsPanel.add(btnAdd); fieldsPanel.add(btnSave); fieldsPanel.add(btnDel);
        fieldsPanel.add(btnView); fieldsPanel.add(btnUndo); fieldsPanel.add(btnReport);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        totalLabel = createLabel("Total: $0.00", new Color(192, 57, 43));
        quantityLabel = createLabel("Items: 0", new Color(41, 128, 185));
        averageLabel = createLabel("Avg: $0.00", new Color(39, 174, 96));
        statsPanel.add(totalLabel); statsPanel.add(quantityLabel); statsPanel.add(averageLabel);

        inputPanel.add(fieldsPanel); inputPanel.add(statsPanel);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // 5. Header & Chart
        JLabel header = new JLabel("BudgetBee");
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(new Color(44, 62, 80));
        mainPanel.add(header, BorderLayout.NORTH);

        chartPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw chart using data from Facade
                chartStrategy.drawChart(g, this, facade.getTotal(), facade.getCategoryTotals(), categoryColors);
            }
        };
        chartPanel.setPreferredSize(new Dimension(350, 0));
        mainPanel.add(chartPanel, BorderLayout.EAST);

        // ===== EVENT LISTENERS (Delegating to Facade) =====

        // Add
        btnAdd.addActionListener(e -> {
            try {
                String d = descriptionField.getText();
                int q = Integer.parseInt(quantityField.getText().equals("Quantity") ? "1" : quantityField.getText());
                double a = Double.parseDouble(amountField.getText().equals("Amount") ? "0" : amountField.getText());
                String c = (String) categoryCombo.getSelectedItem();
                String date = new SimpleDateFormat("MMM dd").format(new Date());

                facade.addExpense(date, d, c, q, a); // Facade call
                resetFields();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Check inputs"); }
        });

        // Delete & Undo
        btnDel.addActionListener(e -> facade.deleteExpense(table.getSelectedRow()));
        btnUndo.addActionListener(e -> facade.undo());

        // File I/O
        btnSave.addActionListener(e -> facade.saveData(new File("expenses.csv")));

        // View Switch (Factory)
        btnView.addActionListener(e -> {
            boolean isPie = (chartStrategy instanceof PieChartStrategy);
            chartStrategy = ChartFactory.create(isPie); // Switch Logic
            btnView.setText(isPie ? "View Pie" : "View Bars");
            chartPanel.repaint();
        });

        // Composite Report
        btnReport.addActionListener(e -> showCompositeReport());

        // Table Updates
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 5) return;
            facade.recalculateAll(); // Facade handles math
            updateUIStats();         // UI just updates text
        });

        // Initial Load
        facade.loadData(new File("expenses.csv"));
        updateUIStats();
    }

    public static synchronized BudgetBee getInstance() {
        if (instance == null) instance = new BudgetBee();
        return instance;
    }

    private void updateUIStats() {
        totalLabel.setText(String.format("Total: $%.2f", facade.getTotal()));
        quantityLabel.setText(String.format("Items: %d", facade.getTotalItems()));
        double avg = facade.getTotalItems() > 0 ? facade.getTotal() / facade.getTotalItems() : 0;
        averageLabel.setText(String.format("Avg: $%.2f", avg));
        chartPanel.repaint();
    }

    private void showCompositeReport() {
        // ... (Keep your composite logic here, calling facade.getCategoryTotals() if needed) ...
        CategoryComponent budget = new CategoryGroup("Total Budget");
        CategoryComponent needs = new CategoryGroup("Needs");
        CategoryComponent wants = new CategoryGroup("Wants");

        var totals = facade.getCategoryTotals(); // Get data from Facade

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

    // ===== UI HELPERS (No Logic Here) =====
    private JTextField createField(String txt) {
        JTextField f = new JTextField(txt); f.setForeground(Color.GRAY);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (f.getText().equals(txt)) { f.setText(""); f.setForeground(Color.BLACK); }}
            public void focusLost(FocusEvent e) { if (f.getText().isEmpty()) { f.setText(txt); f.setForeground(Color.GRAY); }}
        });
        return f;
    }
    private JButton createBtn(String t, Color c) {
        JButton b = new JButton(t); b.setBackground(c); b.setForeground(Color.BLACK); return b;
    }
    private JLabel createLabel(String t, Color c) {
        JLabel l = new JLabel(t, SwingConstants.CENTER); l.setFont(new Font("Segoe UI", Font.BOLD, 16)); l.setForeground(c);
        l.setBorder(new LineBorder(new Color(200, 210, 230))); return l;
    }
    private void resetFields() {
        descriptionField.setText("Description"); quantityField.setText("Quantity"); amountField.setText("Amount");
    }
    private void customizeTable(JTable t) {
        t.setRowHeight(30);
        DefaultTableCellRenderer c = new DefaultTableCellRenderer(); c.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(c);
    }
}
