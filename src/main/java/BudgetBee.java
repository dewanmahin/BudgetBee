import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BudgetBee extends JFrame {
    private JTextField descriptionField, amountField, quantityField;
    private JComboBox<String> categoryCombo;
    private DefaultTableModel tableModel;
    private JLabel totalLabel, quantityLabel, averageLabel;

    private double total = 0;
    private int totalItems = 0;
    private Map<String, Double> categoryTotals = new HashMap<>();

    public BudgetBee() {
        setTitle("💰 BudgetBee");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Initialize categories and totals
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
        JTable table = new JTable(tableModel);
        customizeTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        inputPanel.setBackground(new Color(240, 245, 255));

        JPanel fieldsPanel = new JPanel(new GridLayout(1, 6, 10, 10));
        descriptionField = createTextFieldWithPlaceholder("Description");
        categoryCombo = createCategoryCombo(categories);
        quantityField = createTextFieldWithPlaceholder("Quantity");
        amountField = createTextFieldWithPlaceholder("Amount");

        JButton addButton = createButton("Add Expense", new Color(76, 175, 80));
        addButton.setPreferredSize(new Dimension(150, 40));

        JButton saveButton = createButton("Save Data", new Color(33, 150, 243));
        saveButton.setPreferredSize(new Dimension(150, 40));

        fieldsPanel.add(descriptionField);
        fieldsPanel.add(categoryCombo);
        fieldsPanel.add(quantityField);
        fieldsPanel.add(amountField);
        fieldsPanel.add(addButton);
        fieldsPanel.add(saveButton);

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

        addButton.addActionListener(e -> addExpense());
        amountField.addActionListener(e -> addExpense());
        quantityField.addActionListener(e -> addExpense());

        saveButton.addActionListener(e -> saveData());

        loadData();
    }

    private void addExpense() {
        try {
            String desc = descriptionField.getText().trim();
            if (desc.equals("Description") || desc.isEmpty()) {
                throw new IllegalArgumentException("Please enter a description");
            }

            String qtyText = quantityField.getText().trim();
            if (qtyText.equals("Quantity")) qtyText = "1"; // default quantity
            int quantity = Integer.parseInt(qtyText);

            String amtText = amountField.getText().trim();
            if (amtText.equals("Amount")) {
                throw new IllegalArgumentException("Please enter an amount");
            }
            double amount = Double.parseDouble(amtText);

            String category = (String) categoryCombo.getSelectedItem();
            double totalCost = quantity * amount;
            String date = new SimpleDateFormat("MMM dd").format(new Date());

            tableModel.addRow(new Object[]{
                    date,
                    desc,
                    category,
                    quantity,
                    String.format("৳%.2f", amount),
                    String.format("৳%.2f", totalCost)
            });

            total += totalCost;
            totalItems += quantity;
            categoryTotals.put(category, categoryTotals.get(category) + totalCost);

            updateStats();
            resetInputFields();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and amount",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveData() {
        try (PrintWriter writer = new PrintWriter("expenses.csv")) {
            // Write CSV header
            writer.println("Date,Description,Category,Quantity,Amount,Total");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                // Remove currency symbol before saving
                String amount = tableModel.getValueAt(i, 4).toString().replace("৳", "").trim();
                String total = tableModel.getValueAt(i, 5).toString().replace("৳", "").trim();

                writer.println(String.join(",",
                        tableModel.getValueAt(i, 0).toString(),
                        tableModel.getValueAt(i, 1).toString(),
                        tableModel.getValueAt(i, 2).toString(),
                        tableModel.getValueAt(i, 3).toString(),
                        amount,
                        total
                ));
            }

            JOptionPane.showMessageDialog(this, "Data saved to expenses.csv",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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

    private void updateStats() {
        totalLabel.setText(String.format("Total: $%.2f", total));
        quantityLabel.setText(String.format("Items: %d", totalItems));
        averageLabel.setText(String.format("Avg: $%.2f", totalItems > 0 ? total / totalItems : 0));
    }

    // UI methods
    private JTextField createTextFieldWithPlaceholder(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 210, 230)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        field.setForeground(Color.GRAY);
        field.setText(placeholder);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
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
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            BudgetBee app = new BudgetBee();
            app.setVisible(true);
        });
    }
}
