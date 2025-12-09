package org.example.tools;

import javax.swing.table.DefaultTableModel;

public class DeleteExpenseCommand implements Command {
    private DefaultTableModel model;
    private int rowIndex;
    private Object[] deletedData;

    public DeleteExpenseCommand(DefaultTableModel model, int rowIndex) {
        this.model = model;
        this.rowIndex = rowIndex;

        // Save the data before deleting so we can restore it later
        int colCount = model.getColumnCount();
        this.deletedData = new Object[colCount];
        for (int i = 0; i < colCount; i++) {
            deletedData[i] = model.getValueAt(rowIndex, i);
        }
    }

    @Override
    public void execute() {
        model.removeRow(rowIndex);
    }

    @Override
    public void undo() {
        // Restore the row at the specific index
        model.insertRow(rowIndex, deletedData);
    }
}
