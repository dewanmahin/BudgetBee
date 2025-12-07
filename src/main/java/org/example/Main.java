package org.example;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BudgetBee app = BudgetBee.getInstance();
            app.setVisible(true);
        });
    }
}