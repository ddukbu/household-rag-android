package com.example.householdrag.api

import com.example.householdrag.model.AskRequest
import com.example.householdrag.model.AskResponse
import retrofit2.http.Body
import retrofit2.http.POST

// 질문/답변(RAG) 기능 전용 API 계약.
interface QuestionApiService {
    // 질문을 서버에 전달하고 답변/참고문헌을 받는다.
    @POST("ask")
    suspend fun ask(@Body request: AskRequest): AskResponse
}
