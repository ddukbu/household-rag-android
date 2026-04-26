package com.example.householdrag.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.Expense

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
                    color = Color.Black // 요청하신 대로 검은색!
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