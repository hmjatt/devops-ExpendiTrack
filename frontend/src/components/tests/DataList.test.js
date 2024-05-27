import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { I18nextProvider, initReactI18next } from 'react-i18next';
import i18next from 'i18next';
import { ExpenseProvider } from '../../contexts/ExpenseContext';
import DataList from '../DataList';
import enTranslations from '../../translations/en/common.json';
import frTranslations from '../../translations/fr/common.json';

const mockFetch = (data, ok = true) => {
    global.fetch = jest.fn(() =>
        Promise.resolve({
            ok,
            json: () => Promise.resolve(data),
        })
    );
};

jest.mock('../../contexts/ExpenseContext', () => ({
    useExpenseContext: () => ({
        updateCounter: 0,
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

i18next
    .use(initReactI18next)
    .init({
        resources,
        lng: 'en',
        interpolation: {
            escapeValue: false,
        },
    });

describe('DataList Component', () => {
    it('displays loading message while fetching data', () => {
        render(
            <I18nextProvider i18n={i18next}>
                <ExpenseProvider>
                    <DataList />
                </ExpenseProvider>
            </I18nextProvider>
        );

        expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    it('displays error message when fetch fails', async () => {
        mockFetch({}, false);

        render(
            <I18nextProvider i18n={i18next}>
                <ExpenseProvider>
                    <DataList />
                </ExpenseProvider>
            </I18nextProvider>
        );

        await waitFor(() => expect(screen.getByText('Error: Network response was not ok')).toBeInTheDocument());
    });

    it('displays no data available message when data is empty', async () => {
        mockFetch([]);

        render(
            <I18nextProvider i18n={i18next}>
                <ExpenseProvider>
                    <DataList />
                </ExpenseProvider>
            </I18nextProvider>
        );

        await waitFor(() => expect(screen.getByText('Data')).toBeInTheDocument());
        expect(screen.getByText('No data available')).toBeInTheDocument();
    });

    it('displays data correctly when fetch succeeds', async () => {
        const mockData = { 'Budget A': 100, 'Budget B': 200 };
        mockFetch(mockData);

        render(
            <I18nextProvider i18n={i18next}>
                <ExpenseProvider>
                    <DataList />
                </ExpenseProvider>
            </I18nextProvider>
        );

        await waitFor(() => expect(screen.getByText('Data List')).toBeInTheDocument());
        expect(screen.getByText('Budget A')).toBeInTheDocument();
        expect(screen.getByText('Budget B')).toBeInTheDocument();
        expect(screen.getByText('100')).toBeInTheDocument();
        expect(screen.getByText('200')).toBeInTheDocument();
    });
});
