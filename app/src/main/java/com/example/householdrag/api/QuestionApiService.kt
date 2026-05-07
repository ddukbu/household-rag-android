package com.example.householdrag.api

import com.example.householdrag.model.AskRequest
import com.example.householdrag.model.AskResponse
import com.example.householdrag.model.ChatHistoryDto
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST

// 질문/답변(RAG) 기능 전용 API 계약.
interface QuestionApiService {
    // 질문을 서버에 전달하고 답변/참고문헌을 받는다.
    @POST("ask")
    suspend fun ask(@Body request: AskRequest): AskResponse

    // 서버에 저장된 채팅 이력을 최신순으로 불러온다.
    @GET("chat-history")
    suspend fun getChatHistory(): List<ChatHistoryDto>

    // 최근 카테고리별 소비 패턴 분석을 요청한다.
    @POST("analysis")
    suspend fun analyzeSpending(): AskResponse
}
