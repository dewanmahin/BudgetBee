package org.example;

import org.example.model.Expense;
import org.example.tools.*; // Imports all your new strategies and iterators

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

<<<<<<< HEAD:src/main/java/org/example/BudgetBee.java
// ===== Main Context Class (Singleton) =====
=======
// ===== Expense Data Class =====
class Expense {
    String date;
    String description;
    String category;
    int quantity;
    double amount;

    public Expense(String date, String description, String category, int quantity, double amount) {
        this.date = date;
        this.description = description;
        this.category = category;
        this.quantity = quantity;
        this.amount = amount;
    }

    public double getTotal() {
        return quantity * amount;
    }
}

// ===== Iterator Interface =====
interface ExpenseIterator {
    boolean hasNext();
    Expense next();
}

// ===== TableModel-based Iterator =====
class TableModelExpenseIterator implements ExpenseIterator {
    private final DefaultTableModel model;
    private int index = 0;

    public TableModelExpenseIterator(DefaultTableModel model) {
        this.model = model;
    }

    @Override
    public boolean hasNext() {
        return index < model.getRowCount();
    }

    @Override
    public Expense next() {
        if (!hasNext()) return null;

        String date = model.getValueAt(index, 0).toString();
        String description = model.getValueAt(index, 1).toString();
        String category = model.getValueAt(index, 2).toString();
        int quantity = Integer.parseInt(model.getValueAt(index, 3).toString());
        double amount = Double.parseDouble(model.getValueAt(index, 4).toString().replace("৳", "").trim());
        index++;
        return new Expense(date, description, category, quantity, amount);
    }
}

// ===== Chart Strategy Interfaces =====
interface ChartStrategy {
    void drawChart(Graphics g, JPanel panel, double total, Map<String, Double> categoryTotals, Color[] colors);
}

// ===== Pie Chart Strategy Implementation =====
class PieChartStrategy implements ChartStrategy {
    @Override
    public void drawChart(Graphics g, JPanel panel, double total, Map<String, Double> categoryTotals, Color[] colors) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (total == 0) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2d.setColor(new Color(100, 100, 100));
            g2d.drawString("No data to display", 50, 50);
            return;
        }

        int diameter = Math.min(panel.getWidth(), panel.getHeight()) - 100;
        int x = (panel.getWidth() - diameter) / 2;
        int y = 20;

        double startAngle = 0;
        int colorIndex = 0;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {
                double arcAngle = 360 * (entry.getValue() / total);
                g2d.setColor(colors[colorIndex % colors.length]);
                g2d.fillArc(x, y, diameter, diameter, (int) startAngle, (int) arcAngle);
                startAngle += arcAngle;
                colorIndex++;
            }
        }

        // Legend
        int legendX = 20;
        int legendY = y + diameter + 20;
        int boxSize = 15;
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        colorIndex = 0;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {
                g2d.setColor(colors[colorIndex % colors.length]);
                g2d.fillRect(legendX, legendY, boxSize, boxSize);
                g2d.setColor(Color.BLACK);
                String label = String.format("%s (%.1f%%)", entry.getKey(), (entry.getValue() / total) * 100);
                g2d.drawString(label, legendX + boxSize + 5, legendY + boxSize - 3);
                legendY += boxSize + 5;
                colorIndex++;
            }
        }
    }
}

>>>>>>> 95dcd31c83bc36f18c18ab6780cae5500f97c6c2:src/main/java/BudgetBee.java
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

    private final Color[] categoryColors = {
            new Color(255, 99, 132), new Color(54, 162, 235),
            new Color(255, 206, 86), new Color(75, 192, 192),
            new Color(153, 102, 255), new Color(255, 159, 64)
    };

    // Strategy Pattern field
<<<<<<< HEAD:src/main/java/org/example/BudgetBee.java
    private ChartStrategy chartStrategy = new PieChartStrategy(); // Default to Pie
=======
    private ChartStrategy chartStrategy = new PieChartStrategy();
>>>>>>> 95dcd31c83bc36f18c18ab6780cae5500f97c6c2:src/main/java/BudgetBee.java

    private BudgetBee() {
        setTitle("💰 BudgetBee");
        setSize(1100, 700); // Widened slightly to fit buttons
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

        // Increased GridLayout columns to 8 to fit the new Switch button
        JPanel fieldsPanel = new JPanel(new GridLayout(1, 8, 10, 10));
        descriptionField = createTextFieldWithPlaceholder("Description");
        categoryCombo = createCategoryCombo(categories);
        quantityField = createTextFieldWithPlaceholder("Quantity");
        amountField = createTextFieldWithPlaceholder("Amount");

        JButton addButton = createButton("Add", new Color(76, 175, 80));
        JButton saveButton = createButton("Save", new Color(33, 150, 243));
        JButton deleteButton = createButton("Delete", new Color(244, 67, 54));

        // NEW: Toggle Strategy Button
        JButton switchChartBtn = createButton("View Bars", new Color(156, 39, 176));

        fieldsPanel.add(descriptionField);
        fieldsPanel.add(categoryCombo);
        fieldsPanel.add(quantityField);
        fieldsPanel.add(amountField);
        fieldsPanel.add(addButton);
        fieldsPanel.add(saveButton);
        fieldsPanel.add(deleteButton);
        fieldsPanel.add(switchChartBtn); // Add the switch button

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
<<<<<<< HEAD:src/main/java/org/example/BudgetBee.java
                // Delegate drawing to the current strategy
=======
>>>>>>> 95dcd31c83bc36f18c18ab6780cae5500f97c6c2:src/main/java/BudgetBee.java
                chartStrategy.drawChart(g, this, total, categoryTotals, categoryColors);
            }
        };

