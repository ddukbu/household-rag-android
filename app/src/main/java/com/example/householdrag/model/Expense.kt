package com.example.householdrag.model

// 서버에 저장된 가계부 항목 1건을 표현하는 응답 모델.
data class Expense(
    val id: String,
    val date: String,
    val category: String,
    val amount: Int,
    val payment_method: String,
    val place: String,
    val memo: String
)
