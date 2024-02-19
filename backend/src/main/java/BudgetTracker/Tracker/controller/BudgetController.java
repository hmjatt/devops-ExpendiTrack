package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.entity.Budget;
import BudgetTracker.Tracker.entity.User;
import BudgetTracker.Tracker.exceptions.*;
import BudgetTracker.Tracker.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Find budgets by user id", responses = {
            @ApiResponse(description = "budgets found", responseCode = "200"),
            @ApiResponse(description = "budgets not found", responseCode = "200")
    })
    public ResponseEntity<List<Budget>> getBudgetsByUserId(@PathVariable Long userId) {
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
    @Operation(summary = "Create a new budget", responses = {
            @ApiResponse(description = "Budget Created successfully", responseCode = "201",
                    content = @Content(schema = @Schema(implementation = Budget.class))),
            @ApiResponse(description = "Bad Request", responseCode = "400")
    })
    public ResponseEntity<?> createBudget(@RequestBody Budget budget) {
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

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing budget", description = "Updates the budget identified by its ID with new values provided in the request body.", responses = {
            @ApiResponse(description = "Budget updated successfully", responseCode = "200",
                    content = @Content(schema = @Schema(implementation = Budget.class))),
            @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    public ResponseEntity<?> updateBudget(@PathVariable Long id, @RequestBody Budget budget) {
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget", description = "Deletes the budget identified by its ID.", responses = {
            @ApiResponse(description = "Budget deleted successfully", responseCode = "200",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    public ResponseEntity<?> deleteBudget(@PathVariable("id") long id) {
        try {
            budgetService.deleteBudget(id);
            return new ResponseEntity<>("Budget deleted successfully!", HttpStatus.OK);
        } catch (BudgetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid input: " + e.getMessage());
        }
    }

}
