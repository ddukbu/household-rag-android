package com.example.householdrag.model

// AI 기반 예산 안 생성 결과를 받을 때의 응답 모델.
data class BudgetDraftOut(
    val type: String = "budget_draft",
    val message: String,
    val year_month: String,
    val mode: String,
    val saving: Int,
    val total_budget: Int,
    val budget_details: Map<String, Int>,
    val remaining_budget_details: Map<String, Int>,
    val state: String
)
