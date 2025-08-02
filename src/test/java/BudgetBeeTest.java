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
