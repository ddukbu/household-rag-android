package com.example.householdrag.model

// 고정 수입 CRUD 응답 모델.
data class FixedIncomeResponse(
    val budget: BudgetOut,
    val fixed_incomes: List<FixedIncomeItem>,
    val created_fixed_income: FixedIncomeItem? = null,
    val updated_fixed_income: FixedIncomeItem? = null,
    val deleted_fixed_income: FixedIncomeItem? = null
)

data class FixedIncomeItem(
    val id: String,
    val category: String,
    val amount: Int,
    val memo: String = "",
    val is_recorded: Boolean
)