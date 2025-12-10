package org.example.tools;

// THE MEMENTO: Stores the internal state of an object (a table row)
public class TableMemento {
    private final int rowIndex;
    private final Object[] rowData;

    public TableMemento(int rowIndex, Object[] rowData) {
        this.rowIndex = rowIndex;
        this.rowData = rowData;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public Object[] getRowData() {
        return rowData;
    }
}
