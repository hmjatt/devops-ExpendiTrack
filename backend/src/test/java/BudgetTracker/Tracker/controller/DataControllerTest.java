package BudgetTracker.Tracker.controller;


import BudgetTracker.Tracker.entity.*;
import BudgetTracker.Tracker.service.ExpensesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;


import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;



import java.util.*;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.put;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(DataController.class)
public class DataControllerTest{

    @MockBean
    private ExpensesService expensesService;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private ExpensesController expensesController;

    private User user;
    private Budget budget1;
    private Budget budget2;
    private Expenses expense1;
    private Expenses expense2;
    private Expenses expense3;
    private Expenses expense4;

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);

        // Initialize user
        user = new User();
        user.setId(1L);
        user.setName("Abdalla");
        user.setEmail("A@hotmail.com");

        // Initialize budget
        budget1 = new Budget();
        budget1.setBudgetId(1L);
        budget1.setBudgetDescription("Vacation");
        budget1.setBudgetAmount(1000);
        budget1.setUser(user);

        budget2 = new Budget();
        budget2.setBudgetId(2L);
        budget2.setBudgetDescription("Leasure");
        budget2.setBudgetAmount(500);
        budget2.setUser(user);

        // Initialize expenses
        expense1 = new Expenses();
        expense1.setExpensesId(1L);
        expense1.setExpensesDescription("Groceries");
        expense1.setExpensesAmount(50);
        expense1.setExpensesDate(Instant.now());
        expense1.setBudget(budget1);

        expense2 = new Expenses();
        expense2.setExpensesId(2L);
        expense2.setExpensesDescription("Utilities");
        expense2.setExpensesAmount(100);
        expense2.setExpensesDate(Instant.now());
        expense2.setBudget(budget1);

        expense3 = new Expenses();
        expense3.setExpensesId(3L);
        expense3.setExpensesDescription("Food");
        expense3.setExpensesAmount(300);
        expense3.setExpensesDate(Instant.now());
        expense3.setBudget(budget2);

        expense4 = new Expenses();
        expense4.setExpensesId(4L);
        expense4.setExpensesDescription("Movies");
        expense4.setExpensesAmount(100);
        expense4.setExpensesDate(Instant.now());
        expense4.setBudget(budget2);
    }

    @Test
    @DisplayName("Should return total expenses grouped by budget")
    void getTotalExpensesByBudgetTest() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", 1000);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"Vacation\":1000,\"Groceries\":200}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }

    @Test
    @DisplayName("Should handle empty expense grouping")
    void getTotalExpensesByBudget_Empty() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }

    @Test
    @DisplayName("Should handle negative expense amounts")
    void getTotalExpensesByBudget_NegativeAmounts() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", -100);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"Vacation\":-100,\"Groceries\":200}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }

    @Test
    @DisplayName("Should handle null values in expense descriptions")
    void getTotalExpensesByBudget_NullValues() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Uncategorized", 300);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"Uncategorized\":300,\"Groceries\":200}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }


    @Test
    @DisplayName("Should handle expenses with zero amount")
    void getTotalExpensesByBudget_ZeroAmounts() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", 0);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"Vacation\":0,\"Groceries\":200}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }

    @Test
    @DisplayName("Should handle expenses from multiple users")
    void getTotalExpensesByBudget_MultipleUsers() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", 1000);
        expensesByBudget.put("Leasure", 500);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"Vacation\":1000,\"Leasure\":500}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }

    @Test
    @DisplayName("Should handle large number of expenses")
    void getTotalExpensesByBudget_LargeNumberOfExpenses() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            expensesByBudget.put("Budget" + i, i * 10);
        }

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(expensesService).getExpensesGroupedByBudget();
    }

    @Test
    @DisplayName("Should handle future date expenses")
    void getTotalExpensesByBudget_FutureDate() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", 1000);
        expensesByBudget.put("Future Expense", 300);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"Vacation\":1000,\"Future Expense\":300}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }

    @Test
    @DisplayName("Should handle expenses without a budget")
    void getTotalExpensesByBudget_NoBudget() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("No Budget", 150);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"No Budget\":150}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }

    @Test
    @DisplayName("Should handle non-ASCII characters in budget descriptions")
    void getTotalExpensesByBudget_NonAsciiCharacters() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("休暇", 1000);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"休暇\":1000,\"Groceries\":200}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }



//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


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

