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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private Budget budget1;
    private Budget budget2;

    private Expenses expense;
    private Expenses expense1;
    private Expenses expense2;
    private Expenses expense3;

    @BeforeEach
    void setUp() {
        expensesService = new ExpensesService();
        MockitoAnnotations.openMocks(this);

        // Inject the mocked ExpensesRepository into your service
        ReflectionTestUtils.setField(expensesService, "expenseRepository", expensesRepository);



        // Initialize budgets
        budget = new Budget();
        budget.setBudgetId(100L);
        budget.setBudgetDescription("study");
        budget.setBudgetAmount(1000);

        budget1 = new Budget();
        budget1.setBudgetId(1L);
        budget1.setBudgetDescription("Groceries");
        budget1.setBudgetAmount(500);

        budget2 = new Budget();
        budget2.setBudgetId(2L);
        budget2.setBudgetDescription("Utilities");
        budget2.setBudgetAmount(300);

        // Initialize expenses
        expense = new Expenses();
        expense.setExpensesDescription("tuition fees");
        expense.setExpensesDate(Instant.now());

        expense1 = new Expenses();
        expense1.setExpensesId(1L);
        expense1.setExpensesDescription("Milk");
        expense1.setExpensesAmount(50);
        expense1.setExpensesDate(Instant.now());
        expense1.setBudget(budget1);

        expense2 = new Expenses();
        expense2.setExpensesId(2L);
        expense2.setExpensesDescription("Bread");
        expense2.setExpensesAmount(30);
        expense2.setExpensesDate(Instant.now());
        expense2.setBudget(budget1);

        expense3 = new Expenses();
        expense3.setExpensesId(3L);
        expense3.setExpensesDescription("Electricity");
        expense3.setExpensesAmount(100);
        expense3.setExpensesDate(Instant.now());
        expense3.setBudget(budget2);

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
    void getExpensesGroupedByBudget_Success() {
        List<Expenses> allExpenses = Arrays.asList(expense1, expense2, expense3);

        when(expensesRepository.findAll()).thenReturn(allExpenses);

        Map<String, Integer> groupedExpenses = expensesService.getExpensesGroupedByBudget();

        assertEquals(2, groupedExpenses.size());
        assertEquals(80, groupedExpenses.get("Groceries").intValue());
        assertEquals(100, groupedExpenses.get("Utilities").intValue());

        verify(expensesRepository).findAll();
    }

    @Test
    void getExpensesGroupedByBudget_EmptyList() {
        when(expensesRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Integer> groupedExpenses = expensesService.getExpensesGroupedByBudget();

        assertEquals(0, groupedExpenses.size());

        verify(expensesRepository).findAll();
    }

    @Test
    void getExpensesGroupedByBudget_NullBudgetDescription() {
        Expenses expenseWithNullBudgetDescription = new Expenses();
        expenseWithNullBudgetDescription.setExpensesId(4L);
        expenseWithNullBudgetDescription.setExpensesDescription("Unknown");
        expenseWithNullBudgetDescription.setExpensesAmount(50);
        expenseWithNullBudgetDescription.setExpensesDate(Instant.now());
        expenseWithNullBudgetDescription.setBudget(null);

        List<Expenses> allExpenses = Arrays.asList(expense1, expense2, expense3, expenseWithNullBudgetDescription);

        when(expensesRepository.findAll()).thenReturn(allExpenses);

        Map<String, Integer> groupedExpenses = expensesService.getExpensesGroupedByBudget();

        assertEquals(3, groupedExpenses.size());
        assertEquals(80, groupedExpenses.get("Groceries").intValue());
        assertEquals(100, groupedExpenses.get("Utilities").intValue());
        assertEquals(50, groupedExpenses.get("Uncategorized").intValue());

        verify(expensesRepository).findAll();
    }

    @Test
    void getExpensesGroupedByBudget_NegativeAmounts() {
        Expenses expenseWithNegativeAmount = new Expenses();
        expenseWithNegativeAmount.setExpensesId(4L);
        expenseWithNegativeAmount.setExpensesDescription("Refund");
        expenseWithNegativeAmount.setExpensesAmount(-20);
        expenseWithNegativeAmount.setExpensesDate(Instant.now());
        expenseWithNegativeAmount.setBudget(budget1);

        List<Expenses> allExpenses = Arrays.asList(expense1, expense2, expense3, expenseWithNegativeAmount);

        when(expensesRepository.findAll()).thenReturn(allExpenses);

        Map<String, Integer> groupedExpenses = expensesService.getExpensesGroupedByBudget();

        assertEquals(2, groupedExpenses.size());
        assertEquals(60, groupedExpenses.get("Groceries").intValue()); // 80 - 20
        assertEquals(100, groupedExpenses.get("Utilities").intValue());

        verify(expensesRepository).findAll();
    }

    @Test
    void getExpensesGroupedByBudget_ZeroAmount() {
        Expenses expenseWithZeroAmount = new Expenses();
        expenseWithZeroAmount.setExpensesId(4L);
        expenseWithZeroAmount.setExpensesDescription("Free Sample");
        expenseWithZeroAmount.setExpensesAmount(0);
        expenseWithZeroAmount.setExpensesDate(Instant.now());
        expenseWithZeroAmount.setBudget(budget1);

        List<Expenses> allExpenses = Arrays.asList(expense1, expense2, expense3, expenseWithZeroAmount);

        when(expensesRepository.findAll()).thenReturn(allExpenses);

        Map<String, Integer> groupedExpenses = expensesService.getExpensesGroupedByBudget();

        assertEquals(2, groupedExpenses.size());
        assertEquals(80, groupedExpenses.get("Groceries").intValue());
        assertEquals(100, groupedExpenses.get("Utilities").intValue());

        verify(expensesRepository).findAll();
    }

    @Test
    void getExpensesGroupedByBudget_LargeNumberOfExpenses() {
        List<Expenses> allExpenses = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Expenses expense = new Expenses();
            expense.setExpensesId((long) i);
            expense.setExpensesDescription("Expense " + i);
            expense.setExpensesAmount(10);
            expense.setExpensesDate(Instant.now());
            expense.setBudget(budget1);
            allExpenses.add(expense);
        }

        when(expensesRepository.findAll()).thenReturn(allExpenses);

        Map<String, Integer> groupedExpenses = expensesService.getExpensesGroupedByBudget();

        assertEquals(1, groupedExpenses.size());
        assertEquals(10000, groupedExpenses.get("Groceries").intValue());

        verify(expensesRepository).findAll();
    }

    @Test
    void getExpensesGroupedByBudget_NonAsciiCharacters() {
        Budget budgetWithNonAscii = new Budget();
        budgetWithNonAscii.setBudgetId(3L);
        budgetWithNonAscii.setBudgetDescription("休暇");

        Expenses expenseWithNonAscii = new Expenses();
        expenseWithNonAscii.setExpensesId(4L);
        expenseWithNonAscii.setExpensesDescription("Sushi");
        expenseWithNonAscii.setExpensesAmount(200);
        expenseWithNonAscii.setExpensesDate(Instant.now());
        expenseWithNonAscii.setBudget(budgetWithNonAscii);

        List<Expenses> allExpenses = Arrays.asList(expense1, expense2, expense3, expenseWithNonAscii);

        when(expensesRepository.findAll()).thenReturn(allExpenses);

        Map<String, Integer> groupedExpenses = expensesService.getExpensesGroupedByBudget();

        assertEquals(3, groupedExpenses.size());
        assertEquals(80, groupedExpenses.get("Groceries").intValue());
        assertEquals(100, groupedExpenses.get("Utilities").intValue());
        assertEquals(200, groupedExpenses.get("休暇").intValue());

        verify(expensesRepository).findAll();
    }




//    ++++++++++++++++++++++++++++++++++++++++++++++++++++++++




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

