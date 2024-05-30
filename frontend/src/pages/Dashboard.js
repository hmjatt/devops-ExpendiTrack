import React, { useEffect } from 'react';
import { useUserContext } from '../contexts/UserContext';
import { useBudgetContext } from "../contexts/BudgetContext";
import BudgetList from "../components/BudgetList";
import AddBudgetForm from "../components/AddBudgetForm";
import { useExpenseContext } from "../contexts/ExpenseContext";
import '../styles/Dashboard.css';
import { useTranslation } from "react-i18next";
import BarChart from "../components/BarChart";

/**
 * Dashboard component renders the main dashboard with budget and expense details.
 */
const Dashboard = () => {
    const { user } = useUserContext();
    const { budgets, fetchBudgets } = useBudgetContext();
    const { fetchExpenses } = useExpenseContext();
    const { t, i18n } = useTranslation();

    /**
     * Effect hook to handle language changes.
     */
    useEffect(() => {
    }, [i18n.language]);

    /**
     * Effect hook to fetch budgets and expenses when the user changes.
     */
    useEffect(() => {
        if (user && user.id) {
            fetchBudgets(user.id);
            fetchExpenses(user.id);
        }
    }, [user, fetchBudgets, fetchExpenses]);

    return (
        <div className="container" data-testid="dashboard">
            {user && <h1>{t("app.dashboard-welcome")}, {user.name}!</h1>}

            <div className="dashboard-container">

                <div className="dashboard-forms-container">
                    <AddBudgetForm />
                </div>

                <div className="budget-list-container">
                    <BudgetList budgets={budgets} />
                </div>
            </div>

            <BarChart />
        </div>
    );
};

export default Dashboard;
