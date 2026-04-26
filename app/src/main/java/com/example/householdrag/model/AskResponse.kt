package com.example.householdrag.model

// 질문 처리 결과 응답 모델.
data class AskResponse(
    val answer: String? = null,
    val r_sec: Float? = null,
    val g_sec: Float? = null,
    val t_sec: Float? = null,
    val references: List<String> = emptyList()
)
