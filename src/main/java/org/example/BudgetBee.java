package org.example;

import org.example.tools.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

// REMOVED "implements BudgetObserver" to keep it simple
public class BudgetBee extends JFrame {

    // 1. SINGLETON PATTERN
    private static BudgetBee instance;

    // --- UI Components ---
    private JTextField descField, qtyField, amtField;
    private JComboBox<String> catCombo;
    private DefaultTableModel model;
    private JTable table;
    private JLabel lblTotal, lblItems, lblAvg;
    private JPanel chartPanel;
    private JPanel mainPanel;

    // --- Logic & Patterns ---
    private BudgetManagerFacade facade; // FACADE PATTERN
    private ChartStrategy chartStrategy = new PieChartStrategy(); // STRATEGY PATTERN

    // --- Constants ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_COLOR = new Color(44, 62, 80);

    private final Color[] CHART_COLORS = {
            new Color(255, 107, 107), new Color(78, 205, 196), new Color(255, 230, 109),
            new Color(26, 83, 92), new Color(85, 98, 112), new Color(199, 244, 100)
    };

    // ==========================================
    //               CONSTRUCTOR
    // ==========================================
    private BudgetBee() {
        setupFrame();

        // 1. Setup Logic
        setupTableData();
        facade = new BudgetManagerFacade(model);

        // OBSERVER PATTERN (Simple Version)
        // We pass a function (Runnable) that runs whenever data changes
        facade.addObserver(() -> updateUIStats());

        // 2. Setup UI
        setupHeader();
        setupTableUI();
        setupBottomPanel();
        setupChartPanel();

        // 3. Setup Actions & Load Data
        setupListeners();
        facade.loadData(new File("expenses.csv"));
    }

    public static synchronized BudgetBee getInstance() {
        if (instance == null) instance = new BudgetBee();
        return instance;
    }

    // ==========================================
    //             UI SETUP METHODS
    // ==========================================
    private void setupFrame() {
        setTitle("BudgetBee - Modern Expense Tracker");
        setSize(1250, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(BG_COLOR);
        add(mainPanel);
    }

    private void setupTableData() {
        model = new DefaultTableModel(new String[]{"Date", "Desc", "Category", "Qty", "Amount", "Total"}, 0);
        table = new JTable(model);
        customizeTable(table);
    }

    private void setupHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_COLOR);

