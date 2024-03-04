const { By, until } = require('selenium-webdriver');
const buildDriver = require('../configs/webdriverSetup');

// Helper function to sign up and log in a user
async function signupAndLogin(driver) {
    // Navigate to the main/welcome page
    await driver.get('http://localhost:3000/');

    // Click on the "Create a New Account" button to navigate to the signup page
    const createAccountButton = await driver.findElement(By.xpath("//button[contains(text(), 'Create a New Account')]"));
    await driver.executeScript("arguments[0].click();", createAccountButton);

    // Wait for navigation to the signup page
    await driver.wait(until.urlIs('http://localhost:3000/signup'), 5000);

    // Fill in the signup form
    await driver.findElement(By.id('username')).sendKeys('testUser');
    await driver.findElement(By.id('email')).sendKeys('testuser@example.com');

    // Submit the signup form by clicking the submit button
    const submitButton = await driver.findElement(By.xpath("//button[contains(text(), 'Sign Up')]"));
    await driver.executeScript("arguments[0].click();", submitButton);

    // Wait for navigation to the dashboard
    await driver.wait(until.urlIs('http://localhost:3000/dashboard'), 10000);
}

// Helper function to log in a user
async function loginUser(driver, username, password) {
    // Navigate to the login page
    await driver.get('http://localhost:3000/login');

    // Fill in the login form
    await driver.findElement(By.id('username')).sendKeys('testUser');
    await driver.findElement(By.id('email')).sendKeys('testuser@example.com');

    // Submit the login form
    const loginButton = await driver.findElement(By.xpath("//button[contains(text(), 'Login')]"));
    await driver.executeScript("arguments[0].click();", loginButton);

    // Wait for navigation to the dashboard or another indicator of successful login
    await driver.wait(until.urlIs('http://localhost:3000/dashboard'), 10000);
}


// Helper function to fill in the budget form
async function fillBudgetForm(driver, name, amount) {
    const budgetNameInput = await driver.findElement(By.id('budget-description'));
    await budgetNameInput.clear();
    await budgetNameInput.sendKeys(name);

    const budgetAmountInput = await driver.findElement(By.id('budget-amount'));
    await budgetAmountInput.clear();
    await budgetAmountInput.sendKeys(amount);
}

// Helper function to submit the create budget form and check for an error message
async function submitFormAndCheckError(driver, expectedError) {
    const submitButton = await driver.findElement(By.xpath("//button[contains(text(), 'Create Budget')]"));
    await submitButton.click();

    await driver.wait(until.elementLocated(By.css('.alert.alert-danger')), 5000);
    const errorMessageElement = await driver.findElement(By.css('.alert.alert-danger'));
    const errorMessageText = await errorMessageElement.getText();

    expect(errorMessageText).toContain(expectedError);
}

// Helper function to submit the update budget form and check for an error message
async function submitUpdateFormAndCheckError(driver, expectedError) {
    const updateButton = await driver.findElement(By.xpath("//button[contains(text(), 'Update Budget')]"));
    await updateButton.click();

    await driver.wait(until.elementLocated(By.css('.alert.alert-danger')), 5000);
    const errorMessageElement = await driver.findElement(By.css('.alert.alert-danger'));
    const errorMessageText = await errorMessageElement.getText();

    expect(errorMessageText).toContain(expectedError);
}

describe('Budget Creation Tests - Invalid Data Scenarios', () => {
    let driver;

    beforeAll(async () => {
        driver = await buildDriver();
        await signupAndLogin(driver);
    });

    afterAll(async () => {
        await driver.quit();
    });

    it('Create Budget with Negative Amount', async () => {
        await fillBudgetForm(driver, 'Groceries', '-100');
        await submitFormAndCheckError(driver, 'Invalid input: Budget amount cannot be negative or zero.');
    });

    it('Create Budget with Non-Alphanumeric Name', async () => {
        await fillBudgetForm(driver, '$$$ Party Funds $$$', '500');
        await submitFormAndCheckError(driver, 'BudgetDescription must be alphanumeric');
    });

    it('Create Budget with Duplicate Name for the Same User', async () => {
        // First, create a budget with valid data to ensure there's a budget to duplicate
        await fillBudgetForm(driver, 'Groceries', '300');

        // Submit form
        const submitButton = await driver.findElement(By.xpath("//button[contains(text(), 'Create Budget')]"));
        await submitButton.click();

        // Then, attempt to create another budget with the same name
        await fillBudgetForm(driver, 'Groceries', '300');
        await submitFormAndCheckError(driver, 'A budget with the name "Groceries" already exists for this user');
    });
});

describe('Budget Creation Tests - Valid Data Scenarios', () => {
    let driver;

    beforeAll(async () => {
        driver = await buildDriver();
        await loginUser(driver);
    });

    afterAll(async () => {
        await driver.quit();
    });

    it('Create Budget with Valid Data', async () => {
        const budgetName = 'Utilities';
        const budgetAmount = '500';

        await fillBudgetForm(driver, budgetName, budgetAmount);

        // Submit the create budget form
        const submitButton = await driver.findElement(By.xpath("//button[contains(text(), 'Create Budget')]"));
        await submitButton.click();

        // Verify success message
        let successMessageElement;
        try {
            successMessageElement = await driver.wait(until.elementLocated(By.css('.alert.alert-success')), 5000);
        } catch (error) {
            throw new Error('Budget creation with valid data failed or success message not found.');
        }
        const successMessage = await successMessageElement.getText();
        expect(successMessage).toContain('Budget successfully added');

    });

});

describe('Budget Editing Tests - Invalid Data Scenarios', () => {
    let driver;

    beforeAll(async () => {
        driver = await buildDriver();
        await loginUser(driver);
    });

    afterAll(async () => {
        await driver.quit();
    });

    it('Edit Budget with Negative Amount', async () => {

        // Click on an "Edit" button next to the budget that already exists
        const editButton = await driver.findElement(By.id('edit-budget-btn'));
        await editButton.click();

        await fillBudgetForm(driver, 'Utilities', '-100');
        await submitUpdateFormAndCheckError(driver, 'Invalid input: Budget amount cannot be negative or zero.');
    });

    it('Edit Budget with Non-Alphanumeric Name', async () => {

        // Click on an "Edit" button next to the budget that already exists
        const editButton = await driver.findElement(By.id('edit-budget-btn'));
        await editButton.click();

        await fillBudgetForm(driver, '$$$ Party Funds $$$', '500');
        await submitUpdateFormAndCheckError(driver, 'BudgetDescription must be alphanumeric');
    });

    it('Edit Budget with Duplicate Name for the Same User', async () => {

        // Click on an "Edit" button next to the budget that already exists
        const editButton = await driver.findElement(By.id('edit-budget-btn'));
        await editButton.click();

        // Attempt to edit a budget with the name that already exists
        await fillBudgetForm(driver, 'Utilities', '500');
        await submitUpdateFormAndCheckError(driver, 'A budget with the name "Utilities" already exists for this user');
    });
});

