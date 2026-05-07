package com.example.householdrag.model

// 예산 draft 적용/취소처럼 message + budget 형태로 내려오는 응답 모델.
data class BudgetActionResponse(
    val message: String,
    val budget: BudgetOut
)