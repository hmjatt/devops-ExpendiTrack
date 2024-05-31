import React from 'react';
import { render, screen, waitFor, act } from '@testing-library/react';
import DataList from '../DataList';

jest.mock('react-i18next', () => {
    const originalModule = jest.requireActual('react-i18next');
    return {
        ...originalModule,
        useTranslation: () => ({
            t: (key) => key,
            i18n: {
                language: 'en',
                changeLanguage: jest.fn(),
            },
        }),
    };
});

jest.mock('../../contexts/ExpenseContext', () => ({ // Adjust the path as necessary
    useExpenseContext: () => ({
        updateCounter: 0,
    }),
}));

describe('DataList Component', () => {
    beforeEach(() => {
        fetch.resetMocks();
    });

    it('displays loading message initially', async () => {
        fetch.mockResponseOnce(JSON.stringify({}));

        render(<DataList />);

        expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    it('displays error message when fetch fails', async () => {
        fetch.mockReject(new Error('Network response was not ok'));

        await act(async () => {
            render(<DataList />);
        });

        await waitFor(() => expect(screen.getByText('Error: Network response was not ok')).toBeInTheDocument());
    });

    it('displays no data message when data is empty', async () => {
        fetch.mockResponseOnce(JSON.stringify({}));

        await act(async () => {
            render(<DataList />);
        });

        await waitFor(() => expect(screen.getByText('app.dataListNotAvailable')).toBeInTheDocument());
    });

    it('displays data when fetch succeeds', async () => {
        const mockData = {
            "Budget 1": 100,
            "Budget 2": 200
        };

        fetch.mockResponseOnce(JSON.stringify(mockData));

        await act(async () => {
            render(<DataList />);
        });

        await waitFor(() => {
            expect(screen.getByText('Budget 1')).toBeInTheDocument();
            expect(screen.getByText('100')).toBeInTheDocument();
            expect(screen.getByText('Budget 2')).toBeInTheDocument();
            expect(screen.getByText('200')).toBeInTheDocument();
        });
    });

    // Additional Edge Case Tests
    it('handles null values gracefully', async () => {
        const mockData = {
            "Budget 1": 100,
            "Budget 2": null
        };

        fetch.mockResponseOnce(JSON.stringify(mockData));

        await act(async () => {
            render(<DataList />);
        });

        await waitFor(() => {
            expect(screen.getByText('Budget 1')).toBeInTheDocument();
            expect(screen.getByText('100')).toBeInTheDocument();
            expect(screen.getByText('Budget 2')).toBeInTheDocument();
            expect(screen.getByText('0')).toBeInTheDocument(); // Ensuring null is handled as 0
        });
    });

    it('handles unexpected data structure', async () => {
        const mockData = "Unexpected data structure";

        fetch.mockResponseOnce(JSON.stringify(mockData));

        await act(async () => {
            render(<DataList />);
        });

        await waitFor(() => expect(screen.getByText('Error: Unexpected data structure')).toBeInTheDocument());
    });

    it('handles large data sets', async () => {
        const mockData = {};
        for (let i = 1; i <= 1000; i++) {
            mockData[`Budget ${i}`] = i * 10;
        }

        fetch.mockResponseOnce(JSON.stringify(mockData));

        await act(async () => {
            render(<DataList />);
        });

        // Check for a few items to ensure rendering works
        await waitFor(() => {
            expect(screen.getByText('Budget 1')).toBeInTheDocument();
            expect(screen.getByText('10')).toBeInTheDocument();
            expect(screen.getByText('Budget 500')).toBeInTheDocument();
            expect(screen.getByText('5000')).toBeInTheDocument();
            expect(screen.getByText('Budget 1000')).toBeInTheDocument();
            expect(screen.getByText('10000')).toBeInTheDocument();
        });
    });


    it('handles special characters in budget descriptions', async () => {
        const mockData = {
            "休暇": 100,
            "Groceries": 200
        };

        fetch.mockResponseOnce(JSON.stringify(mockData));

        await act(async () => {
            render(<DataList />);
        });

        await waitFor(() => {
            expect(screen.getByText('休暇')).toBeInTheDocument();
            expect(screen.getByText('100')).toBeInTheDocument();
            expect(screen.getByText('Groceries')).toBeInTheDocument();
            expect(screen.getByText('200')).toBeInTheDocument();
        });
    });
});
