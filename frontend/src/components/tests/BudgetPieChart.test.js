import '@testing-library/jest-dom/extend-expect';
import React from 'react';
import {
    render,
    act,
    waitFor,
    screen,
} from '@testing-library/react';
import * as BudgetService from '../../services/BudgetService';
import { BudgetProvider } from '../../contexts/BudgetContext';
import { I18nextProvider, initReactI18next } from "react-i18next";
import i18n from "i18next";
import enTranslations from "../../translations/en/common.json";
import frTranslations from "../../translations/fr/common.json";
import BudgetPieChart from '../BudgetPieChart';

jest.mock('../../services/BudgetService');

// Mock the useUserContext
jest.mock('../../contexts/UserContext', () => ({
    useUserContext: () => ({
        user: { id: 1, name: 'Test User' }
    })
}));

// Mock canvas for Chart.js
HTMLCanvasElement.prototype.getContext = jest.fn(() => {
    return {
        fillRect: jest.fn(),
        clearRect: jest.fn(),
        getImageData: jest.fn(() => ({ data: [] })),
        putImageData: jest.fn(),
        createImageData: jest.fn(() => ([])),
        setTransform: jest.fn(),
        drawImage: jest.fn(),
        save: jest.fn(),
        fillText: jest.fn(),
        restore: jest.fn(),
        beginPath: jest.fn(),
        moveTo: jest.fn(),
        lineTo: jest.fn(),
        closePath: jest.fn(),
        stroke: jest.fn(),
        translate: jest.fn(),
        scale: jest.fn(),
        rotate: jest.fn(),
        arc: jest.fn(),
        fill: jest.fn(),
        measureText: jest.fn(() => ({ width: 0 })),
        transform: jest.fn(),
        rect: jest.fn(),
        clip: jest.fn(),
    };
});

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

describe('BudgetPieChart component', () => {
    const TestComponent = () => {
        return (
            <BudgetProvider>
                <BudgetPieChart />
            </BudgetProvider>
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
                    <TestComponent />
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(BudgetService.getBudgetCategoriesForChart).toHaveBeenCalledWith(userId);
        });

        await waitFor(() => {
            expect(screen.getByText('Category1')).toBeInTheDocument();
            expect(screen.getByText('Category2')).toBeInTheDocument();
        });
    });

    it('handles server error when fetching budget categories and amounts', async () => {
        const userId = 1;
        const errorMessage = 'Failed to fetch budget categories for chart.';
        BudgetService.getBudgetCategoriesForChart.mockRejectedValueOnce(new Error(errorMessage));

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <TestComponent />
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(BudgetService.getBudgetCategoriesForChart).toHaveBeenCalledWith(userId);
        });

        await waitFor(() => {
            expect(screen.getByText('No budgets available to display.')).toBeInTheDocument();
        });
    });

    it('handles user not existing when fetching budget categories and amounts', async () => {
        const userId = 999; // Assume this user ID does not exist
        const errorMessage = 'User not found';
        BudgetService.getBudgetCategoriesForChart.mockRejectedValueOnce({ response: { data: errorMessage } });

        await act(async () => {
            render(
                <I18nextProvider i18n={i18n}>
                    <TestComponent />
                </I18nextProvider>
            );
        });

        await waitFor(() => {
            expect(BudgetService.getBudgetCategoriesForChart).toHaveBeenCalledWith(userId);
        });

        await waitFor(() => {
            expect(screen.getByText('No budgets available to display.')).toBeInTheDocument();
        });
    });
});
