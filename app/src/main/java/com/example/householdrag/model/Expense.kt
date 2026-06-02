package com.example.householdrag.model

// 서버에 저장된 지출 항목 1건을 표현하는 응답 모델.
data class Expense(
    val id: String,
    val date: String,
    val time: String,
    val is_fixed_expense: Boolean,
    val category: String,
    val amount: Int,
    val payment_method: String,
    val place: String,
    val memo: String,
    val fixed_item_id: String?
)
