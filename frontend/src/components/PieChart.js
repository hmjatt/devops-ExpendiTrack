import React from 'react';
import { Pie } from 'react-chartjs-2';

const PieChart = ({ data }) => {
    const chartData = {
        labels: data.map(item => item.category),
        datasets: [
            {
                data: data.map(item => item.amount),
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'],
            },
        ],
    };

    return <Pie data={chartData} />;
};

export default PieChart;
