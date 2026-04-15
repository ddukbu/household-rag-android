package com.example.householdrag.model

// 질문 처리 결과 응답 모델.
data class AskResponse(
    // 생성된 답변 텍스트.
    val answer: String,
    // 답변 생성에 참고된 출처 목록.
    val references: List<String>
)
