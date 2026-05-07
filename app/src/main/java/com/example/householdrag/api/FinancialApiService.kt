package com.example.householdrag.api

import com.example.householdrag.model.*
import retrofit2.http.*

// 재정 관리(Budget, Asset, Fixed Income/Expense) 전용 API 계약.
interface FinancialApiService {

    // =====================
    // Budget - 기본 조회/수정
    // =====================

    // 모든 월의 예산 정보 조회.
    @GET("budgets")
    suspend fun getBudgets(): List<BudgetOut>

    // 특정 월의 예산 정보 조회.
    @GET("budgets/{year_month}")
    suspend fun getBudget(@Path("year_month") yearMonth: String): BudgetOut

    // 월 저축 목표액 수정.
    @PUT("budgets/{year_month}/saving")
    suspend fun updateSaving(
        @Path("year_month") yearMonth: String,
        @Body request: SavingUpdateRequest
    ): BudgetOut

    // 월 카테고리별 예산 수정.
    @PUT("budgets/{year_month}/details")
    suspend fun updateBudgetDetails(
        @Path("year_month") yearMonth: String,
        @Body request: BudgetDetailsUpdateRequest
    ): BudgetOut

    // =====================
    // Budget - AI 기반 예산 안 생성
    // =====================

    // AI가 제안하는 예산 안을 생성 (draft).
    @POST("budgets/{year_month}/draft")
    suspend fun createBudgetDraft(
        @Path("year_month") yearMonth: String,
        @Body request: BudgetDraftRequest
    ): BudgetDraftOut

    // Draft를 실제 예산으로 적용.
    @POST("budgets/{year_month}/apply-draft")
    suspend fun applyBudgetDraft(
        @Path("year_month") yearMonth: String
    ): BudgetActionResponse

    // Draft 취소.
    @DELETE("budgets/{year_month}/draft")
    suspend fun cancelBudgetDraft(
        @Path("year_month") yearMonth: String
    ): BudgetActionResponse

    // =====================
    // Budget - 추가 기능
    // =====================

    // NOTE: budgets/{year_month}/recommend is not implemented on the server (commented out).

    // 이전 월의 예산을 현재 월로 이월.
    @POST("budgets/{from_year_month}/carry-over/{to_year_month}")
    suspend fun carryOverBudget(
        @Path("from_year_month") fromYearMonth: String,
        @Path("to_year_month") toYearMonth: String
    ): BudgetOut

    // =====================
    // Fixed Income - 고정 수입 관리
    // =====================

    // 특정 월의 고정 수입 목록 조회.
    @GET("budgets/{year_month}/fixed-incomes")
    suspend fun getFixedIncomes(@Path("year_month") yearMonth: String): List<FixedIncomeItem>

    // 고정 수입 항목 추가.
    @POST("budgets/{year_month}/fixed-incomes")
    suspend fun createFixedIncome(
        @Path("year_month") yearMonth: String,
        @Body request: FixedIncomeBudget
    ): FixedIncomeResponse

    // 고정 수입 항목 수정.
    @PUT("budgets/{year_month}/fixed-incomes/{fixed_income_id}")
    suspend fun updateFixedIncome(
        @Path("year_month") yearMonth: String,
        @Path("fixed_income_id") fixedIncomeId: String,
        @Body request: FixedIncomeBudget
    ): FixedIncomeResponse

    // 고정 수입 항목 삭제.
    @DELETE("budgets/{year_month}/fixed-incomes/{fixed_income_id}")
    suspend fun deleteFixedIncome(
        @Path("year_month") yearMonth: String,
        @Path("fixed_income_id") fixedIncomeId: String
    ): FixedIncomeResponse

    // =====================
    // Fixed Expense - 고정 지출 관리
    // =====================

    // 특정 월의 고정 지출 목록 조회.
    @GET("budgets/{year_month}/fixed-expenses")
    suspend fun getFixedExpenses(@Path("year_month") yearMonth: String): List<FixedExpenseItem>

    // 고정 지출 항목 추가.
    @POST("budgets/{year_month}/fixed-expenses")
    suspend fun createFixedExpense(
        @Path("year_month") yearMonth: String,
        @Body request: FixedExpenseBudget
    ): FixedExpenseResponse

    // 고정 지출 항목 수정.
    @PUT("budgets/{year_month}/fixed-expenses/{fixed_expense_id}")
    suspend fun updateFixedExpense(
        @Path("year_month") yearMonth: String,
        @Path("fixed_expense_id") fixedExpenseId: String,
        @Body request: FixedExpenseBudget
    ): FixedExpenseResponse

    // 고정 지출 항목 삭제.
    @DELETE("budgets/{year_month}/fixed-expenses/{fixed_expense_id}")
    suspend fun deleteFixedExpense(
        @Path("year_month") yearMonth: String,
        @Path("fixed_expense_id") fixedExpenseId: String
    ): FixedExpenseResponse

    // =====================
    // Asset - 자산 관리
    // =====================

    // 초기 자산액 설정.
    @PUT("assets/initial")
    suspend fun updateInitialAsset(@Body request: InitialAssetRequest): AssetOut

    // 현재 자산 상태 조회.
    @GET("assets")
    suspend fun getAsset(): AssetOut

    // 월별 자산 변화 기록 조회.
    @GET("assets/history")
    suspend fun getAssetHistory(): Map<String, Any>
}
