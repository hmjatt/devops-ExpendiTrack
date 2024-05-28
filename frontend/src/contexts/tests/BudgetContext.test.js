import React, {useEffect} from 'react';
import {
    render,
    act,
    waitFor,
    screen, getByText,
} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BudgetProvider, useBudgetContext } from '../BudgetContext';
import * as BudgetService from '../../services/BudgetService';
import BudgetList from "../../components/BudgetList";
import {BrowserRouter} from "react-router-dom";
import enTranslations from "../../translations/en/common.json";
import frTranslations from "../../translations/fr/common.json";
import i18n from "i18next";
import {I18nextProvider, initReactI18next} from "react-i18next";
import i18next from "i18next";

jest.mock('../../contexts/UserContext', () => ({
    useUserContext: () => ({ user: { id: 1, name: 'Test User' } }),
}));

jest.mock('../../services/BudgetService');

jest.mock('../../contexts/ExpenseContext', () => ({
    useExpenseContext: () => ({
        expenses: [], // Provide a default value for expenses
    }),
}));

const resources = {
    en: {
        translation: enTranslations,
    },
    fr: {
        translation: frTranslations,
    },
};

i18n
    .use(initReactI18next) // passes i18n down to react-i18next
    .init({
        resources,
        lng: 'en',
        interpolation: {
            escapeValue: false, // react already safes from xss
        },
    });

