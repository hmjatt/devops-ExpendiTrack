import React from 'react';
import { render, waitFor, act } from '@testing-library/react';
import BarChart from '../BarChart';
import { getExpensesByCategory } from '../../services/ChartService';
import { useExpenseContext } from '../../contexts/ExpenseContext';
import { useUserContext } from '../../contexts/UserContext';

jest.mock('../../services/ChartService');
jest.mock('../../contexts/ExpenseContext', () => ({
    useExpenseContext: jest.fn(),
}));
jest.mock('../../contexts/UserContext', () => ({
    useUserContext: jest.fn(),
}));

describe('BarChart', () => {
    let mockUpdateCounter;
    let mockContextValue;
    let mockUser;

    beforeEach(() => {
        mockUpdateCounter = 0;
        mockContextValue = { updateCounter: mockUpdateCounter };
        useExpenseContext.mockReturnValue(mockContextValue);
        mockUser = { id: 1 };
        useUserContext.mockReturnValue({ user: mockUser });
    });

    it('should render loading state initially', () => {
        const { getByText } = render(<BarChart />);
        expect(getByText('Loading...')).toBeInTheDocument();
    });

    it('should render error message on fetch failure', async () => {
        const errorMessage = 'Failed to load expenses by category';
        getExpensesByCategory.mockRejectedValueOnce(new Error(errorMessage));

        const { getByText } = render(<BarChart />);

        await waitFor(() => {
            expect(getByText(`Error: ${errorMessage}`)).toBeInTheDocument();
        });
    });

    it('should render chart with fetched data', async () => {
        const mockData = { Food: 200, Transport: 100 };
        getExpensesByCategory.mockResolvedValueOnce(mockData);

        const { getByText } = render(<BarChart />);

        await waitFor(() => {
            expect(getByText('Expenses by Category')).toBeInTheDocument();
        });

        await waitFor(() => {
            expect(document.querySelector('canvas')).toBeInTheDocument();
        });
    });

    it('should handle empty data response', async () => {
        const mockData = {};
        getExpensesByCategory.mockResolvedValueOnce(mockData);

        const { getByText } = render(<BarChart />);

        await waitFor(() => {
            expect(getByText('Expenses by Category')).toBeInTheDocument();
        });

        await waitFor(() => {
            expect(document.querySelector('canvas')).toBeInTheDocument();
        });
    });

    it('should handle response with large number of categories', async () => {
        const mockData = Array.from({ length: 1000 }, (_, i) => ({ [`Category${i}`]: i })).reduce((acc, curr) => ({ ...acc, ...curr }), {});
        getExpensesByCategory.mockResolvedValueOnce(mockData);

        const { getByText } = render(<BarChart />);

        await waitFor(() => {
            expect(getByText('Expenses by Category')).toBeInTheDocument();
        });

        await waitFor(() => {
            expect(document.querySelector('canvas')).toBeInTheDocument();
        });
    });

    it('should handle response with negative amounts', async () => {
        const mockData = { Refunds: -50 };
        getExpensesByCategory.mockResolvedValueOnce(mockData);

        const { getByText } = render(<BarChart />);

        await waitFor(() => {
            expect(getByText('Expenses by Category')).toBeInTheDocument();
        });

        await waitFor(() => {
            expect(document.querySelector('canvas')).toBeInTheDocument();
        });
    });

    it('should handle response with large amounts', async () => {
        const mockData = { Project: Number.MAX_SAFE_INTEGER };
        getExpensesByCategory.mockResolvedValueOnce(mockData);

        const { getByText } = render(<BarChart />);

        await waitFor(() => {
            expect(getByText('Expenses by Category')).toBeInTheDocument();
        });

        await waitFor(() => {
            expect(document.querySelector('canvas')).toBeInTheDocument();
        });
    });

    it('should fetch data again when updateCounter changes', async () => {
        const mockData = { Food: 200, Transport: 100 };
        getExpensesByCategory.mockResolvedValue(mockData);

        const { rerender, getByText } = render(<BarChart />);

        await act(async () => {
            mockUpdateCounter = 1;
            mockContextValue = { updateCounter: mockUpdateCounter };
            useExpenseContext.mockReturnValue(mockContextValue);
            rerender(<BarChart />);
        });

        await waitFor(() => {
            expect(getByText('Expenses by Category')).toBeInTheDocument();
        });
    });
});
