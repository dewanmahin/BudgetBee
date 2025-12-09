package org.example;

import org.example.model.Expense;
import org.example.tools.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BudgetBee extends JFrame {
    private static BudgetBee instance;

    // UI Components
    private JTextField descriptionField, amountField, quantityField;
    private JComboBox<String> categoryCombo;
    private DefaultTableModel tableModel;
    private JLabel totalLabel, quantityLabel, averageLabel;
    private JPanel chartPanel;
    private JTable table;

    // Data
    private double total = 0;
    private int totalItems = 0;
    private Map<String, Double> categoryTotals = new HashMap<>();
    private final Color[] categoryColors = {
            new Color(255, 99, 132), new Color(54, 162, 235), new Color(255, 206, 86),
            new Color(75, 192, 192), new Color(153, 102, 255), new Color(255, 159, 64)
    };

    // Patterns
    private CommandInvoker commandInvoker = new CommandInvoker();
    private ChartStrategy chartStrategy = new PieChartStrategy();
    private boolean isRecalculating = false;

    private BudgetBee() {
        setTitle("💰 BudgetBee");
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Initialize Categories
        String[] categories = {"Food", "Transport", "Shopping", "Entertainment", "Bills", "Other"};
        for (String c : categories) categoryTotals.put(c, 0.0);

        // Layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 245, 255));
        add(mainPanel);

        // Header
        JLabel header = new JLabel("BudgetBee");
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(new Color(44, 62, 80));
        mainPanel.add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Date", "Desc", "Category", "Qty", "Amount", "Total"}, 0);
        table = new JTable(tableModel);
        customizeTable(table);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel fieldsPanel = new JPanel(new GridLayout(1, 10, 10, 10)); // 10 cols for buttons

        descriptionField = createField("Description");
        categoryCombo = new JComboBox<>(categories);
        quantityField = createField("Quantity");
        amountField = createField("Amount");

        JButton btnAdd = createBtn("Add", new Color(76, 175, 80));
        JButton btnSave = createBtn("Save", new Color(33, 150, 243));
        JButton btnDel = createBtn("Del", new Color(244, 67, 54));
        JButton btnView = createBtn("View Bars", new Color(156, 39, 176));
        JButton btnUndo = createBtn("Undo", new Color(255, 193, 7));
        JButton btnReport = createBtn("Report", new Color(96, 125, 139)); // Composite Button

        // Add Components
        fieldsPanel.add(descriptionField); fieldsPanel.add(categoryCombo);
        fieldsPanel.add(quantityField); fieldsPanel.add(amountField);
        fieldsPanel.add(btnAdd); fieldsPanel.add(btnSave); fieldsPanel.add(btnDel);
        fieldsPanel.add(btnView); fieldsPanel.add(btnUndo); fieldsPanel.add(btnReport);

        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        totalLabel = createLabel("Total: $0.00", new Color(192, 57, 43));
        quantityLabel = createLabel("Items: 0", new Color(41, 128, 185));
        averageLabel = createLabel("Avg: $0.00", new Color(39, 174, 96));
        statsPanel.add(totalLabel); statsPanel.add(quantityLabel); statsPanel.add(averageLabel);

        inputPanel.add(fieldsPanel);
        inputPanel.add(statsPanel);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Chart Panel
        chartPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                chartStrategy.drawChart(g, this, total, categoryTotals, categoryColors);
            }
        };
        chartPanel.setPreferredSize(new Dimension(350, 0));
        mainPanel.add(chartPanel, BorderLayout.EAST);

        // ===== EVENTS =====

        // 1. Factory Pattern (Switch View)
        btnView.addActionListener(e -> {
            boolean isPie = (chartStrategy instanceof PieChartStrategy);
            setChartStrategy(ChartFactory.create(!isPie));
            btnView.setText(isPie ? "View Pie" : "View Bars");
        });

        // 2. Command Pattern (Add/Del/Undo)
        btnAdd.addActionListener(e -> addExpenseCommand());
        btnDel.addActionListener(e -> deleteCommand());
        btnUndo.addActionListener(e -> commandInvoker.undo());

        // 3. Composite Pattern (Report)
        btnReport.addActionListener(e -> showCompositeReport());

        // 4. Utils
        btnSave.addActionListener(e -> saveData());

        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 5) return;
            recalculateAll();
        });

        loadData();
    }

    // ===== SINGLETON =====
    public static synchronized BudgetBee getInstance() {
        if (instance == null) instance = new BudgetBee();
        return instance;
    }

    // ===== COMPOSITE PATTERN LOGIC =====
    private void showCompositeReport() {
        // Create Groups
        CategoryComponent budget = new CategoryGroup("Total Budget");
        CategoryComponent needs = new CategoryGroup("Needs");
        CategoryComponent wants = new CategoryGroup("Wants");

        // Add Leaves directly (Hardcoded mapping for simplicity)
        // Ensure you use safety checks (getOrDefault) to prevent null pointers
        needs.add(new CategoryLeaf("Food", categoryTotals.getOrDefault("Food", 0.0)));
        needs.add(new CategoryLeaf("Bills", categoryTotals.getOrDefault("Bills", 0.0)));
        needs.add(new CategoryLeaf("Transport", categoryTotals.getOrDefault("Transport", 0.0)));

        wants.add(new CategoryLeaf("Shopping", categoryTotals.getOrDefault("Shopping", 0.0)));
        wants.add(new CategoryLeaf("Entertainment", categoryTotals.getOrDefault("Entertainment", 0.0)));
        wants.add(new CategoryLeaf("Other", categoryTotals.getOrDefault("Other", 0.0)));

        // Build Tree
        budget.add(needs);
        budget.add(wants);

        // Show Result
        JOptionPane.showMessageDialog(this,
                String.format("Needs: $%.2f\nWants: $%.2f\n\nTotal: $%.2f",
                        needs.getAmount(), wants.getAmount(), budget.getAmount()),
                "Group Report", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== COMMAND METHODS =====
    private void addExpenseCommand() {
        try {
            String d = descriptionField.getText();
            int q = Integer.parseInt(quantityField.getText().equals("Quantity") ? "1" : quantityField.getText());
            double a = Double.parseDouble(amountField.getText().equals("Amount") ? "0" : amountField.getText());
            String c = (String) categoryCombo.getSelectedItem();
            String date = new SimpleDateFormat("MMM dd").format(new Date());

            Object[] row = {date, d, c, q, "৳" + String.format("%.2f", a), "৳" + String.format("%.2f", q * a)};
            commandInvoker.execute(new AddExpenseCommand(tableModel, row));
            resetFields();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Check inputs"); }
    }

    private void deleteCommand() {
        if (table.getSelectedRow() != -1)
            commandInvoker.execute(new DeleteExpenseCommand(tableModel, table.getSelectedRow()));
    }

    // ===== CORE LOGIC =====
    public void setChartStrategy(ChartStrategy s) {
        this.chartStrategy = s;
        chartPanel.repaint();
    }

    private void recalculateAll() {
        if (isRecalculating) return;
        isRecalculating = true;
        try {
            total = 0; totalItems = 0;
            categoryTotals.replaceAll((k, v) -> 0.0);

            ExpenseIterator it = new TableModelExpenseIterator(tableModel);
            int i = 0;
            while (it.hasNext()) {
                Expense e = it.next();
                total += e.getTotal();
                totalItems += e.quantity;
                categoryTotals.put(e.category, categoryTotals.get(e.category) + e.getTotal());
                if (i < tableModel.getRowCount())
                    tableModel.setValueAt("৳" + String.format("%.2f", e.getTotal()), i++, 5);
            }
            updateStats();
            chartPanel.repaint();
        } finally { isRecalculating = false; }
    }

    private void updateStats() {
        totalLabel.setText(String.format("Total: $%.2f", total));
        quantityLabel.setText(String.format("Items: %d", totalItems));
        averageLabel.setText(String.format("Avg: $%.2f", totalItems > 0 ? total / totalItems : 0));
    }

    // ===== FILE I/O =====
    private void saveData() {
        try (PrintWriter w = new PrintWriter("expenses.csv")) {
            w.println("Date,Description,Category,Quantity,Amount,Total"); // Header
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String row = "";
                for(int j=0; j<6; j++) row += tableModel.getValueAt(i, j) + (j<5?",":"");
                w.println(row);
            }
        } catch (Exception e) {}
    }

    private void loadData() {
        File f = new File("expenses.csv");
        if (!f.exists()) return;
        if (isRecalculating) return;
        isRecalculating = true;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            r.readLine(); // Skip header
            String line;
            while ((line = r.readLine()) != null) tableModel.addRow(line.split(","));
        } catch (Exception e) {}
        finally { isRecalculating = false; recalculateAll(); }
    }

    // ===== UI HELPERS =====
    private JTextField createField(String txt) {
        JTextField f = new JTextField(txt);
        f.setForeground(Color.GRAY);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (f.getText().equals(txt)) { f.setText(""); f.setForeground(Color.BLACK); }}
            public void focusLost(FocusEvent e) { if (f.getText().isEmpty()) { f.setText(txt); f.setForeground(Color.GRAY); }}
        });
        return f;
    }
    private JButton createBtn(String t, Color c) {
        JButton b = new JButton(t); b.setBackground(c); b.setForeground(Color.BLACK);
        return b;
    }
    private JLabel createLabel(String t, Color c) {
        JLabel l = new JLabel(t, SwingConstants.CENTER); l.setFont(new Font("Segoe UI", Font.BOLD, 16)); l.setForeground(c);
        l.setBorder(new LineBorder(new Color(200, 210, 230)));
        return l;
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
