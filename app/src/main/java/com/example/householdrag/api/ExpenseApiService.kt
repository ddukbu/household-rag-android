package com.example.householdrag.api

import com.example.householdrag.model.Expense
import com.example.householdrag.model.ExpenseIn
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// 가계부(Expense) 도메인 전용 API 계약.
interface ExpenseApiService {
    // 전체 가계부 목록 조회.
    @GET("expenses")
    suspend fun getExpenses(): List<Expense>

    // 새 가계부 항목 생성.
    @POST("expenses")
    suspend fun createExpense(@Body request: ExpenseIn): Expense

    // 특정 ID 항목 수정.
    @PUT("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: String,
        @Body request: ExpenseIn
    ): Expense

    // 특정 ID 항목 삭제.
    @DELETE("expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: String): Map<String, String>
}
