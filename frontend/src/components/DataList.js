import React, { useEffect, useState } from 'react';
// import { getExpensesByCategory } from '../services/ChartService';
// import { useExpenseContext } from '../contexts/ExpenseContext';

const DataList = () => {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch('http://localhost:8080/data/totalexpenses-by-budget')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                const transformedData = Object.entries(data).map(([key, value]) => ({
                    string: key,
                    int: value
                }));
                setData(transformedData);
                setLoading(false);
            })
            .catch(error => {
                setError(error);
                setLoading(false);
            });
    }, []);

    if (loading) {
        return <div className="text-center">Loading...</div>;
    }

    if (error) {
        return <div className="text-center text-danger">Error: {error.message}</div>;
    }

    return (
        <div className="container">
            <h3 className="my-3">Data List</h3>
            <ul className="list-group">
                {data.map((item, index) => (
                    <li key={index} className="list-group-item d-flex justify-content-between align-items-center">
                        {item.string}
                        <span className="badge bg-primary rounded-pill">{item.int}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default DataList;
