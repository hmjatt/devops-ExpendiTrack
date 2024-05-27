import React, { useEffect, useState } from 'react';
import { useTranslation } from "react-i18next";
import { useExpenseContext } from "../contexts/ExpenseContext";

const DataList = () => {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { t, i18n } = useTranslation();
    const { updateCounter } = useExpenseContext();

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await fetch('http://localhost:8080/data/totalexpenses-by-budget');
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                const result = await response.json();
                const transformedData = Object.entries(result).map(([key, value]) => ({
                    string: key,
                    int: value
                }));
                setData(transformedData);
            } catch (error) {
                setError(error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [updateCounter]);

    useEffect(() => {
        console.log('Language changed:', i18n.language);
    }, [i18n.language]);

    if (loading) {
        return <div className="text-center">Loading...</div>;
    }

    if (error) {
        return <div className="text-center text-danger">Error: {error.message}</div>;
    }

    if (!Array.isArray(data) || data.length === 0) {
        return (
            <div className="no-budget-item-container">
                <h2 className="h2-titles">Data</h2>
                <p>{t("app.dataListNotAvailable")}</p>
            </div>
        );
    }

    return (
        <div className="container data-list-container">
            <h2 className="h2-titles">Data List</h2>
            <ul className="list-group">
                {data.map((item, index) => (
                    <li key={index} className="list-group-item data-list-item d-flex justify-content-between align-items-center">
                        {item.string}
                        <span className="badge bg-primary data-list-badge rounded-pill">{item.int}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default React.memo(DataList);
