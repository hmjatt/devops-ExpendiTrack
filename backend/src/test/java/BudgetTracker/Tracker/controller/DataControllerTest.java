package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.service.ExpensesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DataControllerTest {

    @Mock
    private ExpensesService expensesService;

    @InjectMocks
    private DataController dataController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return total expenses grouped by budget")
    void getTotalExpensesByBudgetTest() {
        // Arrange
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", 1000);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getTotalExpensesByBudget();

        // Assert
        assertEquals(ResponseEntity.ok(expensesByBudget), response);
    }

    @Test
    @DisplayName("Should handle empty expense grouping")
    void getTotalExpensesByBudget_Empty() {
        // Arrange
        Map<String, Integer> expensesByBudget = new HashMap<>();

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getTotalExpensesByBudget();

        // Assert
        assertEquals(ResponseEntity.ok(expensesByBudget), response);
    }

    @Test
    @DisplayName("Should handle negative expense amounts")
    void getTotalExpensesByBudget_NegativeAmounts() {
        // Arrange
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", -100);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getTotalExpensesByBudget();

        // Assert
        assertEquals(ResponseEntity.ok(expensesByBudget), response);
    }

    @Test
    @DisplayName("Should handle null values in expense descriptions")
    void getTotalExpensesByBudget_NullValues() {
        // Arrange
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Uncategorized", 300);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getTotalExpensesByBudget();

        // Assert
        assertEquals(ResponseEntity.ok(expensesByBudget), response);
    }

    @Test
    @DisplayName("Should handle expenses with zero amount")
    void getTotalExpensesByBudget_ZeroAmounts() {
        // Arrange
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", 0);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getTotalExpensesByBudget();

        // Assert
        assertEquals(ResponseEntity.ok(expensesByBudget), response);
    }

    @Test
    @DisplayName("Should handle expenses from multiple users")
    void getTotalExpensesByBudget_MultipleUsers() {
        // Arrange
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", 1000);
        expensesByBudget.put("Leasure", 500);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getTotalExpensesByBudget();

        // Assert
        assertEquals(ResponseEntity.ok(expensesByBudget), response);
    }

    @Test
    @DisplayName("Should handle large number of expenses")
    void getTotalExpensesByBudget_LargeNumberOfExpenses() {
        // Arrange
        Map<String, Integer> expensesByBudget = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            expensesByBudget.put("Budget" + i, i * 10);
        }

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getTotalExpensesByBudget();

        // Assert
        assertEquals(ResponseEntity.ok(expensesByBudget), response);
    }

    @Test
    @DisplayName("Should handle future date expenses")
    void getTotalExpensesByBudget_FutureDate() {
        // Arrange
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", 1000);
        expensesByBudget.put("Future Expense", 300);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getTotalExpensesByBudget();

        // Assert
        assertEquals(ResponseEntity.ok(expensesByBudget), response);
    }

    @Test
    @DisplayName("Should handle expenses without a budget")
    void getTotalExpensesByBudget_NoBudget() {
        // Arrange
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("No Budget", 150);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getTotalExpensesByBudget();

        // Assert
        assertEquals(ResponseEntity.ok(expensesByBudget), response);
    }

    @Test
    @DisplayName("Should handle non-ASCII characters in budget descriptions")
    void getTotalExpensesByBudget_NonAsciiCharacters() {
        // Arrange
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("休暇", 1000);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getTotalExpensesByBudget();

        // Assert
        assertEquals(ResponseEntity.ok(expensesByBudget), response);
    }

    @Test
    void testGetExpensesByCategory_ReturnsData() {
        // Arrange
        Long userId = 1L;
        Map<String, Integer> mockExpensesByCategory = new HashMap<>();
        mockExpensesByCategory.put("Food", 150);
        mockExpensesByCategory.put("Transport", 80);

        // Mock the service method to return the test data
        when(expensesService.getExpensesGroupedByCategory(userId)).thenReturn(mockExpensesByCategory);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getExpensesByCategory(userId);

        // Assert
        assertEquals(ResponseEntity.ok(mockExpensesByCategory), response);
    }

    @Test
    void testGetExpensesByCategory_ReturnsEmptyData() {
        // Arrange
        Long userId = 1L;
        Map<String, Integer> mockExpensesByCategory = new HashMap<>();

        // Mock the service method to return the empty data
        when(expensesService.getExpensesGroupedByCategory(userId)).thenReturn(mockExpensesByCategory);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getExpensesByCategory(userId);

        // Assert
        assertEquals(ResponseEntity.ok(mockExpensesByCategory), response);
    }

    @Test
    void testGetExpensesByCategory_ReturnsNullData() {
        // Arrange
        Long userId = 1L;
        // Mock the service method to return null
        when(expensesService.getExpensesGroupedByCategory(userId)).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getExpensesByCategory(userId);

        // Assert
        assertEquals(ResponseEntity.ok(null), response);
    }

    @Test
    void testGetExpensesByCategory_SingleCategory() {
        // Arrange
        Long userId = 1L;
        Map<String, Integer> mockExpensesByCategory = new HashMap<>();
        mockExpensesByCategory.put("Utilities", 200);

        // Mock the service method to return the test data
        when(expensesService.getExpensesGroupedByCategory(userId)).thenReturn(mockExpensesByCategory);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getExpensesByCategory(userId);

        // Assert
        assertEquals(ResponseEntity.ok(mockExpensesByCategory), response);
    }

    @Test
    void testGetExpensesByCategory_LargeNumberOfCategories() {
        // Arrange
        Long userId = 1L;
        Map<String, Integer> mockExpensesByCategory = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            mockExpensesByCategory.put("Category" + i, i);
        }

        // Mock the service method to return the test data
        when(expensesService.getExpensesGroupedByCategory(userId)).thenReturn(mockExpensesByCategory);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getExpensesByCategory(userId);

        // Assert
        assertEquals(ResponseEntity.ok(mockExpensesByCategory), response);
    }

    @Test
    void testGetExpensesByCategory_NegativeAmounts() {
        // Arrange
        Long userId = 1L;
        Map<String, Integer> mockExpensesByCategory = new HashMap<>();
        mockExpensesByCategory.put("Refunds", -50);

        // Mock the service method to return the test data
        when(expensesService.getExpensesGroupedByCategory(userId)).thenReturn(mockExpensesByCategory);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getExpensesByCategory(userId);

        // Assert
        assertEquals(ResponseEntity.ok(mockExpensesByCategory), response);
    }

    @Test
    void testGetExpensesByCategory_LargeAmounts() {
        // Arrange
        Long userId = 1L;
        Map<String, Integer> mockExpensesByCategory = new HashMap<>();
        mockExpensesByCategory.put("Project", Integer.MAX_VALUE);

        // Mock the service method to return the test data
        when(expensesService.getExpensesGroupedByCategory(userId)).thenReturn(mockExpensesByCategory);

        // Act
        ResponseEntity<Map<String, Integer>> response = dataController.getExpensesByCategory(userId);

        // Assert
        assertEquals(ResponseEntity.ok(mockExpensesByCategory), response);
    }
}
