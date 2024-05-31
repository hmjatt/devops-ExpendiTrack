import axios from 'axios';


// Use the environment variable for the API base URL
const API_URL = `${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/budgets`;

export const createBudget = (budgetData) => {
    return axios.post(API_URL, budgetData)
        .then(response => response.data)
        .catch(error => {
            // Throw an error directly with either the specific error message or a generic one
            throw new Error(error.response?.data|| 'An error occurred while creating the budget. Please try again later.');
        });
};

export const getBudgetsByUserId = (userId) => {
    return axios.get(`${API_URL}/user/${userId}`)
        .then(response => response.data)
        .catch(error => {
            // Throw an error directly for consistency with createBudget
            throw new Error(error.response?.data || 'Failed to load budgets. Please refresh the page to try again.');
        });
};

// Update an existing budget
export const updateBudget = (budgetId, budgetData) => {
    return axios.put(`${API_URL}/${budgetId}`, budgetData)
        .then(response => response.data)
        .catch(error => {
            throw new Error(error.response?.data || 'An error occurred while updating the budget. Please try again later.');
        });
};

// Delete an existing budget
export const deleteBudget = (budgetId) => {
    return axios.delete(`${API_URL}/${budgetId}`)
        .then(response => response.data)
        .catch(error => {
            throw new Error(error.response?.data || 'An error occurred while deleting the budget. Please try again later.');
        });
};


// adding a new method to get the api for budget charts, edited by Emily 2024/5/27
export const getBudgetCategoriesForChart = async (userId) => {
    try {
        const response = await axios.get(`${API_URL}/user/${userId}/names-and-amounts`);
        return response.data;
    } catch (error) {
        throw new Error(error.response?.data || 'Failed to fetch budget categories for chart.');
    }
};