import React, { useEffect } from 'react';
import { render, act, waitFor, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ExpenseProvider, useExpenseContext } from '../ExpenseContext';
import { useUserContext } from '../UserContext';
import * as ExpenseService from '../../services/ExpenseService';
import { BrowserRouter } from 'react-router-dom';
import i18n from 'i18next';
import { I18nextProvider, initReactI18next } from 'react-i18next';
import enTranslations from '../../translations/en/common.json';
import frTranslations from '../../translations/fr/common.json';

jest.mock('../UserContext');
jest.mock('../../services/ExpenseService');

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

describe('ExpenseContext Integration Tests', () => {
    let originalConsoleError;

    const mockUser = { id: 1, name: 'John Doe' };
    const initialExpenses = [
        { expensesId: 1, expensesDescription: 'Groceries', expensesAmount: 150, expensesDate: '2024-02-14', budget: { budgetId: 1 } },
        { expensesId: 2, expensesDescription: 'Utilities', expensesAmount: 100, expensesDate: '2024-02-15', budget: { budgetId: 2 } },
    ];

    beforeEach(() => {
        useUserContext.mockReturnValue({ user: mockUser });
        ExpenseService.getUserExpenses.mockReset();
        ExpenseService.createExpense.mockReset();
        ExpenseService.updateExpense.mockReset();
        ExpenseService.deleteExpense.mockReset();
        originalConsoleError = console.error;
        console.error = jest.fn(); // Mock console.error
    });

    afterEach(() => {
        console.error = originalConsoleError; // Restore original console.error
    });

    it('fetches and displays user-specific expenses upon user change', async () => {
        ExpenseService.getUserExpenses.mockResolvedValueOnce(initialExpenses);

        const TestComponent = () => {
            const { expenses } = useExpenseContext();
            return (
                <div>
                    {expenses.map(expense => (
                        <div key={expense.expensesId}>{expense.expensesDescription} - ${expense.expensesAmount}</div>
                    ))}
                </div>
            );
        };

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <ExpenseProvider>
                        <TestComponent />
                    </ExpenseProvider>
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(ExpenseService.getUserExpenses).toHaveBeenCalledWith(mockUser.id);
            expect(screen.getByText('Groceries - $150')).toBeInTheDocument();
            expect(screen.getByText('Utilities - $100')).toBeInTheDocument();
        });
    });

    it('adds a new expense and updates context accordingly', async () => {
        const newExpense = {
            expensesId: 3,
            expensesDescription: 'Entertainment',
            expensesAmount: 250,
            expensesDate: '2024-02-20',
            budget: { budgetId: 3 },
            userId: mockUser.id,
        };

        ExpenseService.createExpense.mockResolvedValueOnce(newExpense);

        const TestComponent = () => {
            const { addNewExpense, expenses } = useExpenseContext();
            return (
                <>
                    <button onClick={() => addNewExpense(newExpense)}>Add Expense</button>
                    {expenses.map(expense => (
                        <div key={expense.expensesId}>{expense.expensesDescription} - ${expense.expensesAmount}</div>
                    ))}
                </>
            );
        };

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <ExpenseProvider>
                        <TestComponent />
                    </ExpenseProvider>
                </I18nextProvider>
            );
        });

        await act(async () => {
            userEvent.click(screen.getByText('Add Expense'));
        });

        await waitFor(() => {
            expect(ExpenseService.createExpense).toHaveBeenCalledWith(expect.objectContaining(newExpense));
            expect(screen.getByText('Entertainment - $250')).toBeInTheDocument();
        });
    });

    it('handles errors when fetching expenses fails', async () => {
        const errorMessage = 'Failed to fetch expenses';
        ExpenseService.getUserExpenses.mockRejectedValueOnce(new Error(errorMessage));

        const TestComponent = () => {
            const { error } = useExpenseContext();
            return <div>{error}</div>;
        };

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <ExpenseProvider>
                        <TestComponent />
                    </ExpenseProvider>
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(screen.getByText(errorMessage)).toBeInTheDocument();
        });
    });

    it('updates an existing expense and reflects changes in the context and UI', async () => {
        ExpenseService.getUserExpenses.mockResolvedValueOnce(initialExpenses);

        const updatedExpense = { ...initialExpenses[0], expensesAmount: 200 };
        ExpenseService.updateExpense.mockResolvedValueOnce(updatedExpense);

        const TestComponent = () => {
            const { expenses, updateExistingExpense } = useExpenseContext();
            return (
                <>
                    {expenses.map(expense => (
                        <div key={expense.expensesId}>{expense.expensesDescription} - ${expense.expensesAmount}</div>
                    ))}
                    <button onClick={() => updateExistingExpense(initialExpenses[0].expensesId, { expensesAmount: 200 })}>Update Expense</button>
                </>
            );
        };

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <ExpenseProvider>
                        <BrowserRouter>
                            <TestComponent />
                        </BrowserRouter>
                    </ExpenseProvider>
                </I18nextProvider>
            );
        });

        expect(screen.getByText('Groceries - $150')).toBeInTheDocument();

        await act(async () => {
            userEvent.click(screen.getByText('Update Expense'));
        });

        await waitFor(() => {
            expect(ExpenseService.updateExpense).toHaveBeenCalledWith(initialExpenses[0].expensesId, { expensesAmount: 200 });
            expect(screen.queryByText('Groceries - $150')).not.toBeInTheDocument();
            expect(screen.getByText('Groceries - $200')).toBeInTheDocument();
        });
    });

    it('deletes an expense and updates the context and UI accordingly', async () => {
        ExpenseService.getUserExpenses.mockResolvedValueOnce(initialExpenses);
        ExpenseService.deleteExpense.mockResolvedValueOnce({});

        const TestComponent = () => {
            const { expenses, removeExpense, fetchExpenses } = useExpenseContext();
            useEffect(() => {
                fetchExpenses(mockUser.id);
            }, [fetchExpenses, mockUser.id]);

            return (
                <>
                    {expenses.map(expense => (
                        <div key={expense.expensesId}>
                            {expense.expensesDescription} - ${expense.expensesAmount}
                            <button onClick={() => removeExpense(expense.expensesId)}>Delete Expense</button>
                        </div>
                    ))}
                </>
            );
        };

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <ExpenseProvider>
                        <BrowserRouter>
                            <TestComponent />
                        </BrowserRouter>
                    </ExpenseProvider>
                </I18nextProvider>
            );
        });

        expect(screen.getByText('Groceries - $150')).toBeInTheDocument();
        expect(screen.getByText('Utilities - $100')).toBeInTheDocument();

        await act(async () => {
            userEvent.click(screen.getAllByText('Delete Expense')[0]);
        });

        await waitFor(() => {
            expect(ExpenseService.deleteExpense).toHaveBeenCalledWith(initialExpenses[0].expensesId);
            expect(screen.queryByText('Groceries - $150')).not.toBeInTheDocument();
            expect(screen.getByText('Utilities - $100')).toBeInTheDocument();
        });
    });

});

