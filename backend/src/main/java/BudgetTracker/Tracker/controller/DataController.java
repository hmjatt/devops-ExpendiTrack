package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.service.ExpensesService;
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
     * @return A ResponseEntity containing a map where the key is the expense category
     * and the value is the total amount for that category.
     */
    @GetMapping("/expenses-by-category")
    public ResponseEntity<Map<String, Integer>> getExpensesByCategory() {
        Map<String, Integer> expensesByCategory = expensesService.getExpensesGroupedByCategory();
        return ResponseEntity.ok(expensesByCategory);
    }
}
