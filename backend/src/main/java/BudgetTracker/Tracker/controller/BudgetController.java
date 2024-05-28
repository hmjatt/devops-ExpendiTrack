package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.entity.Budget;
import BudgetTracker.Tracker.entity.User;
import BudgetTracker.Tracker.exceptions.*;
import BudgetTracker.Tracker.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Controller class responsible for handling HTTP requests related to budgets.
 */
@RestController
@RequestMapping("/budgets")
public class BudgetController {
    @Autowired
    private  BudgetService budgetService;
    /**
     * Retrieves budgets associated with a specific user.
     *
     * @param userId The ID of the user whose budgets are to be retrieved.
     * @return ResponseEntity containing a list of budgets associated with the user, if found, along with the HTTP status.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Find budgets by user id", description = "Provide an user id to get user's budgets", responses = {
            @ApiResponse(responseCode = "200", description = "Budgets found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Budget.class)))),
            @ApiResponse(responseCode = "404", description = "Budgets not found")
    })
    public ResponseEntity<List<Budget>> getBudgetsByUserId(@Parameter(name="userId", description = "ID of the user to find budgets for", example = "1")
                                                               @PathVariable Long userId) {
        List<Budget> budgets = budgetService.getBudgetsByUserId(userId);
        return new ResponseEntity<>(budgets, HttpStatus.OK);
    }
    /**
     * Creates a new budget.
     *
     * @param budget The budget object to be created.
     * @return ResponseEntity containing the created budget along with the HTTP status.
     *         If the budget creation fails due to duplicate name, invalid input, or user not found, an appropriate error message is returned.
     */
    @PostMapping
    @Operation(summary = "Create a new budget", description = "Creates the budget with provided name and email.",
            responses = {
                    @ApiResponse(description = "Budget Created successfully", responseCode = "201",
                            content = @Content(schema = @Schema(implementation = Budget.class))),
                    @ApiResponse(description = "Bad Request", responseCode = "400")
            })
    public ResponseEntity<?> createBudget (@RequestBody Budget budget) {
        try {
            Budget createdBudget = budgetService.createBudget(budget);
            return new ResponseEntity<>(createdBudget, HttpStatus.CREATED);
        } catch (DuplicateBudgetNameException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (InvalidInputException e) {
            // Handling for invalid input exception
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (UserNotFoundException e) {
            // Handling for invalid input exception
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid input: " + e.getMessage());
        }

    }
    /**
     * Endpoint for Updating a budget by its ID.
     *
     * @param id     the ID of the budget to be updated
     * @param budget the details of the budget to update
     * @return a ResponseEntity with the updated budget if the update is successful,
     *         or an error message if the update fails due to duplicate name, invalid input, or user not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing budget", description = "Updates the budget identified by its ID with new values provided in the request body.", responses = {
            @ApiResponse(description = "Budget updated successfully", responseCode = "200",
                    content = @Content(schema = @Schema(implementation = Budget.class))),
            @ApiResponse(description = "Bad Request", responseCode = "400"),
            @ApiResponse(description = "Not Found", responseCode = "404")
    })
    public ResponseEntity<?> updateBudget(@Parameter(name="id", description = "Budget id to update budget", example="1")@PathVariable Long id,
                                          @RequestBody Budget budget) {
        try {
            Budget updatedBudget = budgetService.updateBudget(id, budget);
            return new ResponseEntity<>(updatedBudget, HttpStatus.OK);
        } catch (DuplicateBudgetNameException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (InvalidInputException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
    /**
     * Endpoint for deleting an budget by its ID.
     *
     * @param id the ID of the budget to be deleted
     * @return a ResponseEntity with a success message if the budget is deleted successfully,
     *         or an error message if the budget is not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget", description = "Deletes the budget identified by its ID.", responses = {
            @ApiResponse(description = "Budget deleted successfully", responseCode = "200",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(description = "Not Found", responseCode = "404")
    })
    public ResponseEntity<?> deleteBudget(@Parameter(name="id",description = "Budget id to delete budget", example="1")@PathVariable("id") long id) {
        try {
            budgetService.deleteBudget(id);
            return new ResponseEntity<>("Budget deleted successfully!", HttpStatus.OK);
        } catch (BudgetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid input: " + e.getMessage());
        }
    }

    /**
     * Endpoint to retrieve budget names and amounts by user ID.
     *
     * @param userId The ID of the user whose budget names and amounts are to be retrieved.
     * @return A ResponseEntity containing a list of budget names and amounts associated with the specified user.
     */
    @GetMapping("/user/{userId}/names-and-amounts")
    @Operation(summary = "Get budget names and amounts by user ID", description = "Retrieve the names and amounts of budgets associated with a specific user ID", responses = {
            @ApiResponse(responseCode = "200", description = "Budget names and amounts found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BudgetService.BudgetNameAndAmount.class)))),
            @ApiResponse(responseCode = "404", description = "Budgets not found")
    })
    public ResponseEntity<List<BudgetService.BudgetNameAndAmount>> getBudgetNamesAndAmountsByUserId(@Parameter(name="userId", description = "ID of the user to find budgets for", example = "1")
                                                                                                    @PathVariable Long userId) {
        System.out.println("Fetching budget names and amounts for userId: " + userId); // Log userId
        List<BudgetService.BudgetNameAndAmount> budgetNamesAndAmounts = budgetService.getBudgetNamesAndAmountsByUserId(userId);
        System.out.println("Budget names and amounts: " + budgetNamesAndAmounts); // Log the result
        if (budgetNamesAndAmounts == null || budgetNamesAndAmounts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(budgetNamesAndAmounts);
    }


}
