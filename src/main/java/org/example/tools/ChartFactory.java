package org.example.tools;

public class ChartFactory {
    public static ChartStrategy create(boolean isBar) {
        return isBar ? new BarChartStrategy() : new PieChartStrategy();
    }
}
