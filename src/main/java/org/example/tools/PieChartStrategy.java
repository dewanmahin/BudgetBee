package org.example.tools;

import java.awt.*;
import java.util.Map;
import javax.swing.JPanel;

public class PieChartStrategy implements ChartStrategy {
    @Override
    public void drawChart(Graphics g, JPanel panel, double total, Map<String, Double> categoryTotals, Color[] colors) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (total == 0) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2d.setColor(new Color(100, 100, 100));
            g2d.drawString("No data to display", 50, 50);
            return;
        }

        int diameter = Math.min(panel.getWidth(), panel.getHeight()) - 100;
        int x = (panel.getWidth() - diameter) / 2;
        int y = 20;

        double startAngle = 0;
        int colorIndex = 0;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {
                double arcAngle = 360 * (entry.getValue() / total);
                g2d.setColor(colors[colorIndex % colors.length]);
                g2d.fillArc(x, y, diameter, diameter, (int) startAngle, (int) arcAngle);
                startAngle += arcAngle;
                colorIndex++;
            }
        }

        // Legend
        int legendX = 20;
        int legendY = y + diameter + 20;
        int boxSize = 15;
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        colorIndex = 0;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {
                g2d.setColor(colors[colorIndex % colors.length]);
                g2d.fillRect(legendX, legendY, boxSize, boxSize);
                g2d.setColor(Color.BLACK);
                String label = String.format("%s (%.1f%%)", entry.getKey(), (entry.getValue() / total) * 100);
                g2d.drawString(label, legendX + boxSize + 5, legendY + boxSize - 3);
                legendY += boxSize + 5;
                colorIndex++;
            }
        }
    }
}