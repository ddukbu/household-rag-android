package com.example.householdrag.api

import com.example.householdrag.model.Income
import com.example.householdrag.model.IncomeIn
import retrofit2.http.*

// 수입(Income) 도메인 전용 API 계약.
interface IncomeApiService {

    // 전체 수입 목록 조회.
    @GET("Incomes")
    suspend fun getIncomes(): List<Income>

    // 새 수입 항목 생성.
    @POST("Incomes")
    suspend fun createIncome(@Body request: IncomeIn): Income

    // 특정 ID 항목 수정.
    @PUT("Incomes/{id}")
    suspend fun updateIncome(
        @Path("id") id: String,
        @Body request: IncomeIn
    ): Income

    // 특정 ID 항목 삭제.
    @DELETE("Incomes/{id}")
    suspend fun deleteIncome(@Path("id") id: String): Map<String, String>
}
