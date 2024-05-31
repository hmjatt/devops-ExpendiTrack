package BudgetTracker.Tracker.controller;

import BudgetTracker.Tracker.entity.Budget;
import BudgetTracker.Tracker.entity.User;
import BudgetTracker.Tracker.exceptions.*;
import BudgetTracker.Tracker.service.BudgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BudgetController.class)
class BudgetControllerTest {

    @MockBean // Use @MockBean instead of @Mock for Spring Boot's context
    private BudgetService budgetService;

    @Autowired
    private MockMvc mockMvc;

    private User user;
    private Budget budget;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setName("Seif");
        user.setEmail("Seif@hotmail.com");

        budget = new Budget();
        budget.setBudgetDescription("Holiday");
        budget.setBudgetAmount(1000);
        budget.setUser(user);
    }

    @Test
    void createBudget_Success() throws Exception {
        when(budgetService.createBudget(any(Budget.class))).thenReturn(budget);

        mockMvc.perform(post("/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(budget)))
                .andExpect(status().isCreated());

        // Verify that the createBudget method was called once
        verify(budgetService).createBudget(any(Budget.class));
    }

    @Test
    void createBudget_ThrowsDuplicateBudgetNameException() throws Exception {
        doThrow(new DuplicateBudgetNameException("Budget with name 'Holiday' already exists")).when(budgetService).createBudget(any(Budget.class));

        mockMvc.perform(post("/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(budget)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Budget with name 'Holiday' already exists")));

        // Verify that the createBudget method was called once
        verify(budgetService).createBudget(any(Budget.class));

    }

    @Test
    void createBudgetThrowsInvalidInputException() throws Exception {
        given(budgetService.createBudget(any(Budget.class))).willThrow(new InvalidInputException("Invalid input"));
        mockMvc.perform(post("/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"budgetDescription\":\"Holiday\",\"budgetAmount\":\"-500\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input")));
    }

    @Test
    void createBudgetThrowsUserNotFoundException() throws Exception {
        given(budgetService.createBudget(any(Budget.class))).willThrow(new UserNotFoundException("User not found"));
        mockMvc.perform(post("/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(budget)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }

    @Test
    void updateBudgetSuccess() throws Exception {
        Long budgetId = 1L;
        when(budgetService.updateBudget(eq(budgetId), any(Budget.class))).thenReturn(budget);

        mockMvc.perform(put("/budgets/{id}", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(budget)))
                .andExpect(status().isOk());

        verify(budgetService).updateBudget(eq(budgetId), any(Budget.class));
    }

    @Test
    void deleteBudgetSuccess () throws Exception {
        Long budgetId = 1L;
        willDoNothing().given(budgetService).deleteBudget(budgetId);
        ResultActions response = mockMvc.perform(delete("/budgets/{id}", budgetId));
        response.andExpect(status().isOk()).andDo(print());
    }

    @Test
    void updateBudgetThrowsDuplicateBudgetNameException() throws Exception {
        Long budgetId = 1L; // Example ID
        doThrow(new DuplicateBudgetNameException("Budget with name 'Holiday' already exists"))
                .when(budgetService).updateBudget(eq(budgetId), any(Budget.class));

        mockMvc.perform(put("/budgets/{id}", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(budget)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Budget with name 'Holiday' already exists")));

        verify(budgetService).updateBudget(eq(budgetId), any(Budget.class));
    }

    @Test
    void updateBudgetAmountThrowsInvalidInputException() throws Exception {
        Long budgetId = 1L;
        given(budgetService.updateBudget(eq(budgetId), any(Budget.class))).willThrow(new InvalidInputException("Invalid input"));

        mockMvc.perform(put("/budgets/{id}", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"budgetDescription\":\"Holiday\",\"budgetAmount\":\"-500\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input")));
    }

    @Test
    void updateBudgetDescriptionThrowsInvalidInputException() throws Exception {
        Long budgetId = 1L;
        given(budgetService.updateBudget(eq(budgetId), any(Budget.class))).willThrow(new InvalidInputException("Invalid description"));

        mockMvc.perform(put("/budgets/{id}", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"budgetDescription\":\"\",\"budgetAmount\":\"500\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid description")));
    }

    @Test
    void updateBudgetThrowsUserNotFoundException() throws Exception {
        Long budgetId = 1L;
        given(budgetService.updateBudget(eq(budgetId), any(Budget.class))).willThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(put("/budgets/{id}", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(budget)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }

    @Test
    void deleteBudgetThrowsInvalidIdException() throws Exception {
        Long invalidBudgetId = 999L;
        doThrow(new BudgetNotFoundException("Budget with ID " + invalidBudgetId + " not found."))
                .when(budgetService).deleteBudget(invalidBudgetId);
        mockMvc.perform(delete("/budgets/{id}", invalidBudgetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBudgetNamesAndAmountsByUserId_Success() throws Exception {
        Long userId = 1L;
        BudgetService.BudgetNameAndAmount budget1 = new BudgetService.BudgetNameAndAmount("Holiday", 1000);
        BudgetService.BudgetNameAndAmount budget2 = new BudgetService.BudgetNameAndAmount("Savings", 2000);

        List<BudgetService.BudgetNameAndAmount> budgetList = Arrays.asList(budget1, budget2);

        when(budgetService.getBudgetNamesAndAmountsByUserId(userId)).thenReturn(budgetList);

        mockMvc.perform(get("/budgets/user/{userId}/names-and-amounts", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[{'name':'Holiday','amount':1000},{'name':'Savings','amount':2000}]"));
    }

    @Test
    void getBudgetNamesAndAmountsByUserId_NotFound() throws Exception {
        Long userId = 999L;

        when(budgetService.getBudgetNamesAndAmountsByUserId(userId)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/budgets/user/{userId}/names-and-amounts", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }

 }
