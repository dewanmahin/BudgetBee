import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BudgetBeeTest {

    private BudgetBee tracker;

    @BeforeEach
    public void setUp() {
        tracker = new BudgetBee();
    }

    @Test
    public void testInitialState() {
        assertNotNull(tracker, "BudgetBee instance should not be null");

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
        File file = new File("expenses.csv");
        if (file.exists()) file.delete();

        JTable table = getPrivateField(tracker, "table", JTable.class);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        model.addRow(new Object[]{"Jul 25", "SaveTest", "Bills", 1, "৳20.00", "৳20.00"});
        invokePrivateMethod(tracker, "saveData");

        model.setRowCount(0);
        invokePrivateMethod(tracker, "loadData");

        assertTrue(model.getRowCount() > 0, "Data should be loaded from CSV file");
        assertEquals("SaveTest", model.getValueAt(0, 1), "Loaded description should match saved one");

        // assertArrayEquals
        String[] expected = {"Jul 25", "SaveTest", "Bills", "1", "৳20.00", "৳20.00"};
        String[] actual = new String[6];
        for (int i = 0; i < 6; i++) actual[i] = model.getValueAt(0, i).toString();
        assertArrayEquals(expected, actual, "Row data should match what was saved");

        // assertLinesMatch
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
        JTable table = getPrivateField(tracker, "table", JTable.class);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.addRow(new Object[]{"Jul 25", "Test", "Food", 2, "৳50.00", "৳100.00"});
        table.setRowSelectionInterval(0, 0);

        invokePrivateMethod(tracker, "deleteSelectedExpense");

        assertEquals(0, model.getRowCount(), "Row should be deleted from table");
    }

    @Test
    public void testObjectReference() {
        JTable table1 = getPrivateField(tracker, "table", JTable.class);
        JTable table2 = getPrivateField(tracker, "table", JTable.class);

        JTable newTable = new JTable();

        assertSame(table1, table2, "Same reference should be same");
        assertNotSame(table1, newTable, "Different JTable instances should not be same");
    }

    // @ValueSource Test
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4})
    public void testTotalCalculationWithDiffQty(int quantity) {
        JTextField descField = getPrivateField(tracker, "descriptionField", JTextField.class);
        JTextField qtyField = getPrivateField(tracker, "quantityField", JTextField.class);
        JTextField amtField = getPrivateField(tracker, "amountField", JTextField.class);
        JComboBox<String> categoryCombo = getPrivateField(tracker, "categoryCombo", JComboBox.class);

        descField.setText("Burger");
        amtField.setText("50");
        qtyField.setText(String.valueOf(quantity));
        categoryCombo.setSelectedItem("Food");

        invokePrivateMethod(tracker, "addExpense");

        JTable table = getPrivateField(tracker, "table", JTable.class);
        int lastRow = table.getRowCount() - 1;

        String totalCell = table.getValueAt(lastRow, 5).toString().replace("৳", "").trim();
        double totalInTable = Double.parseDouble(totalCell);

        double expectedTotal = 50 * quantity;

        assertEquals(expectedTotal, totalInTable, 0.01, "Total should be quantity × amount");
    }

    // @CsvSource Test
    @ParameterizedTest
    @CsvSource({
            "2, 10, 20.00",
            "3, 15, 45.00",
            "0, 100, 0.00"
    })
    public void testTotalCalculationWithCsvSource(int qty, double amt, double expectedTotal) throws Exception {
        getPrivateField(tracker, "descriptionField", JTextField.class).setText("Item");
        getPrivateField(tracker, "quantityField", JTextField.class).setText(String.valueOf(qty));
        getPrivateField(tracker, "amountField", JTextField.class).setText(String.valueOf(amt));
        getPrivateField(tracker, "categoryCombo", JComboBox.class).setSelectedItem("Bills");

        invokePrivateMethod(tracker, "addExpense");

        JTable table = getPrivateField(tracker, "table", JTable.class);
        int lastRow = table.getRowCount() - 1;
        double actualTotal = Double.parseDouble(table.getValueAt(lastRow, 5).toString().replace("৳", "").trim());

        assertEquals(expectedTotal, actualTotal, 0.01);
    }

    // CsvFileSource test
    @ParameterizedTest
    @CsvFileSource(resources = "/testdata.csv", numLinesToSkip = 1)
    public void testTotalCalculationWithCsvFileSource(int qty, double amt, double expectedTotal) throws Exception {
        getPrivateField(tracker, "descriptionField", JTextField.class).setText("Item");
        getPrivateField(tracker, "quantityField", JTextField.class).setText(String.valueOf(qty));
        getPrivateField(tracker, "amountField", JTextField.class).setText(String.valueOf(amt));
        getPrivateField(tracker, "categoryCombo", JComboBox.class).setSelectedItem("Bills");

        invokePrivateMethod(tracker, "addExpense");

        JTable table = getPrivateField(tracker, "table", JTable.class);
        int lastRow = table.getRowCount() - 1;
        double actualTotal = Double.parseDouble(table.getValueAt(lastRow, 5).toString().replace("৳", "").trim());

        assertEquals(expectedTotal, actualTotal, 0.01);
    }

    // MethodSource test
    private static Stream<Arguments> expenseDataProvider() {
        return Stream.of(
                Arguments.of(1, 10.0, 10.00),
                Arguments.of(3, 15.0, 45.00),
                Arguments.of(0, 100.0, 0.00)
        );
    }
    @ParameterizedTest
    @MethodSource("expenseDataProvider")
    public void testTotalCalculationWithMethodSource(int qty, double amt, double expectedTotal) {
        JTextField descField = getPrivateField(tracker, "descriptionField", JTextField.class);
        JTextField qtyField = getPrivateField(tracker, "quantityField", JTextField.class);
        JTextField amtField = getPrivateField(tracker, "amountField", JTextField.class);
        JComboBox<String> categoryCombo = getPrivateField(tracker, "categoryCombo", JComboBox.class);

        descField.setText("Item");
        qtyField.setText(String.valueOf(qty));
        amtField.setText(String.valueOf(amt));
        categoryCombo.setSelectedItem("Bills");

        invokePrivateMethod(tracker, "addExpense");

        JTable table = getPrivateField(tracker, "table", JTable.class);
        int lastRow = table.getRowCount() - 1;
        double actualTotal = Double.parseDouble(table.getValueAt(lastRow, 5).toString().replace("৳", "").trim());

        assertEquals(expectedTotal, actualTotal, 0.01);
    }

    @AfterEach
    public void tearDown() {
        File file = new File("expenses.csv");
        if (file.exists()) file.delete();
    }

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