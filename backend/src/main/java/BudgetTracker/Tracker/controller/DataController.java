package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.service.ExpensesService;
import BudgetTracker.Tracker.service.BudgetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private ExpensesService expensesService;

    /**
     * Endpoint to retrieve expenses grouped by category.
     *
     * @return A ResponseEntity containing a map where the key is the budget budget
     * and the value is the total amount of expenses for that budget.
     */

    @GetMapping("/totalexpenses-by-budget")
    public ResponseEntity<Map<String, Integer>> getTotalExpensesByBudget() {
        Map<String, Integer> expensesByBudget = expensesService.getExpensesGroupedByBudget();
        return ResponseEntity.ok(expensesByBudget);
    }
}
