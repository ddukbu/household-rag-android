package com.example.householdrag.model

// 서버에 저장된 월별 요약본을 조회할 때 받는 응답 모델.
data class Summary(
    val id: String,
    val year_month: String,
    val embedding: List<Float> = emptyList(),
    // 통계
    val total_income: Int = 0,
    val total_expense: Int = 0,
    // 수익 
    val fixed_income_details: Map<String, Int> = emptyMap(),
    val variable_income_details: Map<String, Int> = emptyMap(),
    // 지출 
    val fixed_expense_details: Map<String, Int> = emptyMap(),
    val variable_expense_details: Map<String, Int> = emptyMap()
)
