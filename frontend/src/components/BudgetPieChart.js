import React, { useEffect, useState, useCallback } from 'react';
import { Pie } from 'react-chartjs-2';
import { useBudgetContext } from '../contexts/BudgetContext';
import 'chart.js/auto';

const BudgetPieChart = () => {
    const [chartData, setChartData] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [noData, setNoData] = useState(false);
    const { budgets, fetchBudgets } = useBudgetContext();

    const prepareChartData = useCallback(() => {
        if (budgets && budgets.length > 0) {
            const labels = budgets.map(budget => budget.budgetDescription);
            const values = budgets.map(budget => budget.budgetAmount);

            setChartData({
                labels: labels,
                datasets: [{
                    label: 'Budgets by Category',
                    data: values,
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.6)',
                        'rgba(54, 162, 235, 0.6)',
                        'rgba(255, 206, 86, 0.6)',
                        'rgba(75, 192, 192, 0.6)',
                        'rgba(153, 102, 255, 0.6)'
                    ]
                }]
            });

            setLoading(false);
            setNoData(false);
        } else {
            setLoading(false);
            setNoData(true);
        }
    }, [budgets]);

    useEffect(() => {
        const loadBudgets = async () => {
            setLoading(true);
            setError(null);
            setNoData(false);
            try {
                await fetchBudgets(); // calling fetchBudgets method to retrieve budget
                prepareChartData(); // prepare dat for chart
            } catch (error) {
                setError('Failed to load budgets. Please refresh the page to try again.');
                setLoading(false);
            }
        };

        loadBudgets();
    }, [fetchBudgets, prepareChartData])

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    if (noData) {
        return <div>No budgets available to display.</div>;
    }

    return (
        <div>
            <h2>Budgets by Category</h2>
            <Pie
                data={chartData}
                options={{
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'top'
                        },
                        title: {
                            display: true,
                            text: 'Budgets by Category'
                        }
                    }
                }}
            />
        </div>
    );
};

export default BudgetPieChart;
