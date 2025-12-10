package org.example.tools;

import javax.swing.table.TableModel;

public class CSVAdapter {
    private final TableModel model;

    public CSVAdapter(TableModel model) {
        this.model = model;
    }

    public String export() {
        StringBuilder sb = new StringBuilder("Date,Description,Category,Quantity,Amount,Total\n");

        for (int i = 0; i < model.getRowCount(); i++) {
            String[] row = new String[6];

            // Collect row data
            for (int j = 0; j < 6; j++) {
                row[j] = model.getValueAt(i, j).toString().replace("৳", "").trim();
            }

            // Join with commas automatically
            sb.append(String.join(",", row)).append("\n");
        }
        return sb.toString();
    }
}
