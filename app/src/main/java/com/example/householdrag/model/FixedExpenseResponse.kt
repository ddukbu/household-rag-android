package com.example.householdrag.model

// 고정 지출 CRUD 응답 모델.
data class FixedExpenseResponse(
    val budget: BudgetOut,
    val fixed_expenses: List<FixedExpenseItem>,
    val created_fixed_expense: FixedExpenseItem? = null,
    val updated_fixed_expense: FixedExpenseItem? = null,
    val deleted_fixed_expense: FixedExpenseItem? = null
)

data class FixedExpenseItem(
    val id: String,
    val category: String,
    val amount: Int,
    val memo: String = "",
    val is_recorded: Boolean
)