package com.example.householdrag.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.model.BudgetOut
import com.example.householdrag.ui.theme.HouseholdRAGTheme
import com.example.householdrag.ui.theme.MonthSelector
import com.example.householdrag.ui.theme.formatAmount


@Composable
fun BudgetScreen(
    currentYM: String,
    budgetData: BudgetOut?,
    fixedIncomeTotal: Int,
    fixedExpenseTotal: Int,
    onMonthChange: (String) -> Unit
) {
    // 서버 데이터가 오기 전에는 로딩 UI를 보여주거나 빈 화면
    if (budgetData == null) {
        LoadingOverlay()
        return
    }

    // 월이 바뀔 때마다 실행됨
    LaunchedEffect(currentYM) {
        onMonthChange(currentYM)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            //.padding(16.dp)
            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
    ) {
        MonthSelector(currentYM = currentYM, onMonthChange = onMonthChange)

//        Text(
//            text = "${budgetData.year_month}월 예산 관리",
//            style = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.Bold
//        )

        Spacer(modifier = Modifier.height(10.dp))

        // 메인 요약 카드 (남은 돈이 실시간으로 변화)
        BudgetSummaryCard(
            totalBudget = budgetData.total_budget,
            saving = budgetData.saving,
            state = budgetData.state,
            fixedIncome = fixedIncomeTotal,
            fixedExpense = fixedExpenseTotal
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "카테고리별 자산 현황",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        budgetData.budget_details.forEach { (category, limit) ->
            val remaining = budgetData.remaining_budget_details[category] ?: 0
            val spent = limit - remaining

            BudgetProgressItem(
                category = category,
                budget = limit,
                spent = spent,
                remaining = remaining,
                onClick = { /* TODO: 클릭 시 수정 API 호출 */ })
        }

    }
}

@Composable
fun BudgetSummaryCard(
    totalBudget: Int,
    saving: Int,
    state: String,
    fixedIncome: Int,
    fixedExpense: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = if (state == "good") MaterialTheme.colorScheme.primary else Color(
                0xFFFFCDD2
            )
        ), shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("사용 가능한 여유 자금", style = MaterialTheme.typography.labelMedium)
            Text(
                "${formatAmount(totalBudget)}원",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.Black.copy(alpha = 0.1f)
            )

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("고정 수익", style = MaterialTheme.typography.bodyMedium)
                    Text("+ ${formatAmount(fixedIncome)}원", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("고정 지출", style = MaterialTheme.typography.bodyMedium)
                    Text("- ${formatAmount(fixedExpense)}원", fontWeight = FontWeight.Bold)
                }

            }
        }
    }
}

@Composable
fun BudgetProgressItem(
    category: String, budget: Int, spent: Int, remaining: Int, onClick: () -> Unit // 클릭 콜백 추가
) {
    val progressValue = if (budget > 0) spent.toFloat() / budget.toFloat() else 0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "남은 돈 ${formatAmount(remaining)}원 / 총 ${formatAmount(budget)}원",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progressValue },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color(0xFFF0F0F0)
        )
    }
}

@Preview(showSystemUi = true, name = "예산 관리 화면 미리보기")
@Composable
fun BudgetScreenPreview() {
    HouseholdRAGTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            BudgetScreen(
                currentYM = "2026-05",
                onMonthChange = { },
                fixedIncomeTotal = 1500000,
                fixedExpenseTotal = 600000,
                budgetData = BudgetOut(
                    id = "preview",
                    year_month = "2026-05",
                    saving = 500000,
                    total_budget = 1200000,
                    budget_details = mapOf("식비" to 500000, "교통비" to 150000),
                    remaining_budget_details = mapOf("식비" to 250000, "교통비" to 80000),
                    state = "good",
                    created_by = "user",
                    updated_at = "2026-05-11"
                )
            )
        }
    }
}