import axios from 'axios';

// Use the environment variable for the API base URL
const API_URL = `${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/data`;

/**
 * Fetches expenses grouped by category for a specific user.
 *
 * @param {number} userId - The ID of the user whose expenses are to be fetched.
 * @returns {Promise<Object>} A promise that resolves to an object containing expenses grouped by category.
 * @throws {Error} Throws an error if the request fails.
 */
export const getExpensesByCategory = (userId) => {
    return axios.get(`${API_URL}/expenses-by-category`, { params: { userId } })
        .then(response => response.data)
        .catch(error => {
            console.error('Error fetching expenses by category:', error);
            throw new Error(error.response?.data || 'Failed to load expenses by category. Please try again later.');
        });
};
