import axios from 'axios';


const API_URL = `${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/expenses`;

export const getUserExpenses = ( userId ) => {
    return axios.get( `${API_URL}/user/${userId}` )
        .then( response => response.data )
        .catch( error => {
            throw new Error(error.response?.data || 'Failed to load expenses. Please refresh the page to try again.');
        });
};


export const createExpense = (expenseData) => {
    // Ensure the structure matches the backend expectation
    const formattedData = {
        expensesDescription: expenseData.expensesDescription,
        expensesAmount: expenseData.expensesAmount,
        expensesDate: expenseData.expensesDate,
        budget: { budgetId: parseInt(expenseData.budget.budgetId, 10) },
    };

    return axios.post( API_URL, formattedData )
        .then(response  =>  response.data)
        .catch(error  =>  {
            throw new Error(error.response?.data || 'An error occurred while creating an expense. Please try again later.');
        });
};

// Update an existing expense
export const updateExpense = (expenseId, expenseData) => {
    const formattedData = {
        expensesDescription: expenseData.expensesDescription,
        expensesAmount: expenseData.expensesAmount,
        expensesDate: expenseData.expensesDate,
        budget: { budgetId: parseInt(expenseData.budget.budgetId, 10) }, // Ensure correct parsing
    };


    return axios.put(`${API_URL}/${expenseId}`, formattedData)
        .then(response => response.data)
        .catch(error => {
            throw new Error(error.response?.data || 'An error occurred while updating the expense. Please try again later.');
        });
};

// Delete an existing expense
export const deleteExpense = (expenseId) => {
    return axios.delete(`${API_URL}/${expenseId}`)
        .then(response => response.data)
        .catch(error => {
            throw new Error(error.response?.data || 'An error occurred while deleting the expense. Please try again later.');
        });
};
