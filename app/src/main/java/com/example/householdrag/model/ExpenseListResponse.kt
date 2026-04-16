package com.example.householdrag.model

// 목록을 객체 형태로 감싸서 내려주는 서버 스펙에 대응하는 DTO.
data class ExpenseListResponse(
    val expenses: List<Expense>
)
