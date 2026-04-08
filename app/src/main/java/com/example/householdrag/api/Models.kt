package com.example.householdrag.api

data class Expense(
    val id: String,
    val date: String,
    val category: String,
    val amount: Int,
    val payment_method: String,
    val place: String,
    val memo: String
)

data class ExpenseRequest(
    val date: String,
    val category: String,
    val amount: Int,
    val payment_method: String,
    val place: String,
    val memo: String
)

data class AskRequest(
    val question: String
)

data class AskResponse(
    val answer: String,
    val references: List<String>
)