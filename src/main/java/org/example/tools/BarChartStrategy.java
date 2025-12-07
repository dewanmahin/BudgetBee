package org.example.tools;

import java.awt.*;
import java.util.Map;
import javax.swing.JPanel;

public class BarChartStrategy implements ChartStrategy {
    @Override
    public void drawChart(Graphics g, JPanel panel, double total, Map<String, Double> categoryTotals, Color[] colors) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = panel.getWidth();
        int height = panel.getHeight();
        int padding = 40;
        int graphHeight = height - (2 * padding);
        int barWidth = (width - (2 * padding)) / Math.max(1, categoryTotals.size());

        // Draw Axis
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(padding, height - padding, width - padding, height - padding); // X-Axis
        g2d.drawLine(padding, padding, padding, height - padding); // Y-Axis

        if (total == 0) {
            g2d.setColor(Color.GRAY);
            g2d.drawString("No data", width / 2 - 20, height / 2);
            return;
        }

        double maxVal = 0;
        for (double val : categoryTotals.values()) {
            if (val > maxVal) maxVal = val;
        }

        int x = padding + 10;
        int colorIndex = 0;
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            double value = entry.getValue();

            // We draw all categories in bar chart even if 0, or you can filter if (value > 0)
            int barHeight = (maxVal > 0) ? (int) ((value / maxVal) * graphHeight) : 0;

            g2d.setColor(colors[colorIndex % colors.length]);
            g2d.fillRect(x, height - padding - barHeight, barWidth - 10, barHeight);

            g2d.setColor(Color.BLACK);
            if (value > 0) {
                String valText = String.format("%.0f", value);
                g2d.drawString(valText, x + (barWidth / 4), height - padding - barHeight - 5);
            }

            // Truncate long labels
            String label = category.length() > 6 ? category.substring(0, 6) + ".." : category;
            g2d.drawString(label, x, height - padding + 15);

            x += barWidth;
            colorIndex++;
        }
    }
}