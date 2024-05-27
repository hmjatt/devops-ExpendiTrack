package BudgetTracker.Tracker.service;

import BudgetTracker.Tracker.entity.Budget;
import BudgetTracker.Tracker.entity.Expenses;
import BudgetTracker.Tracker.exceptions.BudgetNotFoundException;
import BudgetTracker.Tracker.exceptions.ExpenseNotFoundException;
import BudgetTracker.Tracker.exceptions.InvalidInputException;
import BudgetTracker.Tracker.repository.BudgetRepository;
import BudgetTracker.Tracker.repository.ExpensesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpensesServiceTest{

    @Mock
    private ExpensesRepository expensesRepository;
    @InjectMocks
    private ExpensesService expensesService;

    @Mock
    private BudgetRepository budgetRepository;
    private Budget budget;
    private Expenses expense;

    @BeforeEach
    void setUp() {
        expensesService = new ExpensesService();
        MockitoAnnotations.openMocks(this);

        // Inject the mocked ExpensesRepository into your service
        ReflectionTestUtils.setField(expensesService, "expenseRepository", expensesRepository);

        expense = new Expenses();
        expense.setExpensesDescription("tuition fees");
        expense.setExpensesDate(Instant.now());

        budget = new Budget();
        budget.setBudgetId(100L);
        budget.setBudgetDescription("study");
        budget.setBudgetAmount(1000);

    }

    @Test
    void canGetAllExpenses() {
        List<Expenses> expensesList = Collections.singletonList(expense);
        when(expensesRepository.findAll()).thenReturn(expensesList);

        expensesService.getAllExpenses();

        verify(expensesRepository).findAll();
        assertEquals(1, expensesList.size());
    }

    @Test
    void canGetExpenseById() {
        Long expenseId = expense.getExpensesId();
        when(expensesRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        Expenses result = expensesService.getExpenseById(expenseId);
        // Verify that findById was called with the correct ID
        verify(expensesRepository).findById(expenseId);
        assertEquals(expense.getExpensesId(), result.getExpensesId());

    }

    @Test
    void updateExpenseValidInput() {
        Long expenseId = 1L;
        Expenses expenseToUpdate = new Expenses();
        expenseToUpdate.setExpensesDescription("Updated Expense Description");
        expenseToUpdate.setExpensesAmount(100);

        Budget budget = new Budget();
        budget.setBudgetId(1L);
        expenseToUpdate.setBudget(budget);

        // Stubbing repository methods
        when(expensesRepository.findById(expenseId)).thenReturn(Optional.of(expenseToUpdate));
        when(budgetRepository.findById(any(Long.class))).thenReturn(Optional.of(budget));
        when(expensesRepository.existsByExpensesDescriptionAndBudget_User_Id(any(String.class), any(Long.class))).thenReturn(false);
        when(expensesRepository.save(any(Expenses.class))).thenReturn(expenseToUpdate);

        // Call the service method
        Expenses updatedExpense = expensesService.updateExpense(expenseId, expenseToUpdate);

        // Verify that the service method is called with the correct parameters
        verify(expensesRepository).findById(expenseId);
        verify(budgetRepository).findById(expenseToUpdate.getBudget().getBudgetId());
        verify(expensesRepository).existsByExpensesDescriptionAndBudget_User_Id(expenseToUpdate.getExpensesDescription(), expenseToUpdate.getBudget().getBudgetId());
        verify(expensesRepository).save(expenseToUpdate);

        assertNotNull(updatedExpense);
        assertEquals(expenseToUpdate.getExpensesDescription(), updatedExpense.getExpensesDescription());
        assertEquals(expenseToUpdate.getExpensesAmount(), updatedExpense.getExpensesAmount());
    }
    @Test
    void deleteExpense_ExistingExpense() {
        // Given
        Long expenseId = 1L;
        when(expensesRepository.existsById(expenseId)).thenReturn(true);

        // When
        expensesService.deleteExpense(expenseId);

        // Then
        verify(expensesRepository).deleteById(expenseId);
    }

    @Test
    void createExpenseThrowsExceptionAlphanumeric() {
        Expenses expense = new Expenses();
        expense.setExpensesDescription("89");
        expense.setExpensesAmount(1000);
        expense.setBudget(new Budget());
        expense.getBudget().setBudgetId(1L);
        assertThrows(InvalidInputException.class, () -> expensesService.createExpense(expense),
                "Expenses Description must be alphanumeric");
    }

    @Test
    void whenCreateExpenseWithNegativeAmount_thenThrowInvalidInputException() {
        Expenses expense = new Expenses();
        expense.setExpensesDescription("ValidDescription");
        expense.setExpensesAmount(-100);
        expense.setBudget(new Budget());
        expense.getBudget().setBudgetId(1L);

        assertThrows(InvalidInputException.class, () -> expensesService.createExpense(expense),
                "expenses amount cannot be negative.");
    }

    @Test
    void whenCreateExpenseForNonexistentBudget_thenThrowBudgetNotFoundException() {
        Expenses expense = new Expenses();
        expense.setExpensesDescription("ValidDescription");
        expense.setExpensesAmount(100);
        Budget budget = new Budget();
        budget.setBudgetId(999L);
        expense.setBudget(budget);

        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BudgetNotFoundException.class, () -> expensesService.createExpense(expense),
                "Budget with ID " + expense.getBudget().getBudgetId() + " not found");
    }
    @Test
    void updateExpenseNonexistentExpense() {
        // Prepare test data
        Long expenseId = 1L;
        Expenses expenseToUpdate = new Expenses();
        expenseToUpdate.setExpensesDescription("Updated Expense Description");
        expenseToUpdate.setExpensesAmount(100);
        Budget budget = new Budget();
        budget.setBudgetId(1L);
        expenseToUpdate.setBudget(budget);

        // Stubbing repository methods
        when(expensesRepository.findById(expenseId)).thenReturn(Optional.empty());

        // Call the service method and expect an exception
        assertThrows(ExpenseNotFoundException.class, () -> expensesService.updateExpense(expenseId, expenseToUpdate));
    }

    @Test
    void updateExpense_InvalidInput_NegativeAmount() {
        // Given
        Long expenseId = 1L;
        Expenses existingExpense = new Expenses();
        existingExpense.setExpensesId(expenseId);
        existingExpense.setExpensesDescription("Existing Expense Description");
        existingExpense.setExpensesAmount(100); // Positive amount

        Expenses expenseToUpdate = new Expenses();
        expenseToUpdate.setExpensesId(expenseId);
        expenseToUpdate.setExpensesDescription("Updated Expense Description");
        expenseToUpdate.setExpensesAmount(-50); // Negative amount

        Budget budget = new Budget();
        budget.setBudgetId(1L);

        when(expensesRepository.findById(expenseId)).thenReturn(java.util.Optional.of(existingExpense));

        // When/Then
        assertThrows(InvalidInputException.class, () -> expensesService.updateExpense(expenseId, expenseToUpdate));
    }

    @Test
    void updateExpenseNonexistentBudget() {
        // Prepare test data
        Long expenseId = 1L;
        Expenses expenseToUpdate = new Expenses();
        expenseToUpdate.setExpensesDescription("Updated Expense Description");
        expenseToUpdate.setExpensesAmount(100);
        Budget budget = new Budget();
        budget.setBudgetId(1L);
        expenseToUpdate.setBudget(budget);

        // Stubbing repository methods
        when(expensesRepository.findById(expenseId)).thenReturn(Optional.of(new Expenses()));
        when(budgetRepository.findById(budget.getBudgetId())).thenReturn(Optional.empty());

        // Call the service method and expect an exception
        assertThrows(BudgetNotFoundException.class, () -> expensesService.updateExpense(expenseId, expenseToUpdate));
    }

    @Test
    void deleteExpense_NonExistingExpense() {
        // Given
        Long expenseId = 1L;
        when(expensesRepository.existsById(expenseId)).thenReturn(false);

        // When/Then
        assertThrows(ExpenseNotFoundException.class, () -> expensesService.deleteExpense(expenseId));
        verify(expensesRepository, never()).deleteById(expenseId);
    }

    @Test
    void deleteExpense_ExceptionDuringDeletion() {
        // Given
        Long expenseId = 1L;
        when(expensesRepository.existsById(expenseId)).thenReturn(true);
        doThrow(RuntimeException.class).when(expensesRepository).deleteById(expenseId);

        // When/Then
        assertThrows(RuntimeException.class, () -> expensesService.deleteExpense(expenseId));
    }


    @Test
    void getExpensesGroupedByCategory_ReturnsData() {
        Long userId = 1L;

        // Prepare test data
        Expenses expense1 = new Expenses();
        expense1.setExpensesDescription("Food");
        expense1.setExpensesAmount(100);
        expense1.setBudget(budget);

        Expenses expense2 = new Expenses();
        expense2.setExpensesDescription("Transport");
        expense2.setExpensesAmount(50);
        expense2.setBudget(budget);

        Expenses expense3 = new Expenses();
        expense3.setExpensesDescription("Food");
        expense3.setExpensesAmount(150);
        expense3.setBudget(budget);

        List<Expenses> expensesList = List.of(expense1, expense2, expense3);

        // Mock the repository method
        when(expensesRepository.findByBudget_User_Id(userId)).thenReturn(expensesList);

        // Call the service method
        Map<String, Integer> result = expensesService.getExpensesGroupedByCategory(userId);

        // Prepare the expected result
        Map<String, Integer> expected = new HashMap<>();
        expected.put("Food", 250);
        expected.put("Transport", 50);

        // Verify the result
        assertEquals(expected, result);
    }

    @Test
    void getExpensesGroupedByCategory_ReturnsEmptyMap() {
        Long userId = 1L;

        // Mock the repository method to return an empty list
        when(expensesRepository.findByBudget_User_Id(userId)).thenReturn(Collections.emptyList());

        // Call the service method
        Map<String, Integer> result = expensesService.getExpensesGroupedByCategory(userId);

        // Verify the result is an empty map
        assertEquals(Collections.emptyMap(), result);
    }

    @Test
    void getExpensesGroupedByCategory_SingleExpense() {
        Long userId = 1L;

        // Prepare test data
        Expenses expense = new Expenses();
        expense.setExpensesDescription("Utilities");
        expense.setExpensesAmount(200);
        expense.setBudget(budget);

        List<Expenses> expensesList = List.of(expense);

        // Mock the repository method
        when(expensesRepository.findByBudget_User_Id(userId)).thenReturn(expensesList);

        // Call the service method
        Map<String, Integer> result = expensesService.getExpensesGroupedByCategory(userId);

        // Prepare the expected result
        Map<String, Integer> expected = new HashMap<>();
        expected.put("Utilities", 200);

        // Verify the result
        assertEquals(expected, result);
    }

    @Test
    void getExpensesGroupedByCategory_LargeNumberOfCategories() {
        Long userId = 1L;

        // Prepare test data with a large number of categories
        List<Expenses> expensesList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Expenses expense = new Expenses();
            expense.setExpensesDescription("Category" + i);
            expense.setExpensesAmount(i);
            expense.setBudget(budget);
            expensesList.add(expense);
        }

        // Mock the repository method
        when(expensesRepository.findByBudget_User_Id(userId)).thenReturn(expensesList);

        // Call the service method
        Map<String, Integer> result = expensesService.getExpensesGroupedByCategory(userId);

        // Verify the result
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, result.get("Category" + i));
        }
    }

    @Test
    void getExpensesGroupedByCategory_NegativeAmounts() {
        Long userId = 1L;

        // Prepare test data with negative amounts
        Expenses expense = new Expenses();
        expense.setExpensesDescription("Refunds");
        expense.setExpensesAmount(-50);
        expense.setBudget(budget);

        List<Expenses> expensesList = List.of(expense);

        // Mock the repository method
        when(expensesRepository.findByBudget_User_Id(userId)).thenReturn(expensesList);

        // Call the service method
        Map<String, Integer> result = expensesService.getExpensesGroupedByCategory(userId);

        // Prepare the expected result
        Map<String, Integer> expected = new HashMap<>();
        expected.put("Refunds", -50);

        // Verify the result
        assertEquals(expected, result);
    }

    @Test
    void getExpensesGroupedByCategory_LargeAmounts() {
        Long userId = 1L;

        // Prepare test data with large amounts
        Expenses expense = new Expenses();
        expense.setExpensesDescription("Project");
        expense.setExpensesAmount(Integer.MAX_VALUE);
        expense.setBudget(budget);

        List<Expenses> expensesList = List.of(expense);

        // Mock the repository method
        when(expensesRepository.findByBudget_User_Id(userId)).thenReturn(expensesList);

        // Call the service method
        Map<String, Integer> result = expensesService.getExpensesGroupedByCategory(userId);

        // Prepare the expected result
        Map<String, Integer> expected = new HashMap<>();
        expected.put("Project", Integer.MAX_VALUE);

        // Verify the result
        assertEquals(expected, result);
    }
}
