import axios from 'axios';

// Use the environment variable for the API base URL
const API_URL = `${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/users`;

// Function to create a user by sending a POST request
export const createUser = (userData) => axios.post(API_URL, userData)
    .then(response => response.data)
    .catch((error) => {
        // Assuming the error message is directly in error.response.data
        const signupError = "An error occurred during the signup process. Please try again later."
        throw new Error(error.response.data || signupError);
    });


// Function to find a user by name and email using a GET request
export const findUser = async (name, email) => {
    try {
        const response = await axios.get(`${API_URL}/find`, { params: { name, email } });
        return response.data;
    } catch (error) {
        const serverError = "Server error occurred. Please try again later."
        // Adjusted to throw an error for consistency with createUser
        throw new Error(error.response?.data || serverError);
    }
};
