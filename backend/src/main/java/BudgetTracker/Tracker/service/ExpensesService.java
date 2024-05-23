package BudgetTracker.Tracker.service;

import BudgetTracker.Tracker.entity.Budget;
import BudgetTracker.Tracker.entity.Expenses;
import BudgetTracker.Tracker.exceptions.BudgetNotFoundException;
import BudgetTracker.Tracker.exceptions.DuplicateExpenseNameException;
import BudgetTracker.Tracker.exceptions.ExpenseNotFoundException;
import BudgetTracker.Tracker.exceptions.InvalidInputException;
import BudgetTracker.Tracker.repository.BudgetRepository;
import BudgetTracker.Tracker.repository.ExpensesRepository;
import BudgetTracker.Tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Service class for handling business logic related to expenses.
 */
@Service
public class ExpensesService {
    @Autowired
    private ExpensesRepository expenseRepository;
    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves all expenses.
     *
     * @return List of all expenses.
     */
    public List<Expenses> getAllExpenses() {
        return expenseRepository.findAll();
    }

    /**
     * Retrieves an expense by its ID.
     *
     * @param id The ID of the expense to retrieve.
     * @return The expense with the specified ID, or null if not found.
     */
    public Expenses getExpenseById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves expenses associated with a specific user.
     *
     * @param userId The ID of the user whose expenses to retrieve.
     * @return List of expenses associated with the specified user.
     */
    public List<Expenses> getExpensesByUserId(Long userId) {
        return expenseRepository.findByBudget_User_Id(userId);
    }

    /**
     * Creates a new expense.
     *
     * @param expense The expense object to be created.
     * @return The created expense.
     * @throws InvalidInputException         If the budget is not set in the expense, expenses description is not alphanumeric,
     *                                       expenses amount is negative, or expenses amount is greater than budget amount.
     * @throws BudgetNotFoundException       If the budget associated with the expense is not found.
     * @throws DuplicateExpenseNameException If an expense with the same name already exists for the user.
     */
    public Expenses createExpense(Expenses expense) {
        // Check if the budget is set
        if (expense.getBudget() == null || expense.getBudget().getBudgetId() == null) {
            throw new InvalidInputException("Budget is not set in the expense");
        }
        // Validate expenses description to be alphanumeric
        if (!isAlphanumeric(expense.getExpensesDescription())) {
            throw new InvalidInputException("ExpensesDescription must be alphanumeric");
        }
        // Validate expenses amount to be non-negative numbers
        if (expense.getExpensesAmount() < 0) {
            throw new InvalidInputException("expenses amount cannot be negative.");
        }
        // Check if the budget exists
        Budget budget = budgetRepository.findById(expense.getBudget().getBudgetId())
                .orElseThrow(() -> new BudgetNotFoundException("Budget with ID " + expense.getBudget().getBudgetId() + " not found"));

        // Check if the expense with the same description already exists for this budget
        boolean exists = expenseRepository.existsByExpensesDescriptionAndBudget_User_Id(expense.getExpensesDescription(), expense.getBudget().getBudgetId());
        if (exists) {
            throw new DuplicateExpenseNameException("An expense with the name \"" + expense.getExpensesDescription() + "\" already exists for this user.");
        }

        return expenseRepository.save(expense);
    }

    /**
     * Checks if a string contains only alphanumeric characters.
     *
     * @param str The string to be checked.
     * @return True if the string contains only alphanumeric characters or spaces, false otherwise.
     */
    private boolean isAlphanumeric(String str) {
        return str.matches("^(?=.*[a-zA-Z])[a-zA-Z0-9 ]+$");
    }

    /**
     * Updates an existing expense with the given ID using the provided expense details.
     *
     * @param id             The ID of the expense to be updated.
     * @param expenseDetails The details of the expense to update.
     * @return The updated expense.
     * @throws ExpenseNotFoundException      If the expense with the specified ID is not found.
     * @throws InvalidInputException         If the provided expense details are invalid.
     * @throws BudgetNotFoundException       If the budget specified in the expense details is not found.
     * @throws DuplicateExpenseNameException If an expense with the same description already exists for the same budget.
     */

