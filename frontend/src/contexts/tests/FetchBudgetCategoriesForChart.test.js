import React, { useEffect, useContext } from 'react';
import {
    act,
    render,
    waitFor,
    screen,
} from '@testing-library/react';
import * as BudgetService from '../../services/BudgetService';
import { BudgetProvider, BudgetContext } from '../BudgetContext';
import { I18nextProvider, initReactI18next } from "react-i18next";
import i18n from "i18next";
import enTranslations from "../../translations/en/common.json";
import frTranslations from "../../translations/fr/common.json";

jest.mock('../../services/BudgetService');

// Mock the useUserContext
jest.mock('../UserContext', () => ({
    useUserContext: () => ({
        user: { id: 1, name: 'Test User' }
    })
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
    .use(initReactI18next)
    .init({
        resources,
        lng: 'en',
        interpolation: {
            escapeValue: false,
        },
    });

describe('fetchBudgetCategoriesForChart method', () => {
    const TestComponent = ({ userId }) => {
        const { chartData, fetchBudgetCategoriesForChart } = useContext(BudgetContext);
        useEffect(() => {
            fetchBudgetCategoriesForChart(userId);
        }, [fetchBudgetCategoriesForChart, userId]);

        return (
            <div>
                {chartData && chartData.length === 0 && <span data-testid="no-data">No data</span>}
                {chartData && chartData.map((category, index) => (
                    <div key={index} data-testid={`category-${index}`}>
                        {category.name}: ${category.amount}
                    </div>
                ))}
            </div>
        );
    };

    it('fetches budget categories and amounts successfully', async () => {
        const userId = 1;
        const mockResponse = [
            { name: 'Category1', amount: 100 },
            { name: 'Category2', amount: 200 },
        ];
        BudgetService.getBudgetCategoriesForChart.mockResolvedValueOnce(mockResponse);

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <BudgetProvider>
                        <TestComponent userId={userId} />
                    </BudgetProvider>
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(BudgetService.getBudgetCategoriesForChart).toHaveBeenCalledWith(userId);
        });

        // 检查是否渲染了正确的内容
        screen.debug();

        await waitFor(() => {
            const category0 = screen.getByTestId('category-0');
            const category1 = screen.getByTestId('category-1');
            expect(category0).toBeInTheDocument();
            expect(category1).toBeInTheDocument();
            expect(category0.textContent).toBe('Category1: $100');
            expect(category1.textContent).toBe('Category2: $200');
        });
    });

    it('handles server error when fetching budget categories and amounts', async () => {
        const userId = 1;
        const errorMessage = 'Failed to fetch budget categories for chart.';
        BudgetService.getBudgetCategoriesForChart.mockRejectedValueOnce(new Error(errorMessage));

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <BudgetProvider>
                        <TestComponent userId={userId} />
                    </BudgetProvider>
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(BudgetService.getBudgetCategoriesForChart).toHaveBeenCalledWith(userId);
        });

        await waitFor(() => {
            expect(screen.getByTestId('no-data').textContent).toBe('No data');
        });
    });

    it('handles user not existing when fetching budget categories and amounts', async () => {
        const userId = 999; // Assume this user ID does not exist
        const errorMessage = 'User not found';
        BudgetService.getBudgetCategoriesForChart.mockRejectedValueOnce({ response: { data: errorMessage } });

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <BudgetProvider>
                        <TestComponent userId={userId} />
                    </BudgetProvider>
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(BudgetService.getBudgetCategoriesForChart).toHaveBeenCalledWith(userId);
        });

        await waitFor(() => {
            expect(screen.getByTestId('no-data').textContent).toBe('No data');
        });
    });
});
