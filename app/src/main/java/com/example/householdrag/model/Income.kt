package com.example.householdrag.model

// 서버에 저장된 수입 항목 1건을 표현하는 응답 모델.
data class Income(
    val id: String,
    val date: String,
    val time: String,
    val is_fixed_income: Boolean,
    val category: String,
    val amount: Int,
    val deposit_method: String,
    val deposit_source: String,
    val memo: String,
    val fixed_item_id: String?
)
