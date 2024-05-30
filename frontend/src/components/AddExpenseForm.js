import React, { useEffect, useState } from 'react';
import { useExpenseContext } from '../contexts/ExpenseContext';
import { formatDate} from '../helpers/HelperFunctions';
import { useUserContext } from '../contexts/UserContext';
import BasicModal from "./Modal";
import {useTranslation} from "react-i18next";

const AddExpenseForm = ({ existingExpense, budgets, onClose }) => {
    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [selectedBudgetId, setSelectedBudgetId] = useState('');
    const { addNewExpense, fetchExpenses, updateExistingExpense, error, resetError, expenses, setError } = useExpenseContext();
    const { user } = useUserContext(); // Get the current user
    const { t, i18n } = useTranslation();


    // Ensure budgets is always an array
    budgets = Array.isArray(budgets) ? budgets : [budgets];

    useEffect(() => {
    }, [i18n.language]);


    const [date, setDate] = useState(() => {
        const today = new Date();
        const localDate = new Date(today.getTime() - (today.getTimezoneOffset() * 60000));
        return localDate.toISOString().slice(0, 10);
    });

    const [showWarningModal, setShowWarningModal] = useState(false);
    const [showSuccessAlert, setShowSuccessAlert] = useState(false);

    useEffect(() => {
        if (existingExpense && existingExpense.budget && budgets.length > 0) {
            setDescription(existingExpense.expensesDescription);
            setAmount(existingExpense.expensesAmount);
            setDate(existingExpense.expensesDate.slice(0, 10)); // Assuming ISO string format
            setSelectedBudgetId(existingExpense.budget.budgetId.toString());
        } else if (budgets && budgets.length > 0) {
            setSelectedBudgetId(budgets[0].budgetId.toString());
        }
        // resetError();
    }, [existingExpense, budgets]);


    const handleWarningClose = async (proceed) => {
        setShowWarningModal(false);
        if (proceed) {
            await submitExpense(); // Proceed with the expense submission.
        }
    };

    const submitExpense = async () => {

        try {
            if (existingExpense) {
                const expenseData = {
                    expensesDescription: description,
                    expensesAmount: parseFloat(amount),
                    budget: { budgetId: parseInt(selectedBudgetId) },
                };
                await updateExistingExpense(existingExpense.expensesId, expenseData);
            } else {
                const expenseData = {
                    expensesDescription: description,
                    expensesAmount: parseFloat(amount),
                    expensesDate: new Date(date).toISOString(),
                    budget: { budgetId: parseInt(selectedBudgetId) },
                };
                await addNewExpense(expenseData);
            }
            setDescription('');
            setAmount('');
            setDate('');
            setShowSuccessAlert(true);
            setTimeout(() => setShowSuccessAlert(false), 5000); // Adjust duration as needed
            fetchExpenses(user.id); // Refresh expense list
            setSelectedBudgetId(budgets.length > 0 ? budgets[0].budgetId.toString() : '');
        } catch (error) {
            setError(error);
            setShowSuccessAlert(false);
            resetError();
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        resetError();

        // No need to fetch budgets and expenses if they are already up-to-date.
        const budgetAmount = budgets.find(budget => budget.budgetId.toString() === selectedBudgetId)?.budgetAmount;
        let totalExpensesForBudget = expenses
            .filter(expense => expense.budget?.budgetId.toString() === selectedBudgetId)
            .reduce((acc, current) => acc + parseFloat(current.expensesAmount), 0);

        // Directly proceed with submission if the expense amount is 0 or negative.
        if (parseFloat(amount) <= 0) {
            await submitExpense();
            return;
        }

        // Adjust total if we are updating an expense.
        if (existingExpense) {
            totalExpensesForBudget -= parseFloat(existingExpense.expensesAmount);
        }

        const newTotal = totalExpensesForBudget + parseFloat(amount);

        if (budgetAmount < newTotal) {
            setShowWarningModal(true);
            return
        }

        await submitExpense();

    };

    return (
        <div className="dashboard-expense-form">
            <form onSubmit={handleSubmit}>
                <section >

                    <div className="form-section-container">
                        <div className="form-group mrgn-tp-sm">
                            <label htmlFor="expense-description">
                                <span className="field-name">{t("app.add-expenses-add")}</span> <strong
                                className="required">{t("app.add-budget-required")}</strong>
                            </label>

                            <div>
                                <input
                                    data-testid="expense-description"
                                    type="text"
                                    className="form-control"
                                    value={description}
                                    onChange={(e) => setDescription(e.target.value)}
                                    onInvalid={(e) => e.target.setCustomValidity(t("app.expenseDescriptionRequired"))}
                                    onInput={(e) => e.target.setCustomValidity('')}
                                    placeholder="e.g., Walmart"
                                    id="expense-description"
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="expense-amount">
                                <span className="field-name">{t("app.add-expenses-amount")}</span> <strong
                                className="required">{t("app.add-budget-required")}</strong>
                            </label>

                            <div>
                                <input
                                    data-testid="expense-amount-input"
                                    type="number"
                                    className="form-control"
                                    value={amount}
                                    onChange={(e) => setAmount(e.target.value)}
                                    onInvalid={(e) => e.target.setCustomValidity(t("app.expenseAmountRequired"))}
                                    onInput={(e) => e.target.setCustomValidity('')}
                                    placeholder="e.g., 150.47"
                                    id="expense-amount"
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-group">

                            {existingExpense ? (
                                <label htmlFor="expense-date">
                                    <span className="field-name">{t("app.add-expenses-date")}</span>
                                </label>
                            ) : (
                                <label htmlFor="expense-date">
                                    <span className="field-name">{t("app.add-expenses-date")}</span> <strong
                                    className="required">{t("app.add-budget-required")}</strong>
                                </label>
                            )}

                            {existingExpense ? (
                                // If editing an existing expense, display the date as text
                                <div id="expense-date" className="form-control-plaintext">
                                    {formatDate(existingExpense.expensesDate)}
                                </div>
                            ) : (

                                // If adding a new expense, display the date input field
                                <input
                                    data-testid="expense-date"
                                    type="date"
                                    className="form-control"
                                    value={date}
                                    onChange={(e) => setDate(e.target.value)}
                                    onInvalid={(e) => e.target.setCustomValidity(t("app.expenseDateRequired"))}
                                    onInput={(e) => e.target.setCustomValidity('')}
                                    placeholder="Date"
                                    id="expense-date"
                                    required
                                />
                            )}
                        </div>

                        <div className="form-group">
                            <label htmlFor="budget-category">
                                <span className="field-name">{t("app.add-budget-budget-name")}</span>
                            </label>

                            <div id="budget-category" className="form-control-plaintext">
                                {existingExpense ? (
                                    existingExpense.budget.budgetDescription
                                ) : (
                                    budgets.find(budget => budget.budgetId.toString() === selectedBudgetId)?.budgetDescription || t("app.add-expenses-budget-selection")
                                )}
                            </div>
                        </div>

                        {existingExpense ? (
                            <div className="button-submit-form">
                                <button type="submit" className=" btn btn-lg btn-success" data-testid="updateExpenseButton">
                                    <span className="glyphicon glyphicon-floppy-save"></span>
                                    &nbsp; {t("app.add-expenses-update")}
                                </button>
                            </div>
                        ) : (
                            <div className="mrgn-bttm-md button-submit-form">
                                <button data-testid="create-expense" type="submit" className="btn btn-lg btn-default">
                                    <span className="glyphicon glyphicon-plus"></span>
                                    &nbsp;{t("app.add-expense-create")}
                                </button>
                            </div>
                        )}
                        {/* Success and error alerts */}
                        {showSuccessAlert && !error && (
                            <div className="expense-message alert alert-success" role="alert">
                                <button type="button" className="close" data-dismiss="alert" aria-label="Close"
                                        onClick={() => setShowSuccessAlert(false)}>
                                    <span aria-hidden="true">&times;</span>
                                </button>
                                <p>{t("app.add-expense-successfully")} {existingExpense ? t("app.add-budget-updated") : t("app.add-expenses-added")}!</p>
                            </div>
                        )}

                        {error && (
                            <div className="alert alert-danger" role="alert">
                                <h4>{t("app.add-expenses-form-cannot-be-submitted")}</h4>
                                <ul>
                                    <li>{error}</li>
                                </ul>
                            </div>
                        )}

                        {/* Warning modal similar to the one in AddBudgetForm */}
                        <BasicModal
                            show={showWarningModal}
                            handleClose={() => handleWarningClose(false)}
                            title="Warning"
                        >
                            <p><strong>{t("app.add-expenses-exceeds-budget")}</strong></p>
                            <div>
                                <button onClick={() => handleWarningClose(true)}
                                        className="btn btn-danger mrgn-rght-lg">{t("app.add-expenses-proceed")}
                                </button>
                                <button onClick={() => handleWarningClose(false)}
                                        className="btn btn-default">{t("app.budgetItem-cancel")}
                                </button>
                            </div>
                        </BasicModal>
                    </div>
                </section>
            </form>
        </div>
    );
};

export default AddExpenseForm;
