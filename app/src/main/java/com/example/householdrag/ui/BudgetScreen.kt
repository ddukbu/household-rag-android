package com.example.householdrag.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.Expense
import com.example.householdrag.ui.theme.HouseholdRAGTheme

@Composable
fun BudgetScreen(
    expenses: List<Expense> // 전체 지출 목록
) {
    val now = java.time.LocalDate.now()
    val currentMonth = now.monthValue
    val currentYear = now.year

    // 이번 달 지출만 필터링 (날짜 형식이 "yyyy-MM-dd"라고 가정)
    val thisMonthExpenses = expenses.filter {
        val date = java.time.LocalDate.parse(it.date)
        date.monthValue == currentMonth && date.year == currentYear
    }

    // 실제 사용 금액 계산 로직
    // 식비 카테고리 합계
    val spentFood = thisMonthExpenses
        .filter { it.category == "식비" }
        .sumOf { it.amount }

    // 교통비 카테고리 합계
    val spentTransport = thisMonthExpenses
        .filter { it.category == "교통비" }
        .sumOf { it.amount }

    // TODO: 나중에 DB나 API 연결
    // 고정 수익/지출 (이건 나중에 DB나 API 연결!)
    val fixedIncome = 2500000
    val fixedExpense = 1200000

    // 사용 가능 금액 = 고정수익 - 고정지출 - 이번달 현재까지의 총 지출
    val totalSpentSoFar = thisMonthExpenses.sumOf { it.amount }
    val availableAmount = fixedIncome - fixedExpense - totalSpentSoFar

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "${currentMonth}월 예산 관리",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 메인 요약 카드 (남은 돈이 실시간으로 변화)
        BudgetSummaryCard(availableAmount, fixedIncome, fixedExpense)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "카테고리별 자산 현황",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // TODO: 다른 카테고리도 추가하기
        // 실제 계산된 spent 값
        BudgetProgressItem(category = "식비", budget = 500000, spent = spentFood)
        BudgetProgressItem(category = "교통비", budget = 150000, spent = spentTransport)
    }
}

@Composable
fun BudgetSummaryCard(available: Int, income: Int, expense: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("사용 가능한 여유 자금", style = MaterialTheme.typography.labelMedium)
            Text(
                "${available}원",
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
                    Text("+ ${income} 원", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("고정 지출", style = MaterialTheme.typography.bodyMedium)
                    Text("- ${expense} 원", fontWeight = FontWeight.Bold)
                }


            }
        }
    }
}

@Composable
fun BudgetProgressItem(category: String, budget: Int, spent: Int) {
    val progress = spent.toFloat() / budget.toFloat()
    val remaining = budget - spent

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "남은 돈 ${remaining}원",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
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
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) { BudgetScreen(expenses = listOf()) }
    }
}