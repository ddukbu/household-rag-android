package com.example.householdrag.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("profile/init")
    suspend fun initProfile(@Body request: ProfileInitRequest): Map<String, String>

    @GET("expenses")
    suspend fun getExpenses(): List<Expense>

    @POST("expenses")
    suspend fun createExpense(@Body request: ExpenseRequest): Expense

    @PUT("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: String,
        @Body request: ExpenseRequest
    ): Expense

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: String): Map<String, String>

    @POST("ask")
    suspend fun ask(@Body request: AskRequest): AskResponse
}