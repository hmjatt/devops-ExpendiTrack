import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter as Router } from 'react-router-dom';
import { UserContext } from '../../contexts/UserContext';
import { BudgetContext } from '../../contexts/BudgetContext';
import { ExpenseContext } from '../../contexts/ExpenseContext';
import Dashboard from '..//Dashboard';
import axios from 'axios';
import { I18nextProvider, initReactI18next } from "react-i18next";
import i18next from "i18next";

import enTranslations from "../../translations/en/common.json";
import frTranslations from "../../translations/fr/common.json";

// Mock axios for all tests in this file
jest.mock('axios');

const resources = {
    en: {
        translation: enTranslations,
    },
    fr: {
        translation: frTranslations,
    },
};

i18next
    .use(initReactI18next) // passes i18n down to react-i18next
    .init({
        resources,
        lng: 'en',
        interpolation: {
            escapeValue: false, // react already safes from xss
        },
    });

describe('Dashboard Component', () => {
    const mockUser = {
        id: 1,
        name: 'Harmeet',
        email: 'Harmeet@gmail.com'
    };

    const mockBudget = {
        budgetId: 1,
        budgetDescription: 'Groceries',
        budgetAmount: 500,
    };

    const mockExpense = {
        expensesId: 2,
        expensesDescription: "Coffee",
        expensesAmount: 5,
        expensesDate: "2024-02-06T10:00:00Z",
        budget: mockBudget
    };

    beforeEach(() => {
        // Setup initial context values and mock API responses
        axios.get.mockResolvedValueOnce({ data: [mockExpense] }); // Mock fetching expenses
    });

    it('renders user budgets data correctly', async () => {
        render(
            <Router>
                <I18nextProvider i18n={i18next}>
                    <UserContext.Provider value={{ user: mockUser }}>
                        <BudgetContext.Provider value={{ budgets: [mockBudget], fetchBudgets: jest.fn() }}>
                            <ExpenseContext.Provider value={{ expenses: [mockExpense], fetchExpenses: jest.fn() }}>
                                <Dashboard />
                            </ExpenseContext.Provider>
                        </BudgetContext.Provider>
                    </UserContext.Provider>
                </I18nextProvider>
            </Router>
        );

        // Wait for the component to receive the mocked response and update the UI
        await waitFor(() => {
            expect(screen.getByText("Budget Name: Groceries")).toBeInTheDocument();
            expect(screen.getByText("Budget Amount: $500.00")).toBeInTheDocument();
        });

        // Validate if the user's name is rendered
        expect(screen.getByText(`Welcome, Harmeet!`)).toBeInTheDocument();

        // Check if the BarChart component is rendered
        await waitFor(() => {
            expect(screen.getByText('Expenses by Category')).toBeInTheDocument();
        });
    });
});
