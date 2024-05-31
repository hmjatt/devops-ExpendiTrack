import React, { useEffect } from 'react';
import { useUserContext } from '../contexts/UserContext';
import { useBudgetContext } from "../contexts/BudgetContext";
import BudgetList from "../components/BudgetList";
import AddBudgetForm from "../components/AddBudgetForm";
import { useExpenseContext } from "../contexts/ExpenseContext";
import '../styles/Dashboard.css';
import { useTranslation } from "react-i18next";
import DataList from "../components/DataList";
import BarChart from "../components/BarChart";
import BudgetPieChart from "../components/BudgetPieChart";
import BudgetBarChart from '../components/BudgetBarChart';


const Dashboard = () => {
    const { user } = useUserContext();
    const { budgets, fetchBudgets } = useBudgetContext();
    const { fetchExpenses } = useExpenseContext();
    const { t, i18n } = useTranslation();

    useEffect(() => {
    }, [i18n.language]);

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

            <div className="chart-container">
                <div className="pie-chart-container">
                    <BudgetPieChart />
                </div>
                <div className="budget-bar-chart-container">
                    <BudgetBarChart />  {/* adding BudgetBarChart element */}
                </div>

            </div>

            <DataList />
            <BarChart />

        </div>
    );
};

export default Dashboard;
