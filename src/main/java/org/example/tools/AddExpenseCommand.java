package org.example.tools;

import javax.swing.table.DefaultTableModel;

public class AddExpenseCommand implements Command {
    private DefaultTableModel model;
    private Object[] rowData;

    public AddExpenseCommand(DefaultTableModel model, Object[] rowData) {
        this.model = model;
        this.rowData = rowData;
    }

    @Override
    public void execute() {
        model.addRow(rowData);
    }

    @Override
    public void undo() {
        // Remove the last row (which is the one we just added)
        if (model.getRowCount() > 0) {
            model.removeRow(model.getRowCount() - 1);
        }
    }
}
