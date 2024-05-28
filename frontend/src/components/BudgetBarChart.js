import React, { useEffect, useState, useCallback } from 'react';
import { Bar } from 'react-chartjs-2';
import { useBudgetContext } from '../contexts/BudgetContext';
import 'chart.js/auto';

const BudgetBarChart = () => {
    const [chartData, setChartData] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [noData, setNoData] = useState(false);
    const { chartData: budgetChartData, fetchBudgetCategoriesForChart } = useBudgetContext();

    const prepareChartData = useCallback(() => {
        if (budgetChartData && budgetChartData.length > 0) {
            const labels = budgetChartData.map(budget => budget.name);
            const values = budgetChartData.map(budget => budget.amount);

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
    }, [budgetChartData]);

    useEffect(() => {
        const loadBudgetChartData = async () => {
            setLoading(true);
            setError(null);
            setNoData(false);
            try {
                const userId = 1; // Replace with the actual user ID
                console.log("Fetching data for user ID:", userId); // Log user ID
                await fetchBudgetCategoriesForChart(userId);
            } catch (error) {
                console.error('Failed to load budget chart data:', error.response ? error.response.data : error.message || error);
                setError('Failed to load budget chart data. Please refresh the page to try again.');
                setLoading(false);
            }
        };

        loadBudgetChartData();
    }, [fetchBudgetCategoriesForChart]);

    useEffect(() => {
        prepareChartData(); // Prepare chart data only when budgetChartData changes
    }, [budgetChartData, prepareChartData]);

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
            <Bar
                data={chartData}
                options={{
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }}
            />
        </div>
    );
};

export default BudgetBarChart;
