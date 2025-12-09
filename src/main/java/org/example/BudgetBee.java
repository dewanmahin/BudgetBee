package org.example;

import org.example.model.Expense;
import org.example.tools.*; // Imports Command, CommandInvoker, AddExpenseCommand, DeleteExpenseCommand, etc.

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// ===== Main Context Class (Singleton) =====
public class BudgetBee extends JFrame {
    private static BudgetBee instance;

    private JTextField descriptionField, amountField, quantityField;
    private JComboBox<String> categoryCombo;
    private DefaultTableModel tableModel;
    private JLabel totalLabel, quantityLabel, averageLabel;
    private JPanel chartPanel;
    private JTable table;

    private double total = 0;
    private int totalItems = 0;
    private Map<String, Double> categoryTotals = new HashMap<>();

    // ===== NEW: Command Invoker Instance =====
    private CommandInvoker commandInvoker = new CommandInvoker();

    // ===== Flag to prevent infinite loops during recalculation =====
    private boolean isRecalculating = false;

    private final Color[] categoryColors = {
            new Color(255, 99, 132), new Color(54, 162, 235),
            new Color(255, 206, 86), new Color(75, 192, 192),
            new Color(153, 102, 255), new Color(255, 159, 64)
    };

    // Strategy Pattern field
    private ChartStrategy chartStrategy = new PieChartStrategy(); // Default to Pie

