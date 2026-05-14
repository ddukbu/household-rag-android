package com.example.householdrag.model

// 서버 에러 응답 바디: HTTP 에러 상황에서 {"detail": "..."} 형태로 내려옴
data class ErrorDetail(
    val detail: String? = null
)
