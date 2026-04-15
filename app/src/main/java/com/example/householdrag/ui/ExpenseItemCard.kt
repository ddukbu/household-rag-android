package com.example.householdrag.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.Expense

@Composable
fun ExpenseItemCard(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${expense.date} / ${expense.category} / ${expense.amount}원", style = MaterialTheme.typography.bodyLarge)
            Text("결제수단: ${expense.payment_method}", style = MaterialTheme.typography.bodySmall)
            Text("사용처: ${expense.place}", style = MaterialTheme.typography.bodySmall)
            Text("메모: ${expense.memo}", style = MaterialTheme.typography.bodySmall)

            Row(modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = onEditClick) { Text("수정") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDeleteClick) { Text("삭제") }
            }
        }
    }
}