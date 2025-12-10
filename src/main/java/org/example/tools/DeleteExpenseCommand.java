package org.example.tools;

import javax.swing.table.DefaultTableModel;

public class DeleteExpenseCommand implements Command {
    private DefaultTableModel model;
    private TableMemento memento; // <--- The Saved State Object

    public DeleteExpenseCommand(DefaultTableModel model, int rowIndex) {
        this.model = model;

        // 1. CREATE MEMENTO (Capture state before deleting)
        int colCount = model.getColumnCount();
        Object[] data = new Object[colCount];
        for (int i = 0; i < colCount; i++) {
            data[i] = model.getValueAt(rowIndex, i);
        }

        // Save the snapshot
        this.memento = new TableMemento(rowIndex, data);
    }

    @Override
    public void execute() {
        // Use the saved index from the memento to remove
        model.removeRow(memento.getRowIndex());
    }

    @Override
    public void undo() {
        // 2. RESTORE MEMENTO (Put the data back exactly where it was)
        model.insertRow(memento.getRowIndex(), memento.getRowData());
    }
}
