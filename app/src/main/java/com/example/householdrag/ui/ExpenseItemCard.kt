package com.example.householdrag.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.householdrag.ui.theme.HouseholdRAGTheme

@Composable
fun ExpenseItemCard(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // [상단] 사용처와 금액
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.place,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${expense.amount}원",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }

            // [중간] 날짜와 메모 (데이터가 있을 때만 한 줄로)
            Text(
                text = "${expense.date}${if(expense.memo.isNotBlank()) " · ${expense.memo}" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )

            // [하단] 카테고리 · 결제수단 · 수정/삭제 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${expense.category} · ${expense.payment_method}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // 버튼들
                Row {
                    TextButton(
                        onClick = onEditClick,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("수정", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
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
            payment_method = "카카오페이"
        )

        Column(modifier = Modifier.padding(16.dp)) {
            ExpenseItemCard(
                expense = demoExpense,
                onEditClick = { /* 미리보기라 작동 안함 */ },
                onDeleteClick = { /* 미리보기라 작동 안함 */ }
            )
        }
    }
}