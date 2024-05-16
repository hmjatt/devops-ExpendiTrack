import React, { useEffect, useState } from 'react';
import { Bar } from 'react-chartjs-2';
import { getBudgetsByCategory } from '../services/ChartService';
import { useBudgetContext } from '../contexts/BudgetContext';
import 'chart.js/auto';

/**
 * BudgetBarChart component renders a bar chart of budgets grouped by category.
 */
const BudgetBarChart = () => {
    const [chartData, setChartData] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { updateCounter } = useBudgetContext();

    /**
     * Fetches the chart data for budgets grouped by category.
     */
    const fetchChartData = () => {
        setLoading(true);
        getBudgetsByCategory()
            .then(data => {
                if (data) {
                    const labels = Object.keys(data);
                    const values = Object.values(data);

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
                }
                setLoading(false);
            })
            .catch(error => {
                console.error("There was an error fetching the data!", error);
                setError(error.message);
                setLoading(false);
            });
    };

    // Fetch the chart data when the component mounts or updateCounter changes.
    useEffect(() => {
        fetchChartData();
    }, [updateCounter]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
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
