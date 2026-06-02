package com.example.householdrag.model

// 수입 항목을 서버로 생성/수정할 때 보내는 요청 모델.
data class IncomeIn(
    val date: String,
    val time: String,
    val is_fixed_income: Boolean,
    val category: String,
    val amount: Int,
    val deposit_method: String,
    val deposit_source: String,
    val memo: String,
    val fixed_item_id: String = ""
)
