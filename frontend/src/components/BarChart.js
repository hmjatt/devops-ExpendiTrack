import React, { useEffect, useState } from 'react';
import { Bar } from 'react-chartjs-2';
import { getExpensesByCategory } from '../services/ChartService';
import { useExpenseContext } from '../contexts/ExpenseContext';
import { useUserContext } from '../contexts/UserContext';
import 'chart.js/auto';

const BarChart = () => {
    // State to manage chart data
    const [chartData, setChartData] = useState({});
    // State to manage loading state
    const [loading, setLoading] = useState(true);
    // State to manage errors
    const [error, setError] = useState(null);
    // Access updateCounter from ExpenseContext
    const { updateCounter } = useExpenseContext();
    // Access user information from UserContext
    const { user } = useUserContext();

    // Function to fetch chart data from the backend
    const fetchChartData = async (userId) => {
        setLoading(true);
        try {
            // Call the service to get expenses by category for the given userId
            const data = await getExpensesByCategory(userId);
            if (data) {
                const labels = Object.keys(data); // Extract categories
                const values = Object.values(data); // Extract values

                // Update the chartData state with the fetched data
                setChartData({
                    labels: labels,
                    datasets: [{
                        label: 'Expenses by Category',
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
        } catch (error) {
            console.error("There was an error fetching the data!", error);
            setError(error.message);
        } finally {
            setLoading(false);
        }
    };

    // useEffect to fetch chart data when user.id or updateCounter changes
    useEffect(() => {
        if (user?.id) {
            fetchChartData(user.id);
        }
    }, [user?.id, updateCounter]);

    // Display loading message while data is being fetched
    if (loading) {
        return <div>Loading...</div>;
    }

    // Display error message if there is an error fetching data
    if (error) {
        return <div>Error: {error}</div>;
    }

    // Render the Bar chart
    return (
        <div>
            <h2>Expenses by Category</h2>
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

export default BarChart;
