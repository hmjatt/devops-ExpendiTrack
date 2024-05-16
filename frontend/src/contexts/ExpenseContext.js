import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { getUserExpenses, createExpense, updateExpense, deleteExpense } from '../services/ExpenseService';
import { useUserContext } from "./UserContext";
import { useTranslation } from "react-i18next";

export const ExpenseContext = createContext();

export const useExpenseContext = () => useContext(ExpenseContext);

/**
 * Provides the context for managing expenses, including fetching, adding, updating, and deleting expenses.
 * Also handles errors and updates the context consumers when changes occur.
 *
 * @param {Object} children - The child components that will consume the context.
 * @returns {JSX.Element} The context provider component.
 */
export const ExpenseProvider = ({ children }) => {
    const [expenses, setExpenses] = useState([]);
    const { user } = useUserContext();

    const [error, setError] = useState('');
    const userId = user?.id;

    const { t, i18n } = useTranslation();

    const [errorKey, setErrorKey] = useState('');
    const [dynamicErrorContent, setDynamicErrorContent] = useState({});
    const [updateCounter, setUpdateCounter] = useState(0); // This will be used to notify changes

    const errorMapping = useMemo(() => ({
        "invalid input: expenses amount cannot be negative.": "app.invalidExpenseInput",
        "invalid input: expensesdescription must be alphanumeric": "app.expenseDescriptionError",
        "unexpectederror": "app.unexpectedError",
    }), []);

    useEffect(() => {
        const handleLanguageChange = () => {
            if (errorKey) {
                setError(t(errorKey, dynamicErrorContent));
            }
        };

        i18n.on('languageChanged', handleLanguageChange);

        return () => {
            i18n.off('languageChanged', handleLanguageChange);
        };
    }, [i18n, errorKey, dynamicErrorContent, t]);

    /**
     * Fetches the expenses for a specific user.
     *
     * @param {number} userId - The ID of the user whose expenses are to be fetched.
     */
    const fetchExpenses = useCallback(async (userId) => {
        try {
            const response = await getUserExpenses(userId);
            if (Array.isArray(response)) {
                setExpenses(response);
            } else {
                setError('Failed to fetch expenses correctly');
            }
        } catch (error) {
            const errorMessage = error.message || 'An unexpected error occurred';
            setError(errorMessage);
        }
    }, []);

    useEffect(() => {
        if (userId) {
            fetchExpenses(userId);
        }
    }, [userId, fetchExpenses]);

    /**
     * Handles errors by setting the appropriate error message and key.
     *
     * @param {Error} error - The error to handle.
     */
    const handleError = useCallback((error) => {
        if (error.message.startsWith("An expense with the name")) {
            const expenseNameMatch = error.message.match(/"([^"]+)"/);
            const expenseName = expenseNameMatch ? expenseNameMatch[1] : "Unknown";

            setDynamicErrorContent({ name: expenseName });
            setErrorKey("app.expenseExistsError");
            setError(t("app.expenseExistsError", { name: expenseName }));
        } else {
            const normalizedErrorMessage = error.message.toLowerCase();
            const key = errorMapping[normalizedErrorMessage] || "app.unexpectedError";
            setErrorKey(key);
            setError(t(key));
        }
    }, [errorMapping, t]);

    /**
     * Adds a new expense for the current user.
     *
     * @param {Object} expenseData - The data for the new expense.
     */
    const addNewExpense = useCallback(async (expenseData) => {
        if (!userId) return;

        try {
            const response = await createExpense({
                ...expenseData,
                userId: userId,
            });
            setExpenses(prevExpenses => [...prevExpenses, response]);
            setUpdateCounter(prev => prev + 1); // Notify change
            setError('');
        } catch (error) {
            handleError(error);
        }
    }, [userId, handleError]);

    /**
     * Updates an existing expense for the current user.
     *
     * @param {number} expenseId - The ID of the expense to update.
     * @param {Object} expenseData - The new data for the expense.
     */
    const updateExistingExpense = useCallback(async (expenseId, expenseData) => {
        if (!userId) return;

        try {
            await updateExpense(expenseId, expenseData);
            setUpdateCounter(prev => prev + 1); // Notify change
            setError('');
        } catch (error) {
            handleError(error);
        }
    }, [userId, handleError]);

    /**
     * Removes an expense for the current user.
     *
     * @param {number} expenseId - The ID of the expense to remove.
     */
    const removeExpense = useCallback(async (expenseId) => {
        if (!userId) return;

        try {
            await deleteExpense(expenseId);
            setUpdateCounter(prev => prev + 1); // Notify change
            setError('');
        } catch (error) {
            const errorMessage = error.message || 'An unexpected error occurred';
            setError(errorMessage);
        }
    }, [userId]);

    const resetError = useCallback(() => setError(''), []);
    const resetExpError = useCallback(() => setError(''), []);

    const providerValue = useMemo(() => ({
        expenses,
        addNewExpense,
        updateExistingExpense,
        removeExpense,
        fetchExpenses,
        error,
        resetError,
        resetExpError,
        setError,
        updateCounter // Expose update counter
    }), [expenses, addNewExpense, updateExistingExpense, removeExpense, fetchExpenses, error, resetError, resetExpError, updateCounter]);

    return (
        <ExpenseContext.Provider value={providerValue}>
            {children}
        </ExpenseContext.Provider>
    );
};
