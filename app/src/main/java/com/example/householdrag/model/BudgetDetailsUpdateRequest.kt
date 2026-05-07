package com.example.householdrag.model

// 월별 카테고리별 예산을 수정할 때 보내는 요청 모델.
data class BudgetDetailsUpdateRequest(
    val budget_details: Map<String, Int>
)
