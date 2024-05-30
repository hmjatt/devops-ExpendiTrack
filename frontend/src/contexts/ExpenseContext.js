import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { getUserExpenses, createExpense, deleteExpense } from '../services/ExpenseService';
import { useUserContext } from "./UserContext";
import { useTranslation } from "react-i18next";

// Create ExpenseContext
export const ExpenseContext = createContext();

// Custom hook to use the ExpenseContext
export const useExpenseContext = () => useContext(ExpenseContext);

// ExpenseProvider component to provide expense-related data and functions to its children
export const ExpenseProvider = ({ children }) => {
    const [expenses, setExpenses] = useState([]); // State to hold the list of expenses
    const { user } = useUserContext(); // Get the current user from UserContext

    const [error, setError] = useState(''); // State to hold any error message
    const userId = user?.id; // Get the userId from the user object

    const { t, i18n } = useTranslation(); // Initialize translation

    const [errorKey, setErrorKey] = useState(''); // State to hold the error message key for translation
    const [dynamicErrorContent, setDynamicErrorContent] = useState({}); // State to hold dynamic content for error messages
    const [updateCounter, setUpdateCounter] = useState(0); // Counter to track updates

    // Memoized error mapping
    const errorMapping = useMemo(() => ({
        "invalid input: expenses amount cannot be negative.": "app.invalidExpenseInput",
        "invalid input: expensesdescription must be alphanumeric": "app.expenseDescriptionError",
        "unexpectederror": "app.unexpectedError",
    }), []);

    // Effect to update error message on language change
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

    // Function to fetch expenses for a specific user
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

    // Effect to fetch expenses when userId changes
    useEffect(() => {
        if (userId) {
            fetchExpenses(userId);
        }
    }, [userId, fetchExpenses]);

    // Function to add a new expense
    const addNewExpense = useCallback(async (expenseData) => {
        if (!userId) return;

        try {
            const response = await createExpense({
                ...expenseData,
                userId: userId,
            });
            setExpenses(prevExpenses => [...prevExpenses, response]);
            setUpdateCounter(prev => prev + 1); // Increment update counter
            setError('');
        } catch (error) {
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
        }
    }, [userId, errorMapping, t]);

    // Function to update an existing expense
    const updateExistingExpense = useCallback(async (expenseId, expenseData) => {
        if (!userId) return;

        try {

            const updatedExpense = await updateExpense(expenseId, expenseData);

            setExpenses(prevExpenses => prevExpenses.map(exp =>
                exp.expensesId === expenseId ? { ...exp, ...expenseData } : exp
            ));
            setUpdateCounter(prev => prev + 1);
            setError('');
        } catch (error) {
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
        }
    }, [userId, errorMapping, t]);

    // Function to remove an expense
    const removeExpense = useCallback(async (expenseId) => {
        if (!userId) return;

        try {
            await deleteExpense(expenseId);
            setExpenses(prevExpenses => prevExpenses.filter(exp => exp.expensesId !== expenseId));
            setUpdateCounter(prev => prev + 1);
            setError('');
        } catch (error) {
            const errorMessage = error.message || 'An unexpected error occurred';
            setError(errorMessage);
        }
    }, [userId]);

    // Function to reset the error state
    const resetError = useCallback(() => setError(''), []);

    // Function to reset the expense error state
    const resetExpError = useCallback(() => setError(''), []);

    // Memoize the provider value to prevent unnecessary re-renders
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