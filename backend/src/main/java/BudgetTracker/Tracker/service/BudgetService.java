package BudgetTracker.Tracker.service;

import BudgetTracker.Tracker.entity.Budget;
import BudgetTracker.Tracker.exceptions.*;
import BudgetTracker.Tracker.repository.BudgetRepository;
import BudgetTracker.Tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Service class for handling business logic related to budgets.
 */
@Service
public class BudgetService {

    @Autowired
    private  BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;


    /**
     * Retrieves budgets associated with a specific user.
     *
     * @param userId The ID of the user whose budgets to retrieve.
     * @return List of budgets associated with the specified user.
     */
    public List<Budget> getBudgetsByUserId(Long userId) {

        return budgetRepository.findByUserId(userId);
    }

    /**
     * Creates a new budget.
     *
     * @param budget The budget object to be created.
     * @return The created budget.
     * @throws UserNotFoundException       If the user associated with the budget does not exist.
     * @throws DuplicateBudgetNameException If a budget with the same name already exists for the user.
     * @throws InvalidInputException       If the budget description is not alphanumeric or if the budget amount is negative.
     */
    public Budget createBudget(Budget budget) {

        // Check if the user exists in the database
        boolean userExists = userService.existsById(budget.getUser().getId());
        if (!userExists) {
            throw new UserNotFoundException("User with ID " + budget.getUser().getId() + " not found.");
        }
        boolean exists = budgetRepository.existsByBudgetDescriptionAndUserId(budget.getBudgetDescription(), budget.getUser().getId());
        if (exists) {
            throw new DuplicateBudgetNameException("A budget with the name \"" + budget.getBudgetDescription() + "\" already exists for this user.");
        }
        // Validate alphanumeric name
        if (!isValidAlphanumeric(budget.getBudgetDescription())) {
            throw new InvalidInputException("BudgetDescription must be alphanumeric");
        }

        // Check if budget amount is negative
        if (budget.getBudgetAmount() <= 0) {
            throw new InvalidInputException("Budget amount cannot be negative or zero.");
        }

        return budgetRepository.save(budget);
    }

    /**
     * Checks if a string contains only alphanumeric characters.
     *
     * @param name The string to be checked.
     * @return True if the string contains only alphanumeric characters or spaces, false otherwise.
     */
    private boolean isValidAlphanumeric(String name) {
        return name.matches("^(?=.*[a-zA-Z])[a-zA-Z0-9 ]+$");
    }
    /**
     * Updates an existing budget with the given ID using the provided budget details.
     *
     * @param id             The ID of the budget to be updated.
     * @param budgetDetails  The details of the budget to update.
     * @return               The updated budget.
     * @throws BudgetNotFoundException       If the budget with the specified ID is not found.
     * @throws UserNotFoundException         If the user associated with the budget is not found.
     * @throws InvalidInputException         If the provided budget details are invalid.
     * @throws DuplicateBudgetNameException  If a budget with the same description already exists for the same user.
     */
    public Budget updateBudget(Long id, Budget budgetDetails) {
        Budget budgetToUpdate = budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException("Budget with ID " + id + " not found"));
        boolean userExist = userService.existsById(budgetDetails.getUser().getId());
        if(!userExist) {
            throw new UserNotFoundException("User with ID " + budgetDetails.getUser().getId() + " not found");
        }
        boolean exists = budgetRepository.existsByBudgetDescriptionAndUserIdExcludingId(
                budgetDetails.getBudgetDescription(),
                budgetDetails.getUser().getId(),
                id);
        if(exists) {
            throw new DuplicateBudgetNameException("A budget with the name \"" + budgetDetails.getBudgetDescription() + "\" already exists for this user.");
        }
        if (!isValidAlphanumeric(budgetDetails.getBudgetDescription())) {
            throw new InvalidInputException("BudgetDescription must be alphanumeric");
        }
        if (budgetDetails.getBudgetAmount() <= 0) {
            throw new InvalidInputException("Budget amount cannot be negative or zero.");
        }
        budgetToUpdate.setBudgetDescription(budgetDetails.getBudgetDescription());
        budgetToUpdate.setBudgetAmount(budgetDetails.getBudgetAmount());
        budgetToUpdate.setUser(budgetDetails.getUser());

        return budgetRepository.save(budgetToUpdate);
    }
    /**
     * Deletes an budget by its ID.
     *
     * @param id The ID of the budget to be deleted.
     * @throws BudgetNotFoundException If the budget with the specified ID is not found.
     */
    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new BudgetNotFoundException("Budget with ID " + id + " not found.");
        }
        budgetRepository.deleteById(id);
    }

    /*
    *
    *
    */



}