<<<<<<< HEAD:src/main/java/org/example/BudgetBee.java
        chartPanel.setPreferredSize(new Dimension(350, 0));
=======
        chartPanel.setPreferredSize(new Dimension(300, 0));
>>>>>>> 95dcd31c83bc36f18c18ab6780cae5500f97c6c2:src/main/java/BudgetBee.java
        mainPanel.add(chartPanel, BorderLayout.EAST);

        // ===== Action Listeners =====

        // Switch Strategy Logic
        switchChartBtn.addActionListener(e -> {
            if (chartStrategy instanceof PieChartStrategy) {
                setChartStrategy(new BarChartStrategy());
                switchChartBtn.setText("View Pie");
            } else {
                setChartStrategy(new PieChartStrategy());
                switchChartBtn.setText("View Bars");
            }
        });

        addButton.addActionListener(e -> {
            addExpense();
            chartPanel.repaint();
        });
        amountField.addActionListener(e -> {
            addExpense();
            chartPanel.repaint();
        });
        quantityField.addActionListener(e -> {
            addExpense();
            chartPanel.repaint();
        });

        saveButton.addActionListener(e -> saveData());
        deleteButton.addActionListener(e -> deleteSelectedExpense());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedCell();
                }
            }
        });

        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 3 || column == 4) {
                    recalculateRowTotal(row);
                }
                if (column == 2) {
                    updateCategoryTotals();
                }
                updateStats();
                chartPanel.repaint();
                saveData();
            }
        });

        loadData();
        chartPanel.repaint();
    }

    // ===== Singleton =====
    public static synchronized BudgetBee getInstance() {
        if (instance == null) {
            instance = new BudgetBee();
        }
        return instance;
    }

<<<<<<< HEAD:src/main/java/org/example/BudgetBee.java
    // ===== Strategy Setter =====
    public void setChartStrategy(ChartStrategy strategy) {
        this.chartStrategy = strategy;
        chartPanel.repaint(); // Force redraw immediately upon switching
=======
    // ===== Chart Strategy Setter =====
    public void setChartStrategy(ChartStrategy strategy) {
        this.chartStrategy = strategy;
        chartPanel.repaint();
>>>>>>> 95dcd31c83bc36f18c18ab6780cae5500f97c6c2:src/main/java/BudgetBee.java
    }

    private void recalculateRowTotal(int row) {
        try {
            int quantity = Integer.parseInt(table.getValueAt(row, 3).toString());
            double amount = Double.parseDouble(table.getValueAt(row, 4).toString().replace("৳", "").trim());
            double newTotal = quantity * amount;
            table.setValueAt("৳" + String.format("%.2f", newTotal), row, 5);
            recalculateAllTotals();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== Recalculate Totals using Iterator =====
    private void recalculateAllTotals() {
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

<<<<<<< HEAD:src/main/java/org/example/BudgetBee.java
            // Update the total column text in case it drifted
=======
>>>>>>> 95dcd31c83bc36f18c18ab6780cae5500f97c6c2:src/main/java/BudgetBee.java
            tableModel.setValueAt("৳" + String.format("%.2f", e.getTotal()), rowIndex++, 5);
        }
    }

    private void updateCategoryTotals() {
        categoryTotals.replaceAll((k, v) -> 0.0);
<<<<<<< HEAD:src/main/java/org/example/BudgetBee.java
=======

>>>>>>> 95dcd31c83bc36f18c18ab6780cae5500f97c6c2:src/main/java/BudgetBee.java
        ExpenseIterator it = new TableModelExpenseIterator(tableModel);
        while (it.hasNext()) {
            Expense e = it.next();
            categoryTotals.put(e.category, categoryTotals.get(e.category) + e.getTotal());
        }
    }

    private void editSelectedCell() {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        if (row == -1 || col == -1) return;
        if (col == 0 || col == 5) return; // Date and Total are read-only
        table.editCellAt(row, col);
        table.getEditorComponent().requestFocus();
    }

    private void deleteSelectedExpense() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
<<<<<<< HEAD:src/main/java/org/example/BudgetBee.java
=======

>>>>>>> 95dcd31c83bc36f18c18ab6780cae5500f97c6c2:src/main/java/BudgetBee.java
        tableModel.removeRow(selectedRow);
        recalculateAllTotals();
        updateStats();
        chartPanel.repaint();
        saveData();
    }

    private void addExpense() {
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

            tableModel.addRow(new Object[]{
                    date, desc, category, quantity, "৳" + String.format("%.2f", amount), "৳" + String.format("%.2f", quantity * amount)
            });

            recalculateAllTotals();
            updateStats();
            resetInputFields();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
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
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String date = parts[0], desc = parts[1], category = parts[2];
                    int quantity = Integer.parseInt(parts[3]);
                    double amount = Double.parseDouble(parts[4]);
                    tableModel.addRow(new Object[]{date, desc, category, quantity, "৳" + parts[4], "৳" + parts[5]});
<<<<<<< HEAD:src/main/java/org/example/BudgetBee.java
=======

>>>>>>> 95dcd31c83bc36f18c18ab6780cae5500f97c6c2:src/main/java/BudgetBee.java
                    total += quantity * amount;
                    totalItems += quantity;
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + quantity * amount);
                }
            }
            updateStats();
        } catch (IOException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Slightly smaller font to fit
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Smaller padding
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
<<<<<<< HEAD:src/main/java/org/example/BudgetBee.java
}
=======

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BudgetBee app = BudgetBee.getInstance();
            app.setVisible(true);
        });
    }
}
>>>>>>> 95dcd31c83bc36f18c18ab6780cae5500f97c6c2:src/main/java/BudgetBee.java
