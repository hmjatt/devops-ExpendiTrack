import React, {useEffect, useState} from 'react';
import { useUserContext } from "../contexts/UserContext";
import { calculateTotalSpent, formatCurrency } from "../helpers/HelperFunctions";
import { useExpenseContext } from "../contexts/ExpenseContext";
import BasicModal from './Modal';
import AddBudgetForm from './AddBudgetForm';
import { useBudgetContext } from "../contexts/BudgetContext";
import {useTranslation} from "react-i18next";
import AddExpenseForm from "./AddExpenseForm";
import ExpenseList from "./ExpenseList";

const BudgetItem = ({ budget }) => {
    const { user } = useUserContext();
    const { expenses, resetExpError } = useExpenseContext();
    const { removeBudget, fetchBudgets, resetError, budgets } = useBudgetContext();
    const { t, i18n} = useTranslation();

    // Filter expenses for the current budget
    const expensesForCurrentBudget = expenses.filter(expense => expense.budget.budgetId === budget.budgetId);


    useEffect(() => {
    }, [i18n.language]);

    const { enableFormPopulation } = useBudgetContext();

    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);
    const [showDeleteWarning, setShowDeleteWarning] = useState(false); // Added state for delete warning modal

    // No need to keep track of totalSpent, remaining, and percentSpent in state if they're derived directly from props and context
    const totalSpent = calculateTotalSpent(expenses, budget.budgetId);
    const remaining = budget.budgetAmount - totalSpent;
    const percentSpent = Math.min((totalSpent / budget.budgetAmount) * 100, 100);

    const [showAddExpenseModal, setShowAddExpenseModal] = useState(false);

    const [showViewExpensesModal, setShowViewExpensesModal] = useState(false);


    const ProgressBar = ({ percentSpent }) => {
        let progressBarClass = "progress-bar progress-bar-striped progress-bar-animated percent-spent";

        if (percentSpent <= 50) {
            progressBarClass += " bg-success";
        } else if (percentSpent <= 75) {
            progressBarClass += " bg-warning";
        } else {
            progressBarClass += " bg-danger";
        }

        return (
            <div className="progress mb-3">
                <div
                    className={progressBarClass}
                    role="progressbar"
                    style={{width: `${percentSpent}%`}}
                    aria-valuenow={percentSpent}
                    aria-valuemin="0"
                    aria-valuemax="100"
                ></div>
            </div>
        );
    };

    const handleCloseModal = () => {
        resetError();
        resetExpError();
        setShowEditModal(false);
        setShowDeleteConfirmation(false);
        setShowDeleteWarning(false);
        setShowAddExpenseModal(false);
        setShowViewExpensesModal(false);

    };

    const handleEditClick = () => {
        resetError(); // Assuming resetError() is available and resets the global error state
        enableFormPopulation();
        setShowEditModal(true);
    };

    const handleDeleteClick = () => {
        resetError();
        // Perform the check for associated expenses directly within this handler
        const linkedExpenses = expenses.some(exp => String(exp.budget.budgetId) === String(budget.budgetId));

        if (linkedExpenses) {
            setShowDeleteWarning(true); // Show warning modal if there are linked expenses
        } else {
            setShowDeleteConfirmation(true); // Otherwise, show confirmation modal
        }
    };

    const handleDeleteConfirmation = async () => {
        resetError();
        try {
            await removeBudget(budget.budgetId);
            fetchBudgets(user.id);
            setShowDeleteConfirmation(false);
        } catch (serverError) {
            resetError();
        }
    }

    const handleAddExpenseClick = async () => {
        resetError(); // Assuming resetError() is available and resets the global error state
        setShowAddExpenseModal(true);
    }

    const handleViewExpensesClick = async () => {
        resetError(); // Assuming resetError() is available and resets the global error state
        setShowViewExpensesModal(true);
    }

    return (
        <div className="budget-item ">
            <div className="card h-100 shadow-sm custom-card-border">
                <div className="card-body">
                    <h5 className="card-title" data-testid="budget-title-test-id">
                        <strong>{t("app.add-budget-budget-name")}: {budget.budgetDescription}</strong></h5>
                    <p className="card-text"><strong>
                        {t("app.add-budget-amount")}: {formatCurrency(budget.budgetAmount)}</strong></p>
                    <p className="card-text total-spent">
                        <strong>{t("app.budgetItem-spent")}: {formatCurrency(totalSpent)}</strong></p>
                    <p className={`card-text remaining`}>
                        <strong
                            className={`${remaining < 0 ? 'text-danger' : 'text-success'}`}>
                            {remaining < 0 ?
                                `${t("app.budgetItem-overspent")}: ${formatCurrency(-remaining)}` :
                                `${t("app.budgetItem-remaining")}: ${formatCurrency(remaining)}`
                            }
                        </strong>
                    </p>

                    <ProgressBar percentSpent={percentSpent}/>

                    <hr className="brdr-bttm"/>

                    <div className="action-buttons mrgn-tp-md">
                        {/* Show "View Expenses" button only if there are expenses for the current budget */}
                        {expensesForCurrentBudget.length > 0 && (
                            <button onClick={handleViewExpensesClick} data-testid="view-expenses-btn"
                                    className="btn btn-sm btn-default">
                                <span className="glyphicon glyphicon-open"></span>
                                &nbsp;  {t("app.budgetItem-view")}
                            </button>
                        )}

                        <button onClick={handleAddExpenseClick} data-testid="create-expense-btn"
                                className="btn btn-sm btn-default">
                            <span className="glyphicon glyphicon-plus"></span>
                            &nbsp;  {t("app.add-expenses-create")}
                        </button>
                    </div>

                    <hr className="brdr-bttm"/>

                    {/* Edit and Delete Buttons */}
                    <div className="action-buttons mrgn-tp-md">
                        <button onClick={handleEditClick} className="btn btn-sm btn-default" id="edit-budget-btn"
                                data-testid="edit-budget">
                            <span className="glyphicon glyphicon-edit"></span>
                            &nbsp; {t("app.budgetItem-edit")}
                        </button>
                        <button onClick={handleDeleteClick} className="btn btn-sm btn-danger" id="delete-budget-btn">
                            <span className="glyphicon glyphicon-trash"></span>
                            &nbsp; {t("app.budgetItem-delete")}
                        </button>
                    </div>
                </div>
            </div>


            {/* View Expenses Modal */}
            {showViewExpensesModal && (
                <BasicModal show={showViewExpensesModal} handleClose={handleCloseModal} title={t("app.expenseListTitle")}>
                    <ExpenseList expenses={expensesForCurrentBudget} budgets={budgets} />
                </BasicModal>
            )}

            {/* Add Expense Modal */}
            {showAddExpenseModal && <BasicModal show={showAddExpenseModal} handleClose={handleCloseModal} title={t("app.add-expenses-create")}>
                <AddExpenseForm budgets={budget} onClose={() => setShowAddExpenseModal(false)}/>
            </BasicModal>}

            {/* Edit Modal */}
            {showEditModal && <BasicModal show={showEditModal} handleClose={handleCloseModal} title={t("app.budgetListEdit")}>
                <AddBudgetForm existingBudget={budget} onClose={() => setShowEditModal(false)}/>
            </BasicModal>}

            {/* Delete confirmation Modal */}
            {showDeleteConfirmation && <BasicModal show={showDeleteConfirmation} handleClose={handleCloseModal}  title={t("app.budgetListConfirmDeletion")}>
                <div className="text-center">
                    <h5>{t("app.budgetItem-are-you-sure-delete")}</h5>
                    <div className="action-buttons mrgn-tp-md">
                        <button className="btn btn-danger m-2" onClick={handleDeleteConfirmation}>{t("app.budgetItem-confirm-delete")}
                        </button>
                        <button className="btn btn-default m-2"
                                onClick={() => setShowDeleteConfirmation(false)}>{t("app.budgetItem-cancel")}
                        </button>
                    </div>
                </div>
            </BasicModal>}

            {/* Delete Warning Modal */}
            {showDeleteWarning && <BasicModal show={showDeleteWarning} handleClose={handleCloseModal} title="Cannot Delete Budget">
                <div className="text-center">
                    <p><strong>{t("app.budgetItem-cannot-delete")}</strong></p>
                </div>
            </BasicModal>}
        </div>
    );
};

export default BudgetItem;
