package com.example.householdrag.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.householdrag.ui.theme.CommonTextField
import java.util.Calendar

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

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 달력 다이얼로그를 띄우는 함수
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateChange(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

//    // 클릭 감지 시 다이얼로그 띄우기
//    if (isPressed) {
//        datePickerDialog.show()
//    }

    androidx.compose.runtime.LaunchedEffect(isPressed) {
        if (isPressed) {
            datePickerDialog.show()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = Color.White
        ), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (editId == null) "가계부 입력" else "가계부 수정",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 날짜 입력 (직접 입력 대신 클릭 시 달력 호출)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // 클릭하면 달력이 뜨는 Box
                Box(
                    modifier = Modifier
                        .weight(1f) // 버튼 제외 남은 공간 다 차지
                        // .clickable { datePickerDialog.show() }
                ) {
                    CommonTextField(
                        value = date,
                        onValueChange = {},
                        label = "날짜 선택",
                        readOnly = true,
                        // 아이콘만 깔끔하게 표시
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
                        },
                        interactionSource = interactionSource
                    )
                }

//                Spacer(modifier = Modifier.width(8.dp))
//                TextButton(
//                    onClick = {
//                        val today = java.time.LocalDate.now().toString()
//                        onDateChange(today)
//                    }
//                ) {
//                    Text("오늘", fontWeight = FontWeight.Bold)
//                }
            }

            // 카테고리 드롭다운 (스피너 역할)
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true, // 직접 타이핑 방지
                    label = { Text("카테고리") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black
                    )
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.background(Color.White)
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
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("금액") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Black
                )
            )

            // 결제수단 드롭다운 (스피너 역할)
            ExposedDropdownMenuBox(
                expanded = paymentExpanded,
                onExpandedChange = { paymentExpanded = !paymentExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("결제수단") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black
                    )
                )
                ExposedDropdownMenu(
                    expanded = paymentExpanded,
                    onDismissRequest = { paymentExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    paymentOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { onPaymentChange(option); paymentExpanded = false }
                        )
                    }
                }
            }

            // 사용처 및 메모 입력
            CommonTextField(value = place, onValueChange = onPlaceChange, label = "사용처")
            CommonTextField(value = memo, onValueChange = onMemoChange, label = "메모")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onResetClick) {
                    Text("초기화", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSaveClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (editId == null) "저장" else "수정")
                }
            }
        }
    }
}

@Composable
fun InputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isNumber: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
    )
}