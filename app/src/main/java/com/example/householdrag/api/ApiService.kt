package com.example.householdrag.api

// 앱이 참조하는 통합 API 타입.
// 기능별 인터페이스를 합쳐 호출 진입점을 단순화한다.
interface ApiService :
    ExpenseApiService,
    QuestionApiService,
    AuthApiService,
    IncomeApiService,
    FinancialApiService,
    SummaryApiService