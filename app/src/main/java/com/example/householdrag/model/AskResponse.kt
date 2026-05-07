package com.example.householdrag.model

import com.google.gson.annotations.SerializedName

// 질문 처리 결과 응답 모델.
data class AskResponse(
    val answer: String? = null,
    @SerializedName("retrieval_seconds")
    val retrieval_seconds: Double? = null,
    @SerializedName("generation_seconds")
    val generation_seconds: Double? = null,
    @SerializedName("total_seconds")
    val total_seconds: Double? = null,
    val references: List<String> = emptyList()
)
