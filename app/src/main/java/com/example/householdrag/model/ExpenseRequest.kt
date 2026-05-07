package com.example.householdrag.model

// 가계부 생성/수정 시 서버로 보내는 요청 바디.
data class ExpenseRequest(
    val date: String,
    val category: String,
    val amount: Int,
    val payment_method: String,
    val place: String,
    val memo: String,
    // Backwards-compatible additions required by server's ExpenseIn.
    // Placed at the end with defaults so existing positional call sites keep working.
    val time: String = "",
    val is_fixed_expense: Boolean = false
)
