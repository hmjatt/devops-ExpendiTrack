import React, { useEffect } from 'react';
import { render, act, waitFor, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ExpenseProvider, useExpenseContext } from '../ExpenseContext';
import { useUserContext } from '../UserContext';

import * as ExpenseService from '../../services/ExpenseService';
import { BrowserRouter } from 'react-router-dom';
import enTranslations from '../../translations/en/common.json';
import frTranslations from '../../translations/fr/common.json';
import { I18nextProvider } from 'react-i18next';
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

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
    .use(initReactI18next)
    .init({
        resources,
        lng: 'en',
        interpolation: {
            escapeValue: false,
        },
    });

describe('ExpenseContext Integration Tests', () => {
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
    });

    const FetchExpensesTestComponent = () => {
        const { expenses } = useExpenseContext();
        return (
            <div>
                {expenses.map(expense => (
                    <div key={expense.expensesId}>{expense.expensesDescription} - ${expense.expensesAmount}</div>
                ))}
            </div>
        );
    };

    it('fetches and displays user-specific expenses upon user change', async () => {
        ExpenseService.getUserExpenses.mockResolvedValueOnce(initialExpenses);

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <ExpenseProvider>
                        <FetchExpensesTestComponent />
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

    const AddExpenseTestComponent = ({ newExpense }) => {
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

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <ExpenseProvider>
                        <AddExpenseTestComponent newExpense={newExpense} />
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

    const ErrorTestComponent = () => {
        const { error } = useExpenseContext();
        return <div>{error}</div>;
    };

    it('handles errors when fetching expenses fails', async () => {
        const errorMessage = 'Failed to fetch expenses';
        ExpenseService.getUserExpenses.mockRejectedValueOnce(new Error(errorMessage));

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <ExpenseProvider>
                        <ErrorTestComponent />
                    </ExpenseProvider>
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(screen.getByText(errorMessage)).toBeInTheDocument();
        });
    });

    const UpdateExpenseTestComponent = ({ expenseId, newAmount }) => {
        const { expenses, updateExistingExpense } = useExpenseContext();
        return (
            <>
                {expenses.map(expense => (
                    <div key={expense.expensesId}>{expense.expensesDescription} - ${expense.expensesAmount}</div>
                ))}
                <button onClick={() => updateExistingExpense(expenseId, { expensesAmount: newAmount })}>Update Expense</button>
            </>
        );
    };

    it('updates an existing expense and reflects changes in the context and UI', async () => {
        ExpenseService.getUserExpenses.mockResolvedValueOnce(initialExpenses);
        ExpenseService.updateExpense.mockResolvedValueOnce({});

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <ExpenseProvider>
                        <BrowserRouter>
                            <UpdateExpenseTestComponent expenseId={initialExpenses[0].expensesId} newAmount={200} />
                        </BrowserRouter>
                    </ExpenseProvider>
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(screen.getByText('Groceries - $150')).toBeInTheDocument();
        });

        await act(async () => {
            userEvent.click(screen.getByText('Update Expense'));
        });

        ExpenseService.getUserExpenses.mockResolvedValueOnce([
            { ...initialExpenses[0], expensesAmount: 200 },
            initialExpenses[1],
        ]);

        await waitFor(() => {
            expect(ExpenseService.updateExpense).toHaveBeenCalledWith(initialExpenses[0].expensesId, { expensesAmount: 200 });
            expect(screen.getByText('Groceries - $200')).toBeInTheDocument();
        });
    });

    const DeleteExpenseTestComponent = ({ expenseId }) => {
        const { expenses, removeExpense } = useExpenseContext();
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

    it('deletes an expense and updates the context and UI accordingly', async () => {
        ExpenseService.getUserExpenses.mockResolvedValueOnce(initialExpenses);
        ExpenseService.deleteExpense.mockResolvedValueOnce({});

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <ExpenseProvider>
                        <BrowserRouter>
                            <DeleteExpenseTestComponent expenseId={initialExpenses[0].expensesId} />
                        </BrowserRouter>
                    </ExpenseProvider>
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(screen.getByText('Groceries - $150')).toBeInTheDocument();
            expect(screen.getByText('Utilities - $100')).toBeInTheDocument();
        });

        await act(async () => {
            userEvent.click(screen.getAllByText('Delete Expense')[0]);
        });

        ExpenseService.getUserExpenses.mockResolvedValueOnce([initialExpenses[1]]);

        await waitFor(() => {
            expect(ExpenseService.deleteExpense).toHaveBeenCalledWith(initialExpenses[0].expensesId);
            expect(screen.queryByText('Groceries - $150')).not.toBeInTheDocument();
            expect(screen.getByText('Utilities - $100')).toBeInTheDocument();
        });
    });
});