    private BudgetBee() {
        setTitle("💰 BudgetBee");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        String[] categories = {"Food", "Transport", "Shopping", "Entertainment", "Bills", "Other"};
        for (String category : categories) {
            categoryTotals.put(category, 0.0);
        }

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 245, 255));
        add(mainPanel);

        JLabel header = new JLabel("BudgetBee");
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(new Color(44, 62, 80));
        mainPanel.add(header, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Date", "Description", "Category", "Quantity", "Amount", "Total"}, 0);
        table = new JTable(tableModel);
        customizeTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        inputPanel.setBackground(new Color(240, 245, 255));

        // Increased GridLayout columns to 9 to fit the Undo button
        JPanel fieldsPanel = new JPanel(new GridLayout(1, 9, 10, 10));
        descriptionField = createTextFieldWithPlaceholder("Description");
        categoryCombo = createCategoryCombo(categories);
        quantityField = createTextFieldWithPlaceholder("Quantity");
        amountField = createTextFieldWithPlaceholder("Amount");

        JButton addButton = createButton("Add", new Color(76, 175, 80));
        JButton saveButton = createButton("Save", new Color(33, 150, 243));
        JButton deleteButton = createButton("Delete", new Color(244, 67, 54));

        // Toggle Strategy Button
        JButton switchChartBtn = createButton("View Bars", new Color(156, 39, 176));

        // ===== NEW: Undo Button =====
        JButton undoButton = createButton("Undo", new Color(255, 193, 7)); // Yellow/Orange

        fieldsPanel.add(descriptionField);
        fieldsPanel.add(categoryCombo);
        fieldsPanel.add(quantityField);
        fieldsPanel.add(amountField);
        fieldsPanel.add(addButton);
        fieldsPanel.add(saveButton);
        fieldsPanel.add(deleteButton);
        fieldsPanel.add(switchChartBtn);
        fieldsPanel.add(undoButton); // Add Undo button to panel

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        totalLabel = createStatLabel("Total: $0.00", new Color(192, 57, 43));
        quantityLabel = createStatLabel("Items: 0", new Color(41, 128, 185));
        averageLabel = createStatLabel("Avg: $0.00", new Color(39, 174, 96));

        statsPanel.add(totalLabel);
        statsPanel.add(quantityLabel);
        statsPanel.add(averageLabel);

        inputPanel.add(fieldsPanel);
        inputPanel.add(statsPanel);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // ===== Chart Panel =====
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                chartStrategy.drawChart(g, this, total, categoryTotals, categoryColors);
            }
        };

        chartPanel.setPreferredSize(new Dimension(350, 0));
        mainPanel.add(chartPanel, BorderLayout.EAST);

        // ===== Action Listeners =====

        switchChartBtn.addActionListener(e -> {
            boolean wantBarChart = (chartStrategy instanceof PieChartStrategy);
            setChartStrategy(ChartFactory.create(wantBarChart));
            switchChartBtn.setText(wantBarChart ? "View Pie" : "View Bars");
        });

        // Updated: Use Command logic
        addButton.addActionListener(e -> addExpenseCommand());
        amountField.addActionListener(e -> addExpenseCommand());
        quantityField.addActionListener(e -> addExpenseCommand());

        saveButton.addActionListener(e -> saveData());

        // Updated: Use Command logic
        deleteButton.addActionListener(e -> deleteSelectedExpenseCommand());

        // NEW: Undo Listener
        undoButton.addActionListener(e -> {
            commandInvoker.undo();
            // TableModelListener will handle the UI updates automatically
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedCell();
                }
            }
        });

        // IMPORTANT: Safe Listener to prevent StackOverflowError
        tableModel.addTableModelListener(e -> {
            // Ignore updates to the Total column (index 5) to prevent loops
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 5) {
                return;
            }

            recalculateAllTotals();
            updateStats();
            chartPanel.repaint();
            saveData();
            updateCategoryTotals();
        });

        loadData();
        chartPanel.repaint();
    }

    public static synchronized BudgetBee getInstance() {
        if (instance == null) instance = new BudgetBee();
        return instance;
    }

    public void setChartStrategy(ChartStrategy strategy) {
        this.chartStrategy = strategy;
        chartPanel.repaint();
    }

    // ===== NEW: Add Expense using Command Pattern =====
    private void addExpenseCommand() {
        try {
            String desc = descriptionField.getText().trim();
            if (desc.equals("Description") || desc.isEmpty()) throw new IllegalArgumentException("Please enter a description");

            String qtyText = quantityField.getText().trim();
            if (qtyText.equals("Quantity")) qtyText = "1";
            int quantity = Integer.parseInt(qtyText);

            String amtText = amountField.getText().trim();
            if (amtText.equals("Amount")) throw new IllegalArgumentException("Please enter an amount");
            double amount = Double.parseDouble(amtText);

            String category = (String) categoryCombo.getSelectedItem();
            String date = new SimpleDateFormat("MMM dd").format(new Date());

            // Create data array
            Object[] rowData = {
                    date, desc, category, quantity, "৳" + String.format("%.2f", amount), "৳" + String.format("%.2f", quantity * amount)
            };

            // Create and Execute Command
            Command cmd = new AddExpenseCommand(tableModel, rowData);
            commandInvoker.execute(cmd);

            resetInputFields();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== NEW: Delete Expense using Command Pattern =====
    private void deleteSelectedExpenseCommand() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create and Execute Command
        Command cmd = new DeleteExpenseCommand(tableModel, selectedRow);
        commandInvoker.execute(cmd);
    }

    private void recalculateRowTotal(int row) {
        try {
            int quantity = Integer.parseInt(table.getValueAt(row, 3).toString());
            double amount = Double.parseDouble(table.getValueAt(row, 4).toString().replace("৳", "").trim());
            double newTotal = quantity * amount;
            table.setValueAt("৳" + String.format("%.2f", newTotal), row, 5);
            recalculateAllTotals();
        } catch (Exception ex) {
            // Ignore parse errors during typing
        }
    }

    // ===== Updated: Safe Recalculation with Flag =====
    private void recalculateAllTotals() {
        if (isRecalculating) return; // Guard clause
        isRecalculating = true; // Lock

        try {
            total = 0;
            totalItems = 0;
            categoryTotals.replaceAll((k, v) -> 0.0);

            ExpenseIterator it = new TableModelExpenseIterator(tableModel);
            int rowIndex = 0;
            while (it.hasNext()) {
                Expense e = it.next();
                total += e.getTotal();
                totalItems += e.quantity;
                categoryTotals.put(e.category, categoryTotals.get(e.category) + e.getTotal());

                if (rowIndex < tableModel.getRowCount()) {
                    tableModel.setValueAt("৳" + String.format("%.2f", e.getTotal()), rowIndex++, 5);
                }
            }
        } finally {
            isRecalculating = false; // Unlock
        }
    }

    private void updateCategoryTotals() {
        categoryTotals.replaceAll((k, v) -> 0.0);
        ExpenseIterator it = new TableModelExpenseIterator(tableModel);
        while (it.hasNext()) {
            Expense e = it.next();
            categoryTotals.put(e.category, categoryTotals.get(e.category) + e.getTotal());
        }
    }

    private void editSelectedCell() {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        if (row != -1 && col != -1 && col != 0 && col != 5) {
            table.editCellAt(row, col);
            table.getEditorComponent().requestFocus();
        }
    }

    private void saveData() {
        try (PrintWriter writer = new PrintWriter("expenses.csv")) {
            writer.println("Date,Description,Category,Quantity,Amount,Total");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String amount = tableModel.getValueAt(i, 4).toString().replace("৳", "").trim();
                String totalStr = tableModel.getValueAt(i, 5).toString().replace("৳", "").trim();

                writer.println(String.join(",",
                        tableModel.getValueAt(i, 0).toString(),
                        tableModel.getValueAt(i, 1).toString(),
                        tableModel.getValueAt(i, 2).toString(),
                        tableModel.getValueAt(i, 3).toString(),
                        amount,
                        totalStr
                ));
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        File file = new File("expenses.csv");
        if (!file.exists()) return;

        // Guard against listener loops during loading
        if (isRecalculating) return;
        isRecalculating = true;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    tableModel.addRow(new Object[]{parts[0], parts[1], parts[2], parts[3], "৳" + parts[4], "৳" + parts[5]});
                }
            }
        } catch (IOException | NumberFormatException ex) {
            // Ignore errors
        } finally {
            isRecalculating = false;
            // Recalculate once after loading is finished
            recalculateAllTotals();
            updateStats();
        }
    }

    private void updateStats() {
        totalLabel.setText(String.format("Total: $%.2f", total));
        quantityLabel.setText(String.format("Items: %d", totalItems));
        averageLabel.setText(String.format("Avg: $%.2f", totalItems > 0 ? total / totalItems : 0));
    }

    private void resetInputFields() {
        descriptionField.setText("Description");
        descriptionField.setForeground(Color.GRAY);
        quantityField.setText("Quantity");
        quantityField.setForeground(Color.GRAY);
        amountField.setText("Amount");
        amountField.setForeground(Color.GRAY);
        descriptionField.requestFocus();
    }

    private JTextField createTextFieldWithPlaceholder(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 210, 230)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        field.setForeground(Color.GRAY);
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private JComboBox<String> createCategoryCombo(String[] categories) {
        JComboBox<String> combo = new JComboBox<>(categories);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 210, 230)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return combo;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return button;
    }

    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(color);
        label.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 210, 230)),
                new EmptyBorder(10, 10, 10, 10)
        ));
        return label;
    }

    private void customizeTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setGridColor(new Color(220, 220, 220));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
}
