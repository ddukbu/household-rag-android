package com.example.householdrag.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseInputCard(
    editId: String?,
    date: String, onDateChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    amount: String, onAmountChange: (String) -> Unit,
    paymentMethod: String, onPaymentChange: (String) -> Unit,
    place: String, onPlaceChange: (String) -> Unit,
    memo: String, onMemoChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onResetClick: () -> Unit
) {
    // 드롭다운의 펼침 상태를 관리하는 변수들
    var categoryExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }

    // 드롭다운 선택지 리스트
    val categoryOptions = listOf("식비", "교통비", "쇼핑", "여가", "생활", "의료", "기타")
    val paymentOptions = listOf("카드", "현금", "계좌이체", "기타")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (editId == null) "가계부 입력" else "가계부 수정",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 날짜 입력
            InputTextField(value = date, onValueChange = onDateChange, label = "날짜 (예: 2026-03-05)")

            // 카테고리 드롭다운 (스피너 역할)
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true, // 직접 타이핑 방지
                    label = { Text("카테고리") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onCategoryChange(option)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // 금액 입력
            InputTextField(value = amount, onValueChange = onAmountChange, label = "금액", isNumber = true)

            // 결제수단 드롭다운 (스피너 역할)
            ExposedDropdownMenuBox(
                expanded = paymentExpanded,
                onExpandedChange = { paymentExpanded = !paymentExpanded },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("결제수단") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = paymentExpanded,
                    onDismissRequest = { paymentExpanded = false }
                ) {
                    paymentOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onPaymentChange(option)
                                paymentExpanded = false
                            }
                        )
                    }
                }
            }

            // 사용처 및 메모 입력
            InputTextField(value = place, onValueChange = onPlaceChange, label = "사용처")
            InputTextField(value = memo, onValueChange = onMemoChange, label = "메모")

            Row(modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = onSaveClick) {
                    Text(if (editId == null) "저장" else "수정")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onResetClick) {
                    Text("초기화")
                }
            }
        }
    }
}

@Composable
fun InputTextField(value: String, onValueChange: (String) -> Unit, label: String, isNumber: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
    )
}