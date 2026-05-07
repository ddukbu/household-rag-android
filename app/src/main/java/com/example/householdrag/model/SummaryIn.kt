package com.example.householdrag.model

// 월별 지출/수입 요약본을 생성/수정할 때 보내는 요청 모델.
data class SummaryIn(
    val year_month: String,
    val embedding: List<Float> = emptyList(),
    // 통계
    val total_income: Int = 0,                      // 총소득
    val total_expense: Int = 0,                     // 총지출
    // 수익 섹션
    val fixed_income_details: Map<String, Int> = emptyMap(),    // 고정 수입
    val variable_income_details: Map<String, Int> = emptyMap(), // 변동 수입
    // 지출 섹션
    val fixed_expense_details: Map<String, Int> = emptyMap(),   // 고정 지출
    val variable_expense_details: Map<String, Int> = emptyMap() // 변동 지출
)
