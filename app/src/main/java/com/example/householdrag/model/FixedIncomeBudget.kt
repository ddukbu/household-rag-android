package com.example.householdrag.model

// 고정 수입 항목을 생성/수정할 때 보내는 요청 모델.
data class FixedIncomeBudget(
    val category: String,
    val amount: Int,
    val memo: String = ""
)
