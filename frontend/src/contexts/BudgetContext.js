import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import { getBudgetsByUserId, createBudget, deleteBudget, updateBudget, getBudgetCategoriesForChart } from '../services/BudgetService';
import { useUserContext } from "./UserContext";
import { useTranslation } from "react-i18next";

export const BudgetContext = createContext();

export const useBudgetContext = () => useContext(BudgetContext);

export const BudgetProvider = ({ children }) => {
    const [budgets, setBudgets] = useState([]);
    const { user } = useUserContext();
    const [error, setError] = useState('');
    const [chartData, setChartData] = useState([]); // Adding new variables for budget chart data

    const { t, i18n } = useTranslation();

    const [errorKey, setErrorKey] = useState('');
    const [dynamicErrorContent, setDynamicErrorContent] = useState({});

    const errorMapping = useMemo(() => ({
        "Invalid input: Budget amount cannot be negative or zero.": "app.invalidBudgetInput",
        "Invalid input: BudgetDescription must be alphanumeric": "app.budgetDescriptionError",
        "unexpectedError": "app.unexpectedError",
    }), []);

    useEffect(() => {
        const handleLanguageChange = () => {
            if (errorKey && Object.keys(dynamicErrorContent).length > 0) {
                setError(t(errorKey, dynamicErrorContent));
            }
        };

        i18n.on('languageChanged', handleLanguageChange);

        return () => {
            i18n.off('languageChanged', handleLanguageChange);
        };
    }, [i18n, errorKey, dynamicErrorContent, t]);

    const userId = user?.id;
    const [shouldPopulateForm, setShouldPopulateForm] = useState(false);

    const fetchBudgets = useCallback(async (userId) => {
        try {
            const response = await getBudgetsByUserId(userId);
            if (Array.isArray(response)) {
                setBudgets(response);
            } else {
                setError('Failed to fetch budgets correctly');
            }
        } catch (error) {
            const errorMessage = error.message || 'An unexpected error occurred';
            setError(errorMessage);
        }
    }, []);

    const fetchBudgetCategoriesForChart = useCallback(async (userId) => {
        try {
            const data = await getBudgetCategoriesForChart(userId);
            console.log('Fetched data:', data); // Add log
            if (data && Array.isArray(data)) {
                setChartData(data);
            } else {
                console.error("Invalid response format", data); // Add data to log
                setChartData([]);
            }
        } catch (error) {
            console.error("Failed to fetch budget categories for chart:", error.response ? error.response.data : error.message || error);
            setChartData([]); // Set default empty array in case of error
        }
    }, []);

    const enableFormPopulation = useCallback(() => {
        setShouldPopulateForm(true);
    }, []);

    const disableFormPopulation = useCallback(() => {
        setShouldPopulateForm(false);
    }, []);

    useEffect(() => {
        if (userId) {
            fetchBudgets(userId);
            fetchBudgetCategoriesForChart(userId);
        }
    }, [userId, fetchBudgets, fetchBudgetCategoriesForChart]);

    const addNewBudget = useCallback(async (budgetData) => {
        try {
            const response = await createBudget({
                ...budgetData,
                userId: userId,
            });
            setBudgets(prevBudgets => [...prevBudgets, response]);
            setError('');
        } catch (error) {
            if (error.message.startsWith("A budget with the name")) {
                const budgetNameMatch = error.message.match(/"([^"]+)"/);
                const budgetName = budgetNameMatch ? budgetNameMatch[1] : "Unknown";
                setDynamicErrorContent({ name: budgetName });
                setErrorKey("app.budgetExistsError");
                setError(t("app.budgetExistsError", { name: budgetName }));
            } else {
                const key = errorMapping[error.message] || "app.unexpectedError";
                setErrorKey(key);
                setError(t(key));
            }
        }
    }, [userId, errorMapping, t]);

    const updateExistingBudget = useCallback(async (budgetId, budgetData) => {
        if (!budgetId) {
            setError('Failed to update budget: Missing budget ID');
            return;
        }
        try {
            const updatedBudget = await updateBudget(budgetId, budgetData);
            setBudgets((prevBudgets) =>
                prevBudgets.map((budget) => budget.id === budgetId ? { ...budget, ...updatedBudget } : budget)
            );
            setError('');
        } catch (error) {
            if (error.message.startsWith("A budget with the name")) {
                const budgetNameMatch = error.message.match(/"([^"]+)"/);
                const budgetName = budgetNameMatch ? budgetNameMatch[1] : "Unknown";
                setDynamicErrorContent({ name: budgetName });
                setErrorKey("app.budgetExistsError");
                setError(t("app.budgetExistsError", { name: budgetName }));
            } else {
                const key = errorMapping[error.message] || "app.unexpectedError";
                setErrorKey(key);
                setError(t(key));
            }
        }
    }, [setBudgets, setError, errorMapping, t]);

    const removeBudget = useCallback(async (budgetId) => {
        try {
            await deleteBudget(budgetId);
            setBudgets(prevBudgets => prevBudgets.filter(budget => budget.id !== budgetId));
            setError('');
        } catch (error) {
            const errorMessage = error.message || 'An unexpected error occurred';
            setError(errorMessage);
        }
    }, [setBudgets, setError]);

    const resetError = useCallback(() => setError(''), []);

    const providerValue = useMemo(() => ({
        budgets,
        chartData,
        addNewBudget,
        updateExistingBudget,
        shouldPopulateForm,
        enableFormPopulation,
        disableFormPopulation,
        removeBudget,
        fetchBudgets,
        fetchBudgetCategoriesForChart,
        error,
        setError,
        resetError,
    }), [budgets, chartData, addNewBudget, updateExistingBudget, shouldPopulateForm, enableFormPopulation, disableFormPopulation, removeBudget, fetchBudgets, fetchBudgetCategoriesForChart, error, resetError]);

    return (
        <BudgetContext.Provider value={providerValue}>
            {children}
        </BudgetContext.Provider>
    );
};
