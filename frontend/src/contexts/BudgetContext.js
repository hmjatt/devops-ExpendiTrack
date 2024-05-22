import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import {getBudgetsByUserId, createBudget, deleteBudget, updateBudget} from '../services/BudgetService';
import { useUserContext } from "./UserContext";
import {useTranslation} from "react-i18next";


export const BudgetContext = createContext();

export const useBudgetContext = () => useContext(BudgetContext);

export const BudgetProvider = ({ children }) => {
    const [budgets, setBudgets] = useState([]);
    const { user } = useUserContext();
    const [error, setError] = useState('');

    const { t, i18n } = useTranslation();

    // Update to hold the error message key instead of the translated message
    const [errorKey, setErrorKey] = useState('');

    const [dynamicErrorContent, setDynamicErrorContent] = useState({});

    const [updateCounter, setUpdateCounter] = useState(0); // Notify data change


    const errorMapping = useMemo(() => ({
        "Invalid input: Budget amount cannot be negative or zero.": "app.invalidBudgetInput",
        "Invalid input: BudgetDescription must be alphanumeric": "app.budgetDescriptionError",
        "unexpectedError": "app.unexpectedError",
    }), []);

    useEffect(() => {
        const handleLanguageChange = () => {
            // Check if there's an error key set and if dynamic content is needed
            if (errorKey && Object.keys(dynamicErrorContent).length > 0) {
                setError(t(errorKey, dynamicErrorContent));
            }
        };

        // Listen for language changes
        i18n.on('languageChanged', handleLanguageChange);

        // Cleanup function to remove the event listener
        return () => {
            i18n.off('languageChanged', handleLanguageChange);
        };
    }, [i18n, errorKey, dynamicErrorContent, t]);

    const userId = user?.id;
    const [shouldPopulateForm, setShouldPopulateForm] = useState(false);

    const fetchBudgets = useCallback(async (userId) => {
        try {
            const response = await getBudgetsByUserId(userId);
            //console.log('Fetched budgets:', response); // print data

            if (Array.isArray(response)) {
                setBudgets(response);
            } else {
                setError('Failed to fetch budgets correctly');
            }
        } catch (error) {
            //console.error('Error fetching budgets:', error); // print log

            const errorMessage = error.message || 'An unexpected error occurred';
            setError(errorMessage);
        }
    }, []);

    // Functions to toggle the state of updating input form fields during budget update
    const enableFormPopulation = useCallback(() => {
        setShouldPopulateForm(true);
    }, []);

    const disableFormPopulation = useCallback(() => {
        setShouldPopulateForm(false);
    }, []);


    useEffect(() => {
        if (userId) {
            fetchBudgets(userId);
        }
    }, [userId, fetchBudgets]);

    const addNewBudget = useCallback(async (budgetData) => {
        try {
            const response = await createBudget({
                ...budgetData,
                userId: userId,
            });
            setBudgets(prevBudgets => [...prevBudgets, response]);
            setUpdateCounter(prev => prev + 1); // Notify data change

            setError('');

        } catch (error) {
            if (error.message.startsWith("A budget with the name")) {
                // Extract the dynamic budget name from the error message
                const budgetNameMatch = error.message.match(/"([^"]+)"/);
                const budgetName = budgetNameMatch ? budgetNameMatch[1] : "Unknown";

                // Use a dynamic key or a placeholder message in translations
                setDynamicErrorContent({ name: budgetName });
                setErrorKey("app.budgetExistsError"); // Assume this is a generic key in translations
                setError(t("app.budgetExistsError", { name: budgetName }));
            } else {
                // Handle other errors as before
                const key = errorMapping[error.message] || "app.unexpectedError";
                setErrorKey(key);
                setError(t(key));
            }
        }

    }, [userId, errorMapping, t]); // Corrected dependency

    const updateExistingBudget = useCallback(async (budgetId, budgetData) => {
        if (!budgetId) {
            setError('Failed to update budget: Missing budget ID');
            return;
        }
        try {
            const updatedBudget = await updateBudget(budgetId, budgetData);
            setBudgets((prevBudgets) =>
                prevBudgets.map((budget) => budget.budgetId === budgetId ? { ...budget, ...updatedBudget } : budget)
            );
            setError('');
        } catch (error) {
            if (error.message.startsWith("A budget with the name")) {
                // Extract the dynamic budget name from the error message
                const budgetNameMatch = error.message.match(/"([^"]+)"/);
                const budgetName = budgetNameMatch ? budgetNameMatch[1] : "Unknown";

                // Use a dynamic key or a placeholder message in translations
                setDynamicErrorContent({ name: budgetName });
                setErrorKey("app.budgetExistsError"); // Assume this is a generic key in translations
                setError(t("app.budgetExistsError", { name: budgetName }));
            } else {
                // Handle other errors as before
                const key = errorMapping[error.message] || "app.unexpectedError";
                setErrorKey(key);
                setError(t(key));
            }
        }
    }, [setBudgets, setError, errorMapping, t]);

    const removeBudget = useCallback(async (budgetId) => {
        try {
            await deleteBudget(budgetId);
            setBudgets(prevBudgets => prevBudgets.filter(budget => budget.budgetId !== budgetId));
            setUpdateCounter(prev => prev + 1); // Notify data change

            setError('');
        } catch (error) {

            const errorMessage = error.message || 'An unexpected error occurred';
            setError(errorMessage);
        }
    }, [setBudgets, setError]);



    const resetError = useCallback(() => setError(''), []);

    const providerValue = useMemo(() => ({
        budgets,
        addNewBudget,
        updateExistingBudget,
        shouldPopulateForm,
        enableFormPopulation,
        disableFormPopulation,
        removeBudget,
        fetchBudgets,
        error,
        setError,
        resetError,
        updateCounter // show updateCounter

    }), [budgets, addNewBudget, updateExistingBudget, shouldPopulateForm, enableFormPopulation, disableFormPopulation, removeBudget, fetchBudgets, error, resetError, updateCounter]);

    return (
        <BudgetContext.Provider value={providerValue}>
            {children}
        </BudgetContext.Provider>
    );
};
