package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.service.BudgetService;
import BudgetTracker.Tracker.service.ExpensesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private ExpensesService expensesService;

    //created by emily 05/16
    @Autowired
    private BudgetService budgetService;

    /**
     * Endpoint to retrieve expenses grouped by category.
     *
     * @return A ResponseEntity containing a map where the key is the expense category
     * and the value is the total amount for that category.
     */
    @GetMapping("/expenses-by-category")
    public ResponseEntity<Map<String, Integer>> getExpensesByCategory() {
        Map<String, Integer> expensesByCategory = expensesService.getExpensesGroupedByCategory();
        return ResponseEntity.ok(expensesByCategory);
    }

    /**
     * Endpoint to retrieve budgets grouped by category.
     *
     * @return A ResponseEntity containing a map where the key is the budget category
     * and the value is the total amount for that category.
     */
    @GetMapping("/budgets-by-category")
    public ResponseEntity<Map<String, Integer>> getBudgetsByCategory(@RequestParam Long userId) {
        Map<String, Integer> budgetsByCategory = budgetService.getBudgetGroupedByCategory(userId);
        return ResponseEntity.ok(budgetsByCategory);
    }
}