describe('BudgetContext Integration Tests', () => {

    let originalConsoleError;

    const user = { id: 1, name: 'Test User' };
    const initialBudgets = [
        { id: 1, budgetDescription: 'Groceries', budgetAmount: 300 },
        { id: 2, budgetDescription: 'Utilities', budgetAmount: 150 },

    ];
    const newBudget = { id: 3, budgetDescription: 'Entertainment', budgetAmount: 250 };

    beforeEach(() => {
        // Mock the service calls with default implementations
        BudgetService.getBudgetsByUserId.mockResolvedValue(initialBudgets);
        BudgetService.createBudget.mockImplementation((newBudget) =>
            Promise.resolve({ ...newBudget, id: Math.random() }) // Simulate adding an ID to the new budget
        );
        BudgetService.updateBudget.mockImplementation((id, updatedBudget) =>
            Promise.resolve({ ...updatedBudget, id }) // Return the updated budget with its ID
        );
        BudgetService.deleteBudget.mockResolvedValue({}); // Simulate successful deletion
    });

    afterEach(() => {
        // Reset mocks after each test
        jest.resetAllMocks();
    });

    it('fetches and updates budgets when the user changes', async () => {
        BudgetService.getBudgetsByUserId.mockResolvedValueOnce(initialBudgets);

        const TestComponent = () => {
            const { budgets } = useBudgetContext();
            return (
                <>
                    {budgets.map((budget) => (
                        <div key={budget.id}>{budget.budgetDescription}</div>
                    ))}
                </>
            );
        };

        await act(async () => {
            render(
                <I18nextProvider i18n={i18next}>
                    <BudgetProvider>
                        <TestComponent />
                    </BudgetProvider>
                </I18nextProvider>
            );
        });

        expect(BudgetService.getBudgetsByUserId).toHaveBeenCalledWith(user.id);
    });

    it('adds a new budget and updates the context', async () => {
        BudgetService.getBudgetsByUserId.mockResolvedValueOnce({ data: initialBudgets });
        BudgetService.createBudget.mockResolvedValueOnce(newBudget);

        let renderResult;
        const TestComponent = () => {
            const { addNewBudget, budgets } = useBudgetContext();
            return (
                <div>
                    {budgets.map((budget) => (
                        <div key={budget.id}>{budget.budgetDescription}</div>
                    ))}
                    <button onClick={() => addNewBudget({ budgetDescription: 'Entertainment', budgetAmount: 250 })}>Add Budget</button>
                </div>
            );
        };

        await act(async () => {
            renderResult = render(
                <I18nextProvider i18n={i18next}>
                    <BudgetProvider>
                        <TestComponent />
                    </BudgetProvider>
                </I18nextProvider>
            );
        });

        // Simulate adding a new budget
        await act(async () => {
            userEvent.click(renderResult.getByText('Add Budget'));
        });

        expect(BudgetService.createBudget).toHaveBeenCalledWith({
            budgetDescription: 'Entertainment',
            budgetAmount: 250,
            userId: user.id, // Adjusted to expect userId directly instead of a user object
        });

    });

    it('handles errors when fetching budgets fails', async () => {
        const errorMessage = 'Failed to fetch budgets';
        BudgetService.getBudgetsByUserId.mockRejectedValueOnce(new Error(errorMessage));

        let testError;
        const TestComponent = () => {
            const { error } = useBudgetContext();
            testError = error;
            return null;
        };

        await act(async () => {
            render(
                <I18nextProvider i18n={i18next}>
                    <BudgetProvider>
                        <TestComponent />
                    </BudgetProvider>
                </I18nextProvider>
            );
        });

        // Wait for the error state to be updated after the rejection
        await waitFor(() => {
            expect(testError).toEqual(errorMessage);
        });
    })



    it('updates an existing budget and reflects changes in the context and UI', async () => {
        // Setup initial budgets and the updated budget
        const initialBudgets = [
            { budgetId: 1, budgetDescription: 'Groceries', budgetAmount: 300 },
            { budgetId: 2, budgetDescription: 'Utilities', budgetAmount: 150 },
        ];
        const updatedBudget = { budgetDescription: 'Updated Groceries', budgetAmount: 350 };

        // Mock the API calls
        BudgetService.getBudgetsByUserId.mockResolvedValue(initialBudgets);
        BudgetService.updateBudget.mockResolvedValue({ ...initialBudgets[0], ...updatedBudget });

        // Define the test component
        const TestComponent = () => {
            const { budgets, updateExistingBudget, fetchBudgets } = useBudgetContext();
            useEffect(() => {
                fetchBudgets(); // Trigger fetching budgets on component mount
            }, [fetchBudgets]);

            return (
                <>
                    {budgets.map((budget) => (
                        <div key={budget.budgetId} data-testid={`budget-${budget.budgetId}`}>
                            {budget.budgetDescription} - ${budget.budgetAmount}
                        </div>
                    ))}
                    <button
                        onClick={() => updateExistingBudget(initialBudgets[0].budgetId, updatedBudget)}
                    >
                        Update Budget
                    </button>
                </>
            );
        };

        // Render the component within the BudgetProvider
        await act(async () => {
            render(
                <I18nextProvider i18n={i18next}>
                    <BrowserRouter>
                        <BudgetProvider>
                            <TestComponent />
                        </BudgetProvider>
                    </BrowserRouter>
                </I18nextProvider>
            );
        });

        // Simulate clicking the "Update Budget" button
        await act(async () => {
            userEvent.click(screen.getByText('Update Budget'));
        });

        // Assertions
        await waitFor(() => {
            expect(BudgetService.updateBudget).toHaveBeenCalledWith(initialBudgets[0].budgetId, expect.objectContaining(updatedBudget));
        });

        // Verify the updated budget is displayed in the UI
        await waitFor(() => {
            expect(screen.getByTestId(`budget-${initialBudgets[0].budgetId}`).textContent).toContain('Updated Groceries - $350');
        });
    });

    it('deletes an existing budget and updates the context', async () => {
        const initialBudgets = [
            { id: 1, budgetDescription: 'Groceries', budgetAmount: 300 },
            { id: 2, budgetDescription: 'Utilities', budgetAmount: 150 },
        ];
        BudgetService.getBudgetsByUserId.mockResolvedValue(initialBudgets);

        const budgetIdToDelete = 2;

        const TestComponent = () => {
            const { removeBudget, budgets } = useBudgetContext();
            return (
                <>
                    {budgets.map((budget) => (
                        <div key={budget.id}>{budget.budgetDescription}</div>
                    ))}
                    <button onClick={() => removeBudget(budgetIdToDelete)}>Delete Budget</button>
                </>
            );
        };

        await act(async () => {
            render(
                <I18nextProvider i18n={i18next}>
                    <BudgetProvider>
                        <TestComponent />
                    </BudgetProvider>
                </I18nextProvider>
            );
        });

        // Simulate deleting a budget
        await act(async () => {
            userEvent.click(screen.getByText('Delete Budget'));
        });

        expect(BudgetService.deleteBudget).toHaveBeenCalledWith(budgetIdToDelete);

        await waitFor(() => {
            const deletedBudgetText = screen.queryByText('Utilities: 150');
            expect(deletedBudgetText).not.toBeInTheDocument();
        });
    });
});