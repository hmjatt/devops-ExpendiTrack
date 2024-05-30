package BudgetTracker.Tracker.service;

import BudgetTracker.Tracker.entity.Budget;
import BudgetTracker.Tracker.entity.Expenses;
import BudgetTracker.Tracker.entity.User;
import BudgetTracker.Tracker.exceptions.BudgetNotFoundException;
import BudgetTracker.Tracker.exceptions.DuplicateBudgetNameException;
import BudgetTracker.Tracker.exceptions.InvalidInputException;
import BudgetTracker.Tracker.exceptions.UserNotFoundException;
import BudgetTracker.Tracker.repository.BudgetRepository;
import BudgetTracker.Tracker.repository.BudgetRepositoryTest;
import BudgetTracker.Tracker.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Budget budget1;
    private Budget budget2;

    @BeforeEach
    void init() {
        user = new User();
        user.setId(1L);
        user.setName("Seif");
        user.setEmail("Seif@hotmail.com");

        budget1 = new Budget();
        budget1.setBudgetId(1L);
        budget1.setBudgetDescription("Vacation");
        budget1.setBudgetAmount(1000);
        budget1.setUser(user);

        budget2 = new Budget();
        budget2.setBudgetId(2L);
        budget2.setBudgetDescription("Groceries");
        budget2.setBudgetAmount(500);
        budget2.setUser(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Get budgets by user ID")
    void testGetBudgetsByUserId() {
        Long userId = 1L;
        List<Budget> userBudgets = Arrays.asList(budget1, budget2);
        when(budgetRepository.findByUserId(userId)).thenReturn(userBudgets);

        List<Budget> result = budgetService.getBudgetsByUserId(userId);

        assertEquals(2, result.size());
        assertEquals(userBudgets, result);
    }

    @Test
    @DisplayName("Create a budget successfully")
    void createBudget_Success() {
        // Mocking userService.existsById method
        when(userService.existsById(anyLong())).thenReturn(true);
        when(budgetRepository.existsByBudgetDescriptionAndUserId(budget1.getBudgetDescription(), user.getId())).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget1);

        Budget savedBudget = budgetService.createBudget(budget1);

        assertNotNull(savedBudget);
        assertEquals(budget1.getBudgetDescription(), savedBudget.getBudgetDescription());
        verify(budgetRepository).save(budget1);
    }

    @Test
    @DisplayName("Fail to create a budget due to duplicate name")
    void createBudget_ThrowsDuplicateBudgetNameException() {
        // Mocking userService.existsById method
        when(userService.existsById(anyLong())).thenReturn(true);

        // Mocking the behavior of existsByBudgetDescriptionAndUserId method
        when(budgetRepository.existsByBudgetDescriptionAndUserId(budget1.getBudgetDescription(), user.getId())).thenReturn(true);

        // Asserting that calling createBudget method throws DuplicateBudgetNameException
        assertThatThrownBy(() -> budgetService.createBudget(budget1))
                .isInstanceOf(DuplicateBudgetNameException.class)
                .hasMessageContaining("already exists");

        // Verify that the save method of budgetRepository was never called
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudgetThrowsUserNotFoundException() {
        lenient().when(userRepository.findById(user.getId()))
                .thenReturn(Optional.empty());;
        assertThrows(UserNotFoundException.class, () -> budgetService.createBudget(budget1),
                "User with ID " + budget1.getUser().getId() + " not found.");
    }

    @Test
    void createBudgetThrowsInvalidInputException() {
        Budget budget = new Budget();
        budget.setBudgetDescription("");
        budget.setBudgetAmount(500);
        User user = new User();
        user.setId(1L);
        budget.setUser(user);
        when(userService.existsById(1L)).thenReturn(true);
        assertThrows(InvalidInputException.class, () -> budgetService.createBudget(budget),
                "Budget Description must be alphanumeric and non-empty");
    }

    @Test
    void updateBudgetSuccess() {
        Long budgetId = budget1.getBudgetId();
        given(userService.existsById(budget1.getUser().getId())).willReturn(true);
        given(budgetRepository.findById(budgetId)).willReturn(Optional.of(budget1));
        given(budgetRepository.existsByBudgetDescriptionAndUserIdExcludingId(
                budget1.getBudgetDescription(), budget1.getUser().getId(), budgetId)).willReturn(false);
        given(budgetRepository.save(any(Budget.class))).willReturn(budget1);

        Budget updatedBudget = budgetService.updateBudget(budgetId, budget1);

        assertThat(updatedBudget.getBudgetDescription()).isEqualTo("Vacation");
        assertThat(updatedBudget.getBudgetAmount()).isEqualTo(1000);
    }

    @Test
    void deleteBudgetSuccess() {
        given(budgetRepository.existsById(budget1.getBudgetId())).willReturn(true);
        willDoNothing().given(budgetRepository).deleteById(budget1.getBudgetId());
        budgetService.deleteBudget(budget1.getBudgetId());
        verify(budgetRepository, times(1)).deleteById(budget1.getBudgetId());
    }

    @Test
    void updateBudgetThrowsDuplicateBudgetNameException() {
        Long budgetId = budget1.getBudgetId();
        when(userService.existsById(budget1.getUser().getId())).thenReturn(true);
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));
        when(budgetRepository.existsByBudgetDescriptionAndUserIdExcludingId(
                budget1.getBudgetDescription(), budget1.getUser().getId(), budgetId)).thenReturn(true);
        assertThatThrownBy(() -> budgetService.updateBudget(budgetId, budget1))
                .isInstanceOf(DuplicateBudgetNameException.class)
                .hasMessageContaining("already exists");
        verify(budgetRepository, never()).save(any(Budget.class));
    }



    @Test
    void updateBudgetThrowsUserNotFoundException() {
        Long budgetId = budget1.getBudgetId();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));
        when(userService.existsById(budget1.getUser().getId())).thenReturn(false);
        Exception exception = assertThrows(UserNotFoundException.class,
                () -> budgetService.updateBudget(budgetId, budget1));
        assertTrue(exception.getMessage().contains("User with ID " + budget1.getUser().getId() + " not found"));
        verify(budgetRepository, never()).save(any(Budget.class));
    }


    @Test
    void updateBudgetThrowsInvalidInputExceptionForDescription() {
        Long budgetId = budget1.getBudgetId();
        budget1.setBudgetDescription("");
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));
        when(userService.existsById(budget1.getUser().getId())).thenReturn(true);
        Exception exception = assertThrows(InvalidInputException.class,
                () -> budgetService.updateBudget(budgetId, budget1));
        assertTrue(exception.getMessage().contains("BudgetDescription must be alphanumeric"));
    }


    @Test
    void updateBudgetThrowsInvalidInputExceptionForAmount() {
        Long budgetId = budget1.getBudgetId();
        budget1.setBudgetAmount(-500);
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget1));
        when(userService.existsById(budget1.getUser().getId())).thenReturn(true);
        Exception exception = assertThrows(InvalidInputException.class,
                () -> budgetService.updateBudget(budgetId, budget1));
        assertTrue(exception.getMessage().contains("Budget amount cannot be negative"));
    }

    @Test
    void deleteBudgetThrowsInvalidIdExceptionForNonExistentBudget() {
        Long invalidBudgetId = 999L;
        when(budgetRepository.existsById(invalidBudgetId)).thenReturn(false);
        assertThrows(BudgetNotFoundException.class, () -> budgetService.deleteBudget(invalidBudgetId),
                "Expected deleteBudget to throw, but it didn't");
    }


    @Test
    @DisplayName("Get budget names and amounts by user ID")
    void testGetBudgetNamesAndAmountsByUserId() {
        Long userId = 1L;
        List<Budget> userBudgets = Arrays.asList(budget1, budget2);
        when(budgetRepository.findByUserId(userId)).thenReturn(userBudgets);

        List<BudgetService.BudgetNameAndAmount> result = budgetService.getBudgetNamesAndAmountsByUserId(userId);

        assertEquals(2, result.size());
        assertEquals("Vacation", result.get(0).getName());
        assertEquals(1000, result.get(0).getAmount());
        assertEquals("Groceries", result.get(1).getName());
        assertEquals(500, result.get(1).getAmount());

        verify(budgetRepository, times(1)).findByUserId(userId);
    }

}
