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

    private ChartStrategy chartStrategy = new PieChartStrategy();

    private BudgetBee() {
        setTitle("💰 BudgetBee");
        setSize(1000, 700);
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

        JPanel fieldsPanel = new JPanel(new GridLayout(1, 7, 10, 10));
        descriptionField = createTextFieldWithPlaceholder("Description");
        categoryCombo = createCategoryCombo(categories);
        quantityField = createTextFieldWithPlaceholder("Quantity");
        amountField = createTextFieldWithPlaceholder("Amount");

        JButton addButton = createButton("Add Expense", new Color(76, 175, 80));
        JButton saveButton = createButton("Save Data", new Color(33, 150, 243));
        JButton deleteButton = createButton("Delete Selected", new Color(244, 67, 54)); // New

        fieldsPanel.add(descriptionField);
        fieldsPanel.add(categoryCombo);
        fieldsPanel.add(quantityField);
        fieldsPanel.add(amountField);
        fieldsPanel.add(addButton);
        fieldsPanel.add(saveButton);
        fieldsPanel.add(deleteButton);

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

        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                chartStrategy.drawChart(g, this, total, categoryTotals, categoryColors);
            }
        };

        chartPanel.setPreferredSize(new Dimension(300, 0));
        mainPanel.add(chartPanel, BorderLayout.EAST);

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
        deleteButton.addActionListener(e -> deleteSelectedExpense()); // New

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedCell();
                }
            }
        });

        // Adding table model listener to detect changes
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

    // ===== Chart Strategy Setter =====
    public void setChartStrategy(ChartStrategy strategy) {
        this.chartStrategy = strategy;
        chartPanel.repaint();
    }

    private void recalculateRowTotal(int row) {
        try {
            int quantity = Integer.parseInt(table.getValueAt(row, 3).toString());
            double amount = Double.parseDouble(table.getValueAt(row, 4).toString().replace("৳", "").trim());

            double newTotal = quantity * amount;

            table.setValueAt("৳" + String.format("%.2f", newTotal), row, 5);

            recalculateAllTotals();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid input. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recalculateAllTotals() {
        total = 0;
        totalItems = 0;
        categoryTotals.replaceAll((k, v) -> 0.0);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                String category = table.getValueAt(i, 2).toString();
                int quantity = Integer.parseInt(table.getValueAt(i, 3).toString());
                double amount = Double.parseDouble(table.getValueAt(i, 4).toString().replace("৳", "").trim());
                double rowTotal = quantity * amount;

                total += rowTotal;
                totalItems += quantity;
                categoryTotals.put(category, categoryTotals.get(category) + rowTotal);

                // Ensure the displayed total is correct
                table.setValueAt("৳" + String.format("%.2f", rowTotal), i, 5);
            } catch (Exception ex) {

            }
        }
    }

    private void updateCategoryTotals() {
        categoryTotals.replaceAll((k, v) -> 0.0);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                String category = table.getValueAt(i, 2).toString();
                double rowTotal = Double.parseDouble(table.getValueAt(i, 5).toString().replace("৳", "").trim());
                categoryTotals.put(category, categoryTotals.get(category) + rowTotal);
            } catch (Exception ex) {

            }
        }
    }

    private void editSelectedCell() {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();

        if (row == -1 || col == -1) return;

        if (col == 0 || col == 5) return;

        table.editCellAt(row, col);
        table.getEditorComponent().requestFocus();
    }

    private void deleteSelectedExpense() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String category = tableModel.getValueAt(selectedRow, 2).toString();
            int quantity = Integer.parseInt(tableModel.getValueAt(selectedRow, 3).toString());
            double totalCost = Double.parseDouble(tableModel.getValueAt(selectedRow, 5).toString().replace("৳", "").trim());

            total -= totalCost;
            totalItems -= quantity;
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) - totalCost);

            tableModel.removeRow(selectedRow);
            saveData();

            updateStats();
            chartPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to delete row properly: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            double totalCost = quantity * amount;
            String date = new SimpleDateFormat("MMM dd").format(new Date());

            tableModel.addRow(new Object[]{
                    date,
                    desc,
                    category,
                    quantity,
                    "৳" + String.format("%.2f", amount),
                    "৳" + String.format("%.2f", totalCost)
            });

            total += totalCost;
            totalItems += quantity;
            categoryTotals.put(category, categoryTotals.get(category) + totalCost);

            updateStats();
            resetInputFields();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and amount", "Input Error", JOptionPane.ERROR_MESSAGE);
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
                    String date = parts[0];
                    String desc = parts[1];
                    String category = parts[2];
                    int quantity = Integer.parseInt(parts[3]);
                    String amountStr = parts[4];
                    String totalStr = parts[5];

                    tableModel.addRow(new Object[]{
                            date,
                            desc,
                            category,
                            quantity,
                            "৳" + amountStr,
                            "৳" + totalStr
                    });

                    double totalCost = Double.parseDouble(totalStr);
                    total += totalCost;
                    totalItems += quantity;
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + totalCost);
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BudgetBee app = BudgetBee.getInstance();
            app.setVisible(true);
        });
    }
}