        JLabel title = new JLabel("BudgetBee");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_COLOR);

        header.add(title, BorderLayout.NORTH);
        header.add(new JLabel("Smart Expense Tracking"), BorderLayout.SOUTH);

        mainPanel.add(header, BorderLayout.NORTH);
    }

    private void setupTableUI() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
        tableCard.add(scroll);

        mainPanel.add(tableCard, BorderLayout.CENTER);
    }

    private void setupBottomPanel() {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(BG_COLOR);

        // Row 1: Inputs
        JPanel inputPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        inputPanel.setBackground(BG_COLOR);

        descField = createField("Description");
        catCombo = new JComboBox<>(new String[]{"Food", "Transport", "Shopping", "Entertainment", "Bills", "Other"});
        catCombo.setBackground(CARD_BG);
        qtyField = createField("Qty");
        amtField = createField("Amount");

        inputPanel.add(descField);
        inputPanel.add(catCombo);
        inputPanel.add(qtyField);
        inputPanel.add(amtField);

        // Row 2: Stats
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setPreferredSize(new Dimension(-1, 80));

        lblTotal = createStatLabel("TOTAL SPENT", new Color(255, 107, 107));
        lblItems = createStatLabel("ITEMS", new Color(78, 205, 196));
        lblAvg = createStatLabel("AVERAGE", new Color(255, 206, 86));

        statsPanel.add(lblTotal);
        statsPanel.add(lblItems);
        statsPanel.add(lblAvg);

        bottom.add(inputPanel);
        bottom.add(createButtonPanel()); // Add Buttons Row
        bottom.add(statsPanel);

        mainPanel.add(bottom, BorderLayout.SOUTH);
    }

    private void setupChartPanel() {
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw Card Background
                g.setColor(CARD_BG);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Draw Chart (Strategy Pattern)
                chartStrategy.drawChart(g, this, facade.getTotal(), facade.getCategoryTotals(), CHART_COLORS);
            }
        };
        chartPanel.setPreferredSize(new Dimension(400, 0));
        chartPanel.setBackground(BG_COLOR);
        chartPanel.setBorder(new EmptyBorder(0, 15, 0, 0));

        mainPanel.add(chartPanel, BorderLayout.EAST);
    }

    // ==========================================
    //           BUTTONS & LISTENERS
    // ==========================================
    private JPanel createButtonPanel() {
        JPanel btns = new JPanel(new GridLayout(1, 6, 10, 0));
        btns.setBackground(BG_COLOR);
        btns.setBorder(new EmptyBorder(15, 0, 15, 0));

        JButton bAdd = createBtn("Add", new Color(46, 204, 113));
        JButton bSave = createBtn("Save", new Color(52, 152, 219));
        JButton bDel = createBtn("Delete", new Color(231, 76, 60));
        JButton bView = createBtn("View Bars", new Color(155, 89, 182));
        JButton bUndo = createBtn("Undo", new Color(241, 196, 15));
        JButton bRep = createBtn("Report", new Color(52, 73, 94));

        // Add Listeners
        bAdd.addActionListener(e -> performAddAction());
        bSave.addActionListener(e -> facade.saveData(new File("expenses.csv")));
        bDel.addActionListener(e -> facade.deleteExpense(table.getSelectedRow()));
        bUndo.addActionListener(e -> facade.undo());
        bRep.addActionListener(e -> showReport());

        bView.addActionListener(e -> {
            boolean isPie = (chartStrategy instanceof PieChartStrategy);
            chartStrategy = ChartFactory.create(isPie);
            bView.setText(isPie ? "View Pie" : "View Bars");
            chartPanel.repaint();
        });

        btns.add(bAdd); btns.add(bSave); btns.add(bDel);
        btns.add(bView); btns.add(bUndo); btns.add(bRep);

        return btns;
    }

    private void setupListeners() {
        // Table Model Listener to trigger Recalculation
        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 5) return;
            facade.recalculateAll();
        });
    }

    // ==========================================
    //              LOGIC METHODS
    // ==========================================

    // Called automatically by the Facade Observer
    private void updateUIStats() {
        updateStatLabel(lblTotal, String.format("$%.2f", facade.getTotal()));
        updateStatLabel(lblItems, String.valueOf(facade.getTotalItems()));

        double avg = facade.getTotalItems() > 0 ? facade.getTotal() / facade.getTotalItems() : 0;
        updateStatLabel(lblAvg, String.format("$%.2f", avg));

        chartPanel.repaint();
    }

    private void performAddAction() {
        String desc = descField.getText().trim();
        String qtyStr = qtyField.getText().trim();
        String amtStr = amtField.getText().trim();

        if (desc.equals("Description") || qtyStr.equals("Qty") || amtStr.equals("Amount") ||
                desc.isEmpty() || qtyStr.isEmpty() || amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            double amt = Double.parseDouble(amtStr);

            if (qty <= 0 || amt <= 0) throw new NumberFormatException();

            String date = new SimpleDateFormat("MMM dd").format(new Date());
            String category = (String) catCombo.getSelectedItem();

            facade.addExpense(date, desc, category, qty, amt);

            // Reset Fields
            descField.setText("Description"); descField.setForeground(Color.GRAY);
            qtyField.setText("Qty"); qtyField.setForeground(Color.GRAY);
            amtField.setText("Amount"); amtField.setForeground(Color.GRAY);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid positive numbers.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReport() {
        CategoryComponent root = new CategoryGroup("Total Budget");
        CategoryComponent needs = new CategoryGroup("Needs");
        CategoryComponent wants = new CategoryGroup("Wants");

        var totals = facade.getCategoryTotals();

        needs.add(new CategoryLeaf("Food", totals.getOrDefault("Food", 0.0)));
        needs.add(new CategoryLeaf("Bills", totals.getOrDefault("Bills", 0.0)));
        needs.add(new CategoryLeaf("Transport", totals.getOrDefault("Transport", 0.0)));

        wants.add(new CategoryLeaf("Shopping", totals.getOrDefault("Shopping", 0.0)));
        wants.add(new CategoryLeaf("Entertainment", totals.getOrDefault("Entertainment", 0.0)));
        wants.add(new CategoryLeaf("Other", totals.getOrDefault("Other", 0.0)));

        root.add(needs);
        root.add(wants);

        JOptionPane.showMessageDialog(this,
                String.format("Needs: $%.2f\nWants: $%.2f\n\nTotal: $%.2f",
                        needs.getAmount(), wants.getAmount(), root.getAmount()),
                "Composite Report", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==========================================
    //              UI HELPER METHODS
    // ==========================================

    private JTextField createField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setForeground(Color.GRAY);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 210, 230), 1), new EmptyBorder(8, 8, 8, 8)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(TEXT_COLOR);
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(Color.GRAY);
                }
            }
        });
        return f;
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    private JLabel createStatLabel(String title, Color accentColor) {
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setBackground(CARD_BG);
        label.setBorder(new LineBorder(new Color(220, 220, 220)));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        // Store properties to reuse in updateStatLabel
        label.putClientProperty("title", title);
        label.putClientProperty("color", accentColor);

        updateStatLabel(label, "0");
        return label;
    }

    private void updateStatLabel(JLabel label, String value) {
        String title = (String) label.getClientProperty("title");
        Color color = (Color) label.getClientProperty("color");
        String hexColor = String.format("#%06x", color.getRGB() & 0xFFFFFF);

        label.setText("<html><center>" +
                "<span style='color:gray;font-size:10px;letter-spacing:1px'>" + title + "</span><br>" +
                "<span style='color:" + hexColor + ";font-size:20px;font-weight:bold'>" + value + "</span>" +
                "</center></html>");
    }

    private void customizeTable(JTable table) {
        table.setRowHeight(50);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));
    }
}
