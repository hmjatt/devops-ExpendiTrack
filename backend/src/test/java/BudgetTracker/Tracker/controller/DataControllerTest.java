package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.entity.*;
import BudgetTracker.Tracker.service.ExpensesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;



import java.util.*;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.put;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(DataController.class)
public class DataControllerTest{

    @MockBean
    private ExpensesService expensesService;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private ExpensesController expensesController;

    private User user;
    private Budget budget1;
    private Budget budget2;
    private Expenses expense1;
    private Expenses expense2;
    private Expenses expense3;
    private Expenses expense4;

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);

        // Initialize user
        user = new User();
        user.setId(1L);
        user.setName("Abdalla");
        user.setEmail("A@hotmail.com");

        // Initialize budget
        budget1 = new Budget();
        budget1.setBudgetId(1L);
        budget1.setBudgetDescription("Vacation");
        budget1.setBudgetAmount(1000);
        budget1.setUser(user);

        budget2 = new Budget();
        budget2.setBudgetId(2L);
        budget2.setBudgetDescription("Leasure");
        budget2.setBudgetAmount(500);
        budget2.setUser(user);

        // Initialize expenses
        expense1 = new Expenses();
        expense1.setExpensesId(1L);
        expense1.setExpensesDescription("Groceries");
        expense1.setExpensesAmount(50);
        expense1.setExpensesDate(Instant.now());
        expense1.setBudget(budget1);

        expense2 = new Expenses();
        expense2.setExpensesId(2L);
        expense2.setExpensesDescription("Utilities");
        expense2.setExpensesAmount(100);
        expense2.setExpensesDate(Instant.now());
        expense2.setBudget(budget1);

        expense3 = new Expenses();
        expense3.setExpensesId(3L);
        expense3.setExpensesDescription("Food");
        expense3.setExpensesAmount(300);
        expense3.setExpensesDate(Instant.now());
        expense3.setBudget(budget2);

        expense4 = new Expenses();
        expense4.setExpensesId(4L);
        expense4.setExpensesDescription("Movies");
        expense4.setExpensesAmount(100);
        expense4.setExpensesDate(Instant.now());
        expense4.setBudget(budget2);
    }

    @Test
    @DisplayName("Should return total expenses grouped by budget")
    void getTotalExpensesByBudgetTest() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();
        expensesByBudget.put("Vacation", 1000);
        expensesByBudget.put("Groceries", 200);

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"Vacation\":1000,\"Groceries\":200}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }

    @Test
    @DisplayName("Should handle empty expense grouping")
    void getTotalExpensesByBudget_Empty() throws Exception {
        Map<String, Integer> expensesByBudget = new HashMap<>();

        when(expensesService.getExpensesGroupedByBudget()).thenReturn(expensesByBudget);

        mockMvc.perform(get("/data/totalexpenses-by-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));

        verify(expensesService).getExpensesGroupedByBudget();
    }
}