    public Expenses updateExpense(Long id, Expenses expenseDetails) {
        // Check if the expense with the given ID exists
        Expenses expenseToUpdate = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense with ID " + id + " not found"));

        // Check if the budget is set in the expense details
        if (expenseDetails.getBudget() == null || expenseDetails.getBudget().getBudgetId() == null) {
            throw new InvalidInputException("Budget is not set in the expense");
        }

        // Validate expenses description to be alphanumeric
        if (!isAlphanumeric(expenseDetails.getExpensesDescription())) {
            throw new InvalidInputException("ExpensesDescription must be alphanumeric");
        }

        // Validate expenses amount to be non-negative numbers
        if (expenseDetails.getExpensesAmount() < 0) {
            throw new InvalidInputException("Expenses amount cannot be negative.");
        }

        // Check if the budget exists
        Budget budget = budgetRepository.findById(expenseDetails.getBudget().getBudgetId())
                .orElseThrow(() -> new BudgetNotFoundException("Budget with ID " + expenseDetails.getBudget().getBudgetId() + " not found"));

        // Check if the expense with the same description already exists for this budget
        boolean exists = expenseRepository.existsByExpensesDescriptionAndBudget_User_Id(expenseDetails.getExpensesDescription(), expenseDetails.getBudget().getBudgetId());
        if (exists && !expenseToUpdate.getExpensesDescription().equals(expenseDetails.getExpensesDescription())) {
            throw new DuplicateExpenseNameException("An expense with the name \"" + expenseDetails.getExpensesDescription() + "\" already exists for this user.");
        }

        // Update the expense details
        expenseToUpdate.setExpensesDescription(expenseDetails.getExpensesDescription());
        expenseToUpdate.setExpensesAmount(expenseDetails.getExpensesAmount());
        // Update other fields as needed

        return expenseRepository.save(expenseToUpdate);
    }


    /**
     * Deletes an expense by its ID.
     *
     * @param id The ID of the expense to be deleted.
     * @throws ExpenseNotFoundException If the expense with the specified ID is not found.
     * @throws RuntimeException        If an unexpected error occurs during the deletion process.
     */
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ExpenseNotFoundException("Expense with ID " + id + " not found");
        }

        try {
            expenseRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while deleting expense with ID " + id, e);
        }
    }

    private static final Logger logger = Logger.getLogger(ExpensesService.class.getName());

    /**
     * Retrieves expenses grouped by their category.
     *
     * @return A map where the key is the expense category and the value is the total amount for that category.
     * If no expenses are found, an empty map is returned.
     */
    public Map<String, Integer> getExpensesGroupedByCategory() {
        List<Expenses> allExpenses = expenseRepository.findAll();
        if (allExpenses.isEmpty()) {
            logger.info("No expenses found for grouping by category.");
            return Collections.emptyMap();
        }
        Map<String, Integer> result = allExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expenses::getExpensesDescription,
                        Collectors.summingInt(Expenses::getExpensesAmount)
                ));
        logger.info("Expenses grouped by category: " + result);
        return result;
    }

    public Map<String, Integer> getExpensesGroupedByBudget() {
        List<Expenses> allExpenses = expenseRepository.findAll();
        if (allExpenses.isEmpty()) {
            logger.info("No expenses found for grouping by category.");
            return Collections.emptyMap();
        }
        Map<String, Integer> result = allExpenses.stream()
                .collect(Collectors.groupingBy(
                                e -> e.getBudget().getBudgetDescription(),
                        Collectors.summingInt(Expenses::getExpensesAmount)
                ));
        logger.info("Expenses grouped by category: " + result);
        return result;
    }
}







