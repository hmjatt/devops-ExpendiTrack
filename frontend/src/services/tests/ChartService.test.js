import axios from 'axios';
import { getExpensesByCategory } from '../ChartService';

jest.mock('axios');

describe('getExpensesByCategory', () => {
    const userId = 1;

    beforeEach(() => {
        axios.get.mockClear();
    });

    it('should successfully fetch expenses by category for a specific user', async () => {
        const mockResponse = {
            data: { "Food": 200, "Transport": 100 }
        };

        axios.get.mockResolvedValue(mockResponse);

        await expect(getExpensesByCategory(userId)).resolves.toEqual(mockResponse.data);
        expect(axios.get).toHaveBeenCalledWith(`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/data/expenses-by-category`, { params: { userId } });
    });

    it('should handle server error response', async () => {
        const errorMessage = 'Internal Server Error';
        axios.get.mockRejectedValue({ response: { status: 500, data: errorMessage } });

        await expect(getExpensesByCategory(userId)).rejects.toThrow(errorMessage);
        expect(axios.get).toHaveBeenCalledWith(`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/data/expenses-by-category`, { params: { userId } });
    });

    it('should handle network error', async () => {
        const networkErrorMessage = 'Failed to load expenses by category. Please try again later.';
        axios.get.mockRejectedValue(new Error(networkErrorMessage));

        await expect(getExpensesByCategory(userId)).rejects.toThrow(networkErrorMessage);
        expect(axios.get).toHaveBeenCalledWith(`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/data/expenses-by-category`, { params: { userId } });
    });

    it('should handle empty response', async () => {
        const mockResponse = { data: {} };
        axios.get.mockResolvedValue(mockResponse);

        await expect(getExpensesByCategory(userId)).resolves.toEqual(mockResponse.data);
        expect(axios.get).toHaveBeenCalledWith(`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/data/expenses-by-category`, { params: { userId } });
    });

    it('should handle unexpected error format', async () => {
        axios.get.mockRejectedValue({ response: { data: null } });

        await expect(getExpensesByCategory(userId)).rejects.toThrow('Failed to load expenses by category. Please try again later.');
        expect(axios.get).toHaveBeenCalledWith(`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/data/expenses-by-category`, { params: { userId } });
    });

    it('should handle response with single category', async () => {
        const mockResponse = {
            data: { "Utilities": 200 }
        };

        axios.get.mockResolvedValue(mockResponse);

        await expect(getExpensesByCategory(userId)).resolves.toEqual(mockResponse.data);
        expect(axios.get).toHaveBeenCalledWith(`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/data/expenses-by-category`, { params: { userId } });
    });

    it('should handle response with large number of categories', async () => {
        const mockResponse = {
            data: Array.from({ length: 1000 }, (_, i) => ({ [`Category${i}`]: i })).reduce((acc, curr) => ({ ...acc, ...curr }), {})
        };

        axios.get.mockResolvedValue(mockResponse);

        const result = await getExpensesByCategory(userId);
        expect(result).toEqual(mockResponse.data);
        expect(Object.keys(result)).toHaveLength(1000);
        expect(axios.get).toHaveBeenCalledWith(`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/data/expenses-by-category`, { params: { userId } });
    });

    it('should handle response with negative amounts', async () => {
        const mockResponse = {
            data: { "Refunds": -50 }
        };

        axios.get.mockResolvedValue(mockResponse);

        await expect(getExpensesByCategory(userId)).resolves.toEqual(mockResponse.data);
        expect(axios.get).toHaveBeenCalledWith(`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/data/expenses-by-category`, { params: { userId } });
    });

    it('should handle response with large amounts', async () => {
        const mockResponse = {
            data: { "Project": Number.MAX_SAFE_INTEGER }
        };

        axios.get.mockResolvedValue(mockResponse);

        await expect(getExpensesByCategory(userId)).resolves.toEqual(mockResponse.data);
        expect(axios.get).toHaveBeenCalledWith(`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/data/expenses-by-category`, { params: { userId } });
    });
});
