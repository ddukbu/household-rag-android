package com.example.householdrag.model

// 자산 상태 정보를 조회할 때 받는 응답 모델.
data class AssetOut(
    val initial_asset: Int,
    val current_asset: Int,
    val total_income: Int,
    val total_expense: Int,
    val updated_at: String = ""
)
