package org.example.tools;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Map;
import javax.swing.JPanel;

public interface ChartStrategy {
    void drawChart(Graphics g, JPanel panel, double total, Map<String, Double> categoryTotals, Color[] colors);
}