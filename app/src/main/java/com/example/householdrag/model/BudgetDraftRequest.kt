package com.example.householdrag.model

// AI 기반 예산 안을 생성할 때 보내는 요청 모델.
data class BudgetDraftRequest(
    val mode: String = "balanced",  // balanced, saving, relaxed
    val user_message: String = ""
)
