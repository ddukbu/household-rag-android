package com.example.householdrag.api

// model 패키지 DTO를 api 네임스페이스로도 접근 가능하게 하는 호환성 브리지.
typealias Expense = com.example.householdrag.model.Expense
typealias ExpenseRequest = com.example.householdrag.model.ExpenseRequest
typealias ExpenseListResponse = com.example.householdrag.model.ExpenseListResponse
typealias ExpenseIn = com.example.householdrag.model.ExpenseIn
typealias IncomeIn = com.example.householdrag.model.IncomeIn
typealias SummaryIn = com.example.householdrag.model.SummaryIn
typealias AskRequest = com.example.householdrag.model.AskRequest
typealias AskResponse = com.example.householdrag.model.AskResponse
typealias ChatHistoryDto = com.example.householdrag.model.ChatHistoryDto
typealias MessageResponse = com.example.householdrag.model.MessageResponse
typealias LoginRequest = com.example.householdrag.model.LoginRequest
typealias LoginResponse = com.example.householdrag.model.LoginResponse
typealias SignupRequest = com.example.householdrag.model.SignupRequest
typealias SignupResponse = com.example.householdrag.model.SignupResponse
typealias ProfileInitRequest = com.example.householdrag.model.ProfileInitRequest
typealias BudgetActionResponse = com.example.householdrag.model.BudgetActionResponse
typealias FixedIncomeItem = com.example.householdrag.model.FixedIncomeItem
typealias FixedIncomeResponse = com.example.householdrag.model.FixedIncomeResponse
typealias FixedExpenseItem = com.example.householdrag.model.FixedExpenseItem
typealias FixedExpenseResponse = com.example.householdrag.model.FixedExpenseResponse