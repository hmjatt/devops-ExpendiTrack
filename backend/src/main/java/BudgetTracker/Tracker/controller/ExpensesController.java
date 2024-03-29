package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.entity.Expenses;
import BudgetTracker.Tracker.exceptions.*;
import BudgetTracker.Tracker.service.ExpensesService;
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
 * Controller class for handling HTTP requests related to expenses.
 * Provides endpoints for creating, retrieving, updating, and deleting expenses,
 * as well as retrieving expenses by user ID.
 */
@RestController
@RequestMapping("/expenses")
public class ExpensesController {
    /**
     * Service class for handling business logic related to expenses.
     */
    @Autowired
    ExpensesService expenseService;
    /**
     * Endpoint for creating a new expense.
     *
     * @param expense The expense object to be created. Must be provided in the request body.
     * @return ResponseEntity containing the created expense if successful, or an error message with a
     * bad request status if the expense name is duplicate or if input is invalid.
     */
    @PostMapping
    @Operation(summary = "Create a new expense",
            description = "Create a new expense with provided Expense description, Expense Amount, Budget Id and User Id",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Expense Created successfully",
                            content = @Content(schema = @Schema(implementation = Expenses.class))),
                    @ApiResponse(responseCode = "400", description = "Bad Request due to invalid input or duplicate expense name",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Budget not found for the expense",
                            content = @Content(schema = @Schema(implementation = String.class)))
            })
    public ResponseEntity<?> createExpense(@RequestBody Expenses expense) {
        try {
            Expenses createdExpense = expenseService.createExpense(expense);
            return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
        } catch (DuplicateExpenseNameException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (InvalidInputException | InvalidDAteException | BudgetNotFoundException e) {
            // Handling for invalid input exception
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        }
    }
    /**
     * Endpoint for retrieving all expenses.
     *
     * @return List of all expenses.
     */
    @GetMapping
    @Operation(summary = "Get All expenses", description = "Get All Expenses",
            responses = {
            @ApiResponse(description = "Expenses found", responseCode = "200"),
            @ApiResponse(description = "Expenses not found", responseCode = "200")
    })
    public List<Expenses> getAllExpenses() {

        return expenseService.getAllExpenses();
    }
    /**
     * Endpoint for retrieving an expense by its ID.
     *
     * @param id The ID of the expense to retrieve.
     * @return The expense corresponding to the provided ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Expense by Id", description = "Provide Expense id to get information",responses = {
            @ApiResponse(description = "Expense found", responseCode = "200"),
            @ApiResponse(description = "Expense not found", responseCode = "200")
    })
    public Expenses getExpenseById(@Parameter(name="id", description = "Provide Expense id", example="1")@PathVariable Long id) {

        return expenseService.getExpenseById(id);
    }
    /**
     * Endpoint for Updating an expense by its ID.
     *
     * @param id              The ID of the expense to be updated.
     * @param expenseDetails The details of the expense to be updated.
     * @return A ResponseEntity containing the updated expense if the update was successful,
     *         or an appropriate error response if the expense was not found or if there were
     *         validation errors.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing expense",
            description = "Updates an expense identified by its Id with new details provided in the request body.",
            parameters = {
                    @Parameter(name = "id", description = "Id of the expense to update", required = true, example = "1")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Expense object that needs to be updated in the database",
                    required = true, content = @Content(
                            schema = @Schema(implementation = Expenses.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Expense not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            })
    public ResponseEntity<?> updateExpense(@Parameter(name="id", description = "Provide Expense id to update the expense", example="1")@PathVariable Long id, @RequestBody Expenses expenseDetails) {
        try {
            Expenses updatedExpense = expenseService.updateExpense(id, expenseDetails);
            // Return the updated expense with expensesId field in the response
            return ResponseEntity.ok(updatedExpense);
        } catch (ExpenseNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("An expense with ID = " + id + " is not found");
        } catch (InvalidInputException | BudgetNotFoundException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Endpoint for deleting an expense by its ID.
     *
     * @param id The ID of the expense to be deleted.
     * @return a ResponseEntity with a success message if the expense is deleted successfully,
     *         or an error message if the expense is not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense",
            description = "Deletes an expense identified by its id.",
            parameters = {
                    @Parameter(name = "id", description = "Id of the expense to delete", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Expense deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Expense not found")
            })
    public ResponseEntity<?> deleteExpense(@Parameter(name="id", description = "Provide expense id to delete the expense", example = "1")@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok().body("Expense with ID " + id + " deleted successfully");
        } catch (ExpenseNotFoundException e) {
            String errorMessage = "Expense with ID " + id + " not found";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the expense");
        }
    }

    /**
     * Endpoint for retrieving expenses by user ID.
     *
     * @param userId The ID of the user whose expenses to retrieve.
     * @return List of expenses belonging to the specified user.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get Expenses By User",
            description = "Provide an user Id to find the user Expenses",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully. Returns an empty list if no expenses are found.",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Expenses.class)))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content)
            })
    public List<Expenses> getExpensesByUserId(@Parameter(name="userId", description = "Provide User Id", example = "1")@PathVariable Long userId) {
        return expenseService.getExpensesByUserId(userId);
    }

}
