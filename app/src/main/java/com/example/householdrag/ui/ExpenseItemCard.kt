package com.example.householdrag.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.Expense
import com.example.householdrag.model.Income
import com.example.householdrag.ui.theme.HouseholdRAGTheme
import com.example.householdrag.ui.theme.formatAmount

@Composable
fun ExpenseItemCard(
    item: Any, onEditClick: () -> Unit, onDeleteClick: () -> Unit
) {
    val date: String
    val time: String
    val title: String
    val amount: Int
    val isIncome = item is Income
    val subText: String // 카테고리, 결제수단(입금방법), 메모

    if (item is Expense) {
        date = item.date
        time = item.time
        title = item.place  // 사용처
        amount = item.amount
        subText =
            "${item.category} · ${item.payment_method}${if (item.memo.isNotBlank()) " · ${item.memo}" else ""}"
    } else if (item is Income) {
        date = item.date
        time = item.time
        title = item.category   // 카테고리
        amount = item.amount
        subText =
            //"${item.deposit_method ?: "미지정"} · 입금처: ${item.deposit_source ?: "미지정"}${if (!item.memo.isNullOrBlank()) " · ${item.memo}" else ""}"
            "${item.deposit_source ?: "미지정"}${if (!item.memo.isNullOrBlank()) "· ${item.deposit_method ?: "미지정"} · ${item.memo}" else "" }"

        } else {
        return // 알 수 없는 타입 방어 로직
    }

    val amountSign = if (isIncome) "+" else "-"
    val amountColor = if (isIncome) Color(0xFF2E7D32) else Color.Black
    val cardBackground = if (isIncome) Color(0xFFF9FBF9) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 날짜와 시간
            Row {
                Text(
                    text = "$date $time",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 사용처(입금 종류)와 금액
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$amountSign ${formatAmount(amount)}원",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }

            // 카테고리 / 상세 설명, 수정 삭제 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 카테고리 메모 등
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                // 버튼들
                Row {
                    TextButton(
                        onClick = onEditClick, contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            "수정",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    TextButton(
                        onClick = onDeleteClick,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                    ) {
                        Text("삭제", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "가계부 카드 미리보기")
@Composable
fun ExpenseItemCardPreview() {
    HouseholdRAGTheme {
        // 실제 데이터 대신 임시 데이터를 만들어서 넣어줍니다.
        val demoExpense = Expense(
            id = "1",
            place = "배달의 민족",
            amount = 18000,
            date = "2026-04-30",
            memo = "점심 햄버거 세트",
            category = "식비",
            payment_method = "카카오페이",
            time = "12:00",
            is_fixed_expense = false
        )
        val demoIncome = Income(
            id = "2",
            category = "월급",
            amount = 2500000,
            date = "2026-04-25",
            memo = "기분 좋은 날",
            deposit_method = "계좌이체",
            deposit_source = "주식회사 테스트",
            time = "10:00",
            is_fixed_income = false
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text("1. 지출 예시 피드", fontWeight = FontWeight.Bold)
            ExpenseItemCard(item = demoExpense, onEditClick = {}, onDeleteClick = {})

            Spacer(modifier = Modifier.height(16.dp))

            Text("2. 수입 예시 피드 (혜림 님 통합 버전 폼)", fontWeight = FontWeight.Bold)
            ExpenseItemCard(item = demoIncome, onEditClick = {}, onDeleteClick = {})
        }
    }
}