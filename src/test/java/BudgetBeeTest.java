import org.junit.jupiter.api.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BudgetBeeTest {

    private BudgetBee tracker;

    @BeforeEach
    public void setUp() {
        tracker = new BudgetBee();
    }

    @Test
    public void testInitialState() {
        assertNotNull(tracker, "ExpenseTracker instance should not be null");

        JTable table = getPrivateField(tracker, "table", JTable.class);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        assertEquals(0, model.getRowCount(), "Initial table row count should be zero");
        assertEquals(6, model.getColumnCount(), "There should be 6 columns in the table");

        assertTrue(model.getRowCount() == 0, "Table should be empty initially");
        assertFalse(model.getColumnCount() < 6, "Table should not have fewer than 6 columns");
    }

    @Test
    public void testCategoryTotalsAfterAdd() {
        JTextField descField = getPrivateField(tracker, "descriptionField", JTextField.class);
        JTextField qtyField = getPrivateField(tracker, "quantityField", JTextField.class);
        JTextField amtField = getPrivateField(tracker, "amountField", JTextField.class);
        JComboBox<String> categoryCombo = getPrivateField(tracker, "categoryCombo", JComboBox.class);

        descField.setText("Pizza");
        qtyField.setText("3");
        amtField.setText("30");
        categoryCombo.setSelectedItem("Food");

        invokePrivateMethod(tracker, "addExpense");

        Map<String, Double> categoryTotals = getPrivateField(tracker, "categoryTotals", Map.class);
        assertEquals(90.0, categoryTotals.get("Food"), 0.01, "Category total should reflect the added expense");
    }

    @Test
    public void testSaveAndLoadData() {
        // Clear any existing CSV content before test
        File file = new File("expenses.csv");
        if (file.exists()) file.delete();

        JTable table = getPrivateField(tracker, "table", JTable.class);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Ensure table is empty

        model.addRow(new Object[]{"Jul 25", "SaveTest", "Bills", 1, "৳20.00", "৳20.00"});
        invokePrivateMethod(tracker, "saveData");

        // Clear table again to simulate loading from fresh state
        model.setRowCount(0);
        invokePrivateMethod(tracker, "loadData");

        assertTrue(model.getRowCount() > 0, "Data should be loaded from CSV file");
        assertEquals("SaveTest", model.getValueAt(0, 1), "Loaded description should match saved one");

        // assertArrayEquals
        String[] expected = {"Jul 25", "SaveTest", "Bills", "1", "৳20.00", "৳20.00"};
        String[] actual = new String[6];
        for (int i = 0; i < 6; i++) actual[i] = model.getValueAt(0, i).toString();
        assertArrayEquals(expected, actual, "Row data should match what was saved");

        // assertLinesMatch (useful if reading lines from file)
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // skip header
            List<String> expectedLines = List.of("Jul 25,SaveTest,Bills,1,20.00,20.00");
            List<String> actualLines = List.of(reader.readLine());
            assertLinesMatch(expectedLines, actualLines, "CSV lines should match expected values");
        } catch (IOException e) {
            fail("File reading failed");
        }
    }

    @Test
    public void testDeleteExpense() {
        // Add a dummy row manually
        JTable table = getPrivateField(tracker, "table", JTable.class);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.addRow(new Object[]{"Jul 25", "Test", "Food", 2, "৳50.00", "৳100.00"});
        table.setRowSelectionInterval(0, 0);

        invokePrivateMethod(tracker, "deleteSelectedExpense");

        assertEquals(0, model.getRowCount(), "Row should be deleted from table");
    }

    @AfterEach
    public void tearDown() {
        File file = new File("expenses.csv");
        if (file.exists()) file.delete();
    }

    // === Utility Methods for Reflection ===

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName, Class<T> clazz) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return clazz.cast(field.get(obj));
        } catch (Exception e) {
            fail("Unable to access field: " + fieldName);
            return null;
        }
    }

    private void invokePrivateMethod(Object obj, String methodName) {
        try {
            var method = obj.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(obj);
        } catch (Exception e) {
            fail("Unable to invoke method: " + methodName);
        }
    }
}
