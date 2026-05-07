package com.example.householdrag.model

// 지출 항목을 서버로 생성/수정할 때 보내는 요청 모델.
data class ExpenseIn(
    val date: String,
    val time: String,
    val is_fixed_expense: Boolean,
    val category: String,
    val amount: Int,
    val payment_method: String,
    val place: String,
    val memo: String
)
