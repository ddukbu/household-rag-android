package com.example.householdrag.model

// 고정 지출 항목을 생성/수정할 때 보내는 요청 모델.
data class FixedExpenseBudget(
    val category: String,
    val amount: Int,
    val memo: String = ""
)
