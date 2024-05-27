import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { useTranslation } from 'react-i18next';
import { useExpenseContext } from '../../contexts/ExpenseContext'; // Adjust the path as necessary
import DataList from '../DataList';

jest.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (key) => key,
        i18n: {
            language: 'en',
            changeLanguage: jest.fn(),
        },
    }),
}));

jest.mock('../../contexts/ExpenseContext', () => ({ // Adjust the path as necessary
    useExpenseContext: () => ({
        updateCounter: 0,
    }),
}));

describe('DataList Component', () => {
    beforeEach(() => {
        fetch.resetMocks();
    });

    it('displays loading message initially', () => {
        fetch.mockResponseOnce(JSON.stringify({}));

        render(<DataList />);

        expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    it('displays error message when fetch fails', async () => {
        fetch.mockReject(new Error('Network response was not ok'));

        render(<DataList />);

        await waitFor(() => expect(screen.getByText('Error: Network response was not ok')).toBeInTheDocument());
    });

    it('displays no data message when data is empty', async () => {
        fetch.mockResponseOnce(JSON.stringify({}));

        render(<DataList />);

        await waitFor(() => expect(screen.getByText('app.dataListNotAvailable')).toBeInTheDocument());
    });

    it('displays data when fetch succeeds', async () => {
        const mockData = {
            "Budget 1": 100,
            "Budget 2": 200
        };

        fetch.mockResponseOnce(JSON.stringify(mockData));

        render(<DataList />);

        await waitFor(() => expect(screen.getByText('Budget 1')).toBeInTheDocument());
        expect(screen.getByText('100')).toBeInTheDocument();
        expect(screen.getByText('Budget 2')).toBeInTheDocument();
        expect(screen.getByText('200')).toBeInTheDocument();
    });
});
