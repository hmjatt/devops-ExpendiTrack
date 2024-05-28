
jest.mock('axios');
import axios from 'axios';
import {createBudget, deleteBudget, getBudgetsByUserId, updateBudget, getBudgetCategoriesForChart} from '../BudgetService';

describe('BudgetService', () => {
    // Verify Successful Budget Creation
    it('should create a budget successfully', async () => {
        const budgetData = {
            budgetDescription: "MonthlySavings",
            budgetAmount: 500,
            user: {id: 1}
        };

        const mockResponse = {
            budgetId: 1,
            budgetDescription: "MonthlySavings",
            budgetAmount: 500,
            user: {
                id: 1,
                name: null,
                email: null
            }
        };

        axios.post.mockResolvedValue({response: {status: 201, mockResponse}});

        await expect(createBudget(budgetData)).resolves.toEqual(mockResponse.data);
    });



    // Verify Budget Creation Failure due to Duplicate Budget Name
    it('should handle duplicate budget name error', async () => {
        const errorMessage = 'A budget with the name "ExistingBudget" already exists for this user.';

        axios.post.mockRejectedValue({response: {status: 400, data: errorMessage}});

        await expect(createBudget({
            userId: 1,
            budgetDescription: "ExistingBudget",
            budgetAmount: 300
        })).rejects.toThrow(errorMessage);

    });

    // Verify Budget Creation Failure due to Non-Existent User
    it('should handle non-existent user error', async () => {
        const errorMessage = 'Invalid input: User with ID 999 not found.';
        axios.post.mockRejectedValue({response: {status: 400, data: errorMessage}});

        await expect(createBudget({
            userId: 999,
            budgetDescription: "NewBudget",
            budgetAmount: 200
        })).rejects.toThrow(errorMessage);
    });

    // Verify Budget Creation Failure due to Invalid Budget Amount
    it('should handle invalid budget amount error', async () => {
        const errorMessage = 'Invalid input: Budget amount cannot be negative.';
        axios.post.mockRejectedValue({response: {status: 400, data: errorMessage}});

        await expect(createBudget({
            userId: 1,
            budgetDescription: "InvalidBudget",
            budgetAmount: -100
        })).rejects.toThrow(errorMessage);
    });

    // Verify Successful Budget Retrieval
    it('should fetch user-specific budgets successfully', async () => {
        const mockResponse = [
            {
                budgetId: 1,
                budgetDescription: "MonthlySavings",
                budgetAmount: 500,
                user: {
                    id: 1,
                    name: "Jane",
                    email: "jane@example.com"
                }
            }];
        axios.get.mockResolvedValue({response: {status: 200, mockResponse}});

        await expect(getBudgetsByUserId(1)).resolves.toEqual(mockResponse.data);
    });

    // Verify Budget Retrieval Failure due to Server Error
    it('should handle server error during budget retrieval', async () => {
        axios.get.mockRejectedValue(new Error('Failed to load budgets. Please refresh the page to try again.'));

        await expect(getBudgetsByUserId(1)).rejects.toThrow('Failed to load budgets. Please refresh the page to try again.');
    });

    // Verify Budget Retrieval Failure due to Nonexistent User
    it('should handle user not existing during budget retrieval', async () => {
        // Assuming similar error handling for getBudgetsByUserId as createBudget for consistency
        const errorMessage = '[]';
        axios.get.mockRejectedValue({response: {data: errorMessage}});

        await expect(getBudgetsByUserId(999)).rejects.toThrow(errorMessage);
    });

    // Verify Budget Creation Failure due to Invalid Budget Name
    it('should reject budget creation due to invalid budget name', async () => {
        const errorMessage = 'Invalid input: BudgetDescription must be alphanumeric';
        axios.post.mockRejectedValue({response: {status: 400, data: errorMessage}});

        await expect(createBudget({
            userId: 1,
            budgetDescription: "!@#$%",
            budgetAmount: 200
        })).rejects.toThrow(errorMessage);
    });

    // Verify successful budget update
    it('should update a budget successfully', async () => {
        const budgetId = 1;
        const budgetUpdateData = {
            budgetDescription: "Updated MonthlySavings",
            budgetAmount: 600,
        };
        const mockResponse = {
            budgetId: 1,
            ...budgetUpdateData,
        };

        axios.put.mockResolvedValue({ data: mockResponse });

        await expect(updateBudget(budgetId, budgetUpdateData)).resolves.toEqual(mockResponse);
    });

    // Verify failure on updating a non-existent budget
    it('should handle error when updating a non-existent budget', async () => {
        const budgetId = 999; // Assuming this ID does not exist
        const budgetUpdateData = { budgetDescription: "NonExistent", budgetAmount: 300 };
        const errorMessage = 'Budget not found';

        axios.put.mockRejectedValue({ response: { data: errorMessage } });

        await expect(updateBudget(budgetId, budgetUpdateData)).rejects.toThrow(errorMessage);
    });

    // Verify failure on server error during budget update
    it('should handle server error during budget update', async () => {
        const budgetId = 2;
        const budgetUpdateData = { budgetDescription: "Update Fail", budgetAmount: 400 };
        const errorMessage = 'An error occurred while updating the budget. Please try again later.';

        axios.put.mockRejectedValue(new Error(errorMessage));

        await expect(updateBudget(budgetId, budgetUpdateData)).rejects.toThrow(errorMessage);
    });

    // Verify successful budget deletion
    it('should delete a budget successfully', async () => {
        const budgetId = 1;
        const mockResponse = { message: "Budget deleted successfully" };

        axios.delete.mockResolvedValue({ data: mockResponse });

        await expect(deleteBudget(budgetId)).resolves.toEqual(mockResponse);
    });

    // Verify failure on deleting a non-existent budget
    it('should handle error when deleting a non-existent budget', async () => {
        const budgetId = 999; // Assuming this ID does not exist
        const errorMessage = 'Budget not found';

        axios.delete.mockRejectedValue({ response: { data: errorMessage } });

        await expect(deleteBudget(budgetId)).rejects.toThrow(errorMessage);
    });

    // Verify failure on server error during budget deletion
    it('should handle server error during budget deletion', async () => {
        const budgetId = 2;
        const errorMessage = 'An error occurred while deleting the budget. Please try again later.';

        axios.delete.mockRejectedValue(new Error(errorMessage));

        await expect(deleteBudget(budgetId)).rejects.toThrow(errorMessage);
    });

    it('should handle duplicate budget name error on update', async () => {
        const budgetId = 2; // Assuming this ID exists
        const budgetUpdateData = {
            budgetDescription: "ExistingBudgetName",
            budgetAmount: 300
        };
        const errorMessage = 'A budget with the name "ExistingBudgetName" already exists for this user.';

        axios.put.mockRejectedValue({ response: { data: errorMessage } });

        await expect(updateBudget(budgetId, budgetUpdateData)).rejects.toThrow(errorMessage);
    });

    it('should handle non-existent user error on budget update', async () => {
        const budgetId = 3; // Assuming this ID exists
        const errorMessage = 'User with ID 999 not found.';
        const budgetUpdateData = {
            budgetDescription: "SomeBudget",
            budgetAmount: 500,
            user: { id: 999 } // Non-existent user ID
        };

        axios.put.mockRejectedValue({ response: { data: errorMessage } });

        await expect(updateBudget(budgetId, budgetUpdateData)).rejects.toThrow(errorMessage);
    });

    it('should prevent budget deletion when linked expenses exist', async () => {
        const budgetId = 4; // Assuming this ID exists and has linked expenses
        const errorMessage = 'Budget cannot be deleted as it has linked expenses.';

        axios.delete.mockRejectedValue({ response: { data: errorMessage } });

        await expect(deleteBudget(budgetId)).rejects.toThrow(errorMessage);
    });
    // New test added: Validate successful retrieval of budget category and amount data.
    it('should fetch budget categories and amounts successfully', async () => {
        const userId = 1;
        const mockResponse = [
            { name: 'Category1', amount: 100 },
            { name: 'Category2', amount: 200 }
        ];

        axios.get.mockResolvedValue({ data: mockResponse });

        await expect(getBudgetCategoriesForChart(userId)).resolves.toEqual(mockResponse);
    });

    // New test added: Validate handling of server errors when retrieving budget category and amount data.
    it('should handle server error during fetching budget categories and amounts', async () => {
        const userId = 1;
        const errorMessage = 'Failed to fetch budget categories for chart.';

        axios.get.mockRejectedValue(new Error(errorMessage));

        await expect(getBudgetCategoriesForChart(userId)).rejects.toThrow(errorMessage);
    });

    // New test added: Validate retrieval of budget category and amount data when user does not exist.
    it('should handle user not existing during fetching budget categories and amounts', async () => {
        const userId = 999; // 假设该用户ID不存在
        const errorMessage = 'User not found';

        axios.get.mockRejectedValue({ response: { data: errorMessage } });

        await expect(getBudgetCategoriesForChart(userId)).rejects.toThrow('User not found');
    });
});



