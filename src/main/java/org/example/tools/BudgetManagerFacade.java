package org.example.tools;

import org.example.model.Expense;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class BudgetManagerFacade {
    private final DefaultTableModel tableModel;
    private final CommandInvoker commandInvoker;

    // Application State
    private double total = 0;
    private int totalItems = 0;
    private final Map<String, Double> categoryTotals = new HashMap<>();

    // Guard flag for recursion
    private boolean isRecalculating = false;

    public BudgetManagerFacade(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
        this.commandInvoker = new CommandInvoker();

        // Initialize Categories
        String[] categories = {"Food", "Transport", "Shopping", "Entertainment", "Bills", "Other"};
        for (String c : categories) categoryTotals.put(c, 0.0);
    }

    // ===== COMMAND OPERATIONS =====
    public void addExpense(String date, String desc, String category, int qty, double amount) {
        Object[] row = {date, desc, category, qty, "৳" + String.format("%.2f", amount), "৳" + String.format("%.2f", qty * amount)};
        commandInvoker.execute(new AddExpenseCommand(tableModel, row));
    }

    public void deleteExpense(int rowIndex) {
        if (rowIndex != -1) {
            commandInvoker.execute(new DeleteExpenseCommand(tableModel, rowIndex));
        }
    }

    public void undo() {
        commandInvoker.undo();
    }

    // ===== CALCULATION LOGIC (Moved from Main) =====
    public void recalculateAll() {
        if (isRecalculating) return;
        isRecalculating = true;

        try {
            total = 0;
            totalItems = 0;
            categoryTotals.replaceAll((k, v) -> 0.0);

            ExpenseIterator it = new TableModelExpenseIterator(tableModel);
            int i = 0;
            while (it.hasNext()) {
                Expense e = it.next();
                total += e.getTotal();
                totalItems += e.quantity;

                // Update Category Map
                categoryTotals.put(e.category, categoryTotals.getOrDefault(e.category, 0.0) + e.getTotal());

                // Update "Total" column in table safely
                if (i < tableModel.getRowCount()) {
                    tableModel.setValueAt("৳" + String.format("%.2f", e.getTotal()), i++, 5);
                }
            }
        } finally {
            isRecalculating = false;
        }
    }

    // ===== FILE I/O (Adapter Pattern inside Facade) =====
    public void saveData(File file) {
        try (PrintWriter w = new PrintWriter(file)) {
            CSVAdapter adapter = new CSVAdapter(tableModel);
            w.print(adapter.export());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadData(File file) {
        if (!file.exists()) return;

        // Lock calculations during load
        isRecalculating = true;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            r.readLine(); // Skip Header
            String line;
            while ((line = r.readLine()) != null) {
                tableModel.addRow(line.split(","));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRecalculating = false;
            recalculateAll(); // Recalculate once at the end
        }
    }

    // ===== GETTERS FOR UI =====
    public double getTotal() { return total; }
    public int getTotalItems() { return totalItems; }
    public Map<String, Double> getCategoryTotals() { return categoryTotals; }
}
