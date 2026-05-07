package com.example.householdrag.model

// 서버에 저장된 예산 정보를 조회할 때 받는 응답 모델.
data class BudgetOut(
    val id: String,
    val year_month: String,
    val saving: Int,
    val total_budget: Int,
    val budget_details: Map<String, Int>,
    val remaining_budget_details: Map<String, Int>,
    val state: String,
    val created_by: String,
    val updated_at: String
)
