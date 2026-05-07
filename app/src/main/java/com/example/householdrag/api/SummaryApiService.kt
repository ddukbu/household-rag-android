package com.example.householdrag.api

import com.example.householdrag.model.Summary
import com.example.householdrag.model.SummaryIn
import retrofit2.http.*

// 월별 요약본(Summary) 도메인 전용 API 계약.
interface SummaryApiService {

    // 전체 월별 요약본 조회.
    @GET("summaries")
    suspend fun getSummaries(): List<Summary>
    // NOTE: POST/PUT for summaries are not implemented on the server
    // (PUT was intentionally commented out). Only retrieval is available.
}
