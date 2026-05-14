package com.example.householdrag.model

// 월별 자산 변화를 기록한 단일 항목.
data class AssetHistoryItem(
    val year_month: String,
    val asset: Int,
    val total_income: Int,
    val total_expense: Int,
    val net_change: Int,
    val updated_at: String
)

// /assets/history 응답 전체 구조.
data class AssetHistoryResponse(
    val message: String,
    val current_asset: Int,
    val asset_history: List<AssetHistoryItem>
)
