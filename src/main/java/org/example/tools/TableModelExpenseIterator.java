package org.example.tools;

import org.example.model.Expense;
import javax.swing.table.DefaultTableModel;

public class TableModelExpenseIterator implements ExpenseIterator {
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
        // Handle currency symbol parsing
        double amount = Double.parseDouble(model.getValueAt(index, 4).toString().replace("৳", "").trim());
        index++;
        return new Expense(date, description, category, quantity, amount);
    }
}