package com.example.householdrag.model

// 서버 chat_history 컬렉션에 저장된 대화 기록 응답 모델.
data class ChatHistoryDto(
    val id: String,
    val mode: String,
    val question: String,
    val answer: String,
    val context_text: String? = null,
    val embedding: List<Float>? = null,
    val created_at: String
)