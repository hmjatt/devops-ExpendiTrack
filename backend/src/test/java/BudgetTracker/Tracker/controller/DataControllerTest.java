package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.service.ExpensesService;
import org.junit.jupiter.api.BeforeEach;
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

    // Mock the ExpensesService to isolate the controller tests
    @Mock
    private ExpensesService expensesService;

    // Inject the mocked service into the controller
    @InjectMocks
    private DataController dataController;

    // Initialize the mocks before each test
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test that the controller returns data correctly
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

    // Test that the controller handles empty data correctly
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

    // Test that the controller handles null data correctly
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

    // Test that the controller handles a single category correctly
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

    // Test that the controller handles a large number of categories correctly
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

    // Test that the controller handles negative amounts correctly
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

    // Test that the controller handles large amounts correctly
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
